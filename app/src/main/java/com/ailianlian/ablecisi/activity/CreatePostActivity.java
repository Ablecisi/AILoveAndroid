package com.ailianlian.ablecisi.activity;

import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.baseclass.BaseActivity;
import com.ailianlian.ablecisi.baseclass.BaseRepository;
import com.ailianlian.ablecisi.databinding.ActivityCreatePostBinding;
import com.ailianlian.ablecisi.pojo.dto.PostCreateBodyDTO;
import com.ailianlian.ablecisi.repository.CommunityRepository;
import com.ailianlian.ablecisi.repository.ProfileRepository;
import com.ailianlian.ablecisi.utils.LoginInfoUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreatePostActivity extends BaseActivity<ActivityCreatePostBinding> {

    private static final int REQUEST_IMAGE_PICK = 103;
    private Uri selectedImageUri;
    private CommunityRepository communityRepository;
    private ProfileRepository profileRepository;

    @Override
    protected ActivityCreatePostBinding getViewBinding() {
        return ActivityCreatePostBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        if (!LoginInfoUtil.isLoggedIn(this)) {
            showToast("请先登录");
            finish();
            return;
        }
        communityRepository = new CommunityRepository(this);
        profileRepository = new ProfileRepository(this);
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    protected void initData() {
    }

    @Override
    protected void setListeners() {
        binding.btnPickImage.setOnClickListener(v -> openImagePicker());
        binding.btnClearImage.setOnClickListener(v -> clearImage());
        binding.btnPublish.setOnClickListener(v -> submit());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void clearImage() {
        selectedImageUri = null;
        binding.ivPreview.setVisibility(android.view.View.GONE);
        binding.ivPreview.setImageDrawable(null);
        binding.btnClearImage.setVisibility(android.view.View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                binding.ivPreview.setImageURI(selectedImageUri);
                binding.ivPreview.setVisibility(android.view.View.VISIBLE);
                binding.btnClearImage.setVisibility(android.view.View.VISIBLE);
            }
        }
    }

    private void submit() {
        String text = binding.editContent.getText() != null
                ? binding.editContent.getText().toString().trim() : "";
        if (text.isEmpty()) {
            showToast("请输入内容");
            return;
        }
        binding.btnPublish.setEnabled(false);
        if (selectedImageUri != null) {
            isLoading(true);
            profileRepository.uploadAvatarImage(selectedImageUri, getContentResolver(),
                    new BaseRepository.DataCallback<String>() {
                        @Override
                        public void onSuccess(String url) {
                            runOnUiThread(() -> {
                                isLoading(false);
                                binding.btnPublish.setEnabled(true);
                                List<String> urls = new ArrayList<>();
                                if (url != null && !url.isEmpty()) {
                                    urls.add(url);
                                }
                                publishWithUrls(text, urls);
                            });
                        }

                        @Override
                        public void onError(String msg) {
                            runOnUiThread(() -> {
                                isLoading(false);
                                binding.btnPublish.setEnabled(true);
                                showToast(msg != null ? msg : "图片上传失败");
                            });
                        }

                        @Override
                        public void onNetworkError() {
                            runOnUiThread(() -> {
                                isLoading(false);
                                binding.btnPublish.setEnabled(true);
                                showToast(R.string.error_network);
                            });
                        }
                    });
        } else {
            publishWithUrls(text, new ArrayList<>());
        }
    }

    private void publishWithUrls(String content, List<String> imageUrls) {
        PostCreateBodyDTO body = new PostCreateBodyDTO();
        body.content = content;
        body.imageUrls = imageUrls == null ? new ArrayList<>() : new ArrayList<>(imageUrls);
        body.tags = new ArrayList<>();
        isLoading(true);
        binding.btnPublish.setEnabled(false);
        communityRepository.createPost(body, new BaseRepository.DataCallback<Long>() {
            @Override
            public void onSuccess(Long id) {
                runOnUiThread(() -> {
                    isLoading(false);
                    binding.btnPublish.setEnabled(true);
                    showToast(R.string.post_publish_success);
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onError(String msg) {
                runOnUiThread(() -> {
                    isLoading(false);
                    binding.btnPublish.setEnabled(true);
                    showToast(msg != null ? msg : "发布失败");
                });
            }

            @Override
            public void onNetworkError() {
                runOnUiThread(() -> {
                    isLoading(false);
                    binding.btnPublish.setEnabled(true);
                    showToast(R.string.error_network);
                });
            }
        });
    }
}
