package com.ailianlian.ablecisi.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.pojo.entity.Article;
import com.ailianlian.ablecisi.utils.ImageLoader;
import com.ailianlian.ablecisi.utils.TimeAgoUtil;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * 相关文章适配器
 */
public class RelatedArticleAdapter extends RecyclerView.Adapter<RelatedArticleAdapter.RelatedArticleViewHolder> {

    private List<Article> articles = new ArrayList<>();
    private OnArticleClickListener listener;

    public interface OnArticleClickListener {
        void onArticleClicked(Article article);
    }

    public void setOnArticleClickListener(OnArticleClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Article> newArticles) {
        this.articles.clear();
        if (newArticles != null) {
            this.articles.addAll(newArticles);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RelatedArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_related_article, parent, false);
        return new RelatedArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RelatedArticleViewHolder holder, int position) {
        holder.bind(articles.get(position));
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public class RelatedArticleViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView ivCover;
        private final TextView tvTitle;
        private final TextView tvAuthor;
        private final TextView tvPublishTime;
        private final TextView tvViewCount;
        private final TextView tvLikeCount;

        public RelatedArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvPublishTime = itemView.findViewById(R.id.tvPublishTime);
            tvViewCount = itemView.findViewById(R.id.tvViewCount);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
        }

        public void bind(Article article) {
            // 设置文章封面
            ImageLoader.load(
                    itemView.getContext(),
                    article.getCoverImageUrl(),
                    ivCover,
                    R.drawable.placeholder_image,
                    R.drawable.placeholder_image);
            tvTitle.setText(article.getTitle());// 设置文章标题
            tvAuthor.setText(article.getAuthorName());// 设置作者
            tvPublishTime.setText(TimeAgoUtil.toTimeAgo(article.getPublishTime()));// 设置发布时间
            tvViewCount.setText(String.valueOf(article.getViewCount()));// 设置阅读量
            tvLikeCount.setText(String.valueOf(article.getLikeCount()));// 设置点赞数
            // 设置点击事件
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onArticleClicked(article);
                }
            });
        }
    }
} 