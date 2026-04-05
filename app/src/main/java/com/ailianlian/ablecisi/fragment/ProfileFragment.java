package com.ailianlian.ablecisi.fragment;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.activity.EditProfileActivity;
import com.ailianlian.ablecisi.activity.FavoritesActivity;
import com.ailianlian.ablecisi.activity.HistoryActivity;
import com.ailianlian.ablecisi.activity.MyPostsActivity;
import com.ailianlian.ablecisi.activity.SettingsActivity;
import com.ailianlian.ablecisi.activity.WebViewActivity;
import com.ailianlian.ablecisi.baseclass.BaseFragment;
import com.ailianlian.ablecisi.constant.ExtrasConstant;
import com.ailianlian.ablecisi.constant.NetWorkPathConstant;
import com.ailianlian.ablecisi.constant.StatusCodeConstant;
import com.ailianlian.ablecisi.databinding.FragmentProfileBinding;
import com.ailianlian.ablecisi.pojo.entity.User;
import com.ailianlian.ablecisi.utils.ImageLoader;
import com.ailianlian.ablecisi.utils.LoginInfoUtil;
import com.ailianlian.ablecisi.viewmodel.ProfileViewModel;

import java.util.Locale;

/**
 * 个人资料页面
 */
public class ProfileFragment extends BaseFragment<FragmentProfileBinding> {

    private ProfileViewModel viewModel;
    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private Uri selectedAvatarUri;

    @Override
    protected FragmentProfileBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentProfileBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    @Override
    protected void setListeners() {
        binding.buttonEditAvatar.setOnClickListener(v -> pickImageFromGallery());

        binding.buttonEditProfile.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), EditProfileActivity.class)));

        binding.buttonShare.setOnClickListener(v -> shareProfile());

        binding.buttonSettings.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SettingsActivity.class)));

        binding.layoutFollowing.setVisibility(View.VISIBLE);
        binding.layoutFollowers.setVisibility(View.VISIBLE);
        binding.layoutPosts.setVisibility(View.VISIBLE);

        binding.layoutPosts.setOnClickListener(v -> {
            if (!LoginInfoUtil.isLoggedIn(requireContext())) {
                showToast("请先登录");
                return;
            }
            startActivity(new Intent(requireContext(), MyPostsActivity.class));
        });

        binding.cardFavorites.setOnClickListener(v -> {
            if (!LoginInfoUtil.isLoggedIn(requireContext())) {
                showToast("请先登录");
                return;
            }
            startActivity(new Intent(requireContext(), FavoritesActivity.class));
        });

        binding.cardHistory.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), HistoryActivity.class)));

        binding.cardHelp.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), WebViewActivity.class);
            intent.putExtra(ExtrasConstant.EXTRA_WEB_VIEW_TITLE, getString(R.string.profile_help));
            intent.putExtra(ExtrasConstant.EXTRA_ASSET_PATH, "terms.html");
            startActivity(intent);
        });

        binding.cardAbout.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), WebViewActivity.class);
            intent.putExtra(ExtrasConstant.EXTRA_WEB_VIEW_TITLE, getString(R.string.profile_about));
            intent.putExtra(ExtrasConstant.EXTRA_ASSET_PATH, "about.html");
            startActivity(intent);
        });
    }

    @Override
    protected void lazyLoadData() {
        viewModel.loadUserProfile();
        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null && binding != null) {
            viewModel.loadUserProfile();
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == androidx.fragment.app.FragmentActivity.RESULT_OK && data != null) {
            selectedAvatarUri = data.getData();
            if (selectedAvatarUri != null) {
                com.bumptech.glide.Glide.with(this).load(selectedAvatarUri).into(binding.imageAvatar);
                viewModel.uploadAndSaveAvatar(selectedAvatarUri);
            }
        }
    }

    private void shareProfile() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        User u = viewModel.getUserProfile().getValue();
        String base = NetWorkPathConstant.BASE_URL;
        intent.putExtra(Intent.EXTRA_TEXT, "我在AI恋恋的个人资料：" + base + "/u/" + (u != null ? u.getId() : ""));
        startActivity(Intent.createChooser(intent, "分享个人资料"));
    }

    private void observeViewModel() {
        viewModel.getResultMutableLiveData().observe(getViewLifecycleOwner(), result -> {
            if (result != null && result.getCode() != StatusCodeConstant.SUCCESS
                    && result.getMsg() != null && !result.getMsg().isEmpty()) {
                showToast(result.getMsg());
            }
        });
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), this::updateUserProfile);
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading ->
                binding.progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE));
    }

    private void updateUserProfile(User profile) {
        if (profile == null) {
            return;
        }
        binding.textUsername.setText(profile.getName());
        String idText;
        try {
            idText = "ID: " + fillInWith11Digits(profile.getId());
        } catch (Exception e) {
            idText = "ID: " + profile.getId();
        }
        binding.textUserId.setText(idText);
        binding.textDescription.setText(profile.getDescription() != null ? profile.getDescription() : "");

        int following = profile.getFollowingCount() != null ? profile.getFollowingCount() : 0;
        int followers = profile.getFollowersCount() != null ? profile.getFollowersCount() : 0;
        int posts = profile.getPostCount() != null ? profile.getPostCount()
                : (profile.getPostIds() != null ? profile.getPostIds().size() : 0);
        binding.textFollowingCount.setText(String.valueOf(following));
        binding.textFollowersCount.setText(String.valueOf(followers));
        binding.textPostsCount.setText(String.valueOf(posts));

        ImageLoader.load(getContext(), profile.getAvatarUrl(), binding.imageAvatar);
    }

    private String fillInWith11Digits(Object input) {
        long number;
        if (input instanceof Long) {
            number = (Long) input;
        } else {
            number = Long.parseLong(input.toString());
        }
        return String.format(Locale.CHINA, "%011d", number);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
