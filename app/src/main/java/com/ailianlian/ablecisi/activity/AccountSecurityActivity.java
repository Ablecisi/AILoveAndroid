package com.ailianlian.ablecisi.activity;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.baseclass.BaseActivity;
import com.ailianlian.ablecisi.baseclass.BaseRepository;
import com.ailianlian.ablecisi.databinding.ActivityAccountSecurityBinding;
import com.ailianlian.ablecisi.repository.ProfileRepository;
import com.ailianlian.ablecisi.utils.LoginInfoUtil;

public class AccountSecurityActivity extends BaseActivity<ActivityAccountSecurityBinding> {

    private ProfileRepository profileRepository;

    @Override
    protected ActivityAccountSecurityBinding getViewBinding() {
        return ActivityAccountSecurityBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        if (!LoginInfoUtil.isLoggedIn(this)) {
            showToast("请先登录");
            finish();
            return;
        }
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
        binding.buttonSubmit.setOnClickListener(v -> {
            String oldP = binding.editOldPassword.getText() != null
                    ? binding.editOldPassword.getText().toString() : "";
            String newP = binding.editNewPassword.getText() != null
                    ? binding.editNewPassword.getText().toString() : "";
            if (oldP.isEmpty() || newP.isEmpty()) {
                showToast("请填写完整");
                return;
            }
            if (newP.length() < 6) {
                showToast(R.string.password_new_hint);
                return;
            }
            isLoading(true);
            profileRepository.changePassword(oldP, newP, new BaseRepository.DataCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    runOnUiThread(() -> {
                        isLoading(false);
                        showToast("密码已更新，请重新登录");
                        LoginInfoUtil.logout(AccountSecurityActivity.this);
                    });
                }

                @Override
                public void onError(String msg) {
                    runOnUiThread(() -> {
                        isLoading(false);
                        showToast(msg != null ? msg : "修改失败");
                    });
                }

                @Override
                public void onNetworkError() {
                    runOnUiThread(() -> {
                        isLoading(false);
                        showToast(R.string.error_network);
                    });
                }
            });
        });
    }
}
