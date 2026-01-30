package com.ailianlian.ablecisi.activity;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.baseclass.BaseActivity;
import com.ailianlian.ablecisi.constant.ExtrasConstant;
import com.ailianlian.ablecisi.databinding.ActivityCharacterCustomizeBinding;
import com.ailianlian.ablecisi.pojo.dto.AiCharacterCreateDTO;
import com.ailianlian.ablecisi.pojo.entity.AiCharacter;
import com.ailianlian.ablecisi.utils.ImageLoader;
import com.ailianlian.ablecisi.utils.LoginInfoUtil;
import com.ailianlian.ablecisi.viewmodel.CharacterViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * AI角色定制页面
 */
public class CharacterCustomizeActivity extends BaseActivity<ActivityCharacterCustomizeBinding> {

    private CharacterViewModel characterViewModel;
    private static final int REQUEST_IMAGE_PICK = 100; // 请求码，用于图片选择
    private Uri selectedImageUri; // 选择的头像图片URI
    private Long characterId; // 用于编辑时传入角色ID
    private List<String> typesList;

    @Override
    protected ActivityCharacterCustomizeBinding getViewBinding() {
        return ActivityCharacterCustomizeBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        // 设置Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // 获取传入的角色ID（如果有）
        String i = getIntent().getStringExtra(ExtrasConstant.EXTRA_CHARACTER_ID);
        if (i != null) {
            characterId = Long.parseLong(i);
            binding.buttonCreate.setText("保存修改");
        } else {
            characterId = -1L; // -1表示创建新角色
        }

        Log.i("CharacterCustomizeActivity", "获取 characterId: " + characterId);
        // 初始化ViewModel
        characterViewModel = new ViewModelProvider(this).get(CharacterViewModel.class);
        typesList = new ArrayList<>();
    }

    @Override
    protected void initData() {
        characterViewModel.loadCharacterTypes();
        setupTypeDropdown();// 设置角色类型下拉菜单
        loadCharacters(characterId);// 设置角色信息（如果是编辑模式）
        observeViewModel();// 观察ViewModel数据变化
    }

    @Override
    protected void setListeners() {
        setupClickListeners();// 设置点击事件
    }

    private void loadCharacters(Long characterId) {
        // 如果是编辑模式，加载角色信息
        characterViewModel.loadCharacterById(characterId);
    }

    private void setupTypeDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                typesList
        );
        binding.dropdownType.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void setupClickListeners() {
        // 更换头像按钮
        binding.buttonChangeAvatar.setOnClickListener(v -> {
            openImagePicker();
        });

        // 添加自定义兴趣按钮
        binding.buttonAddInterest.setOnClickListener(v -> {
            String customInterest = binding.editCustomInterest.getText() != null ?
                    binding.editCustomInterest.getText().toString().trim() : "";
            if (!TextUtils.isEmpty(customInterest)) {
                System.out.println("customInterest = " + customInterest);
                addCustomInterestChip(customInterest);
                binding.editCustomInterest.setText("");
            }
        });

        // 创建角色按钮
        binding.buttonCreate.setOnClickListener(v -> {
            // 判断是否有id，如果有则是编辑模式，否则是创建模式
            if (characterId != null && characterId > 0) {
                showToast("编辑角色功能尚未实现");
            } else {
                createCharacter();
            }

        });
    }

    private void observeViewModel() {
        characterViewModel.getCharacterTypes().observe(this, types -> {
            if (types != null) {
                typesList.clear();
                typesList.addAll(types);
                setupTypeDropdown();
            }
        });

        // 观察加载状态
        characterViewModel.getIsLoading().observe(this, isLoading -> {
            binding.loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // 观察创建成功状态
        characterViewModel.getCreateSuccess().observe(this, isSuccess -> {
            if (isSuccess) {
                showToast("角色创建成功");
                finish();
            }
        });

        // 观察错误信息
        characterViewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                showToast("错误: " + errorMsg);
            }
        });

        characterViewModel.getCurrentCharacter().observe(this, vo -> {
            AiCharacter character = new AiCharacter();
            if (vo != null) {
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

                System.out.println("character = " + character);
                Log.i("CharacterCustomizeActivity", "Loaded character: " + character.getName());
                binding.editName.setText(character.getName());
                (binding.dropdownType).setText(character.getType(), false);
                binding.radioMale.setChecked(character.getGender().equals("男"));
                binding.radioFemale.setChecked(character.getGender().equals("女"));
                binding.radioOther.setChecked(character.getGender().equals("其他"));
                binding.editAge.setText(character.getAge() > 0 ? String.valueOf(character.getAge()) : "");
                if (character.getImageUrl() != null) {
                    ImageLoader.load(this, character.getImageUrl(), binding.imageAvatar);
                }
                binding.editPersonalityDesc.setText(character.getDescription());
                binding.editBackground.setText(character.getBackgroundStory());
                binding.chipGroupInterests.removeAllViews();
                for (String interest : character.getInterests()) {
                    addCustomInterestChip(interest);
                }
                binding.chipGroupPersonality.removeAllViews();
                for (String trait : character.getPersonalityTraits()) {
                    Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_custom_interstate, binding.chipGroupPersonality, false);
                    chip.setText(trait);
                    chip.setChecked(true);
                    binding.chipGroupPersonality.addView(chip);
                }
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

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            binding.imageAvatar.setImageURI(selectedImageUri);
        }
    }

    private void addCustomInterestChip(String interest) {
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_custom_interstate, binding.chipGroupInterests, false);
        chip.setText(interest);
        chip.setCloseIconVisible(true);
        chip.setCloseIconTint(getColorStateList(R.color.chip_close_tint));
        chip.setChecked(true);
        chip.setOnCloseIconClickListener(v -> binding.chipGroupInterests.removeView(chip));
        binding.chipGroupInterests.addView(chip);
    }

    private void createCharacter() {
        AiCharacterCreateDTO body = new AiCharacterCreateDTO();
        if (binding.editName.getText() == null || binding.editName.getText().toString().trim().isEmpty()) {
            showToast("角色名称不能为空");
            return;
        }
        body.name = binding.editName.getText().toString().trim();
        body.userId = Long.parseLong(LoginInfoUtil.getUserId(this));
        long typeId = 1L;
        for (String type : typesList) {
            if (type.equals(binding.dropdownType.getText().toString().trim())) {
                body.typeId = typeId;
                break;
            }
            typeId++;
        }
        body.gender = binding.radioMale.isChecked() ? 0 :
                binding.radioFemale.isChecked() ? 1 : 2;

        // 设置年龄
        String ageStr = Objects.requireNonNull(binding.editAge.getText()).toString().trim();
        if (!TextUtils.isEmpty(ageStr)) {
            try {
                body.age = Integer.parseInt(ageStr);
            } catch (Exception e) {
                Toast.makeText(this, "年龄设置无效", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        body.imageUrl = selectedImageUri != null ? selectedImageUri.toString() : "";

        StringBuilder stb = new StringBuilder();
        for (int i = 0; i < binding.chipGroupPersonality.getChildCount(); i++) {
            Chip chip = (Chip) binding.chipGroupPersonality.getChildAt(i);
            if (chip.isChecked()) {
                if (stb.length() > 0) stb.append(",");
                stb.append(chip.getText().toString());
            }
        }
        // 去掉最后一个逗号
        body.traits = stb.toString();
        body.personaDesc = binding.editPersonalityDesc.getText() != null ?
                binding.editPersonalityDesc.getText().toString().trim() : "";

        stb = new StringBuilder();
        for (int i = 0; i < binding.chipGroupInterests.getChildCount(); i++) {
            Chip chip = (Chip) binding.chipGroupInterests.getChildAt(i);
            if (chip.isChecked()) {
                if (stb.length() > 0) stb.append(",");
                stb.append(chip.getText().toString());
            }
        }
        body.interests = stb.toString();
        body.backstory = binding.editBackground.getText() != null ?
                binding.editBackground.getText().toString().trim() : "";
        body.status = 1; // 默认启用
        String errorMsg = characterViewModel.validateCharacter(body);
        if (errorMsg != null) {
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            return;
        }

        // 显示确认对话框
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.character_create)
                .setMessage("确定要创建这个AI角色吗？")
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    characterViewModel.createCustomCharacter(body);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}