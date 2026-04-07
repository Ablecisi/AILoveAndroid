package com.ailianlian.ablecisi.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

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

    public static final int MESSAGE_PAGE_SIZE = 30;

    private final MutableLiveData<AiCharacterVO> character = new MutableLiveData<>();
    private final MutableLiveData<List<Message>> messages = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isTyping = new MutableLiveData<>(false); // AI正在输入状态
    /** 单次发送错误提示（Activity 可 Toast 后置 null） */
    private final MutableLiveData<String> streamError = new MutableLiveData<>();
    private final MutableLiveData<StreamDelta> streamDelta = new MutableLiveData<>();
    private final MutableLiveData<String> streamStage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingOlder = new MutableLiveData<>(false);
    private final ChatRepository chatRepository;

    private String conversationId;
    private String userId; // 模拟用户ID
    /** 已加载的最早一页页码（从 1 开始） */
    private int oldestLoadedPage = 1;
    private boolean hasMoreOlder = true;
    private boolean isLoadingOlder = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final StringBuilder pendingDeltaBuffer = new StringBuilder();
    private long lastDeltaEmitAt = 0L;
    private boolean deltaFlushScheduled = false;

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

    public LiveData<StreamDelta> getStreamDelta() {
        return streamDelta;
    }

    public LiveData<String> getStreamStage() {
        return streamStage;
    }

    public LiveData<Boolean> getLoadingOlder() {
        return loadingOlder;
    }

    public boolean hasMoreOlder() {
        return hasMoreOlder;
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
        streamStage.setValue("start");

        isTyping.setValue(true);

        AtomicBoolean finished = new AtomicBoolean(false);
        AtomicBoolean firstChunkSeen = new AtomicBoolean(false);

        chatRepository.sendMessageStream(chatSendDTO, new ChatRepository.StreamSendCallback() {
            @Override
            public void onAck(String requestId) {
                streamStage.postValue("ack");
            }

            @Override
            public void onStart() {
                streamStage.postValue("start");
            }

            @Override
            public void onPreprocessDone() {
                streamStage.postValue("preprocess_done");
                isLoading.postValue(false);
            }

            @Override
            public void onHeartbeat() {
                streamStage.postValue("heartbeat");
            }

            @Override
            public void onChunk(String text) {
                if (finished.get() || text == null) {
                    return;
                }
                if (text.isEmpty()) {
                    return;
                }
                if (firstChunkSeen.compareAndSet(false, true)) {
                    isLoading.postValue(false);
                }
                synchronized (pendingDeltaBuffer) {
                    pendingDeltaBuffer.append(text);
                }
                emitDeltaIfNeeded(false);
            }

            @Override
            public void onComplete(ChatReplyVO replyVO) {
                if (!finished.compareAndSet(false, true)) {
                    return;
                }
                emitDeltaIfNeeded(true);
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
                streamStage.postValue("done");
                isTyping.postValue(false);
                isLoading.postValue(false);
            }

            @Override
            public void onError(String message) {
                if (!finished.compareAndSet(false, true)) {
                    return;
                }
                removeStreamingMessage();
                streamStage.postValue("error");
                isTyping.postValue(false);
                isLoading.postValue(false);
                streamError.postValue(message != null ? message : "发送失败");
            }
        });
    }

    private void emitDeltaIfNeeded(boolean force) {
        long now = System.currentTimeMillis();
        if (!force && now - lastDeltaEmitAt < 40L) {
            if (!deltaFlushScheduled) {
                deltaFlushScheduled = true;
                mainHandler.postDelayed(() -> {
                    deltaFlushScheduled = false;
                    emitDeltaIfNeeded(false);
                }, 40L);
            }
            return;
        }
        final String out;
        synchronized (pendingDeltaBuffer) {
            if (pendingDeltaBuffer.length() == 0) {
                return;
            }
            out = pendingDeltaBuffer.toString();
            pendingDeltaBuffer.setLength(0);
        }
        lastDeltaEmitAt = now;
        mainHandler.post(() -> streamDelta.setValue(new StreamDelta(STREAMING_MESSAGE_ID, out)));
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
        oldestLoadedPage = 1;
        hasMoreOlder = true;

        chatRepository.loadChatHistory(conversationId, 1, MESSAGE_PAGE_SIZE,
                new ChatRepository.DataCallback<List<MessageVO>>() {
            @Override
            public void onSuccess(List<MessageVO> ms) {
                List<Message> messageList = new ArrayList<>();
                if (ms != null) {
                    for (MessageVO messageVO : ms) {
                        messageList.add(messageVOToMessage(messageVO));
                    }
                }
                hasMoreOlder = ms != null && ms.size() >= MESSAGE_PAGE_SIZE;
                oldestLoadedPage = 1;
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

    /**
     * 向上滚动接近顶部时加载更早一页，插入列表头部。
     */
    public void loadOlderMessages() {
        if (!hasMoreOlder || isLoadingOlder) {
            return;
        }
        isLoadingOlder = true;
        loadingOlder.postValue(true);
        int nextPage = oldestLoadedPage + 1;
        chatRepository.loadChatHistory(conversationId, nextPage, MESSAGE_PAGE_SIZE,
                new ChatRepository.DataCallback<List<MessageVO>>() {
                    @Override
                    public void onSuccess(List<MessageVO> ms) {
                        if (ms == null || ms.isEmpty()) {
                            hasMoreOlder = false;
                            isLoadingOlder = false;
                            loadingOlder.postValue(false);
                            return;
                        }
                        List<Message> older = new ArrayList<>();
                        for (MessageVO messageVO : ms) {
                            older.add(messageVOToMessage(messageVO));
                        }
                        List<Message> cur = messages.getValue();
                        if (cur == null) {
                            cur = new ArrayList<>();
                        }
                        List<Message> merged = new ArrayList<>(older);
                        merged.addAll(cur);
                        hasMoreOlder = ms.size() >= MESSAGE_PAGE_SIZE;
                        oldestLoadedPage = nextPage;
                        messages.postValue(merged);
                        isLoadingOlder = false;
                        loadingOlder.postValue(false);
                    }

                    @Override
                    public void onError(String error) {
                        isLoadingOlder = false;
                        loadingOlder.postValue(false);
                    }

                    @Override
                    public void onNetworkError() {
                        isLoadingOlder = false;
                        loadingOlder.postValue(false);
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
        AiCharacterVO ch = character.getValue();
        String charId = ch != null && ch.id != null ? ch.id.toString() : "0";
        boolean isUser = messageVO.type != null && messageVO.type == 0;
        String sid = isUser ? userId : charId;
        String rid = isUser ? charId : userId;

        return new Message(
                messageVO.id.toString(),
                messageVO.content,
                messageVO.createTime,
                messageVO.type,
                messageVO.isRead != null && messageVO.isRead == 1,
                sid,
                rid
        );
    }

    public static class StreamDelta {
        public final String messageId;
        public final String text;

        public StreamDelta(String messageId, String text) {
            this.messageId = messageId;
            this.text = text;
        }
    }
} 