package com.ailianlian.ablecisi.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ailianlian.ablecisi.baseclass.BaseRepository;
import com.ailianlian.ablecisi.baseclass.BaseViewModel;
import com.ailianlian.ablecisi.pojo.entity.AiCharacter;
import com.ailianlian.ablecisi.pojo.entity.Article;
import com.ailianlian.ablecisi.pojo.entity.Topic;
import com.ailianlian.ablecisi.pojo.vo.AiCharacterVO;
import com.ailianlian.ablecisi.repository.HomeRepository;
import com.ailianlian.ablecisi.result.PageResult;
import com.ailianlian.ablecisi.result.Result;
import com.ailianlian.ablecisi.utils.JsonUtil;
import com.ailianlian.ablecisi.utils.LoginInfoUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeViewModel extends BaseViewModel {

    private final HomeRepository repository;
    private final MutableLiveData<List<AiCharacter>> characters;
    private final MutableLiveData<Article> featuredContent;
    private final MutableLiveData<List<Topic>> topics;
    private final MutableLiveData<List<String>> categories;

    private List<AiCharacter> allAiCharacters;
    private String currentCategory;

    public HomeViewModel(@NonNull Application application) {
        super(application, new MutableLiveData<>());
        this.repository = new HomeRepository(application);
        this.characters = new MutableLiveData<>(new ArrayList<>());
        this.featuredContent = new MutableLiveData<>();
        this.topics = new MutableLiveData<>(new ArrayList<>());
        // 初始化时设置所有AI角色和当前分类
        this.allAiCharacters = new ArrayList<>();
        this.currentCategory = "全部"; // 默认显示全部分类
        this.categories = new MutableLiveData<>(new ArrayList<>());
    }

    public void filterByCategory(String category) {
        this.currentCategory = category;

        if ("全部".equals(category)) {
            characters.setValue(allAiCharacters);
            return;
        }

        List<AiCharacter> filtered = new ArrayList<>();
        for (AiCharacter aiCharacter : allAiCharacters) {
            if (aiCharacter.getType().contains(category)) {
                filtered.add(aiCharacter);
            }
        }

        characters.setValue(filtered);
    }

    public void loadData() {
        repository.loadUserInterests(new HomeRepository.DataCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> data) {
                // 保存用户兴趣数据
                LoginInfoUtil.saveUserInterests(getApplication(), data);
            }

            @Override
            public void onError(String msg) {
                // 可通过另一个 LiveData 通知 UI 错误
                getResultMutableLiveData().postValue(Result.error(msg));
            }

            @Override
            public void onNetworkError() {
                // 可通过另一个 LiveData 通知 UI 网络错误
                getResultMutableLiveData().postValue(Result.error("网络错误，请稍后重试"));
            }
        });
        repository.loadCategories(new HomeRepository.DataCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> data) {
                List<String> categoryList = new ArrayList<>();
                categoryList.add("全部"); // 添加“全部”选项
                categoryList.addAll(data);
                categories.postValue(categoryList);
            }

            @Override
            public void onError(String msg) {
                // 可通过另一个 LiveData 通知 UI 错误
                getResultMutableLiveData().postValue(Result.error(msg));
            }

            @Override
            public void onNetworkError() {
                // 可通过另一个 LiveData 通知 UI 网络错误
                getResultMutableLiveData().postValue(Result.error("网络错误，请稍后重试"));
            }
        });
        // 获取后端数据并更新LiveData
        repository.loadHotCharacters(null, null, null, null, 1, 10, new BaseRepository.DataCallback<>() {
            @Override
            public void onSuccess(PageResult<AiCharacterVO> data) {
                List<AiCharacter> characterList = new ArrayList<>();
                data.getRecords().forEach(vo -> {
                    AiCharacter character = new AiCharacter();
                    character.setId(String.valueOf(vo.id));
                    character.setAge(vo.age);
                    character.setDescription(vo.personaDesc);
                    character.setInterests(parseCommaSeparatedString(vo.interests));
                    character.setName(vo.name);
                    character.setBackgroundStory(vo.backstory);
                    switch (vo.gender) {
                        case 0:
                            character.setGender("男");
                            break;
                        case 1:
                            character.setGender("女");
                            break;
                        default:
                            character.setGender("其他");
                    }
                    character.setImageUrl(vo.imageUrl);
                    character.setPersonalityTraits(parseCommaSeparatedString(vo.traits));
                    character.setOnline(vo.online == 1);
                    character.setType(vo.typeName);
                    character.setCreatedAt(vo.createTime);
                    characterList.add(character);
                });
                System.out.println("AI角色加载成功: " + JsonUtil.toJson(characterList));
                // 根据当前分类过滤角色
                characters.postValue(characterList);
                allAiCharacters = characterList; // 保存所有AI角色数据
            }

            @Override
            public void onError(String msg) {
                // 可通过另一个 LiveData 通知 UI 错误
                getResultMutableLiveData().postValue(Result.error(msg));
            }

            @Override
            public void onNetworkError() {
                // 可通过另一个 LiveData 通知 UI 网络错误
                getResultMutableLiveData().postValue(Result.error("网络错误，请稍后重试"));
            }
        });
        repository.loadFeaturedContent(new HomeRepository.DataCallback<Article>() {
            @Override
            public void onSuccess(Article data) {
                System.out.println("精选内容加载成功: " + JsonUtil.toJson(data));
                featuredContent.postValue(data);
            }

            @Override
            public void onError(String msg) {
                // 可通过另一个 LiveData 通知 UI 错误
                getResultMutableLiveData().postValue(Result.error(msg));
            }

            @Override
            public void onNetworkError() {
                // 可通过另一个 LiveData 通知 UI 网络错误
                getResultMutableLiveData().postValue(Result.error("网络错误，请稍后重试"));
            }
        });
        repository.loadTopics(new HomeRepository.DataCallback<List<Topic>>() {
            @Override
            public void onSuccess(List<Topic> data) {
                topics.postValue(data);
                getResultMutableLiveData().postValue(Result.success("数据加载成功", data));
            }

            @Override
            public void onError(String msg) {
                // 可通过另一个 LiveData 通知 UI 错误
                getResultMutableLiveData().postValue(Result.error(msg));
            }

            @Override
            public void onNetworkError() {
                // 可通过另一个 LiveData 通知 UI 网络错误
                getResultMutableLiveData().postValue(Result.error("网络错误，请稍后重试"));
            }
        });
    }

    private List<String> parseCommaSeparatedString(String str) {
        if (str == null || str.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String[] parts = str.split(",");
        List<String> list = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return list;
    }

    public LiveData<List<AiCharacter>> getCharacters() {
        return characters;
    }

    public LiveData<Article> getFeaturedContent() {
        return featuredContent;
    }

    public LiveData<List<Topic>> getTopics() {
        return topics;
    }

    public LiveData<List<String>> getCategories() {
        return categories;
    }
}