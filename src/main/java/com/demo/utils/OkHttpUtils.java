package com.demo.utils;

import com.demo.config.plugins.OkhttpInterceptor;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author mort
 * @Description
 * @date 2022/7/8
 **/
public class OkHttpUtils {

    private static final Logger logger = LoggerFactory.getLogger(OkhttpInterceptor.class);

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");


    private static final Dispatcher dispatcher = new Dispatcher();

    static {
        dispatcher.setMaxRequests(500); // 设置最大请求数
        dispatcher.setMaxRequestsPerHost(30); // 设置每个主机最大请求数
    }

    //注意和networkIntercept的区别  networkIntercept可以修改request response
    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .callTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(new OkhttpInterceptor())  //增加拦截器 用于监控 链路追踪 参数打印 debug等
            .addInterceptor(chain -> {
                Request request = chain.request();
                Response response = null;
                int tryCount = 0;
                while (tryCount < 2) { // 最多尝试3次
                    try {
                        logger.info("consult Request URL: {}, Method: {}, Headers: {}", request.url(), request.method(), request.headers());
                        response = chain.proceed(request);
                        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                        logger.info("consult Response URL: {}, Code: {}, Message: {}", response.request().url(), response.code(), response.message());
                        return response;
                    } catch (IOException e) {
                        logger.error("consult Request failed, url: {}, tryCount: {}, error: {}", request.url(), tryCount, e.getMessage());
                        tryCount++;
                        if (tryCount >= 2) throw e;
                    }
                }
                return response;
            })
            .connectionPool(new ConnectionPool(20, 5, TimeUnit.MINUTES))
            .dispatcher(dispatcher)
            .eventListener(ConnectionLogger.INSTANCE) //增加事件监听器 用于监控 链路追踪 参数打印 debug等

            .build();

    public String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return String.valueOf(response.body());
        }
    }


    public String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }


}
