package com.ailianlian.ablecisi.viewmodel;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ailianlian.ablecisi.baseclass.BaseRepository;
import com.ailianlian.ablecisi.baseclass.BaseViewModel;
import com.ailianlian.ablecisi.pojo.entity.User;
import com.ailianlian.ablecisi.pojo.vo.AiCharacterVO;
import com.ailianlian.ablecisi.repository.CharacterRepository;
import com.ailianlian.ablecisi.repository.ProfileRepository;
import com.ailianlian.ablecisi.result.PageResult;
import com.ailianlian.ablecisi.result.Result;
import com.ailianlian.ablecisi.utils.LoginInfoUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 个人资料页面的ViewModel
 */
public class ProfileViewModel extends BaseViewModel {

    private final MutableLiveData<User> userProfile;
    private final MutableLiveData<List<AiCharacterVO>> characters;
    private final MutableLiveData<Boolean> isLoading;
    private final Application application;
    private final CharacterRepository characterRepository;
    private final ProfileRepository profileRepository;

    public ProfileViewModel(Application application) {
        super(application);
        this.application = application;
        this.userProfile = new MutableLiveData<>();
        this.characters = new MutableLiveData<>(new ArrayList<>());
        this.isLoading = new MutableLiveData<>(false);
        this.characterRepository = new CharacterRepository(application);
        this.profileRepository = new ProfileRepository(application);
    }

    public LiveData<User> getUserProfile() {
        return userProfile;
    }

    public LiveData<List<AiCharacterVO>> getCharacters() {
        return characters;
    }
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    private String getCurrentUserId(Application application) {
        return LoginInfoUtil.getUserId(application);
    }

    private String getCurrentUserName(Application application) {
        return LoginInfoUtil.getUserName(application);
    }

    /**
     * 加载用户资料
     */
    public void loadUserProfile() {
        isLoading.setValue(true);
        profileRepository.loadUserProfile(new ProfileRepository.DataCallback<User>() {
            @Override
            public void onSuccess(User data) {
                userProfile.postValue(data);
                isLoading.postValue(false);
            }

            @Override
            public void onError(String msg) {
                getResultMutableLiveData().postValue(Result.error(msg));
                isLoading.postValue(false);
            }

            @Override
            public void onNetworkError() {
                getResultMutableLiveData().postValue(Result.error("网络错误，请稍后重试"));
                isLoading.postValue(false);
            }
        });

    }

    /**
     * 加载用户的AI角色列表
     */
    public void loadCharacters() {
        isLoading.setValue(true);
        Long userId = Long.parseLong(getCurrentUserId(application));
        characterRepository.list(userId, null, null, null, 1, 10, new BaseRepository.DataCallback<PageResult<AiCharacterVO>>() {
            @Override
            public void onSuccess(PageResult<AiCharacterVO> data) {
                characters.postValue(data.getRecords());
                isLoading.postValue(false);
            }

            @Override
            public void onError(String msg) {
                getResultMutableLiveData().postValue(Result.error(msg));
                isLoading.postValue(false);
            }

            @Override
            public void onNetworkError() {
                getResultMutableLiveData().postValue(Result.error("网络错误，请稍后重试"));
                isLoading.postValue(false);
            }
        });
    }

    /**
     * 编辑用户资料
     */
    public void editProfile(String username, String description) {
        // TODO: 保存资料到服务器
        isLoading.setValue(true);

        // 获取当前资料
        User currentProfile = userProfile.getValue();
        if (currentProfile != null) {
            // 更新资料
            currentProfile.setUsername(username);
            currentProfile.setDescription(description);

            // 模拟网络请求保存资料
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    userProfile.postValue(currentProfile);
                    isLoading.postValue(false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    isLoading.postValue(false);
                }
            }).start();
        } else {
            isLoading.setValue(false);
        }
    }
} 