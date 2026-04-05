package com.ailianlian.ablecisi.utils;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * 解析 Server-Sent Events（按行：event / data，空行分隔一条事件）。
 */
public final class SseEventParser {

    public interface Sink {
        void onEvent(String eventName, String dataPayload);
    }

    private SseEventParser() {
    }

    public static void parse(BufferedReader reader, Sink sink) throws IOException {
        if (sink == null) {
            return;
        }
        String eventName = null;
        StringBuilder data = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                flush(sink, eventName, data);
                eventName = null;
                data.setLength(0);
                continue;
            }
            if (line.startsWith(":")) {
                continue;
            }
            int idx = line.indexOf(':');
            if (idx < 0) {
                continue;
            }
            String field = line.substring(0, idx);
            String value = idx + 1 < line.length() ? line.substring(idx + 1) : "";
            if (!value.isEmpty() && value.charAt(0) == ' ') {
                value = value.substring(1);
            }
            switch (field) {
                case "event":
                    eventName = value;
                    break;
                case "data":
                    if (data.length() > 0) {
                        data.append('\n');
                    }
                    data.append(value);
                    break;
                default:
                    break;
            }
        }
        flush(sink, eventName, data);
    }

    private static void flush(Sink sink, String eventName, StringBuilder data) {
        if (data.length() == 0) {
            return;
        }
        String ev = (eventName == null || eventName.isEmpty()) ? "message" : eventName;
        sink.onEvent(ev, data.toString());
    }
}
