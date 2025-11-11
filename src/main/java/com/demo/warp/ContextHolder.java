package com.demo.warp;

public class ContextHolder {
    private static final ThreadLocal<String> context = new ThreadLocal<>();

    public static void setContext(String value) {
        context.set(value);
    }

    public static String getContext() {
        return context.get();
    }

    public static void clear() {
        context.remove();
    }
}
