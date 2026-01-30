package com.ailianlian.ablecisi.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ailianlian.ablecisi.baseclass.BaseRepository;
import com.ailianlian.ablecisi.baseclass.BaseViewModel;
import com.ailianlian.ablecisi.pojo.dto.AiCharacterCreateDTO;
import com.ailianlian.ablecisi.pojo.vo.AiCharacterVO;
import com.ailianlian.ablecisi.repository.CharacterRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AI角色定制的ViewModel
 */
public class CharacterViewModel extends BaseViewModel {

    private final MutableLiveData<List<AiCharacterVO>> aiCharacters;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<Boolean> createSuccess;
    private final MutableLiveData<String> errorMessage;
    private final MutableLiveData<AiCharacterVO> currentCharacter;
    private final MutableLiveData<List<String>> characterTypes;
    private final CharacterRepository characterRepository;

    public CharacterViewModel(@NonNull Application application) {
        super(application);
        aiCharacters = new MutableLiveData<>(new ArrayList<>());
        isLoading = new MutableLiveData<>(false);
        createSuccess = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>(null);
        currentCharacter = new MutableLiveData<>(null);
        characterTypes = new MutableLiveData<>(new ArrayList<>());
        characterRepository = new CharacterRepository(getAppContext());
    }

    public LiveData<List<AiCharacterVO>> getAiCharacters() {
        return aiCharacters;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getCreateSuccess() {
        return createSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<AiCharacterVO> getCurrentCharacter() {
        return currentCharacter;
    }

    public LiveData<List<String>> getCharacterTypes() {
        return characterTypes;
    }

    public String validateCharacter(AiCharacterCreateDTO body) {
        if (body.name == null || body.name.trim().isEmpty()) {
            return "角色名称不能为空";
        }
        if (body.typeId == null || body.typeId <= 0) {
            return "请选择角色类型";
        }
        if (body.age == null || body.age < 0) {
            return "请输入有效的年龄";
        }
        if (body.imageUrl == null || body.imageUrl.trim().isEmpty()) {
            return "请上传角色头像";
        }
        if (body.traits == null || body.traits.trim().isEmpty()) {
            return "请填写角色性格特征";
        }
        if (body.personaDesc == null || body.personaDesc.trim().isEmpty()) {
            return "请填写角色简介";
        }
        if (body.interests == null || body.interests.trim().isEmpty()) {
            return "请填写角色兴趣爱好";
        }
        if (body.backstory == null || body.backstory.trim().isEmpty()) {
            return "请填写角色背景故事";
        }
        return null;
    }

    public void createCustomCharacter(AiCharacterCreateDTO body) {
        String validationError = validateCharacter(body);
        if (validationError != null) {
            errorMessage.postValue(validationError);
            return;
        }
        isLoading.postValue(true);
        characterRepository.create(body, new BaseRepository.DataCallback<Long>() {
            @Override
            public void onSuccess(Long data) {
                isLoading.postValue(false);
                createSuccess.postValue(true);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }

            @Override
            public void onNetworkError() {
                isLoading.postValue(false);
                errorMessage.postValue("网络异常，请检查您的网络连接");
            }
        });
    }

    public void loadCharacterTypes() {
        characterRepository.getCharacterTypes(new BaseRepository.DataCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> data) {
                characterTypes.postValue(data);
            }

            @Override
            public void onError(String error) {
                characterTypes.postValue(Collections.emptyList());
            }

            @Override
            public void onNetworkError() {
                characterTypes.postValue(Collections.emptyList());
            }
        });
    }

    public void loadCharacterById(Long characterId) {
        if (characterId == null || characterId <= 0) {
            errorMessage.postValue("无效的角色ID");
            return;
        }
        isLoading.postValue(true);
        characterRepository.detail(characterId, new BaseRepository.DataCallback<AiCharacterVO>() {
            @Override
            public void onSuccess(AiCharacterVO data) {
                isLoading.postValue(false);
                currentCharacter.postValue(data);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }

            @Override
            public void onNetworkError() {
                isLoading.postValue(false);
                errorMessage.postValue("网络异常，请检查您的网络连接");
            }
        });
    }
}