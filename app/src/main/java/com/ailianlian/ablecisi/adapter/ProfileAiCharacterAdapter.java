package com.ailianlian.ablecisi.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.pojo.entity.AiCharacter;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * 资料页「我的AI角色」网格：角色卡片 + 末尾「新建」入口。
 */
public class ProfileAiCharacterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_CHARACTER = 0;
    private static final int TYPE_ADD = 1;

    private final List<AiCharacter> characters = new ArrayList<>();
    private final OnCharacterClickListener onCharacterClick;
    private final Runnable onAddClick;

    public interface OnCharacterClickListener {
        void onCharacterClick(AiCharacter character);
    }

    public ProfileAiCharacterAdapter(OnCharacterClickListener onCharacterClick, Runnable onAddClick) {
        this.onCharacterClick = onCharacterClick;
        this.onAddClick = onAddClick;
    }

    public void setCharacters(List<AiCharacter> list) {
        characters.clear();
        if (list != null) {
            characters.addAll(list);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return position < characters.size() ? TYPE_CHARACTER : TYPE_ADD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ADD) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_character_add_new, parent, false);
            return new AddViewHolder(v);
        }
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_character_small, parent, false);
        return new CharacterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CharacterViewHolder) {
            ((CharacterViewHolder) holder).bind(characters.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return characters.size() + 1;
    }

    private class CharacterViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView imageCharacter;
        private final View viewStatus;
        private final android.widget.TextView textName;
        private final android.widget.TextView textType;

        CharacterViewHolder(@NonNull View itemView) {
            super(itemView);
            imageCharacter = itemView.findViewById(R.id.ivCharacter);
            viewStatus = itemView.findViewById(R.id.viewStatus);
            textName = itemView.findViewById(R.id.tvName);
            textType = itemView.findViewById(R.id.tvType);
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && onCharacterClick != null) {
                    onCharacterClick.onCharacterClick(characters.get(pos));
                }
            });
        }

        void bind(AiCharacter c) {
            Glide.with(imageCharacter).load(c.getImageUrl()).into(imageCharacter);
            viewStatus.setVisibility(c.getOnline() ? View.VISIBLE : View.GONE);
            textName.setText(c.getName());
            textType.setText(c.getType());
        }
    }

    private class AddViewHolder extends RecyclerView.ViewHolder {
        AddViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(v -> {
                if (onAddClick != null) {
                    onAddClick.run();
                }
            });
        }
    }
}
