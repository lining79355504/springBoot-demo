package com.demo.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mort
 * @Description
 * @date 2021/11/5
 **/
public class UrlUtils {
    private static String regex1 = "[0-9a-zA-Z]+((\\.com)|(\\.cn)|(\\.org)|(\\.net)|(\\.edu)|(\\.com.cn))";

    /**
     * 获取一级域名
     * @param url
     * @return
     */
    public static String parseFirstDomain(String url) {


        Pattern p = Pattern.compile(regex1);
        Matcher m = p.matcher(url);
        List<String> strList = new ArrayList<String>();
        while (m.find()) {
            strList.add(m.group());
        }
        String categoryId = strList.toString();
        return categoryId.substring(1, categoryId.length() - 1);
    }
}
