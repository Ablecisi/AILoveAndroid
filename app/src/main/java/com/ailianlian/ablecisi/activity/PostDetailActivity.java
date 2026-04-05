package com.ailianlian.ablecisi.activity;

import android.content.Intent;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.adapter.CommentListAdapter;
import com.ailianlian.ablecisi.baseclass.BaseActivity;
import com.ailianlian.ablecisi.constant.ExtrasConstant;
import com.ailianlian.ablecisi.databinding.ActivityPostDetailBinding;
import com.ailianlian.ablecisi.pojo.entity.Post;
import com.ailianlian.ablecisi.pojo.vo.CommentVO;
import com.ailianlian.ablecisi.utils.BrowseHistoryStore;
import com.ailianlian.ablecisi.utils.ImageLoader;
import com.ailianlian.ablecisi.utils.LoginInfoUtil;
import com.ailianlian.ablecisi.viewmodel.PostDetailViewModel;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;

import java.time.format.DateTimeFormatter;

public class PostDetailActivity extends BaseActivity<ActivityPostDetailBinding> {

    private PostDetailViewModel viewModel;
    private CommentListAdapter commentAdapter;
    private String currentReplyToCommentId;
    private long pendingCommentRootId = -1;

    @Override
    protected ActivityPostDetailBinding getViewBinding() {
        return ActivityPostDetailBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        String postId = getIntent().getStringExtra(ExtrasConstant.EXTRA_POST_ID);
        if (postId == null) {
            showToast("帖子不存在");
            finish();
            return;
        }
        viewModel = new ViewModelProvider(this).get(PostDetailViewModel.class);
        viewModel.initPostId(postId);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.rvComments.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentListAdapter(this, new CommentListAdapter.Listener() {
            @Override
            public void onReplyClick(CommentVO target) {
                currentReplyToCommentId = String.valueOf(target.id);
                binding.editComment.setHint(getString(R.string.article_reply) + " " + target.userName);
                binding.editComment.requestFocus();
            }

            @Override
            public void onLikeClick(CommentVO target) {
                /* 帖子页评论点赞可后续接 CommentRepository */
            }

            @Override
            public void onLoadMoreDepth(long rootId, String afterPathCursor, int size) {
                pendingCommentRootId = rootId;
                viewModel.getCommentsTree(rootId, afterPathCursor, size);
            }

            @Override
            public void onAvatarClick(CommentVO target) {
            }
        });
        binding.rvComments.setAdapter(commentAdapter);

        binding.swipeRefreshLayout.setColorSchemeResources(R.color.primary);
        binding.swipeRefreshLayout.setOnRefreshListener(() -> viewModel.refreshAll());
    }

    @Override
    protected void initData() {
        viewModel.loadPost();
        viewModel.loadComments();
        observe();
    }

    private void observe() {
        viewModel.getPost().observe(this, this::bindPost);
        viewModel.getIsFollowing().observe(this, this::updateFollowButton);
        viewModel.getPageResult().observe(this, pr -> {
            commentAdapter.setDataFromBundle(pr);
            boolean empty = pr == null || pr.getRecords() == null || pr.getRecords().isEmpty();
            binding.tvNoComments.setVisibility(empty ? View.VISIBLE : View.GONE);
        });
        viewModel.getComments().observe(this, list -> {
            if (list == null) {
                return;
            }
            long root = pendingCommentRootId >= 0 ? pendingCommentRootId : 0;
            if (root > 0) {
                commentAdapter.onMoreLoaded(root, list);
            }
            pendingCommentRootId = -1;
        });
        viewModel.getIsLoading().observe(this, loading -> {
            boolean busy = Boolean.TRUE.equals(loading);
            binding.progressBar.setVisibility(busy ? View.VISIBLE : View.GONE);
            if (!busy) {
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });
        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                showToast(msg);
            }
        });
    }

    private void bindPost(Post p) {
        if (p == null) {
            return;
        }
        if (p.getUser() != null) {
            binding.tvAuthorName.setText(p.getUser().getName());
            Glide.with(this)
                    .load(p.getUser().getAvatarUrl())
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(binding.ivAuthorAvatar);
        }
        if (p.getCreatedAt() != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            binding.tvPublishTime.setText(p.getCreatedAt().format(fmt));
        }
        binding.tvContent.setText(p.getContent() != null ? p.getContent() : "");
        String preview = p.getContent() != null && p.getContent().length() > 48
                ? p.getContent().substring(0, 48) + "…" : p.getContent();
        if (p.getId() != null) {
            BrowseHistoryStore.addPost(this, p.getId(), preview);
        }
        if (p.getImageUrls() != null && !p.getImageUrls().isEmpty()) {
            binding.ivPostImage.setVisibility(View.VISIBLE);
            ImageLoader.load(this, p.getImageUrls().get(0), binding.ivPostImage);
        } else {
            binding.ivPostImage.setVisibility(View.GONE);
        }
        binding.chipGroupTags.removeAllViews();
        if (p.getTags() != null) {
            for (String tag : p.getTags()) {
                if (tag == null || tag.isEmpty()) {
                    continue;
                }
                Chip chip = new Chip(this);
                chip.setText("#" + tag);
                chip.setCheckable(false);
                binding.chipGroupTags.addView(chip);
            }
        }
        binding.btnFollow.setVisibility(LoginInfoUtil.isLoggedIn(this) ? View.VISIBLE : View.GONE);
    }

    private void updateFollowButton(Boolean following) {
        boolean f = Boolean.TRUE.equals(following);
        binding.btnFollow.setText(f ? R.string.unfollow : R.string.follow);
    }

    @Override
    protected void setListeners() {
        binding.btnFollow.setOnClickListener(v -> {
            Boolean cur = viewModel.getIsFollowing().getValue();
            viewModel.followAuthor(!Boolean.TRUE.equals(cur));
        });
        binding.btnLike.setOnClickListener(v -> viewModel.togglePostLike());
        binding.btnComment.setOnClickListener(v -> binding.editComment.requestFocus());
        binding.btnShare.setOnClickListener(v -> sharePost());
        binding.btnSendComment.setOnClickListener(v -> {
            String t = binding.editComment.getText() != null ? binding.editComment.getText().toString().trim() : "";
            if (t.isEmpty()) {
                showToast("评论内容不能为空");
                return;
            }
            viewModel.createComment(t, currentReplyToCommentId);
            binding.editComment.setText("");
            currentReplyToCommentId = null;
            binding.editComment.setHint(R.string.post_comment_hint);
        });
    }

    private void sharePost() {
        Post p = viewModel.getPost().getValue();
        if (p == null) {
            return;
        }
        viewModel.recordShare();
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, p.getContent() != null ? p.getContent() : "");
        startActivity(Intent.createChooser(send, getString(R.string.article_share)));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
