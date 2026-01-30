package com.ailianlian.ablecisi.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.adapter.PostAdapter;
import com.ailianlian.ablecisi.databinding.FragmentCommunityBinding;
import com.ailianlian.ablecisi.pojo.entity.Post;
import com.ailianlian.ablecisi.viewmodel.CommunityViewModel;
import com.google.android.material.chip.Chip;

public class CommunityFragment extends Fragment implements PostAdapter.PostInteractionListener {

    private FragmentCommunityBinding binding;
    private CommunityViewModel viewModel;
    private PostAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCommunityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(CommunityViewModel.class);
        
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
        
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.refreshPosts();
        });
    }

    private void setupFab() {
        binding.fabCreatePost.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "创建新帖子功能即将上线", Toast.LENGTH_SHORT).show();
            // TODO: 跳转到创建帖子页面
        });
    }

    private void setupActionButtons() {
        binding.btnSearch.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "搜索功能即将上线", Toast.LENGTH_SHORT).show();
            // TODO: 跳转到搜索页面
        });
        
        binding.btnNotification.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "通知功能即将上线", Toast.LENGTH_SHORT).show();
            // TODO: 跳转到通知页面
        });
    }

    private void observeViewModel() {
        viewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            adapter.submitList(posts);
        });
        
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.swipeRefreshLayout.setRefreshing(isLoading);
        });
        
        viewModel.getCurrentCategory().observe(getViewLifecycleOwner(), category -> {
            // 根据当前分类更新UI
            for (int i = 0; i < binding.chipGroup.getChildCount(); i++) {
                Chip chip = (Chip) binding.chipGroup.getChildAt(i);
                if (chip.getText().toString().equals(category)) {
                    chip.setChecked(true);
                    break;
                }
            }
        });
    }

    @Override
    public void onPostClick(Post post) {
        Toast.makeText(requireContext(), "查看帖子详情: " + post.getId(), Toast.LENGTH_SHORT).show();
        // TODO: 跳转到帖子详情页
    }

    @Override
    public void onUserClick(Post post) {
        Toast.makeText(requireContext(), "查看用户资料: " + post.getUser().getName(), Toast.LENGTH_SHORT).show();
        // TODO: 跳转到用户资料页
    }

    @Override
    public void onLikeClick(Post post, boolean isLiked) {
        viewModel.likePost(post, isLiked);
        String message = isLiked ? "已点赞" : "已取消点赞";
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCommentClick(Post post) {
        Toast.makeText(requireContext(), "查看评论: " + post.getId(), Toast.LENGTH_SHORT).show();
        // TODO: 跳转到评论页面
    }

    @Override
    public void onShareClick(Post post) {
        Toast.makeText(requireContext(), "分享帖子: " + post.getId(), Toast.LENGTH_SHORT).show();
        // TODO: 显示分享对话框
    }

    @Override
    public void onMoreClick(Post post, View view) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_post_options, popupMenu.getMenu());
        
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_report) {
                Toast.makeText(requireContext(), "举报帖子: " + post.getId(), Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_follow) {
                boolean isFollowed = post.getUser().getFollowed();
                post.getUser().setFollowed(!isFollowed);
                String message = isFollowed ? "已取消关注" : "已关注";
                Toast.makeText(requireContext(), message + ": " + post.getUser().getName(), Toast.LENGTH_SHORT).show();
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