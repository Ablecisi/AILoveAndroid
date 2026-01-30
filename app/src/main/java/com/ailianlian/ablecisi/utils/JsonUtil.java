package com.ailianlian.ablecisi.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

/**
 * ailianlian
 * com.ailianlian.ablecisi.utils
 * JsonUtil <br>
 * Json工具类
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/4/19
 * 星期六
 * 15:03
 */
public class JsonUtil {
    /**
     * Gson实例，注册了LocalDateTime的类型适配器
     *
     * @see LocalDateTimeTypeAdapter
     */
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .create();

    /**
     * 将对象转换为JSON字符串
     *
     * @param object 要转换的对象
     * @return JSON字符串
     */
    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    /**
     * 将JSON字符串转换为对象
     *
     * @param json  JSON字符串
     * @param clazz 要转换的对象类型
     * @param <T>   对象类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }


    /**
     * 将JSON字符串转换为对象
     *
     * @param json JSON字符串
     * @param <T>  对象类型
     * @return 转换后的对象
     */
    public static <T> T[] fromJsonArray(String json, Class<T[]> clazz) {
        return gson.fromJson(json, clazz);
    }

    /**
     * 将对象转换为JSON字符串
     *
     * @param array 要转换的对象数组
     * @param <T>   对象类型
     * @return JSON字符串
     */
    public static <T> String toJsonArray(T[] array) {
        return gson.toJson(array);
    }

    /**
     * 将JSON字符串转换为对象
     *
     * @param json JSON字符串
     * @param type 要转换的对象类型
     * @param <T>  对象类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, Type type) {
        return gson.fromJson(json, type);
    }
}
