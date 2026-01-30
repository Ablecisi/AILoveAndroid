package com.ailianlian.ablecisi.baseclass;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.ailianlian.ablecisi.result.Result;

/**
 * ailianlian
 * com.ailianlian.ablecisi.baseclass
 * BaseViewModel <br>
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/6/15
 * 星期日
 * 23:55
 */
public class BaseViewModel extends AndroidViewModel {
    private final MutableLiveData<Result> resultMutableLiveData;

    public BaseViewModel(@NonNull Application application, MutableLiveData<Result> resultMutableLiveData) {
        super(application);
        this.resultMutableLiveData = resultMutableLiveData;
    }

    public BaseViewModel(@NonNull Application application) {
        this(application, new MutableLiveData<>());
    }

    /**
     * 获取应用上下文
     *
     * @return 应用上下文
     */
    public Application getAppContext() {
        return getApplication();
    }

    /**
     * 获取结果数据
     *
     * @return 结果数据
     */
    public MutableLiveData<Result> getResultMutableLiveData() {
        return resultMutableLiveData;
    }

}
