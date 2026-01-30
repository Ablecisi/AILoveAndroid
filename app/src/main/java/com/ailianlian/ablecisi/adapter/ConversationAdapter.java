package com.ailianlian.ablecisi.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.databinding.ItemChatSessionBinding;
import com.ailianlian.ablecisi.pojo.entity.Conversation;
import com.bumptech.glide.Glide;

/**
 * ailianlian
 * com.ailianlian.ablecisi.adapter
 * ChatSessionAdapter <br>
 * 聊天会话适配器
 * @author Ablecisi
 * @version 1.0
 * 2025/4/18
 * 星期五
 * 16:07
 */
public class ConversationAdapter extends ListAdapter<Conversation, ConversationAdapter.ChatSessionViewHolder> {

    private OnItemClickListener listener;

    public ConversationAdapter() {
        super(new ChatSessionDiffCallback());
    }

    public interface OnItemClickListener {
        void onItemClick(Conversation conversation);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatSessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChatSessionBinding binding = ItemChatSessionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ChatSessionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatSessionViewHolder holder, int position) {
        Conversation conversation = getItem(position);
        holder.bind(conversation);
    }

    class ChatSessionViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatSessionBinding binding;

        ChatSessionViewHolder(ItemChatSessionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getItem(position));
                }
            });
        }

        void bind(Conversation conversation) {
            binding.tvName.setText(conversation.getCharacterName());
            binding.tvLastMessage.setText(conversation.getLastMessage());
            binding.tvTime.setText(conversation.getLastTime());

            // 加载头像
            Glide.with(binding.ivAvatar.getContext())
                    .load(conversation.getCharacterAvatar())
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(binding.ivAvatar);

            // 设置在线状态
            binding.viewStatus.setVisibility(conversation.getOnline() ? View.VISIBLE : View.GONE);

            // 设置未读消息数
            if (conversation.getUnreadCount() > 0) {
                binding.tvUnreadCount.setVisibility(View.VISIBLE);
                binding.tvUnreadCount.setText(String.valueOf(conversation.getUnreadCount()));
            } else {
                binding.tvUnreadCount.setVisibility(View.GONE);
            }
        }
    }

    static class ChatSessionDiffCallback extends DiffUtil.ItemCallback<Conversation> {
        @Override
        public boolean areItemsTheSame(@NonNull Conversation oldItem, @NonNull Conversation newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Conversation oldItem, @NonNull Conversation newItem) {
            return oldItem.getCharacterName().equals(newItem.getCharacterName()) &&
                    oldItem.getLastMessage().equals(newItem.getLastMessage()) &&
                    oldItem.getLastTime().equals(newItem.getLastTime()) &&
                    oldItem.getUnreadCount().equals(newItem.getUnreadCount()) &&
                    oldItem.getOnline().equals(newItem.getOnline());
        }
    }
}
