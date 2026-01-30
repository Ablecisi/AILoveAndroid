package com.ailianlian.ablecisi.adapter;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.databinding.ItemMessageReceivedBinding;
import com.ailianlian.ablecisi.databinding.ItemMessageSentBinding;
import com.ailianlian.ablecisi.pojo.entity.Message;
import com.bumptech.glide.Glide;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * ailianlian
 * com.ailianlian.ablecisi.adapter
 * MessageAdapter <br>
 * 消息适配器，用于显示聊天消息列表
 * @author Ablecisi
 * @version 1.0
 * 2025/4/18
 * 星期五
 * 16:03
 */
public class MessageAdapter extends ListAdapter<Message, RecyclerView.ViewHolder> {

    private final String characterAvatar;
    private final String userAvatar;

    public MessageAdapter(String characterAvatar, String userAvatar) {
        super(new MessageDiffCallback());
        this.characterAvatar = characterAvatar;
        this.userAvatar = userAvatar;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Message.TYPE_SENT) {
            ItemMessageSentBinding binding = ItemMessageSentBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new SentMessageViewHolder(binding);
        } else {
            ItemMessageReceivedBinding binding = ItemMessageReceivedBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ReceivedMessageViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = getItem(position);

        // 显示时间戳
        boolean showTimestamp = shouldShowTimestamp(position);

        if (holder.getItemViewType() == Message.TYPE_SENT) {
            ((SentMessageViewHolder) holder).bind(message, showTimestamp);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message, showTimestamp);
        }
    }

    private boolean shouldShowTimestamp(int position) {
        if (position == 0) {
            return true;
        }

        Message currentMessage = getItem(position);
        Message previousMessage = getItem(position - 1);

        // 如果两条消息间隔超过5分钟，显示时间戳
        int timeDiff = currentMessage.getTimestamp().getMinute() - previousMessage.getTimestamp().getMinute();
        return timeDiff > 5;
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageSentBinding binding;

        SentMessageViewHolder(ItemMessageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Message message, boolean showTimestamp) {
            binding.tvMessage.setText(message.getContent());

            // 设置时间戳
            if (showTimestamp) {
                binding.tvTimestamp.setVisibility(View.VISIBLE);
                binding.tvTimestamp.setText(formatDate(message.getTimestamp()));
            } else {
                binding.tvTimestamp.setVisibility(View.GONE);
            }
        }
    }

    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageReceivedBinding binding;

        ReceivedMessageViewHolder(ItemMessageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Message message, boolean showTimestamp) {
            binding.tvMessage.setText(message.getContent());

            // 加载头像
            Glide.with(binding.ivAvatar.getContext())
                    .load(characterAvatar)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(binding.ivAvatar);

            // 设置时间戳
            if (showTimestamp) {
                binding.tvTimestamp.setVisibility(View.VISIBLE);
                binding.tvTimestamp.setText(formatDate(message.getTimestamp()));
            } else {
                binding.tvTimestamp.setVisibility(View.GONE);
            }
        }
    }

    private static String formatDate(LocalDateTime date) {
        // 将时间，例如 2025-04-18T16:03:00，格式化为 "yyyy-MM-dd HH:mm"
        return DateFormat.format("yyyy-MM-dd HH:mm", Date.from(date.atZone(ZoneId.systemDefault()).toInstant())).toString();
    }

    static class MessageDiffCallback extends DiffUtil.ItemCallback<Message> {
        @Override
        public boolean areItemsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return oldItem.getContent().equals(newItem.getContent()) &&
                    oldItem.getType().equals(newItem.getType()) &&
                    oldItem.getRead().equals(newItem.getRead()); // 如果是封装类型，equals 方法会比较内容
        }
    }
}
