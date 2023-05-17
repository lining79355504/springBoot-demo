package com.example.demo.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.demo.utils.GsonUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author mort
 * @Description
 * @date 2022/7/22
 **/
@Slf4j
@Controller
@RequestMapping("/backdoor")
public class BackdoorController {

    @Value("${backdoor.enable:true}")
    private boolean enable;

    @Autowired
    private ApplicationContext applicationContext;

    private static final Pattern BEAN_METHOD = Pattern.compile("([a-zA-Z0-9_$.]+)[.#]([a-zA-Z0-9_$]+)\\((.*)\\)", Pattern.DOTALL);

    @RequestMapping
    public ResponseEntity home(@RequestParam(value = "expression", required = false)
                                       String expression) throws Exception {

        // 解析表达式(允许注释的行)
        String parseExpression = parseExpression(expression);

        if (!enable) {
            return ResponseEntity.ok().body("Forbidden! 开关未开启!");
        }
        String page = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>在线调试</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "<form method=\"post\" action=\"\">\n" +
                "    <label>在线调试</label>\n" +
                "    <br/>\n" +
                "    <textarea rows=\"10\" cols=\"100\" name=\"expression\" placeholder=\"请输入表达式\">";

        // 原参数返回到输入框
        page += expression == null ? "" : expression.trim();

        // 提交按钮
        page += "</textarea>\n" +
                "    <br/>\n" +
                "    <input type=\"submit\"  value=\"　提　交　\"/>\n" +
                "</form>\n" +
                "<br/>\n" +
                "<textarea rows=\"20\" cols=\"100\" id=\"result\" placeholder=\"调用返回的结果\">";

        // 写入返回值
        try {
            if (StringUtils.hasText(parseExpression)) {
                page += methodExecute(parseExpression);
            }
        } catch (Exception e) {
            page += "调用异常：" + e.getMessage();
        }

        // 拼接结束区域
        page += "</textarea>\n" +
                "</body>\n" +
                "</html>";

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("text/html;charset=UTF-8"))
                .body(page);
    }


    /**
     * 解析表达式(按换行符分隔，允许有 # 开头的注释行)
     *
     * @param expression
     * @return
     */
    public String parseExpression(@RequestParam(value = "expression", required = false) String expression) {
        if (StringUtils.isEmpty(expression)) {
            return "";
        }
        String[] lines = expression.split("\\r?\\n");
        // 处理带有#的换行
        String tmpExe = "";
        for (String line : lines) {
            if (line.startsWith("#")) {
                continue;
            }
            tmpExe += line + "\n";
        }
        expression = tmpExe;
        return expression;
    }

    /***
     * 可执行项目中的任意方法。
     * @param expression Spring Bean 方法调用：userServiceImpl.findUser({name:"元宝"})
     *                   或直接复制方法引用：com.ewing.UserServiceImpl#findUser({name:"元宝"})
     *                   静态方法或new一个新对象调用：ewing.common.TimeUtils.getDaysOfMonth(2018,5)
     *                   注意：如果方法重载的，参数Json也是兼容的，将无法确定调用哪个方法。
     */
    public String methodExecute(String expression) {

        Assert.hasText(expression, "表达式不能为空！");
        Matcher matcher = BEAN_METHOD.matcher(expression);
        Assert.isTrue(matcher.find(), "表达式格式不正确！");

        // 根据名称获取Bean
        String classOrBeanName = matcher.group(1);
        Object springBean = getSpringBean(classOrBeanName);
        Class clazz;
        try {
            clazz = springBean == null ? Class.forName(classOrBeanName) : AopProxyUtils.ultimateTargetClass(springBean);
        } catch (Exception e) {
            throw new RuntimeException("初始化类失败！", e);
        }
        Assert.notNull(clazz, "调用Class不能为空！");

        // 根据名称获取方法列表
        List<Method> mayMethods = getMethods(clazz, matcher.group(2));

        // 转换方法参数
        JsonArray params = getJsonArray("[" + matcher.group(3) + "]");
        return GsonUtils.toJson(executeFoundMethod(clazz, springBean, mayMethods, params));
    }

    private Object executeFoundMethod(Class clazz, Object bean, List<Method> mayMethods, JsonArray params) {
        // 根据参数锁定方法
        List<Object> args = new ArrayList<>();
        Method foundMethod = null;
        for (Method method : mayMethods) {
            if (!args.isEmpty()) {
                args.clear();
            }
            Type[] types = method.getGenericParameterTypes();
            if (types.length != params.size()) {
                continue;
            }
            // 参数转换，无异常表示匹配
            Iterator<JsonElement> paramIterator = params.iterator();
            try {
                for (Type type : types) {
                    Object arg = GsonUtils.getGson().fromJson(paramIterator.next(), type);
                    args.add(arg);
                }
            } catch (Exception e) {
                continue;
            }
            Assert.isNull(foundMethod, "方法调用重复：" + foundMethod + " 和 " + method);
            foundMethod = method;
        }

        // 调用方法并返回
        Assert.notNull(foundMethod, "未找到满足参数的方法！");
        try {
            foundMethod.setAccessible(true);
            if (Modifier.isStatic(foundMethod.getModifiers())) {
                return foundMethod.invoke(clazz, args.toArray());
            } else {
                if (bean != null) {
                    Class<?> methodClass = foundMethod.getDeclaringClass();
                    if (!methodClass.equals(bean.getClass())) {
                        foundMethod = bean.getClass().getDeclaredMethod(foundMethod.getName(), foundMethod.getParameterTypes());
                    }
                    return foundMethod.invoke(bean, args.toArray());
                } else {
                    return foundMethod.invoke(clazz.newInstance(), args.toArray());
                }
            }
        } catch (Exception e) {
            log.error("调用方法失败：" + e.getCause(), e);
            throw new RuntimeException("调用方法失败：" + e.getCause().getMessage(), e);
        }
    }

    private JsonArray getJsonArray(String jsonParams) {
        try {
            return GsonUtils.toObject(jsonParams, JsonArray.class);
        } catch (Exception e) {
            throw new RuntimeException("参数格式不正确！");
        }
    }

    private List<Method> getMethods(Class clazz, String methodName) {
        List<Method> mayMethods = Stream.of(clazz.getDeclaredMethods())
                .filter(m -> methodName.equals(m.getName()))
                .collect(Collectors.toList());
        List<Method> methods = Stream.of(clazz.getMethods()).filter(m -> methodName.equals(m.getName())).collect(Collectors.toList());
        Assert.isTrue(!CollectionUtils.isEmpty(mayMethods) && !CollectionUtils.isEmpty(methods), "未找到方法：" + methodName);
        if (CollectionUtils.isEmpty(mayMethods)) {
            mayMethods.addAll(methods);
        }
        return mayMethods;
    }

    private Object getSpringBean(String beanName) {
        try {
            return applicationContext.getBean(beanName);
        } catch (Exception e) {
            try {
                return applicationContext.getBean(Class.forName(beanName));
            } catch (Exception ex) {
                return null;
            }
        }
    }

}
