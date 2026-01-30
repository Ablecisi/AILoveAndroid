package com.ailianlian.ablecisi.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ailianlian.ablecisi.databinding.ItemTopicBinding;
import com.ailianlian.ablecisi.pojo.entity.Topic;

public class TopicAdapter extends ListAdapter<Topic, TopicAdapter.TopicViewHolder> {

    private OnItemClickListener listener;

    public TopicAdapter() {
        super(new TopicDiffCallback());
    }

    public interface OnItemClickListener {
        void onItemClick(Topic topic);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTopicBinding binding = ItemTopicBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TopicViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        Topic topic = getItem(position);
        holder.bind(topic);
    }

    public class TopicViewHolder extends RecyclerView.ViewHolder {
        private final ItemTopicBinding binding;

        TopicViewHolder(ItemTopicBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getItem(position));
                }
            });
        }

        void bind(Topic topic) {
            binding.tvRank.setText(String.valueOf(topic.getRank()));
            binding.tvTitle.setText(topic.getTitle());
            binding.tvViews.setText(topic.getViews());
            binding.tvComments.setText(topic.getComments());
            binding.tvLikes.setText(topic.getLikes());
        }
    }

    static class TopicDiffCallback extends DiffUtil.ItemCallback<Topic> {
        @Override
        public boolean areItemsTheSame(@NonNull Topic oldItem, @NonNull Topic newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Topic oldItem, @NonNull Topic newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getRank().equals(newItem.getRank()) &&
                    oldItem.getViews().equals(newItem.getViews()) &&
                    oldItem.getComments().equals(newItem.getComments()) &&
                    oldItem.getLikes().equals(newItem.getLikes());
        }
    }
} 