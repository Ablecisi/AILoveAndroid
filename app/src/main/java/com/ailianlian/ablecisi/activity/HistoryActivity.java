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
import com.ailianlian.ablecisi.constant.ExtrasConstant;
import com.ailianlian.ablecisi.databinding.ActivitySimpleListBinding;
import com.ailianlian.ablecisi.pojo.entity.BrowseHistoryEntry;
import com.ailianlian.ablecisi.utils.BrowseHistoryStore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Locale;

public class HistoryActivity extends BaseActivity<ActivitySimpleListBinding> {

    private final ArrayList<BrowseHistoryEntry> items = new ArrayList<>();
    private RecyclerView.Adapter<?> listAdapter;
    private final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);

    @Override
    protected ActivitySimpleListBinding getViewBinding() {
        return ActivitySimpleListBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.history_list_title);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.swipeRefresh.setEnabled(false);
        items.addAll(BrowseHistoryStore.getAll(this));
        listAdapter = new RecyclerView.Adapter<Holder>() {
            @NonNull
            @Override
            public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_two_line, parent, false);
                return new Holder(v);
            }

            @Override
            public void onBindViewHolder(@NonNull Holder h, int position) {
                BrowseHistoryEntry e = items.get(position);
                h.title.setText(e.title != null ? e.title : "");
                String typeLabel = BrowseHistoryEntry.KIND_ARTICLE.equals(e.kind) ? "文章" : "帖子";
                h.sub.setText(typeLabel + " · " + fmt.format(new Date(e.visitedAt)));
                h.itemView.setOnClickListener(v -> openEntry(e));
            }

            @Override
            public int getItemCount() {
                return items.size();
            }
        };
        binding.recyclerView.setAdapter(listAdapter);
    }

    private void openEntry(BrowseHistoryEntry e) {
        if (e == null || e.id == null) {
            return;
        }
        if (BrowseHistoryEntry.KIND_ARTICLE.equals(e.kind)) {
            Intent i = new Intent(this, ArticleDetailActivity.class);
            i.putExtra(ExtrasConstant.EXTRA_ARTICLE_ID, e.id);
            startActivity(i);
        } else {
            Intent i = new Intent(this, PostDetailActivity.class);
            i.putExtra(ExtrasConstant.EXTRA_POST_ID, e.id);
            startActivity(i);
        }
    }

    @Override
    protected void initData() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        items.clear();
        items.addAll(BrowseHistoryStore.getAll(this));
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
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
