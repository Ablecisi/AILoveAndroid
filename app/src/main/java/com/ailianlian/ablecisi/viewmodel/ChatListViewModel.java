package com.ailianlian.ablecisi.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ailianlian.ablecisi.pojo.entity.Conversation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatListViewModel extends ViewModel {

    private final MutableLiveData<List<Conversation>> chatSessions = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public ChatListViewModel() {
        loadMockData();
    }

    public LiveData<List<Conversation>> getChatSessions() {
        return chatSessions;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void refreshChatSessions() {
        isLoading.setValue(true);
        // 模拟网络请求延迟
        new Thread(() -> {
            try {
                chatSessions.postValue(new ArrayList<>());
                Thread.sleep(1000);
                loadMockData();
                isLoading.postValue(false);
            } catch (InterruptedException e) {
                e.printStackTrace();
                isLoading.postValue(false);
            }
        }).start();
    }

    private void loadMockData() {
        // 模拟聊天会话数据
        List<Conversation> sessions = Arrays.asList(
                new Conversation(
                        "1",
                        "1",
                        "小雪",
                        "https://images.pexels.com/photos/1036623/pexels-photo-1036623.jpeg",
                        "今天过得怎么样？有什么想和我分享的吗？",
                        "10:30",
                        2,
                        true
                ),
                new Conversation(
                        "2",
                        "2",
                        "阿杰",
                        "https://images.pexels.com/photos/2379005/pexels-photo-2379005.jpeg",
                        "我已经帮你整理好了学习计划，要查看吗？",
                        "昨天",
                        0,
                        false
                ),
                new Conversation(
                        "3",
                        "3",
                        "莉莉",
                        "https://images.pexels.com/photos/1239291/pexels-photo-1239291.jpeg",
                        "我有一个新的创意想法，你想听听吗？",
                        "周一",
                        5,
                        true
                ),
                new Conversation(
                        "4",
                        "4",
                        "小北",
                        "https://images.pexels.com/photos/1681010/pexels-photo-1681010.jpeg",
                        "今天我为你准备了一份健康食谱，希望你会喜欢！",
                        "上周",
                        0,
                        true
                ),
                new Conversation(
                        "5",
                        "5",
                        "星辰",
                        "https://images.pexels.com/photos/1898555/pexels-photo-1898555.jpeg",
                        "记得今天要保持积极的心态哦！",
                        "12/20",
                        0,
                        false
                )
        );
        chatSessions.postValue(sessions);
    }
} 