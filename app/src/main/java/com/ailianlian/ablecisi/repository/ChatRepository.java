package com.ailianlian.ablecisi.repository;

import android.content.Context;

import com.ailianlian.ablecisi.baseclass.BaseRepository;
import com.ailianlian.ablecisi.constant.StatusCodeConstant;
import com.ailianlian.ablecisi.pojo.dto.ChatSendDTO;
import com.ailianlian.ablecisi.pojo.vo.AiCharacterVO;
import com.ailianlian.ablecisi.pojo.vo.ChatReplyVO;
import com.ailianlian.ablecisi.pojo.vo.MessageVO;
import com.ailianlian.ablecisi.result.Result;
import com.ailianlian.ablecisi.utils.HttpClient;
import com.ailianlian.ablecisi.utils.JsonUtil;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

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

    public ChatRepository(Context context) {
        super(context);
    }

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
