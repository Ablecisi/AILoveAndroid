package com.ailianlian.ablecisi.fragment;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.activity.CharacterCustomizeActivity;
import com.ailianlian.ablecisi.activity.EditProfileActivity;
import com.ailianlian.ablecisi.activity.FavoritesActivity;
import com.ailianlian.ablecisi.activity.HistoryActivity;
import com.ailianlian.ablecisi.activity.PostDetailActivity;
import com.ailianlian.ablecisi.activity.SettingsActivity;
import com.ailianlian.ablecisi.activity.WebViewActivity;
import com.ailianlian.ablecisi.adapter.PostAdapter;
import com.ailianlian.ablecisi.adapter.ProfileAiCharacterAdapter;
import com.ailianlian.ablecisi.baseclass.BaseFragment;
import com.ailianlian.ablecisi.constant.ExtrasConstant;
import com.ailianlian.ablecisi.constant.NetWorkPathConstant;
import com.ailianlian.ablecisi.constant.StatusCodeConstant;
import com.ailianlian.ablecisi.databinding.FragmentProfileBinding;
import com.ailianlian.ablecisi.pojo.entity.AiCharacter;
import com.ailianlian.ablecisi.pojo.entity.Post;
import com.ailianlian.ablecisi.pojo.entity.User;
import com.ailianlian.ablecisi.pojo.vo.AiCharacterVO;
import com.ailianlian.ablecisi.utils.ImageLoader;
import com.ailianlian.ablecisi.utils.LoginInfoUtil;
import com.ailianlian.ablecisi.viewmodel.ProfileViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 个人资料页面
 */
public class ProfileFragment extends BaseFragment<FragmentProfileBinding> {

    private static final int TAB_POSTS = 0;
    private static final int TAB_CHARACTERS = 1;

    private ProfileViewModel viewModel;
    private PostAdapter postAdapter;
    private ProfileAiCharacterAdapter characterAdapter;
    private int currentProfileTab = TAB_POSTS;
    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private Uri selectedAvatarUri;

    @Override
    protected FragmentProfileBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentProfileBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        postAdapter = new PostAdapter(new PostAdapter.PostInteractionListener() {
            @Override
            public void onPostClick(Post post) {
                openPostDetail(post);
            }

            @Override
            public void onUserClick(Post post) {
                openPostDetail(post);
            }

            @Override
            public void onLikeClick(Post post, boolean isLiked) {
            }

            @Override
            public void onCommentClick(Post post) {
                openPostDetail(post);
            }

            @Override
            public void onShareClick(Post post) {
            }

            @Override
            public void onMoreClick(Post post, View view) {
            }
        });
        binding.recyclerProfilePosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerProfilePosts.setAdapter(postAdapter);

        characterAdapter = new ProfileAiCharacterAdapter(
                character -> {
                    Intent intent = new Intent(requireContext(), CharacterCustomizeActivity.class);
                    intent.putExtra(ExtrasConstant.EXTRA_CHARACTER_ID, character.getId());
                    startActivity(intent);
                },
                () -> startActivity(new Intent(requireContext(), CharacterCustomizeActivity.class))
        );
        binding.recyclerProfileCharacters.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.recyclerProfileCharacters.setAdapter(characterAdapter);

        showProfileTab(TAB_POSTS);
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
            showProfileTab(TAB_POSTS);
        });

        binding.tabProfilePosts.setOnClickListener(v -> showProfileTab(TAB_POSTS));
        binding.tabProfileCharacters.setOnClickListener(v -> showProfileTab(TAB_CHARACTERS));

        binding.cardFavorites.setOnClickListener(v -> {
            if (!LoginInfoUtil.isLoggedIn(requireContext())) {
                showToast("请先登录");
                return;
            }
            startActivity(new Intent(requireContext(), FavoritesActivity.class));
        });

        binding.cardHistory.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), HistoryActivity.class)));

        binding.cardWallet.setOnClickListener(v ->
                showToast(getString(R.string.profile_wallet_coming_soon)));

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
        viewModel.loadMyPosts();
        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null && binding != null) {
            viewModel.loadUserProfile();
            viewModel.loadMyPosts();
            if (currentProfileTab == TAB_CHARACTERS && LoginInfoUtil.isLoggedIn(requireContext())) {
                viewModel.loadCharacters(false);
            }
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
        viewModel.getMyPosts().observe(getViewLifecycleOwner(), this::bindMyPosts);
        viewModel.getCharacters().observe(getViewLifecycleOwner(), this::bindProfileCharacters);
    }

    private void showProfileTab(int tab) {
        currentProfileTab = tab;
        boolean posts = tab == TAB_POSTS;
        binding.recyclerProfilePosts.setVisibility(posts ? View.VISIBLE : View.GONE);
        binding.recyclerProfileCharacters.setVisibility(posts ? View.GONE : View.VISIBLE);
        updatePostsEmptyVisibility();

        int accent = ContextCompat.getColor(requireContext(), R.color.accent);
        int secondary = ContextCompat.getColor(requireContext(), R.color.text_secondary);
        if (posts) {
            binding.tabProfilePosts.setTextColor(accent);
            binding.tabProfilePosts.setTypeface(null, Typeface.BOLD);
            binding.tabProfileCharacters.setTextColor(secondary);
            binding.tabProfileCharacters.setTypeface(null, Typeface.NORMAL);
        } else {
            binding.tabProfilePosts.setTextColor(secondary);
            binding.tabProfilePosts.setTypeface(null, Typeface.NORMAL);
            binding.tabProfileCharacters.setTextColor(accent);
            binding.tabProfileCharacters.setTypeface(null, Typeface.BOLD);
            if (LoginInfoUtil.isLoggedIn(requireContext())) {
                viewModel.loadCharacters(false);
            } else {
                characterAdapter.setCharacters(Collections.emptyList());
            }
        }
    }

    private void updatePostsEmptyVisibility() {
        List<Post> posts = viewModel.getMyPosts().getValue();
        boolean empty = posts == null || posts.isEmpty();
        boolean show = currentProfileTab == TAB_POSTS
                && empty
                && LoginInfoUtil.isLoggedIn(requireContext());
        binding.tvProfilePostsEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void bindMyPosts(List<Post> posts) {
        postAdapter.submitList(posts != null ? posts : List.of());
        updatePostsEmptyVisibility();
    }

    private void bindProfileCharacters(List<AiCharacterVO> vos) {
        characterAdapter.setCharacters(mapVosToCharacters(vos));
    }

    private List<AiCharacter> mapVosToCharacters(List<AiCharacterVO> vos) {
        List<AiCharacter> list = new ArrayList<>();
        if (vos != null) {
            for (AiCharacterVO vo : vos) {
                if (vo == null) {
                    continue;
                }
                AiCharacter character = new AiCharacter();
                character.setId(String.valueOf(vo.id));
                character.setAge(vo.age);
                character.setDescription(vo.personaDesc);
                character.setInterests(parseCommaSeparated(vo.interests));
                character.setName(vo.name);
                character.setBackgroundStory(vo.backstory);
                if (vo.gender != null) {
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
                }
                character.setImageUrl(vo.imageUrl);
                character.setPersonalityTraits(parseCommaSeparated(vo.traits));
                character.setOnline(vo.online != null && vo.online == 1);
                character.setType(vo.typeName);
                character.setCreatedAt(vo.createTime);
                list.add(character);
            }
        }
        return list;
    }

    private List<String> parseCommaSeparated(String str) {
        if (str == null || str.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String[] parts = str.split(",");
        List<String> out = new ArrayList<>();
        for (String part : parts) {
            String t = part.trim();
            if (!t.isEmpty()) {
                out.add(t);
            }
        }
        return out;
    }

    private void openPostDetail(Post post) {
        if (post == null || post.getId() == null) {
            return;
        }
        Intent i = new Intent(requireContext(), PostDetailActivity.class);
        i.putExtra(ExtrasConstant.EXTRA_POST_ID, post.getId());
        startActivity(i);
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
