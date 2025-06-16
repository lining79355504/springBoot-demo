package com.demo.config.plugins;

import com.dianping.cat.Cat;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author mort
 * @Description
 * @date 2022/7/8
 **/
public class OkhttpInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(OkhttpInterceptor.class);

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();


    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        long t1 = System.nanoTime();
        Cat.logEvent("HTTP_REQUEST", request.url().toString(), "0", String.valueOf(request.body()));
        logger.info(String.format("Sending request %s on %s%n%s",
                request.url(), chain.connection(), request.headers()));

        Response response = chain.proceed(request);

        long t2 = System.nanoTime();
        logger.info(String.format("Received response for %s in %.1fms%n%s",
                response.request().url(), (t2 - t1) / 1e6d, response.headers()));
        Cat.logEvent("REQUEST_RESPONSE", request.url().toString(), "0", String.valueOf(response.body()));
        return response;
    }

    String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }


    String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }


    public static void main(String[] args) throws Exception {

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url("").build();

    }

}
