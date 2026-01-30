package com.ailianlian.ablecisi.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.databinding.ItemCharacterSmallBinding;
import com.ailianlian.ablecisi.pojo.entity.AiCharacter;
import com.ailianlian.ablecisi.utils.ImageLoader;

public class CharacterAdapter extends ListAdapter<AiCharacter, CharacterAdapter.CharacterViewHolder> {

    private OnItemClickListener listener;

    public CharacterAdapter() {
        super(new CharacterDiffCallback());
    }

    public interface OnItemClickListener {
        void onItemClick(AiCharacter aiCharacter);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CharacterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCharacterSmallBinding binding = ItemCharacterSmallBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CharacterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CharacterViewHolder holder, int position) {
        AiCharacter aiCharacter = getItem(position);
        holder.bind(aiCharacter);
    }

    public class CharacterViewHolder extends RecyclerView.ViewHolder {
        private final ItemCharacterSmallBinding binding;

        CharacterViewHolder(ItemCharacterSmallBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getItem(position));
                }
            });
        }

        void bind(AiCharacter aiCharacter) {
            binding.tvName.setText(aiCharacter.getName());
            binding.tvType.setText(aiCharacter.getType());
            
            // 加载角色图片
            ImageLoader.load(binding.ivCharacter.getContext(), aiCharacter.getImageUrl(), binding.ivCharacter, R.drawable.ic_profile, R.drawable.ic_profile);
            
            // 设置在线状态
            binding.viewStatus.setVisibility(aiCharacter.getOnline() ? View.VISIBLE : View.GONE);
            if (aiCharacter.getOnline()) {
                binding.viewStatus.setBackgroundResource(R.drawable.circle_online);
            }
        }
    }

    static class CharacterDiffCallback extends DiffUtil.ItemCallback<AiCharacter> {
        @Override
        public boolean areItemsTheSame(@NonNull AiCharacter oldItem, @NonNull AiCharacter newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull AiCharacter oldItem, @NonNull AiCharacter newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getType().equals(newItem.getType()) &&
                    oldItem.getImageUrl().equals(newItem.getImageUrl()) &&
                    oldItem.getOnline().equals(newItem.getOnline());
        }
    }
} 