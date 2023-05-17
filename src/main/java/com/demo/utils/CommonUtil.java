package com.demo.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;


public class CommonUtil {

    private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    public static void setInvoke(Object data, String filedName, Object targetObj) {

        try {
            Class<?> targetClass = Class.forName(targetObj.getClass().getName());
            PropertyDescriptor pd = new PropertyDescriptor(filedName, targetClass);
            Method writeMethod = pd.getWriteMethod();
            writeMethod.invoke(targetObj, data);
        } catch (Exception e) {
            logger.error("error {}", e);
        }
    }

    public static Object getInvoke(String filedName, Object targetObj) {

        try {
            Class<?> targetClass = Class.forName(targetObj.getClass().getName());
            PropertyDescriptor pd = new PropertyDescriptor(filedName, targetClass);
            Method readMethod = pd.getReadMethod();
            Object result = readMethod.invoke(targetObj, new Object[]{});
            return result;
        } catch (Throwable throwable) {
            logger.error("error {}", throwable);
        }
        return null;
    }
}
