package com.ailianlian.ablecisi.fragment;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.activity.CreatePostActivity;
import com.ailianlian.ablecisi.activity.PostDetailActivity;
import com.ailianlian.ablecisi.adapter.PostAdapter;
import com.ailianlian.ablecisi.constant.ExtrasConstant;
import com.ailianlian.ablecisi.databinding.FragmentCommunityBinding;
import com.ailianlian.ablecisi.pojo.entity.Post;
import com.ailianlian.ablecisi.viewmodel.CommunityViewModel;
import com.google.android.material.chip.Chip;

public class CommunityFragment extends Fragment implements PostAdapter.PostInteractionListener {

    private FragmentCommunityBinding binding;
    private CommunityViewModel viewModel;
    private PostAdapter adapter;

    private final ActivityResultLauncher<Intent> createPostLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && viewModel != null) {
                    viewModel.refreshPosts();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCommunityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Application app = requireActivity().getApplication();
        viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(app))
                .get(CommunityViewModel.class);

        setupRecyclerView();
        setupChipGroup();
        setupSwipeRefresh();
        setupFab();
        setupActionButtons();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new PostAdapter(this);
        binding.rvPosts.setAdapter(adapter);
    }

    private void setupChipGroup() {
        binding.chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.size() > 0) {
                Chip chip = binding.chipGroup.findViewById(checkedIds.get(0));
                if (chip != null) {
                    viewModel.setCategory(chip.getText().toString());
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );

        binding.swipeRefreshLayout.setOnRefreshListener(() -> viewModel.refreshPosts());
    }

    private void setupFab() {
        binding.fabCreatePost.setOnClickListener(v ->
                createPostLauncher.launch(new Intent(requireContext(), CreatePostActivity.class)));
    }

    private void setupActionButtons() {
        binding.btnSearch.setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.feature_not_available, Toast.LENGTH_SHORT).show());

        binding.btnNotification.setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.feature_not_available, Toast.LENGTH_SHORT).show());
    }

    private void observeViewModel() {
        viewModel.getPosts().observe(getViewLifecycleOwner(), posts -> adapter.submitList(posts));

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading ->
                binding.swipeRefreshLayout.setRefreshing(Boolean.TRUE.equals(isLoading)));

        viewModel.getUserMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getCurrentCategory().observe(getViewLifecycleOwner(), category -> {
            for (int i = 0; i < binding.chipGroup.getChildCount(); i++) {
                Chip chip = (Chip) binding.chipGroup.getChildAt(i);
                if (chip.getText().toString().equals(category)) {
                    chip.setChecked(true);
                    break;
                }
            }
        });
    }

    private void openPostDetail(Post post) {
        if (post == null || post.getId() == null) {
            return;
        }
        Intent i = new Intent(requireContext(), PostDetailActivity.class);
        i.putExtra(ExtrasConstant.EXTRA_POST_ID, post.getId());
        startActivity(i);
    }

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
        viewModel.requestToggleLike(post, isLiked);
    }

    @Override
    public void onCommentClick(Post post) {
        openPostDetail(post);
    }

    @Override
    public void onShareClick(Post post) {
        viewModel.recordShare(post);
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, post.getContent() != null ? post.getContent() : "");
        startActivity(Intent.createChooser(send, getString(R.string.article_share)));
    }

    @Override
    public void onMoreClick(Post post, View anchor) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), anchor);
        popupMenu.getMenuInflater().inflate(R.menu.menu_post_options, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_report) {
                Toast.makeText(requireContext(), "举报帖子: " + post.getId(), Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_follow) {
                if (post.getUser() == null) {
                    Toast.makeText(requireContext(), "无法关注该作者", Toast.LENGTH_SHORT).show();
                    return true;
                }
                boolean isFollowed = Boolean.TRUE.equals(post.getUser().getFollowed());
                viewModel.followPostAuthor(post, !isFollowed);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
