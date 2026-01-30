package com.ailianlian.ablecisi.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Space;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.databinding.ItemPostBinding;
import com.ailianlian.ablecisi.pojo.entity.Post;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;

import java.time.LocalDateTime;
import java.util.Date;

public class PostAdapter extends ListAdapter<Post, PostAdapter.PostViewHolder> {

    private final PostInteractionListener listener;

    public PostAdapter(PostInteractionListener listener) {
        super(new PostDiffCallback());
        this.listener = listener;
    }

    public interface PostInteractionListener {
        void onPostClick(Post post);
        void onUserClick(Post post);
        void onLikeClick(Post post, boolean isLiked);
        void onCommentClick(Post post);
        void onShareClick(Post post);
        void onMoreClick(Post post, View view);
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPostBinding binding = ItemPostBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PostViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = getItem(position);
        holder.bind(post, position); // pass position so holder can decide if it's last
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        private final ItemPostBinding binding;
        private static final String SPACER_TAG = "post_item_spacer";

        PostViewHolder(ItemPostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            // 设置点击事件
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPostClick(getItem(position));
                }
            });

            binding.ivUserAvatar.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onUserClick(getItem(position));
                }
            });

            binding.tvUserName.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onUserClick(getItem(position));
                }
            });

            binding.btnLike.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    Post post = getItem(position);
                    boolean newLikeState = !post.getLiked();
                    listener.onLikeClick(post, newLikeState);

                    // 更新UI状态
                    updateLikeState(post, newLikeState);
                }
            });

            binding.btnComment.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCommentClick(getItem(position));
                }
            });

            binding.btnShare.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onShareClick(getItem(position));
                }
            });

            binding.btnMore.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onMoreClick(getItem(position), v);
                }
            });
        }

        void bind(Post post, int position) {
            // 设置用户信息
            binding.tvUserName.setText(post.getUser().getName());
            binding.tvPostTime.setText(formatTimeAgo(post.getCreatedAt()));

            // 加载用户头像
            Glide.with(binding.ivUserAvatar.getContext())
                    .load(post.getUser().getAvatarUrl())
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(binding.ivUserAvatar);

            // 设置帖子内容
            binding.tvPostContent.setText(post.getContent());

            // 设置帖子图片
            if (post.getImageUrls() != null && !post.getImageUrls().isEmpty()) {
                binding.imageContainer.setVisibility(View.VISIBLE);

                // 加载第一张图片
                Glide.with(binding.ivPostImage.getContext())
                        .load(post.getImageUrls().get(0))
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .into(binding.ivPostImage);

                // 如果有多张图片，显示图片计数
                if (post.getImageUrls().size() > 1) {
                    binding.tvImageCount.setVisibility(View.VISIBLE);
                    binding.tvImageCount.setText(String.format("1/%d", post.getImageUrls().size()));
                } else {
                    binding.tvImageCount.setVisibility(View.GONE);
                }
            } else {
                binding.imageContainer.setVisibility(View.GONE);
            }

            // 设置标签
            setupTags(post);

            // 设置互动数据
            binding.tvLikeCount.setText(String.valueOf(post.getLikeCount()));
            binding.tvCommentCount.setText(String.valueOf(post.getCommentCount()));
            binding.tvShareCount.setText(String.valueOf(post.getShareCount()));

            // 设置点赞状态
            updateLikeState(post, post.getLiked());

            // 添加或移除 100dp 高的 spacer，仅在最后一个帖子添加
            handleSpacerForPosition(position);
        }

        private void setupTags(Post post) {
            binding.chipGroupTags.removeAllViews();

            if (post.getTags() != null && !post.getTags().isEmpty()) {
                binding.chipGroupTags.setVisibility(View.VISIBLE);

                for (String tag : post.getTags()) {
                    Chip chip = new Chip(binding.chipGroupTags.getContext());
                    chip.setText("#" + tag);
                    chip.setTextSize(12);
                    chip.setClickable(true);
                    chip.setCheckable(false);

                    binding.chipGroupTags.addView(chip);
                }
            } else {
                binding.chipGroupTags.setVisibility(View.GONE);
            }
        }

        private void updateLikeState(Post post, boolean isLiked) {
            // 更新模型数据
            post.setLiked(isLiked);

            // 更新UI
            if (isLiked) {
                binding.ivLike.setImageResource(R.drawable.ic_like_filled);
                binding.ivLike.setColorFilter(
                        ContextCompat.getColor(binding.ivLike.getContext(), R.color.primary));
                binding.tvLikeCount.setTextColor(
                        ContextCompat.getColor(binding.tvLikeCount.getContext(), R.color.primary));
            } else {
                binding.ivLike.setImageResource(R.drawable.ic_like);
                binding.ivLike.setColorFilter(
                        ContextCompat.getColor(binding.ivLike.getContext(), R.color.text_hint));
                binding.tvLikeCount.setTextColor(
                        ContextCompat.getColor(binding.tvLikeCount.getContext(), R.color.text_hint));
            }
        }

        private void handleSpacerForPosition(int position) {
            Space space = binding.emptySpace;
            int lastIndex = getItemCount() - 1; // ListAdapter method
            if (position == lastIndex) {
                space.setVisibility(View.VISIBLE);
            } else {
                space.setVisibility(View.GONE);
            }
        }
    }

    private String formatTimeAgo(LocalDateTime date) {
        long now = System.currentTimeMillis();
        long time = Date.from(date.atZone(java.time.ZoneId.systemDefault()).toInstant()).getTime();

        return DateUtils.getRelativeTimeSpanString(
                time, now, DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE).toString();
    }

    static class PostDiffCallback extends DiffUtil.ItemCallback<Post> {
        @Override
        public boolean areItemsTheSame(@NonNull Post oldItem, @NonNull Post newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Post oldItem, @NonNull Post newItem) {
            return oldItem.getContent().equals(newItem.getContent()) &&
                    oldItem.getLikeCount().equals(newItem.getLikeCount()) &&
                    oldItem.getCommentCount().equals(newItem.getCommentCount()) &&
                    !oldItem.getShareCount().equals(newItem.getShareCount()) ||
                    !oldItem.getLiked().equals(newItem.getLiked());
        }
    }

    // ...existing helper methods...

    private static int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}
