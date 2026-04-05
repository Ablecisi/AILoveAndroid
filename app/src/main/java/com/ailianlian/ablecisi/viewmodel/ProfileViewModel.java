package com.ailianlian.ablecisi.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ailianlian.ablecisi.baseclass.BaseRepository;
import com.ailianlian.ablecisi.baseclass.BaseViewModel;
import com.ailianlian.ablecisi.pojo.dto.UserProfileUpdateDTO;
import com.ailianlian.ablecisi.pojo.entity.Post;
import com.ailianlian.ablecisi.pojo.entity.User;
import com.ailianlian.ablecisi.pojo.vo.AiCharacterVO;
import com.ailianlian.ablecisi.repository.CharacterRepository;
import com.ailianlian.ablecisi.repository.CommunityRepository;
import com.ailianlian.ablecisi.repository.ProfileRepository;
import com.ailianlian.ablecisi.result.PageResult;
import com.ailianlian.ablecisi.result.Result;
import com.ailianlian.ablecisi.utils.LoginInfoUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 个人资料页面的ViewModel
 */
public class ProfileViewModel extends BaseViewModel {

    private final MutableLiveData<User> userProfile;
    private final MutableLiveData<List<AiCharacterVO>> characters;
    private final MutableLiveData<List<Post>> myPosts;
    private final MutableLiveData<Boolean> isLoading;
    private final Application application;
    private final CharacterRepository characterRepository;
    private final ProfileRepository profileRepository;
    private final CommunityRepository communityRepository;

    public ProfileViewModel(Application application) {
        super(application);
        this.application = application;
        this.userProfile = new MutableLiveData<>();
        this.characters = new MutableLiveData<>(new ArrayList<>());
        this.myPosts = new MutableLiveData<>(new ArrayList<>());
        this.isLoading = new MutableLiveData<>(false);
        this.characterRepository = new CharacterRepository(application);
        this.profileRepository = new ProfileRepository(application);
        this.communityRepository = new CommunityRepository(application);
    }

    public LiveData<User> getUserProfile() {
        return userProfile;
    }

    public LiveData<List<AiCharacterVO>> getCharacters() {
        return characters;
    }

    public LiveData<List<Post>> getMyPosts() {
        return myPosts;
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
     * 当前用户帖子列表（资料页展示，按创建时间倒序）
     */
    public void loadMyPosts() {
        if (!LoginInfoUtil.isLoggedIn(application)) {
            myPosts.postValue(Collections.emptyList());
            return;
        }
        communityRepository.loadMyPosts(1, 100, new BaseRepository.DataCallback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> data) {
                List<Post> list = data != null ? new ArrayList<>(data) : new ArrayList<>();
                list.sort((a, b) -> {
                    java.time.LocalDateTime ta = a.getCreatedAt();
                    java.time.LocalDateTime tb = b.getCreatedAt();
                    if (ta == null && tb == null) {
                        return 0;
                    }
                    if (ta == null) {
                        return 1;
                    }
                    if (tb == null) {
                        return -1;
                    }
                    return tb.compareTo(ta);
                });
                myPosts.postValue(list);
            }

            @Override
            public void onError(String msg) {
                getResultMutableLiveData().postValue(Result.error(msg));
                myPosts.postValue(Collections.emptyList());
            }

            @Override
            public void onNetworkError() {
                getResultMutableLiveData().postValue(Result.error("网络错误，请稍后重试"));
                myPosts.postValue(Collections.emptyList());
            }
        });
    }

    /**
     * 加载用户的AI角色列表（全屏页用，显示全局加载）
     */
    public void loadCharacters() {
        loadCharacters(true);
    }

    /**
     * @param showGlobalLoading 为 false 时不驱动资料页全屏 ProgressBar（如资料页 Tab 切换）
     */
    public void loadCharacters(boolean showGlobalLoading) {
        if (!LoginInfoUtil.isLoggedIn(application)) {
            characters.postValue(new ArrayList<>());
            if (showGlobalLoading) {
                isLoading.postValue(false);
            }
            return;
        }
        if (showGlobalLoading) {
            isLoading.setValue(true);
        }
        Long userId = Long.parseLong(getCurrentUserId(application));
        characterRepository.list(userId, null, null, null, 1, 100, new BaseRepository.DataCallback<PageResult<AiCharacterVO>>() {
            @Override
            public void onSuccess(PageResult<AiCharacterVO> data) {
                characters.postValue(data.getRecords() != null ? data.getRecords() : new ArrayList<>());
                if (showGlobalLoading) {
                    isLoading.postValue(false);
                }
            }

            @Override
            public void onError(String msg) {
                getResultMutableLiveData().postValue(Result.error(msg));
                if (showGlobalLoading) {
                    isLoading.postValue(false);
                }
            }

            @Override
            public void onNetworkError() {
                getResultMutableLiveData().postValue(Result.error("网络错误，请稍后重试"));
                if (showGlobalLoading) {
                    isLoading.postValue(false);
                }
            }
        });
    }

    /**
     * 编辑用户资料：登录名、昵称、简介、头像 URL（未改头像可传 null 则沿用当前）
     */
    public void saveProfileDetails(String username, String name, String description, String avatarUrl) {
        isLoading.setValue(true);
        User currentProfile = userProfile.getValue();
        String av = avatarUrl;
        if (av == null && currentProfile != null) {
            av = currentProfile.getAvatarUrl();
        }
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.username = username != null ? username.trim() : null;
        dto.name = name != null ? name.trim() : null;
        dto.description = description;
        dto.avatarUrl = av;
        profileRepository.updateProfile(dto, new ProfileRepository.DataCallback<User>() {
            @Override
            public void onSuccess(User data) {
                userProfile.postValue(data);
                isLoading.postValue(false);
                if (data != null) {
                    LoginInfoUtil.updateSessionProfile(application,
                            data.getUsername(), data.getName(), data.getAvatarUrl());
                }
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
     * 一次提交：可选新头像（先上传 OSS），与用户名、昵称、简介一并写入服务端。
     */
    public void saveAllProfileFields(String username, String name, String description, Uri newAvatarUri) {
        if (newAvatarUri == null) {
            saveProfileDetails(username, name, description, null);
            return;
        }
        isLoading.setValue(true);
        profileRepository.uploadAvatarImage(newAvatarUri, application.getContentResolver(),
                new ProfileRepository.DataCallback<String>() {
                    @Override
                    public void onSuccess(String url) {
                        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
                        dto.username = username != null ? username.trim() : null;
                        dto.name = name != null ? name.trim() : null;
                        dto.description = description;
                        dto.avatarUrl = url;
                        profileRepository.updateProfile(dto, new ProfileRepository.DataCallback<User>() {
                            @Override
                            public void onSuccess(User data) {
                                userProfile.postValue(data);
                                isLoading.postValue(false);
                                if (data != null) {
                                    LoginInfoUtil.updateSessionProfile(application,
                                            data.getUsername(), data.getName(), data.getAvatarUrl());
                                }
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
     * 相册选图后上传 OSS 并写入用户头像（昵称、简介保持不变）。
     */
    public void uploadAndSaveAvatar(Uri imageUri) {
        if (imageUri == null) {
            return;
        }
        isLoading.setValue(true);
        profileRepository.uploadAvatarImage(imageUri, application.getContentResolver(),
                new ProfileRepository.DataCallback<String>() {
                    @Override
                    public void onSuccess(String url) {
                        User u = userProfile.getValue();
                        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
                        if (u != null) {
                            dto.username = u.getUsername();
                            dto.name = u.getName();
                            dto.description = u.getDescription();
                        }
                        dto.avatarUrl = url;
                        profileRepository.updateProfile(dto, new ProfileRepository.DataCallback<User>() {
                            @Override
                            public void onSuccess(User data) {
                                userProfile.postValue(data);
                                isLoading.postValue(false);
                                if (data != null) {
                                    LoginInfoUtil.updateSessionProfile(application,
                                            data.getUsername(), data.getName(), data.getAvatarUrl());
                                }
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
}
