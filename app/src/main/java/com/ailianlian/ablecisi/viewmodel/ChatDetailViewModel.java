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

public class ChatDetailViewModel extends BaseViewModel {

    private final MutableLiveData<AiCharacterVO> character = new MutableLiveData<>();
    private final MutableLiveData<List<Message>> messages = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isTyping = new MutableLiveData<>(false); // AI正在输入状态
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
        chatSendDTO.userId = Long.parseLong(userId);
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
        messages.setValue(updatedMessages);

        // 模拟AI正在输入
        isTyping.setValue(true);

        chatRepository.sendMessage(chatSendDTO, new ChatRepository.DataCallback<ChatReplyVO>() {
            @Override
            public void onSuccess(ChatReplyVO replyVO) {
                Message aiMessage = chatReplyToMessage(replyVO);
                List<Message> messagesWithResponse = new ArrayList<>(messages.getValue());
                messagesWithResponse.add(aiMessage);
                messages.postValue(messagesWithResponse);
                isTyping.postValue(false);
                isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                isTyping.postValue(false);
                isLoading.postValue(false);
            }

            @Override
            public void onNetworkError() {
                isTyping.postValue(false);
                isLoading.postValue(false);
            }
        });

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