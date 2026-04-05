package com.ailianlian.ablecisi.activity;

import android.content.Intent;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.adapter.CharacterSmallAdapter;
import com.ailianlian.ablecisi.baseclass.BaseActivity;
import com.ailianlian.ablecisi.constant.ExtrasConstant;
import com.ailianlian.ablecisi.databinding.ActivityMyCharactersBinding;
import com.ailianlian.ablecisi.pojo.entity.AiCharacter;
import com.ailianlian.ablecisi.pojo.vo.AiCharacterVO;
import com.ailianlian.ablecisi.viewmodel.ProfileViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyCharactersActivity extends BaseActivity<ActivityMyCharactersBinding> {

    private ProfileViewModel viewModel;
    private CharacterSmallAdapter adapter;

    @Override
    protected ActivityMyCharactersBinding getViewBinding() {
        return ActivityMyCharactersBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(ProfileViewModel.class);

        adapter = new CharacterSmallAdapter(character -> {
            Intent intent = new Intent(this, CharacterCustomizeActivity.class);
            intent.putExtra(ExtrasConstant.EXTRA_CHARACTER_ID, character.getId());
            startActivity(intent);
        });
        binding.recyclerCharacters.setLayoutManager(new GridLayoutManager(this, 3));
        binding.recyclerCharacters.setAdapter(adapter);
    }

    @Override
    protected void initData() {
        viewModel.getCharacters().observe(this, this::bindCharacters);
        viewModel.loadCharacters();
    }

    @Override
    protected void setListeners() {
        binding.fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, CharacterCustomizeActivity.class)));
    }

    private void bindCharacters(List<AiCharacterVO> vos) {
        List<AiCharacter> list = new ArrayList<>();
        if (vos != null) {
            for (AiCharacterVO vo : vos) {
                if (vo == null) {
                    continue;
                }
                AiCharacter character = new AiCharacter();
                character.setId(String.valueOf(vo.id));
                character.setAge(vo.age);
                character.setDescription(vo.personaDesc);
                character.setInterests(parseCommaSeparated(vo.interests));
                character.setName(vo.name);
                character.setBackgroundStory(vo.backstory);
                if (vo.gender != null) {
                    switch (vo.gender) {
                        case 0:
                            character.setGender("男");
                            break;
                        case 1:
                            character.setGender("女");
                            break;
                        default:
                            character.setGender("其他");
                    }
                }
                character.setImageUrl(vo.imageUrl);
                character.setPersonalityTraits(parseCommaSeparated(vo.traits));
                character.setOnline(vo.online != null && vo.online == 1);
                character.setType(vo.typeName);
                character.setCreatedAt(vo.createTime);
                list.add(character);
            }
        }
        adapter.setCharacters(list);
    }

    private List<String> parseCommaSeparated(String str) {
        if (str == null || str.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String[] parts = str.split(",");
        List<String> out = new ArrayList<>();
        for (String part : parts) {
            String t = part.trim();
            if (!t.isEmpty()) {
                out.add(t);
            }
        }
        return out;
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadCharacters();
    }
}
