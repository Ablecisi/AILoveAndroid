package com.ailianlian.ablecisi.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.activity.CharacterCustomizeActivity;
import com.ailianlian.ablecisi.constant.StatusCodeConstant;
import com.ailianlian.ablecisi.activity.SettingsActivity;
import com.ailianlian.ablecisi.adapter.CharacterSmallAdapter;
import com.ailianlian.ablecisi.baseclass.BaseFragment;
import com.ailianlian.ablecisi.constant.ExtrasConstant;
import com.ailianlian.ablecisi.databinding.FragmentProfileBinding;
import com.ailianlian.ablecisi.pojo.entity.AiCharacter;
import com.ailianlian.ablecisi.pojo.entity.User;
import com.ailianlian.ablecisi.constant.NetWorkPathConstant;
import com.ailianlian.ablecisi.utils.ImageLoader;
import com.ailianlian.ablecisi.viewmodel.ProfileViewModel;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 个人资料页面
 */
public class ProfileFragment extends BaseFragment<FragmentProfileBinding> {

    private ProfileViewModel viewModel;
    private CharacterSmallAdapter characterAdapter;
    private static final int REQUEST_CODE_PICK_IMAGE = 1001; // 请求码，用于选择图片
    private Uri selectedAvatarUri;

    @Override
    protected FragmentProfileBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentProfileBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        // 设置UI组件
        setupUI();
    }

    @Override
    protected void setListeners() {
        // 设置头像编辑按钮
        binding.buttonEditAvatar.setOnClickListener(v -> {
            pickImageFromGallery();
        });

        binding.buttonEditProfile.setOnClickListener(v -> showEditProfileDialog());

        // 设置分享按钮
        binding.buttonShare.setOnClickListener(v -> {
            shareProfile();
        });

        binding.layoutFollowing.setVisibility(View.GONE);
        binding.layoutFollowers.setVisibility(View.GONE);
        binding.layoutPosts.setVisibility(View.GONE);
        binding.cardFavorites.setVisibility(View.GONE);
        binding.cardHistory.setVisibility(View.GONE);
        binding.cardWallet.setVisibility(View.GONE);
        binding.cardHelp.setVisibility(View.GONE);
        binding.cardAbout.setVisibility(View.GONE);

        // 设置设置按钮
        binding.buttonSettings.setOnClickListener(v -> {
            // 跳转到设置页面
            Intent intent = new Intent(requireContext(), SettingsActivity.class);
            startActivity(intent);
        });


    }

    @Override
    protected void lazyLoadData() {
        // 观察ViewModel数据变化
        observeViewModel();
    }


    private void pickImageFromGallery() { // 选择图片
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == FragmentActivity.RESULT_OK && data != null) {
            selectedAvatarUri = data.getData();
            Glide.with(this).load(selectedAvatarUri).into(binding.imageAvatar);
            if (selectedAvatarUri != null) {
                viewModel.uploadAndSaveAvatar(selectedAvatarUri);
            }
        }
    }

    private void setupUI() {
        viewModel.loadUserProfile();
        viewModel.loadCharacters();
        setupTabs();// 设置标签页
        setupCharacterList(); // 设置角色列表适配器
    }

    private void showEditProfileDialog() {
        User cur = viewModel.getUserProfile().getValue();
        Context ctx = requireContext();
        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);
        EditText nameEt = new EditText(ctx);
        nameEt.setHint("昵称");
        if (cur != null && cur.getName() != null) {
            nameEt.setText(cur.getName());
        }
        EditText descEt = new EditText(ctx);
        descEt.setHint("个人简介");
        if (cur != null && cur.getDescription() != null) {
            descEt.setText(cur.getDescription());
        }
        layout.addView(nameEt);
        layout.addView(descEt);
        new AlertDialog.Builder(ctx)
                .setTitle("编辑资料")
                .setView(layout)
                .setPositiveButton(android.R.string.ok, (d, w) ->
                        viewModel.editProfile(
                                nameEt.getText().toString().trim(),
                                descEt.getText().toString().trim(),
                                cur != null ? cur.getAvatarUrl() : null))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void shareProfile() {
        // 分享个人资料
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        User u = viewModel.getUserProfile().getValue();
        String base = NetWorkPathConstant.BASE_URL;
        intent.putExtra(Intent.EXTRA_TEXT, "我在AI恋恋的个人资料：" + base + "/u/" + (u != null ? u.getId() : ""));
        startActivity(Intent.createChooser(intent, "分享个人资料"));
    }

    private void setupTabs() {
        // 初始化标签指示器
        ViewGroup.LayoutParams params = binding.tabIndicator.getLayoutParams();
        params.width = getResources().getDisplayMetrics().widthPixels / 2;
        binding.tabIndicator.setLayoutParams(params);

        // 设置帖子标签点击事件
        binding.tabPosts.setOnClickListener(v -> {
            // 如果当前已经是帖子标签，则回到菜单
            if (binding.recyclerPosts.getVisibility() == View.VISIBLE) {
                showMenu();
                return;
            }
            // 切换到帖子标签
            binding.tabPosts.setTextColor(getResources().getColor(R.color.accent, null));
            binding.tabPosts.setTypeface(null, android.graphics.Typeface.BOLD);
            binding.tabCharacters.setTextColor(getResources().getColor(R.color.text_secondary, null));
            binding.tabCharacters.setTypeface(null, android.graphics.Typeface.NORMAL);

            // 移动指示器
            binding.tabIndicator.animate().translationX(0).setDuration(200).start();

            // 显示帖子列表，隐藏其他内容
            binding.recyclerPosts.setVisibility(View.VISIBLE);
            binding.recyclerCharacters.setVisibility(View.GONE);
            binding.layoutMenu.setVisibility(View.GONE);

            showToast(getString(R.string.feature_not_available));
        });

        // 设置角色标签点击事件
        binding.tabCharacters.setOnClickListener(v -> {
            // 如果当前已经是角色标签，则回到菜单
            if (binding.recyclerCharacters.getVisibility() == View.VISIBLE) {
                showMenu();
                return;
            }
            // 切换到角色标签
            binding.tabCharacters.setTextColor(getResources().getColor(R.color.accent, null));
            binding.tabCharacters.setTypeface(null, android.graphics.Typeface.BOLD);
            binding.tabPosts.setTextColor(getResources().getColor(R.color.text_secondary, null));
            binding.tabPosts.setTypeface(null, android.graphics.Typeface.NORMAL);

            // 移动指示器
            binding.tabIndicator.animate().translationX(params.width).setDuration(200).start();

            // 显示角色列表，隐藏其他内容
            binding.recyclerPosts.setVisibility(View.GONE);
            binding.recyclerCharacters.setVisibility(View.VISIBLE);
            binding.layoutMenu.setVisibility(View.GONE);

            // 加载角色数据
            viewModel.loadCharacters();
        });

        // 默认显示菜单
        showMenu();
    }

    private void showMenu() {
        // 重置标签样式
        binding.tabPosts.setTextColor(getResources().getColor(R.color.text_secondary, null));
        binding.tabPosts.setTypeface(null, android.graphics.Typeface.NORMAL);
        binding.tabCharacters.setTextColor(getResources().getColor(R.color.text_secondary, null));
        binding.tabCharacters.setTypeface(null, android.graphics.Typeface.NORMAL);

        // 隐藏指示器
        binding.tabIndicator.setVisibility(View.INVISIBLE);

        // 显示菜单，隐藏其他内容
        binding.recyclerPosts.setVisibility(View.GONE);
        binding.recyclerCharacters.setVisibility(View.GONE);
        binding.layoutMenu.setVisibility(View.VISIBLE);
    }

    private void setupCharacterList() {
        // 初始化角色适配器
        characterAdapter = new CharacterSmallAdapter(character -> {
            // 角色点击事件
            Intent intent = new Intent(getContext(), CharacterCustomizeActivity.class);
            intent.putExtra(ExtrasConstant.EXTRA_CHARACTER_ID, character.getId());
            startActivity(intent);
        });

        // 设置RecyclerView
        binding.recyclerCharacters.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.recyclerCharacters.setAdapter(characterAdapter);
    }

    private void observeViewModel() {
        viewModel.getResultMutableLiveData().observe(getViewLifecycleOwner(), result -> {
            if (result != null && result.getCode() != StatusCodeConstant.SUCCESS
                    && result.getMsg() != null && !result.getMsg().isEmpty()) {
                showToast(result.getMsg());
            }
        });
        // 观察用户资料变化
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), this::updateUserProfile);

        // 观察角色列表变化
        viewModel.getCharacters().observe(getViewLifecycleOwner(), characters -> {
            List<AiCharacter> characterList = new ArrayList<>();
            characters.forEach(vo -> {
                AiCharacter character = new AiCharacter();
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
                characterList.add(character);
            });
            characterAdapter.setCharacters(characterList);
        });

        // 观察加载状态
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
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

    private void updateUserProfile(User profile) {
        if (profile != null) {
            // 更新用户信息
            binding.textUsername.setText(profile.getName());
            binding.textUserId.setText("ID: " + fillInWith11Digits(profile.getId()));
            binding.textDescription.setText(profile.getDescription());

            // 更新统计数据
            binding.textFollowingCount.setText(String.valueOf(profile.getFollowingCount()));
            binding.textFollowersCount.setText(String.valueOf(profile.getFollowersCount()));
            binding.textPostsCount.setText(String.valueOf(profile.getPostIds().size()));

            // 加载头像
            ImageLoader.load(getContext(), profile.getAvatarUrl(), binding.imageAvatar);
        }
    }

    private String fillInWith11Digits(Object input) {
        try {
            long number;
            if (input instanceof Long) {
                number = (Long) input;
            } else {
                number = Long.parseLong(input.toString());
            }
            return String.format(Locale.CHINA, "%011d", number); // 转换为11位数字，前面补0
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无法将输入转换为数字: " + input, e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 