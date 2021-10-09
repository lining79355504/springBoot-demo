package com.demo.config.mybatis.plugins;


import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;

/**
 * @author mort
 * @Description
 * @date 2021/10/9
 **/
@Intercepts({
        @Signature(method = "query", type = Executor.class, args = {
                MappedStatement.class, Object.class, RowBounds.class,
                ResultHandler.class}),
        @Signature(method = "update", type = Executor.class, args = {MappedStatement.class, Object.class})
})
public class ShowSql implements Interceptor {

     private static final Logger logger = LoggerFactory.getLogger(ShowSql.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Executor target = (Executor) invocation.getTarget();
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        //得到类名，方法
        String[] strArr = mappedStatement.getId().split("\\.");
        String methodName = strArr[strArr.length - 2] + "." + strArr[strArr.length - 1];

        //得到sql语句
        Object parameter = null;
        if (invocation.getArgs().length > 1) {
            parameter = invocation.getArgs()[1];
        }
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        Configuration configuration = mappedStatement.getConfiguration();
        String sql = showSql(configuration, boundSql);

        //获取SQL类型
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();

        String s = this.getDatabaseUrl(target);

        Object returnObj = null;
        try {
            returnObj = invocation.proceed();

        } catch (Exception e) {

        } finally {

        }

        return returnObj;

    }

    private String getDatabaseUrl(Executor target) {
        org.apache.ibatis.transaction.Transaction transaction = target.getTransaction();
        if (transaction == null) {
            logger.error("Could not find transaction on target [{}]", target);
            return null;
        }

        try {
            DatabaseMetaData metaData = transaction.getConnection().getMetaData();
            return metaData.getURL();
        } catch (SQLException e) {
            logger.error("Could not get database metadata on target [{}]", target);
            return null;
        }

    }


    public String showSql(Configuration configuration, BoundSql boundSql) {
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();

        // 匹配任意空白字符串，地换为一个空格
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        if (parameterMappings.size() > 0 && parameterObject != null) {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(parameterObject)));
            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);

                // 如果参数大于 N 个则拼接；否则替换 ?
//                if (parameterMappings.size() > SQL_FORMAT_SIZE) {
//                    sql = spliceSqlParamsBySplice.spliceParamsAndBoundSql(boundSql, parameterMappings, sql, metaObject);
//                } else {
//                    sql = spliceSqlParamsByReplace.spliceParamsAndBoundSql(boundSql, parameterMappings, sql, metaObject);
//                }
            }
        }
        return sql;
    }

    public static String getParameterValue(Object obj) {
        String value = null;
        if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
        } else if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + formatter.format(new Date()) + "'";
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "";
            }

        }
        return value;
    }





    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
//            this.target = (Executor) target;
            return Plugin.wrap(target, this);
        }
        return target;

    }

    @Override
    public void setProperties(Properties properties) {

    }
}
