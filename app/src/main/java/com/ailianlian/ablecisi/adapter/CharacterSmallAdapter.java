package com.ailianlian.ablecisi.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.pojo.entity.AiCharacter;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * AI角色小卡片适配器
 */
public class CharacterSmallAdapter extends RecyclerView.Adapter<CharacterSmallAdapter.CharacterViewHolder> {

    private List<AiCharacter> aiCharacters = new ArrayList<>();
    private final OnCharacterClickListener listener;

    public interface OnCharacterClickListener {
        void onCharacterClick(AiCharacter aiCharacter);
    }

    public CharacterSmallAdapter(OnCharacterClickListener listener) {
        this.listener = listener;
    }

    public void setCharacters(List<AiCharacter> aiCharacters) {
        this.aiCharacters = aiCharacters;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CharacterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_character_small, parent, false);
        return new CharacterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CharacterViewHolder holder, int position) {
        AiCharacter aiCharacter = aiCharacters.get(position);
        holder.bind(aiCharacter);
    }

    @Override
    public int getItemCount() {
        return aiCharacters.size();
    }

    public class CharacterViewHolder extends RecyclerView.ViewHolder {
        private ShapeableImageView imageCharacter;
        private View viewStatus;
        private TextView textName;
        private TextView textType;

        CharacterViewHolder(@NonNull View itemView) {
            super(itemView);
            imageCharacter = itemView.findViewById(R.id.ivCharacter);
            viewStatus = itemView.findViewById(R.id.viewStatus);
            textName = itemView.findViewById(R.id.tvName);
            textType = itemView.findViewById(R.id.tvType);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition(); // 获取当前点击的项位置
                // 确保位置有效并且监听器不为null
                // RecyclerView.NO_POSITION表示该项已被删除或不可用
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCharacterClick(aiCharacters.get(position));
                }
            });
        }

        void bind(AiCharacter aiCharacter) {
            // 设置角色图片
            // 实际项目中应使用图片加载库如Glide或Picasso
            Glide.with(imageCharacter).load(aiCharacter.getImageUrl()).into(imageCharacter);
            
            // 设置在线状态
            viewStatus.setVisibility(aiCharacter.getOnline() ? View.VISIBLE : View.GONE);
            
            // 设置名称和类型
            textName.setText(aiCharacter.getName());
            textType.setText(aiCharacter.getType());
        }
    }
} 