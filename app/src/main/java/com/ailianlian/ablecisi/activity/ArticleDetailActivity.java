package com.ailianlian.ablecisi.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.adapter.CommentListAdapter;
import com.ailianlian.ablecisi.adapter.RelatedArticleAdapter;
import com.ailianlian.ablecisi.baseclass.BaseActivity;
import com.ailianlian.ablecisi.constant.ExtrasConstant;
import com.ailianlian.ablecisi.databinding.ActivityArticleDetailBinding;
import com.ailianlian.ablecisi.pojo.entity.Article;
import com.ailianlian.ablecisi.pojo.vo.CommentVO;
import com.ailianlian.ablecisi.utils.CommentExpandStateStore;
import com.ailianlian.ablecisi.utils.ImageLoader;
import com.ailianlian.ablecisi.utils.BrowseHistoryStore;
import com.ailianlian.ablecisi.utils.LoginInfoUtil;
import com.ailianlian.ablecisi.utils.MarkDownUtil;
import com.ailianlian.ablecisi.utils.TimeAgoUtil;
import com.ailianlian.ablecisi.viewmodel.ArticleViewModel;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * 文章详情页面
 */
public class ArticleDetailActivity extends BaseActivity<ActivityArticleDetailBinding> {

    private static final long COMMENT_EXPAND_SAVE_DELAY_MS = 480;

    private CommentListAdapter commentListAdapter;
    private RelatedArticleAdapter relatedArticleAdapter;
    private String currentReplyToCommentId;
    private String articleId;
    private boolean isFollowing; // 当前关注状态
    private ArticleViewModel articleViewModel;
    private long articleCurrentRootCommentId; // 当前正在评论的根评论ID

    private final Handler expandSaveHandler = new Handler(Looper.getMainLooper());
    private final Runnable expandSaveRunnable = () -> {
        if (articleId != null && commentListAdapter != null) {
            CommentExpandStateStore.saveExpandedRootIds(getApplicationContext(), articleId,
                    commentListAdapter.getExpandedRootIds());
        }
    };

    /** 从本地恢复楼中楼展开：顺序请求，避免并发写适配器 */
    private Iterator<Long> expandRestoreIterator;
    private long pendingRestoreRootId = -1;

    @Override
    protected ActivityArticleDetailBinding getViewBinding() {
        return ActivityArticleDetailBinding.inflate(getLayoutInflater());
    }

    private void getArticleViewModel() {
        articleViewModel = new ViewModelProvider(this).get(ArticleViewModel.class);
    }

    @Override
    protected void initView() {
        articleId = getIntent().getStringExtra(ExtrasConstant.EXTRA_ARTICLE_ID);

        // 设置工具栏
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) { // 显示返回按钮
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        setupCommentAdapter();// 初始化评论适配器
        setupRelatedArticleAdapter(); // 初始化相关文章适配器

        binding.swipeRefreshLayout.setColorSchemeResources(R.color.primary);
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            if (articleId != null) {
                articleViewModel.refreshArticleDetailPull(articleId);
            } else {
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    protected void initData() {
        getArticleViewModel(); // 获取ViewModel实例
        observeViewModel(); // 观察LiveData变化

        // 获取文章ID并加载文章
        if (articleId == null) {
            showToast("文章ID不能为空");
            finish();
        }
        articleViewModel.loadArticle(articleId); // 加载文章数据（成功后拉取关注状态）
        articleViewModel.loadRelatedArticles(articleId);
        articleViewModel.loadComments(articleId);
    }

    private void observeViewModel() {
        articleViewModel.getDetailRefreshing().observe(this, refreshing -> {
            if (refreshing != null) {
                binding.swipeRefreshLayout.setRefreshing(Boolean.TRUE.equals(refreshing));
            }
        });

        // 观察文章数据变化
        articleViewModel.getArticle().observe(this, article -> {
            if (article != null) {
                updateArticleUI(article);
                BrowseHistoryStore.addArticle(ArticleDetailActivity.this, article.getId(),
                        article.getTitle() != null ? article.getTitle() : "文章");
            } else {
                showToast("文章加载失败");
            }
            isLoading(false);
        });

        // 观察关注状态变化
        articleViewModel.getIsFollowing().observe(this, isFollowing -> {
            this.isFollowing = isFollowing;
            updateFollowButton(isFollowing);
        });

        // 观察相关文章数据变化
        articleViewModel.getRelatedArticles().observe(this, articles -> {
            if (articles != null) {
                relatedArticleAdapter.submitList(articles);
            }
        });

        // 观察分页评论数据变化
        articleViewModel.getPageResultMutableLiveData().observe(this, pageResult -> {
            commentListAdapter.setDataFromBundle(pageResult);
            boolean empty = pageResult == null
                    || pageResult.getRecords() == null
                    || pageResult.getRecords().isEmpty();
            binding.tvNoComments.setVisibility(empty ? View.VISIBLE : View.GONE);
            scheduleExpandStateRestore();
        });

        // 观察展开评论数据变化
        articleViewModel.getComments().observe(this, comments -> {
            if (comments == null) {
                return;
            }
            long targetRoot = pendingRestoreRootId >= 0 ? pendingRestoreRootId : articleCurrentRootCommentId;
            commentListAdapter.onMoreLoaded(targetRoot, comments);
            if (pendingRestoreRootId >= 0) {
                pendingRestoreRootId = -1;
                postExpandRestoreAdvance();
            }
        });

        // 观察错误信息
        articleViewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                showToast(errorMsg);
            }
            isLoading(false);
        });

    }


    @Override
    protected void setListeners() {
        // 关注按钮
        binding.btnFollow.setOnClickListener(v -> {
            isFollowing = !isFollowing;
            updateFollowButton(isFollowing);
            articleViewModel.followAuthor(isFollowing);
        });

        // 点赞按钮
        binding.btnLike.setOnClickListener(v -> {
            articleViewModel.likeArticle();
        });

        // 评论按钮
        binding.btnComment.setOnClickListener(v -> {
            binding.editComment.requestFocus();
        });

        // 分享按钮
        binding.btnShare.setOnClickListener(v -> {
            shareArticle();
        });

        // 收藏按钮
        binding.btnBookmark.setOnClickListener(v -> {
            articleViewModel.bookmarkArticle();
        });

        // 发送评论按钮
        binding.btnSendComment.setOnClickListener(v -> {
            Editable editText = binding.editComment.getText();
            if (editText == null) {
                showToast("评论内容不能为空");
                return;
            }
            String commentContent = editText.toString().trim();
            if (commentContent.isEmpty()) {
                showToast("评论内容不能为空");
                return;
            }
            articleViewModel.createComment(commentContent, currentReplyToCommentId);// 发送评论
            binding.editComment.setText("");// 清空输入框
            currentReplyToCommentId = null; // 重置回复状态
            binding.layoutComment.setHint(R.string.article_comment_hint);// 重置回复状态
            binding.editComment.clearFocus();
        });

        // 点击空白处取消评论输入框焦点
        binding.scrollView.setOnClickListener(v -> {
            binding.editComment.clearFocus();
        });

        // 监听刷新
    }

    private void setupCommentAdapter() {
        commentListAdapter = new CommentListAdapter(this, new CommentListAdapter.Listener() {
            @Override
            public void onReplyClick(CommentVO target) {
                // 打开输入框，parentId = target.id
                currentReplyToCommentId = String.valueOf(target.id);// 设置当前回复的评论ID
                binding.layoutComment.setHint(getString(R.string.article_reply) + " " + target.userName); // 更新评论输入框提示
                binding.editComment.requestFocus();// 聚焦到评论输入框
            }

            @Override
            public void onLikeClick(CommentVO target) {
                if (target.id == null) {
                    return;
                }
                articleViewModel.toggleCommentLike(target.id, Boolean.TRUE.equals(target.liked));
            }

            @Override
            public void onLoadMoreDepth(long rootId, String afterPathCursor, int size) {
                pendingRestoreRootId = -1;
                articleCurrentRootCommentId = rootId;
                articleViewModel.getCommentsTree(rootId, afterPathCursor, size);
            }

            @Override
            public void onAvatarClick(CommentVO target) {
                // 个人主页
                showToast("点击了用户头像：" + target.userName);
            }

        });
        binding.rvComments.setAdapter(commentListAdapter);
    }

    private void setupRelatedArticleAdapter() {
        relatedArticleAdapter = new RelatedArticleAdapter();
        binding.rvRelatedArticles.setAdapter(relatedArticleAdapter);
        relatedArticleAdapter.setOnArticleClickListener(article -> {
            // 跳转到相关文章详情页
            Intent intent = new Intent(ArticleDetailActivity.this, ArticleDetailActivity.class);
            intent.putExtra(ExtrasConstant.EXTRA_ARTICLE_ID, article.getId());
            startActivity(intent);
        });
    }

    private void updateFollowButton(boolean isFollowing) {
        if (isFollowing) {
            binding.btnFollow.setText(R.string.following);
            binding.btnFollow.setBackgroundColor(getResources().getColor(R.color.white, null));
            binding.btnFollow.setTextColor(getResources().getColor(R.color.primary, null));
        } else {
            binding.btnFollow.setText(R.string.follow);
            binding.btnFollow.setBackgroundColor(getResources().getColor(R.color.primary, null));
            binding.btnFollow.setTextColor(getResources().getColor(R.color.white, null));
        }
    }

    private void updateArticleUI(Article article) {
        isLoading(true);
        if (article == null) return;

        // 设置文章标题
        binding.tvTitle.setText(article.getTitle());

        // 设置作者信息
        binding.tvAuthorName.setText(article.getAuthorName());
        binding.tvPublishTime.setText(TimeAgoUtil.toTimeAgo(article.getPublishTime()));
        ImageLoader.load(this, article.getAuthorAvatarUrl(), binding.ivAuthorAvatar, R.drawable.ic_profile, R.drawable.ic_profile);

        // 设置作者头像
        String avatarUrl = LoginInfoUtil.getAvatarUrl(this);
        ImageLoader.load(this, avatarUrl, binding.ivUserAvatar, R.drawable.ic_profile, R.drawable.ic_profile);

        // 设置文章标签
        binding.chipGroupTags.removeAllViews();
        for (String tag : article.getTags()) {
            Chip chip = new Chip(this);
            chip.setText(tag);
            chip.setClickable(false);
            chip.setChipBackgroundColorResource(R.color.primary_light);
            chip.setTextColor(getResources().getColor(R.color.text_primary, null));
            binding.chipGroupTags.addView(chip);
        }

        // 设置文章内容
        MarkDownUtil.setMarkdown(this, binding.tvContent, article.getContent()); // 使用Markwon渲染Markdown内容

        // 设置文章统计信息
        binding.tvViewCount.setText(article.getViewCount() + " 阅读");
        binding.tvCommentCount.setText(article.getCommentCount() + " 评论");
        binding.tvLikeCount.setText(article.getLikeCount() + " 点赞");
        updateLikeButton(article.getLiked());// 设置点赞状态
        updateBookmarkButton(article.getBookmarked());// 设置收藏状态

    }

    private void updateLikeButton(boolean isLiked) {
        if (isLiked) {
            binding.btnLike.setText(R.string.article_liked);
            binding.btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_heart_filled, 0, 0, 0);
            binding.btnLike.setTextColor(getResources().getColor(R.color.primary, null));
        } else {
            binding.btnLike.setText(R.string.article_like);
            binding.btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_heart, 0, 0, 0);
            binding.btnLike.setTextColor(getResources().getColor(R.color.text_primary, null));
        }
    }

    private void updateBookmarkButton(boolean isBookmarked) {
        if (isBookmarked) {
            binding.btnBookmark.setText(R.string.article_bookmarked);
            binding.btnBookmark.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_bookmark_filled, 0, 0, 0);
            binding.btnBookmark.setTextColor(getResources().getColor(R.color.primary, null));
        } else {
            binding.btnBookmark.setText(R.string.article_bookmark);
            binding.btnBookmark.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_bookmark_border, 0, 0, 0);
            binding.btnBookmark.setTextColor(getResources().getColor(R.color.text_primary, null));
        }
    }

    private void shareArticle() {
        Article article = articleViewModel.getArticle().getValue();
        if (article == null) {
            return;
        }
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, article.getTitle());
        String body = article.getTitle() + "\n\n";
        if (article.getContent() != null && !article.getContent().isEmpty()) {
            body += article.getContent().substring(0, Math.min(100, article.getContent().length())) + "...";
        }
        shareIntent.putExtra(Intent.EXTRA_TEXT, body);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.article_share)));
    }

    private void scheduleExpandStateRestore() {
        expandRestoreIterator = null;
        pendingRestoreRootId = -1;
        if (TextUtils.isEmpty(articleId) || commentListAdapter == null) {
            return;
        }
        Set<Long> saved = CommentExpandStateStore.loadExpandedRootIds(getApplicationContext(), articleId);
        if (saved.isEmpty()) {
            return;
        }
        expandRestoreIterator = new ArrayList<>(saved).iterator();
        postExpandRestoreAdvance();
    }

    private void postExpandRestoreAdvance() {
        binding.getRoot().post(this::expandRestoreAdvance);
    }

    private void expandRestoreAdvance() {
        if (expandRestoreIterator == null || commentListAdapter == null) {
            return;
        }
        while (expandRestoreIterator.hasNext()) {
            long rootId = expandRestoreIterator.next();
            if (!commentListAdapter.hasRoot(rootId)) {
                continue;
            }
            if (commentListAdapter.getExpandedRootIds().contains(rootId)) {
                continue;
            }
            articleCurrentRootCommentId = rootId;
            pendingRestoreRootId = rootId;
            articleViewModel.getCommentsTree(rootId, null, CommentListAdapter.firstExpandBatchSize());
            return;
        }
        expandRestoreIterator = null;
    }

    @Override
    protected void onStop() {
        expandSaveHandler.removeCallbacks(expandSaveRunnable);
        expandSaveHandler.postDelayed(expandSaveRunnable, COMMENT_EXPAND_SAVE_DELAY_MS);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        expandSaveHandler.removeCallbacks(expandSaveRunnable);
        if (articleId != null && commentListAdapter != null) {
            CommentExpandStateStore.saveExpandedRootIds(getApplicationContext(), articleId,
                    commentListAdapter.getExpandedRootIds());
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (articleId != null) {
            articleViewModel.recordArticleViewOnce(articleId);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 