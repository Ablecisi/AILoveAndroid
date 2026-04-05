package com.ailianlian.ablecisi.repository;

import android.content.Context;

import com.ailianlian.ablecisi.baseclass.BaseRepository;
import com.ailianlian.ablecisi.constant.StatusCodeConstant;
import com.ailianlian.ablecisi.pojo.dto.ChatSendDTO;
import com.ailianlian.ablecisi.pojo.entity.Conversation;
import com.ailianlian.ablecisi.pojo.vo.AiCharacterVO;
import com.ailianlian.ablecisi.pojo.vo.ChatReplyVO;
import com.ailianlian.ablecisi.pojo.vo.DialogConversationDTO;
import com.ailianlian.ablecisi.pojo.vo.MessageVO;
import com.ailianlian.ablecisi.result.Result;
import com.ailianlian.ablecisi.constant.NetWorkPathConstant;
import com.ailianlian.ablecisi.utils.HttpClient;
import com.ailianlian.ablecisi.utils.JsonUtil;
import com.ailianlian.ablecisi.utils.LoginInfoUtil;
import com.ailianlian.ablecisi.utils.SseEventParser;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * ailianlian
 * com.ailianlian.ablecisi.repository
 * ChatRepository <br>
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/9/16
 * 星期二
 * 22:06
 */
public class ChatRepository extends BaseRepository {

    private static final String API_BASE = NetWorkPathConstant.BASE_URL + "/api";
    private static final String TOKEN_HEADER = "token";

    public ChatRepository(Context context) {
        super(context);
    }

    /**
     * 流式发送：SSE {@code chunk} / {@code done} / {@code error}，与后端 {@code POST /api/dialog/send/stream} 对齐。
     */
    public interface StreamSendCallback {
        void onChunk(String text);

        void onComplete(ChatReplyVO reply);

        void onError(String message);
    }

    public void sendMessageStream(ChatSendDTO dto, StreamSendCallback callback) {
        getExecutorService().execute(() -> {
            String token = LoginInfoUtil.getUserToken(getContext());
            String url = API_BASE + "/dialog/send/stream";
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(0, TimeUnit.SECONDS)
                    .callTimeout(0, TimeUnit.SECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader(TOKEN_HEADER, token == null ? "" : token)
                    .addHeader("Accept", "text/event-stream")
                    .post(RequestBody.create(
                            JsonUtil.toJson(dto),
                            MediaType.parse("application/json; charset=utf-8")))
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errBody = response.body() != null ? response.body().string() : "";
                    notifyStreamError(callback, "HTTP " + response.code() + " " + errBody);
                    return;
                }
                if (response.body() == null) {
                    notifyStreamError(callback, "响应体为空");
                    return;
                }
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.body().byteStream(), StandardCharsets.UTF_8));
                SseEventParser.parse(reader, (event, data) -> {
                    try {
                        switch (event) {
                            case "chunk":
                                String piece = JsonUtil.fromJson(data, String.class);
                                if (piece != null) {
                                    callback.onChunk(piece);
                                }
                                break;
                            case "done":
                                ChatReplyVO vo = JsonUtil.fromJson(data, ChatReplyVO.class);
                                if (vo != null) {
                                    callback.onComplete(vo);
                                } else {
                                    notifyStreamError(callback, "done 解析失败");
                                }
                                break;
                            case "error":
                                String msg = safeJsonString(data);
                                notifyStreamError(callback, msg != null ? msg : data);
                                break;
                            default:
                                break;
                        }
                    } catch (Exception e) {
                        notifyStreamError(callback, e.getMessage() != null ? e.getMessage() : "SSE 解析异常");
                    }
                });
            } catch (Exception e) {
                notifyStreamError(callback, e.getMessage() != null ? e.getMessage() : "网络错误");
            }
        });
    }

    private static String safeJsonString(String data) {
        try {
            return JsonUtil.fromJson(data, String.class);
        } catch (Exception e) {
            return data;
        }
    }

    private static void notifyStreamError(StreamSendCallback callback, String message) {
        if (callback != null) {
            callback.onError(message);
        }
    }

    /** 非流式兜底（仍可用） */
    public void sendMessage(ChatSendDTO chatSendDTO, DataCallback<ChatReplyVO> dataCallback) {
        getExecutorService().execute(() -> {
            HttpClient.doPost(getContext(), "/dialog/send", chatSendDTO, new HttpClient.HttpCallback() {
                @Override
                public void onSuccess(String response) {
                    Type type = new TypeToken<Result<ChatReplyVO>>() {
                    }.getType();
                    Result<ChatReplyVO> result = JsonUtil.fromJson(response, type);
                    if (result != null && result.getCode() == StatusCodeConstant.SUCCESS) {
                        dataCallback.onSuccess(result.getData());
                    }
                }

                @Override
                public void onFailure(String error) {
                    dataCallback.onError(error);
                }
            });
        });
    }

    public void loadChatHistory(String conversationId, DataCallback<List<MessageVO>> dataCallback) {
        getExecutorService().execute(() -> {
            HttpClient.doGet(getContext(), "/dialog/list?conversationId=" + conversationId, new HttpClient.HttpCallback() {
                @Override
                public void onSuccess(String response) {
                    Type type = new TypeToken<Result<List<MessageVO>>>() {
                    }.getType();
                    Result<List<MessageVO>> result = JsonUtil.fromJson(response, type);
                    if (result != null && result.getCode() == StatusCodeConstant.SUCCESS) {
                        List<MessageVO> messageList = result.getData();
                        dataCallback.onSuccess(messageList);
                    }
                }

                @Override
                public void onFailure(String error) {
                    dataCallback.onError(error);
                }
            });
        });

    }

    public void listMyConversations(int page, int size, DataCallback<List<Conversation>> dataCallback) {
        getExecutorService().execute(() -> {
            String path = "/dialog/conversations?page=" + page + "&size=" + size;
            HttpClient.doGet(getContext(), path, new HttpClient.HttpCallback() {
                @Override
                public void onSuccess(String response) {
                    Type type = new TypeToken<Result<List<DialogConversationDTO>>>() {
                    }.getType();
                    Result<List<DialogConversationDTO>> result = JsonUtil.fromJson(response, type);
                    if (result != null && result.getCode() == StatusCodeConstant.SUCCESS && result.getData() != null) {
                        List<Conversation> out = new ArrayList<>();
                        for (DialogConversationDTO d : result.getData()) {
                            if (d == null || d.id == null) {
                                continue;
                            }
                            String last = d.lastMsgAt != null && !d.lastMsgAt.isEmpty()
                                    ? d.lastMsgAt.replace('T', ' ')
                                    : (d.updateTime != null ? d.updateTime.replace('T', ' ') : "");
                            out.add(new Conversation(
                                    String.valueOf(d.id),
                                    d.characterId != null ? String.valueOf(d.characterId) : "",
                                    d.characterName != null ? d.characterName : "",
                                    d.characterAvatar != null ? d.characterAvatar : "",
                                    d.lastMessage != null ? d.lastMessage : "",
                                    last,
                                    0,
                                    false
                            ));
                        }
                        dataCallback.onSuccess(out);
                    } else {
                        dataCallback.onError(result != null ? result.getMsg() : "解析失败");
                    }
                }

                @Override
                public void onFailure(String error) {
                    dataCallback.onError(error);
                }
            });
        });
    }

    public void loadCharacterInfo(Long conversationId, DataCallback<AiCharacterVO> dataCallback) {
        getExecutorService().execute(() -> {
            HttpClient.doGet(getContext(), "/character/cd?conversationId=" + conversationId, new HttpClient.HttpCallback() {
                @Override
                public void onSuccess(String response) {
                    Type type = new TypeToken<Result<AiCharacterVO>>() {
                    }.getType();
                    Result<AiCharacterVO> result = JsonUtil.fromJson(response, type);
                    if (result != null && result.getCode() == StatusCodeConstant.SUCCESS) {
                        AiCharacterVO characterVO = result.getData();
                        dataCallback.onSuccess(characterVO);
                    }
                }

                @Override
                public void onFailure(String error) {
                    dataCallback.onError(error);
                }
            });
        });
    }
}
