package utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mort
 * @Description
 * @date 2022/7/22
 **/
public class GsonUtils {

    public static final JsonObject EMPTY_JSON_OBJECT = new JsonObject();
    public static final JsonArray EMPTY_JSON_ARRAY = new JsonArray();

    private static final Gson gson = new GsonBuilder()
            .setLenient()
            .enableComplexMapKeySerialization()
            .serializeNulls()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .disableHtmlEscaping()
            .create();

    private GsonUtils() {
    }

    /**
     * 公开可给外部使用。
     */
    public static Gson getGson() {
        return gson;
    }

    /**
     * 将object对象转成json字符串。
     */
    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    /**
     * 将json字符串转成object。
     */
    public static <T> T toObject(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }

    /**
     * 将json字符串转成object，转换出错返回null。
     */
    public static <T> T forceToObject(String json, Class<T> type) {
        try {
            return gson.fromJson(json, type);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将json字符串转成任意泛型object。
     */
    public static <T> T toObject(String json, TypeToken<T> typeToken) {
        return gson.fromJson(json, typeToken.getType());
    }

    /**
     * 将json字符串转成object。
     */
    public static <T> T toObject(JsonElement json, Class<T> type) {
        return gson.fromJson(json, type);
    }

    /**
     * 将json字符串转成object，转换出错返回null。
     */
    public static <T> T forceToObject(JsonElement json, Class<T> type) {
        try {
            return gson.fromJson(json, type);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将json字符串转成任意泛型object。
     */
    public static <T> T toObject(JsonElement json, TypeToken<T> typeToken) {
        return gson.fromJson(json, typeToken.getType());
    }

    /**
     * 将json字符串转成转成list。
     */
    public static <T> List<T> toList(String arrayJson, Class<T> type) {
        List<T> list = new ArrayList<>();
        JsonArray array = new JsonParser().parse(arrayJson).getAsJsonArray();
        for (JsonElement elem : array) {
            list.add(gson.fromJson(elem, type));
        }
        return list;
    }

    /**
     * 将json字符串转成转成list。
     */
    public static <T> List<T> toList(JsonArray jsonArray, Class<T> type) {
        List<T> list = new ArrayList<>();
        for (JsonElement elem : jsonArray) {
            list.add(gson.fromJson(elem, type));
        }
        return list;
    }

    /**
     * 生成用于延迟日志打印的对象。
     */
    public static Wrapper wrap(Object source) {
        return new Wrapper(source);
    }

    /**
     * 用于延迟到需要打印日志时才转成JSON。
     */
    public static class Wrapper {

        private Object source;

        Wrapper(Object source) {
            this.source = source;
        }

        @Override
        public String toString() {
            return gson.toJson(source);
        }
    }

}


