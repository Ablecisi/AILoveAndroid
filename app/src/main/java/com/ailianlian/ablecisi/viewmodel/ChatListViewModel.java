package com.ailianlian.ablecisi.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ailianlian.ablecisi.baseclass.BaseRepository;
import com.ailianlian.ablecisi.pojo.entity.Conversation;
import com.ailianlian.ablecisi.repository.ChatRepository;
import com.ailianlian.ablecisi.utils.LoginInfoUtil;

import java.util.Collections;
import java.util.List;

public class ChatListViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Conversation>> chatSessions = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final ChatRepository chatRepository;

    public ChatListViewModel(@NonNull Application application) {
        super(application);
        chatRepository = new ChatRepository(application);
        loadConversations();
    }

    public LiveData<List<Conversation>> getChatSessions() {
        return chatSessions;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void refreshChatSessions() {
        loadConversations();
    }

    private void loadConversations() {
        String uid = LoginInfoUtil.getUserId(getApplication());
        if (uid == null || "-1".equals(uid) || uid.isEmpty()) {
            chatSessions.postValue(Collections.emptyList());
            return;
        }
        isLoading.postValue(true);
        chatRepository.listMyConversations(1, 50, new BaseRepository.DataCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> data) {
                chatSessions.postValue(data != null ? data : Collections.emptyList());
                isLoading.postValue(false);
            }

            @Override
            public void onError(String msg) {
                chatSessions.postValue(Collections.emptyList());
                isLoading.postValue(false);
            }

            @Override
            public void onNetworkError() {
                chatSessions.postValue(Collections.emptyList());
                isLoading.postValue(false);
            }
        });
    }
}
