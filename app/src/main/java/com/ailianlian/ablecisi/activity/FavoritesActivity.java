package com.ailianlian.ablecisi.activity;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.baseclass.BaseActivity;
import com.ailianlian.ablecisi.baseclass.BaseRepository;
import com.ailianlian.ablecisi.constant.ExtrasConstant;
import com.ailianlian.ablecisi.databinding.ActivitySimpleListBinding;
import com.ailianlian.ablecisi.pojo.entity.Article;
import com.ailianlian.ablecisi.repository.ArticleRepository;
import com.ailianlian.ablecisi.utils.LoginInfoUtil;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends BaseActivity<ActivitySimpleListBinding> {

    private final List<Article> items = new ArrayList<>();
    private ArticleRepository repository;
    private RecyclerView.Adapter<?> listAdapter;

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
        repository = new ArticleRepository(this);
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.favorites_list_title);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listAdapter = new RecyclerView.Adapter<Holder>() {
            @NonNull
            @Override
            public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_two_line, parent, false);
                return new Holder(v);
            }

            @Override
            public void onBindViewHolder(@NonNull Holder h, int position) {
                Article a = items.get(position);
                h.title.setText(a.getTitle() != null ? a.getTitle() : "文章");
                h.sub.setText(a.getAuthorName() != null ? a.getAuthorName() : "");
                h.itemView.setOnClickListener(v -> {
                    Intent i = new Intent(FavoritesActivity.this, ArticleDetailActivity.class);
                    i.putExtra(ExtrasConstant.EXTRA_ARTICLE_ID, a.getId());
                    startActivity(i);
                });
            }

            @Override
            public int getItemCount() {
                return items.size();
            }
        };
        binding.recyclerView.setAdapter(listAdapter);
        binding.swipeRefresh.setOnRefreshListener(this::load);
    }

    @Override
    protected void initData() {
        load();
    }

    private void load() {
        binding.swipeRefresh.setRefreshing(true);
        repository.listCollectedArticles(1, 100, new BaseRepository.DataCallback<List<Article>>() {
            @Override
            public void onSuccess(List<Article> data) {
                runOnUiThread(() -> {
                    binding.swipeRefresh.setRefreshing(false);
                    items.clear();
                    if (data != null) {
                        items.addAll(data);
                    }
                    listAdapter.notifyDataSetChanged();
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

    @Override
    protected void setListeners() {
    }

    private static class Holder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView sub;

        Holder(View v) {
            super(v);
            title = v.findViewById(R.id.tvTitle);
            sub = v.findViewById(R.id.tvSubtitle);
        }
    }
}
