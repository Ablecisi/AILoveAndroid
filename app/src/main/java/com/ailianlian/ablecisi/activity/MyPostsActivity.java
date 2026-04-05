package com.ailianlian.ablecisi.activity;

import android.content.Intent;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.adapter.PostAdapter;
import com.ailianlian.ablecisi.baseclass.BaseActivity;
import com.ailianlian.ablecisi.constant.ExtrasConstant;
import com.ailianlian.ablecisi.databinding.ActivitySimpleListBinding;
import com.ailianlian.ablecisi.pojo.entity.Post;
import com.ailianlian.ablecisi.repository.CommunityRepository;
import com.ailianlian.ablecisi.utils.LoginInfoUtil;
import com.ailianlian.ablecisi.baseclass.BaseRepository;

import java.util.List;

public class MyPostsActivity extends BaseActivity<ActivitySimpleListBinding> {

    private PostAdapter adapter;
    private CommunityRepository repository;

    @Override
    protected ActivitySimpleListBinding getViewBinding() {
        return ActivitySimpleListBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        if (!LoginInfoUtil.isLoggedIn(this)) {
            showToast("请先登录");
            finish();
            return;
        }
        repository = new CommunityRepository(this);
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.my_posts_title);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PostAdapter(new PostAdapter.PostInteractionListener() {
            @Override
            public void onPostClick(Post post) {
                openPost(post);
            }

            @Override
            public void onUserClick(Post post) {
                openPost(post);
            }

            @Override
            public void onLikeClick(Post post, boolean isLiked) {
            }

            @Override
            public void onCommentClick(Post post) {
                openPost(post);
            }

            @Override
            public void onShareClick(Post post) {
            }

            @Override
            public void onMoreClick(Post post, View view) {
            }
        });
        binding.recyclerView.setAdapter(adapter);
        binding.swipeRefresh.setOnRefreshListener(this::load);
    }

    @Override
    protected void initData() {
        load();
    }

    private void load() {
        binding.swipeRefresh.setRefreshing(true);
        repository.loadMyPosts(1, 100, new BaseRepository.DataCallback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> data) {
                runOnUiThread(() -> {
                    binding.swipeRefresh.setRefreshing(false);
                    adapter.submitList(data != null ? data : List.of());
                });
            }

            @Override
            public void onError(String msg) {
                runOnUiThread(() -> {
                    binding.swipeRefresh.setRefreshing(false);
                    showToast(msg != null ? msg : "加载失败");
                });
            }

            @Override
            public void onNetworkError() {
                runOnUiThread(() -> {
                    binding.swipeRefresh.setRefreshing(false);
                    showToast(R.string.error_network);
                });
            }
        });
    }

    private void openPost(Post post) {
        if (post == null || post.getId() == null) {
            return;
        }
        Intent i = new Intent(this, PostDetailActivity.class);
        i.putExtra(ExtrasConstant.EXTRA_POST_ID, post.getId());
        startActivity(i);
    }

    @Override
    protected void setListeners() {
    }
}
