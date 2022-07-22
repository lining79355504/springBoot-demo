package com.demo.config;

import com.demo.entity.Context;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.web.bind.support.WebArgumentResolver.UNRESOLVED;

/**
 * 上下文Context解析器   filter中根据cookie 登录态等信息获取到登录者的信息 放在header里 这里从header里解析出来放到 每一个拦截方法的Context的参数里 这里解析到每一个controller里的参数里
 *
 *
 * @See com.example.demo.controller.GreetingController#greeting(com.demo.entity.Context, com.example.demo.ParamDto)
 */
public class ContextWebArgumentResolver implements HandlerMethodArgumentResolver {


    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(Context.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        if (parameter.getParameterType().equals(Context.class)){
            Object request = webRequest.getNativeRequest();
            if (request instanceof HttpServletRequest) {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                return httpRequest.getAttribute("context");
            }
        }
        return UNRESOLVED;
    }

}
