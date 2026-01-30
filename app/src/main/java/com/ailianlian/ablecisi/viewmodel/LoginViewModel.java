package com.ailianlian.ablecisi.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.pojo.entity.User;
import com.ailianlian.ablecisi.repository.LoginRepository;
import com.ailianlian.ablecisi.result.LoginResult;

/**
 * 登录ViewModel
 */
public class LoginViewModel extends AndroidViewModel {

    private final LoginRepository loginRepository;
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public LoginViewModel(Application application) {
        super(application);
        loginRepository = new LoginRepository(application);
    }

    /**
     * 检查是否已经登录
     *
     * @return 是否已登录
     */
    public boolean isLoggedIn() {
        return loginRepository.isLoggedIn();
    }
    
    /**
     * 获取当前登录用户ID
     * 
     * @return 用户ID
     */
    public String getCurrentUserId() {
        return loginRepository.getCurrentUserId();
    }
    
    /**
     * 获取当前登录类型
     * 
     * @return 登录类型
     */
    public String getUserName() {
        return loginRepository.getUserName();
    }

    /**
     * 登录方法
     */
    public void login(String phone, String password, boolean rememberMe) {
        isLoading.setValue(true);
        // 调用Repository进行登录
        loginRepository.login(phone, password, rememberMe, new LoginRepository.DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                loginResult.postValue(LoginResult.success(user.getToken(), user.getId()));
                isLoading.postValue(false);
            }

            @Override
            public void onError(String errorMessage) {
                loginResult.postValue(LoginResult.error(errorMessage));
                isLoading.postValue(false);
            }

            @Override
            public void onNetworkError() {
                loginResult.postValue(LoginResult.error(getApplication().getString(R.string.error_network)));
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * 退出登录
     */
    public void logout() {
        loginRepository.logout();
    }

    /**
     * 获取登录结果LiveData
     */
    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }
    
    /**
     * 获取加载状态LiveData
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
} 