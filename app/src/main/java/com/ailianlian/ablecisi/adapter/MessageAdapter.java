package com.ailianlian.ablecisi.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.databinding.ItemMessageReceivedBinding;
import com.ailianlian.ablecisi.databinding.ItemMessageSentBinding;
import com.ailianlian.ablecisi.pojo.entity.Message;
import com.ailianlian.ablecisi.viewmodel.ChatDetailViewModel;
import com.bumptech.glide.Glide;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.noties.markwon.Markwon;
import io.noties.markwon.core.CorePlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.image.glide.GlideImagesPlugin;

/**
 * 消息适配器，用于显示聊天消息列表
 */
public class MessageAdapter extends ListAdapter<Message, RecyclerView.ViewHolder> {
    private static final String PAYLOAD_STREAM_APPEND = "stream_append";

    public interface OnMessageActionListener {
        void onCopy(Message message);
    }

    private final Markwon markwon;
    private String characterAvatar;
    private String userAvatar;
    @Nullable
    private final OnMessageActionListener actionListener;
    private final Map<String, StringBuilder> streamingTextOverride = new HashMap<>();

    public MessageAdapter(Context context, String characterAvatar, String userAvatar) {
        this(context, characterAvatar, userAvatar, null);
    }

    public MessageAdapter(Context context, String characterAvatar, String userAvatar,
                          @Nullable OnMessageActionListener actionListener) {
        super(new MessageDiffCallback());
        Context app = context.getApplicationContext();
        this.markwon = Markwon.builder(app)
                .usePlugin(CorePlugin.create())
                .usePlugin(GlideImagesPlugin.create(app))
                .usePlugin(TablePlugin.create(app))
                .build();
        this.characterAvatar = characterAvatar != null ? characterAvatar : "";
        this.userAvatar = userAvatar != null ? userAvatar : "";
        this.actionListener = actionListener;
    }

    public void setAvatars(String characterAvatar, String userAvatar) {
        this.characterAvatar = characterAvatar != null ? characterAvatar : "";
        this.userAvatar = userAvatar != null ? userAvatar : "";
        notifyDataSetChanged();
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

        boolean showTimestamp = shouldShowTimestamp(position);

        if (holder.getItemViewType() == Message.TYPE_SENT) {
            ((SentMessageViewHolder) holder).bind(markwon, resolveContent(message), message, showTimestamp, actionListener);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(markwon, resolveContent(message), message, showTimestamp, actionListener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && payloads.contains(PAYLOAD_STREAM_APPEND)) {
            Message message = getItem(position);
            if (holder.getItemViewType() == Message.TYPE_RECEIVED) {
                ((ReceivedMessageViewHolder) holder).bindMessageOnly(markwon, resolveContent(message));
                return;
            }
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    private String resolveContent(Message message) {
        if (message == null) {
            return "";
        }
        StringBuilder sb = streamingTextOverride.get(message.getId());
        if (sb != null) {
            return sb.toString();
        }
        return message.getContent() != null ? message.getContent() : "";
    }

    public boolean appendStreamingText(String messageId, String piece) {
        if (messageId == null || piece == null || piece.isEmpty()) {
            return false;
        }
        int pos = -1;
        List<Message> list = getCurrentList();
        for (int i = list.size() - 1; i >= 0; i--) {
            Message m = list.get(i);
            if (messageId.equals(m.getId())) {
                pos = i;
                break;
            }
        }
        if (pos < 0) {
            return false;
        }
        StringBuilder sb = streamingTextOverride.computeIfAbsent(messageId, k -> new StringBuilder());
        sb.append(piece);
        notifyItemChanged(pos, PAYLOAD_STREAM_APPEND);
        return true;
    }

    public void clearStreamingOverride(String messageId) {
        if (messageId == null) {
            return;
        }
        streamingTextOverride.remove(messageId);
    }

    private boolean shouldShowTimestamp(int position) {
        if (position == 0) {
            return true;
        }

        Message currentMessage = getItem(position);
        Message previousMessage = getItem(position - 1);
        if (currentMessage.getTimestamp() == null || previousMessage.getTimestamp() == null) {
            return true;
        }

        int timeDiff = currentMessage.getTimestamp().getMinute() - previousMessage.getTimestamp().getMinute();
        return timeDiff > 5;
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageSentBinding binding;

        SentMessageViewHolder(ItemMessageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Markwon markwon, String content, Message message, boolean showTimestamp,
                  @Nullable OnMessageActionListener actionListener) {
            boolean streaming = ChatDetailViewModel.STREAMING_MESSAGE_ID.equals(message.getId());
            if (streaming) {
                // 流式增量阶段避免重型 Markdown 渲染，减少 chunk 合并感。
                binding.tvMessage.setText(content != null ? content : "");
                binding.tvMessage.setMovementMethod(null);
            } else {
                markwon.setMarkdown(binding.tvMessage, content != null ? content : "");
                binding.tvMessage.setMovementMethod(LinkMovementMethod.getInstance());
            }

            if (showTimestamp && message.getTimestamp() != null) {
                binding.tvTimestamp.setVisibility(View.VISIBLE);
                binding.tvTimestamp.setText(formatDate(message.getTimestamp()));
            } else {
                binding.tvTimestamp.setVisibility(View.GONE);
            }

            binding.messageToolbar.setVisibility(streaming ? View.GONE : View.VISIBLE);
            binding.btnCopy.setOnClickListener(v -> {
                if (actionListener != null && !streaming) {
                    actionListener.onCopy(message);
                }
            });
        }

        void bindMessageOnly(Markwon markwon, String content) {
            binding.tvMessage.setText(content != null ? content : "");
            binding.tvMessage.setMovementMethod(null);
        }
    }

    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageReceivedBinding binding;

        ReceivedMessageViewHolder(ItemMessageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Markwon markwon, String content, Message message, boolean showTimestamp,
                  @Nullable OnMessageActionListener actionListener) {
            boolean streaming = ChatDetailViewModel.STREAMING_MESSAGE_ID.equals(message.getId());
            if (streaming) {
                binding.tvMessage.setText(content != null ? content : "");
                binding.tvMessage.setMovementMethod(null);
            } else {
                markwon.setMarkdown(binding.tvMessage, content != null ? content : "");
                binding.tvMessage.setMovementMethod(LinkMovementMethod.getInstance());
            }

            Glide.with(binding.ivAvatar.getContext())
                    .load(characterAvatar)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(binding.ivAvatar);

            if (showTimestamp && message.getTimestamp() != null) {
                binding.tvTimestamp.setVisibility(View.VISIBLE);
                binding.tvTimestamp.setText(formatDate(message.getTimestamp()));
            } else {
                binding.tvTimestamp.setVisibility(View.GONE);
            }

            binding.messageToolbar.setVisibility(streaming ? View.GONE : View.VISIBLE);
            binding.btnCopy.setOnClickListener(v -> {
                if (actionListener != null && !streaming) {
                    actionListener.onCopy(message);
                }
            });
        }

        void bindMessageOnly(Markwon markwon, String content) {
            binding.tvMessage.setText(content != null ? content : "");
            binding.tvMessage.setMovementMethod(null);
        }
    }

    private static String formatDate(LocalDateTime date) {
        return DateFormat.format("yyyy-MM-dd HH:mm", Date.from(date.atZone(ZoneId.systemDefault()).toInstant())).toString();
    }

    static class MessageDiffCallback extends DiffUtil.ItemCallback<Message> {
        @Override
        public boolean areItemsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return Objects.equals(oldItem.getContent(), newItem.getContent())
                    && Objects.equals(oldItem.getType(), newItem.getType())
                    && Objects.equals(oldItem.getRead(), newItem.getRead());
        }
    }
}
