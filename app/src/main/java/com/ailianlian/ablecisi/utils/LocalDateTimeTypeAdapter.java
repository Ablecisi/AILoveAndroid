package com.ailianlian.ablecisi.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * ailianlian
 * com.ailianlian.ablecisi.utils
 * LocalDateTimeTypeAdapter <br>
 * Gson适配器，用于序列化和反序列化LocalDateTime对象
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/6/16
 * 星期一
 * 00:11
 */
public class LocalDateTimeTypeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.format(formatter));
    }

    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String raw = json.getAsString().trim();
        if (raw.isEmpty()) {
            throw new JsonParseException("empty datetime");
        }
        // 兼容 ISO（T 分隔）与带毫秒
        String normalized = raw.replace('T', ' ');
        int dot = normalized.indexOf('.');
        if (dot > 0) {
            normalized = normalized.substring(0, dot);
        }
        try {
            return LocalDateTime.parse(normalized, formatter);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(raw.replace(' ', 'T'), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e2) {
                throw new JsonParseException("无法解析时间: " + raw, e2);
            }
        }
    }
}
