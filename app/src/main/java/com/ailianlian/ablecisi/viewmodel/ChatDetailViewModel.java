package com.ailianlian.ablecisi.viewmodel;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ailianlian.ablecisi.baseclass.BaseViewModel;
import com.ailianlian.ablecisi.pojo.dto.ChatSendDTO;
import com.ailianlian.ablecisi.pojo.entity.Message;
import com.ailianlian.ablecisi.pojo.vo.AiCharacterVO;
import com.ailianlian.ablecisi.pojo.vo.ChatReplyVO;
import com.ailianlian.ablecisi.pojo.vo.MessageVO;
import com.ailianlian.ablecisi.repository.ChatRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatDetailViewModel extends BaseViewModel {

    /** 与列表中占位 AI 气泡 id 一致，流式结束后替换为服务端 messageId */
    public static final String STREAMING_MESSAGE_ID = "__streaming__";

    private final MutableLiveData<AiCharacterVO> character = new MutableLiveData<>();
    private final MutableLiveData<List<Message>> messages = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isTyping = new MutableLiveData<>(false); // AI正在输入状态
    /** 单次发送错误提示（Activity 可 Toast 后置 null） */
    private final MutableLiveData<String> streamError = new MutableLiveData<>();
    private final ChatRepository chatRepository;

    private String conversationId;
    private String userId; // 模拟用户ID

    public LiveData<AiCharacterVO> getCharacter() {
        return character;
    }

    public LiveData<List<Message>> getMessages() {
        return messages;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getIsTyping() {
        return isTyping;
    }

    public LiveData<String> getStreamError() {
        return streamError;
    }

    public void clearStreamError() {
        streamError.setValue(null);
    }

    public ChatDetailViewModel(Application application) {
        super(application);
        this.chatRepository = new ChatRepository(getAppContext());
    }

    public void init(String conversationId, String userId) {
        this.conversationId = conversationId;
        // 获取用户ID
        this.userId = userId;
        // 加载角色信息
        loadCharacterData(Long.parseLong(conversationId));
        // 加载聊天记录
        loadChatData();
    }

    public void sendMessage(String content) {
        if (content == null || content.trim().isEmpty()) {
            return;
        }

        isLoading.setValue(true);

        ChatSendDTO chatSendDTO = new ChatSendDTO();
        chatSendDTO.characterId = character.getValue() != null ? character.getValue().id : null;
        chatSendDTO.conversationId = Long.parseLong(conversationId);
        chatSendDTO.userId = null;
        chatSendDTO.text = content;
        // 创建并添加用户消息
        Message userMessage = new Message(
                UUID.randomUUID().toString(),
                content,
                LocalDateTime.now(),
                Message.TYPE_SENT,
                false,
                userId,
                character.getValue() != null ? character.getValue().id.toString() : null
        );

        List<Message> currentMessages = messages.getValue();
        if (currentMessages == null) {
            currentMessages = new ArrayList<>();
        }

        List<Message> updatedMessages = new ArrayList<>(currentMessages);
        updatedMessages.add(userMessage);

        String charIdStr = character.getValue() != null && character.getValue().id != null
                ? character.getValue().id.toString()
                : "0";
        Message aiPlaceholder = new Message(
                STREAMING_MESSAGE_ID,
                "",
                LocalDateTime.now(),
                Message.TYPE_RECEIVED,
                false,
                charIdStr,
                userId
        );
        updatedMessages.add(aiPlaceholder);
        messages.setValue(updatedMessages);

        isTyping.setValue(true);

        AtomicBoolean finished = new AtomicBoolean(false);

        chatRepository.sendMessageStream(chatSendDTO, new ChatRepository.StreamSendCallback() {
            @Override
            public void onChunk(String text) {
                if (finished.get() || text == null) {
                    return;
                }
                List<Message> cur = messages.getValue();
                if (cur == null) {
                    return;
                }
                List<Message> next = new ArrayList<>(cur);
                for (Message m : next) {
                    if (STREAMING_MESSAGE_ID.equals(m.getId())) {
                        String c = m.getContent() != null ? m.getContent() : "";
                        m.setContent(c + text);
                        break;
                    }
                }
                messages.postValue(next);
            }

            @Override
            public void onComplete(ChatReplyVO replyVO) {
                if (!finished.compareAndSet(false, true)) {
                    return;
                }
                List<Message> cur = messages.getValue();
                if (cur == null) {
                    isTyping.postValue(false);
                    isLoading.postValue(false);
                    return;
                }
                List<Message> next = new ArrayList<>(cur);
                Message finalMsg = chatReplyToMessage(replyVO);
                boolean replaced = false;
                for (int i = 0; i < next.size(); i++) {
                    if (STREAMING_MESSAGE_ID.equals(next.get(i).getId())) {
                        next.set(i, finalMsg);
                        replaced = true;
                        break;
                    }
                }
                if (!replaced) {
                    next.add(finalMsg);
                }
                messages.postValue(next);
                isTyping.postValue(false);
                isLoading.postValue(false);
            }

            @Override
            public void onError(String message) {
                if (!finished.compareAndSet(false, true)) {
                    return;
                }
                removeStreamingMessage();
                isTyping.postValue(false);
                isLoading.postValue(false);
                streamError.postValue(message != null ? message : "发送失败");
            }
        });
    }

    private void removeStreamingMessage() {
        List<Message> cur = messages.getValue();
        if (cur == null) {
            return;
        }
        List<Message> next = new ArrayList<>();
        for (Message m : cur) {
            if (!STREAMING_MESSAGE_ID.equals(m.getId())) {
                next.add(m);
            }
        }
        messages.postValue(next);
    }
    private void loadChatData() {
        isLoading.setValue(true);

        chatRepository.loadChatHistory(conversationId, new ChatRepository.DataCallback<List<MessageVO>>() {
            @Override
            public void onSuccess(List<MessageVO> ms) {
                List<Message> messageList = new ArrayList<>();
                ms.forEach(messageVO -> {
                    messageList.add(messageVOToMessage(messageVO));
                });
                messages.postValue(messageList);
                isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
            }

            @Override
            public void onNetworkError() {
                isLoading.postValue(false);
            }
        });
    }

    private void loadCharacterData(Long conversationId) {
        isLoading.setValue(true);

        chatRepository.loadCharacterInfo(conversationId, new ChatRepository.DataCallback<AiCharacterVO>() {
            @Override
            public void onSuccess(AiCharacterVO aiCharacter) {
                character.postValue(aiCharacter);
                isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
            }

            @Override
            public void onNetworkError() {
                isLoading.postValue(false);
            }
        });
    }

    private Message chatReplyToMessage(ChatReplyVO chatReplyVO) {
        return new Message(
                chatReplyVO.messageId.toString(),
                chatReplyVO.reply,
                LocalDateTime.now(),
                Message.TYPE_RECEIVED,
                true,
                chatReplyVO.characterId.toString(),
                userId
        );
    }

    private Message messageVOToMessage(MessageVO messageVO) {
        String sid = messageVO.emotion != null ? userId : character.getValue() != null ? character.getValue().id.toString() : null;
        String rid = messageVO.emotion != null ? character.getValue() != null ? character.getValue().id.toString() : null : userId;

        return new Message(
                messageVO.id.toString(),
                messageVO.content,
                messageVO.createTime,
                messageVO.type,
                messageVO.isRead == 1,
                sid,
                rid

        );
    }
} 