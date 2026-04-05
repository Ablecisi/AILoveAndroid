package com.ailianlian.ablecisi.activity;

import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.baseclass.BaseActivity;
import com.ailianlian.ablecisi.databinding.ActivityEditProfileBinding;
import com.ailianlian.ablecisi.pojo.entity.User;
import com.ailianlian.ablecisi.utils.ImageLoader;
import com.ailianlian.ablecisi.utils.LoginInfoUtil;
import com.ailianlian.ablecisi.viewmodel.ProfileViewModel;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileActivity extends BaseActivity<ActivityEditProfileBinding> {

    private static final int REQ_PICK = 2201;
    private ProfileViewModel viewModel;
    private Uri pendingAvatarUri;
    private boolean formFilledFromServer;

    @Override
    protected ActivityEditProfileBinding getViewBinding() {
        return ActivityEditProfileBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        if (!LoginInfoUtil.isLoggedIn(this)) {
            showToast("请先登录");
            finish();
            return;
        }
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    protected void initData() {
        viewModel.loadUserProfile();
        viewModel.getUserProfile().observe(this, this::bindUser);
        viewModel.getResultMutableLiveData().observe(this, r -> {
            if (r != null && r.getMsg() != null && !r.getMsg().isEmpty()) {
                showToast(r.getMsg());
            }
        });
        viewModel.getIsLoading().observe(this, loading -> isLoading(Boolean.TRUE.equals(loading)));
    }

    private void bindUser(User u) {
        if (u == null) {
            return;
        }
        if (!formFilledFromServer) {
            binding.editUsername.setText(u.getUsername() != null ? u.getUsername() : "");
            binding.editName.setText(u.getName() != null ? u.getName() : "");
            binding.editDescription.setText(u.getDescription() != null ? u.getDescription() : "");
            formFilledFromServer = true;
        }
        if (pendingAvatarUri == null) {
            ImageLoader.load(this, u.getAvatarUrl(), binding.imageAvatar);
        }
    }

    @Override
    protected void setListeners() {
        binding.buttonPickAvatar.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_PICK);
            i.setType("image/*");
            startActivityForResult(i, REQ_PICK);
        });
        binding.buttonSave.setOnClickListener(v -> {
            String username = textOf(binding.editUsername);
            String name = textOf(binding.editName);
            String desc = textOf(binding.editDescription);
            if (username.isEmpty()) {
                showToast("用户名不能为空");
                return;
            }
            if (name.isEmpty()) {
                showToast("昵称不能为空");
                return;
            }
            viewModel.saveAllProfileFields(username, name, desc, pendingAvatarUri);
            pendingAvatarUri = null;
        });
    }

    private static String textOf(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK && resultCode == RESULT_OK && data != null) {
            pendingAvatarUri = data.getData();
            if (pendingAvatarUri != null) {
                Glide.with(this).load(pendingAvatarUri).into(binding.imageAvatar);
            }
        }
    }
}
