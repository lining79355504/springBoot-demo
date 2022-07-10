package utils;

import com.demo.config.OkhttpInterceptor;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author mort
 * @Description
 * @date 2022/7/8
 **/
public class OkHttpUtils {

    private static final Logger logger = LoggerFactory.getLogger(OkhttpInterceptor.class);

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    //注意和networkIntercept的区别  networkIntercept可以修改request response
    OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new OkhttpInterceptor())  //增加拦截器 用于监控 链路追踪 参数打印 debug等
            .build();

    public String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
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
