package com.ailianlian.ablecisi.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.baseclass.BaseActivity;
import com.ailianlian.ablecisi.baseclass.BaseRepository;
import com.ailianlian.ablecisi.constant.ExtrasConstant;
import com.ailianlian.ablecisi.databinding.ActivityCreateConversationBinding;
import com.ailianlian.ablecisi.pojo.dto.OpenConversationDTO;
import com.ailianlian.ablecisi.pojo.vo.AiCharacterVO;
import com.ailianlian.ablecisi.pojo.vo.DialogConversationDTO;
import com.ailianlian.ablecisi.repository.CharacterRepository;
import com.ailianlian.ablecisi.repository.ChatRepository;
import com.ailianlian.ablecisi.result.PageResult;
import com.ailianlian.ablecisi.utils.LoginInfoUtil;

import java.util.ArrayList;
import java.util.List;

public class CreateConversationActivity extends BaseActivity<ActivityCreateConversationBinding> {

    private static final int REQ_CREATE_CHARACTER = 1002;

    private CharacterRepository characterRepository;
    private ChatRepository chatRepository;
    private final List<AiCharacterVO> characters = new ArrayList<>();
    private Long selectedCharacterId;

    @Override
    protected ActivityCreateConversationBinding getViewBinding() {
        return ActivityCreateConversationBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        characterRepository = new CharacterRepository(this);
        chatRepository = new ChatRepository(this);
    }

    @Override
    protected void initData() {
        loadCharacters();
    }

    @Override
    protected void setListeners() {
        binding.btnPickCharacter.setOnClickListener(v -> showCharacterPicker());
        binding.btnNewCharacter.setOnClickListener(v -> {
            Intent i = new Intent(this, CharacterCustomizeActivity.class);
            i.putExtra(ExtrasConstant.EXTRA_RETURN_CHARACTER_ID, true);
            startActivityForResult(i, REQ_CREATE_CHARACTER);
        });
        binding.btnCreate.setOnClickListener(v -> submit());
    }

    private void loadCharacters() {
        String uid = LoginInfoUtil.getUserId(this);
        if (uid == null || uid.isEmpty() || "-1".equals(uid)) {
            showToast("请先登录");
            return;
        }
        long userId = Long.parseLong(uid);
        isLoading(true);
        characterRepository.list(userId, null, null, null, 1, 100, new BaseRepository.DataCallback<PageResult<AiCharacterVO>>() {
            @Override
            public void onSuccess(PageResult<AiCharacterVO> data) {
                isLoading(false);
                characters.clear();
                if (data != null && data.getRecords() != null) {
                    characters.addAll(data.getRecords());
                }
            }

            @Override
            public void onError(String msg) {
                isLoading(false);
                showToast(msg != null ? msg : "加载角色失败");
            }

            @Override
            public void onNetworkError() {
                isLoading(false);
                showToast(R.string.error_network);
            }
        });
    }

    private void showCharacterPicker() {
        if (characters.isEmpty()) {
            showToast("暂无角色，请先新建角色");
            return;
        }
        String[] names = new String[characters.size()];
        for (int i = 0; i < characters.size(); i++) {
            AiCharacterVO vo = characters.get(i);
            names[i] = vo.name != null ? vo.name : ("角色 " + vo.id);
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.select_character)
                .setItems(names, (d, which) -> {
                    AiCharacterVO vo = characters.get(which);
                    selectedCharacterId = vo.id;
                    binding.btnPickCharacter.setText(names[which]);
                })
                .show();
    }

    private void submit() {
        if (selectedCharacterId == null) {
            showToast("请选择 AI 角色");
            return;
        }
        OpenConversationDTO dto = new OpenConversationDTO();
        dto.characterId = selectedCharacterId;
        String title = binding.etTitle.getText() != null ? binding.etTitle.getText().toString().trim() : "";
        dto.title = title.isEmpty() ? null : title;
        String scene = binding.etScene.getText() != null ? binding.etScene.getText().toString().trim() : "";
        dto.sceneBackground = scene.isEmpty() ? null : scene;

        isLoading(true);
        chatRepository.openConversation(dto, new BaseRepository.DataCallback<DialogConversationDTO>() {
            @Override
            public void onSuccess(DialogConversationDTO data) {
                isLoading(false);
                if (data == null || data.id == null) {
                    showToast("创建失败");
                    return;
                }
                Intent intent = new Intent(CreateConversationActivity.this, ChatDetailActivity.class);
                intent.putExtra(ExtrasConstant.EXTRA_CONVERSATION_ID, String.valueOf(data.id));
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String msg) {
                isLoading(false);
                showToast(!TextUtils.isEmpty(msg) ? msg : "创建失败");
            }

            @Override
            public void onNetworkError() {
                isLoading(false);
                showToast(R.string.error_network);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CREATE_CHARACTER && resultCode == RESULT_OK && data != null) {
            String sid = data.getStringExtra(ExtrasConstant.EXTRA_CHARACTER_ID);
            if (sid != null && !sid.isEmpty()) {
                try {
                    selectedCharacterId = Long.parseLong(sid);
                    binding.btnPickCharacter.setText(getString(R.string.create_conversation_character_selected,
                            getString(R.string.create_conversation_new_character_label, sid)));
                    loadCharacters();
                } catch (NumberFormatException ignored) {
                    showToast("角色 ID 无效");
                }
            }
        }
    }

}
