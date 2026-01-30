package com.ailianlian.ablecisi.fragment;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.activity.ArticleDetailActivity;
import com.ailianlian.ablecisi.activity.CharacterCustomizeActivity;
import com.ailianlian.ablecisi.adapter.CharacterAdapter;
import com.ailianlian.ablecisi.adapter.TopicAdapter;
import com.ailianlian.ablecisi.baseclass.BaseFragment;
import com.ailianlian.ablecisi.constant.ExtrasConstant;
import com.ailianlian.ablecisi.databinding.FragmentHomeBinding;
import com.ailianlian.ablecisi.utils.ImageLoader;
import com.ailianlian.ablecisi.viewmodel.HomeViewModel;
import com.google.android.material.chip.Chip;

import java.util.List;

public class HomeFragment extends BaseFragment<FragmentHomeBinding> {
    private static final String TAG = "HomeFragment";

    private HomeViewModel viewModel;
    private CharacterAdapter characterAdapter;
    private TopicAdapter topicAdapter;
    private String featuredContentId;

    @Override
    protected FragmentHomeBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentHomeBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        getViewModel(); // 获取ViewModel实例
        setupCharacterList();// 设置AI角色列表
        setupTopicList();// 设置热门话题列表
        setRefreshColors();// 设置下拉刷新颜色
    }

    @Override
    protected void lazyLoadData() {
        viewModel.loadData();// 加载数据
        observeViewModel();// 观察数据变化
    }

    @Override
    protected void setListeners() {
        setupSearchBarListener();// 设置搜索框点击事件
        setupCategoryChipsListener();// 设置分类标签点击事件
        setupCharacterListListener(); // 设置AI角色列表点击事件
        setupFeaturedContentListener(); // 设置精选内容点击事件
        setupTopicListListener(); // 设置热门话题列表点击事件
        setRefreshListener(); // 设置下拉刷新监听器
    }

    private void setRefreshColors() {
        binding.swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
    }

    private void setRefreshListener() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            binding.swipeRefreshLayout.setRefreshing(true);
            // 模拟数据刷新
            reloadData();
            binding.swipeRefreshLayout.setRefreshing(false); // 停止刷新动画
        });
    }

    private void setupSearchBarListener() {
        binding.etSearch.setOnClickListener(v -> {
            String query = binding.etSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                showToast(query); // TODO: 替换为实际搜索逻辑
            }
        });
    }

    private void setupCategoryChips(List<String> categories) {
        // 清除所有 Chip
        binding.categoryChipGroup.removeAllViews();

        // 动态添加分类 Chip
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (String category : categories) {
            Chip chip = (Chip) inflater.inflate(R.layout.chip_category_template, binding.categoryChipGroup, false);
            chip.setText(category);
            binding.categoryChipGroup.addView(chip);
        }
        // 确保"全部"为选中状态
        if (binding.categoryChipGroup.getChildCount() > 0) {
            Chip allChip = (Chip) binding.categoryChipGroup.getChildAt(0);
            allChip.setChecked(true);
        }
    }
    private void setupCategoryChipsListener() {
        binding.categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            System.out.println("选中的Chip ID: " + checkedIds);
            if (checkedIds.isEmpty()) {
                return;
            }

            Chip chip = group.findViewById(checkedIds.get(0)); // 获取选中的Chip
            if (chip != null) {
                String category = chip.getText().toString();
                viewModel.filterByCategory(category);
            }
        });
    }

    private void setupCharacterList() {
        characterAdapter = new CharacterAdapter();
        binding.rvCharacters.setAdapter(characterAdapter);
        binding.rvCharacters.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    private void setupCharacterListListener() {
        characterAdapter.setOnItemClickListener(character -> {
            // 跳转到AI自定义角色详情页
            Intent intent = new Intent(requireContext(), CharacterCustomizeActivity.class);
            intent.putExtra(ExtrasConstant.EXTRA_CHARACTER_ID, character.getId());
            startActivity(intent);
        });
    }

    private void setupFeaturedContentListener() {
        binding.featuredCard.setOnClickListener(v -> {
            // 跳转到文章详情页
            Intent intent = new Intent(requireContext(), ArticleDetailActivity.class);
            intent.putExtra(ExtrasConstant.EXTRA_ARTICLE_ID, featuredContentId);
            startActivity(intent);
        });
    }

    private void setupTopicList() {
        topicAdapter = new TopicAdapter();
        binding.rvTopics.setAdapter(topicAdapter);
    }

    private void setupTopicListListener() {
        topicAdapter.setOnItemClickListener(topic -> {
            // 跳转到话题详情页
            Intent intent = new Intent(requireContext(), ArticleDetailActivity.class);
            intent.putExtra(ExtrasConstant.EXTRA_ARTICLE_ID, topic.getId());
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        // 观察分类数据
        viewModel.getCategories().observe(getViewLifecycleOwner(), this::setupCategoryChips);

        // 观察AI角色列表
        viewModel.getCharacters().observe(getViewLifecycleOwner(), characters -> {
            characterAdapter.submitList(characters);
        });

        // 观察精选内容
        viewModel.getFeaturedContent().observe(getViewLifecycleOwner(), featured -> {
            if (featured != null) {

                binding.tvFeaturedTitle.setText(featured.getTitle());
                binding.tvFeaturedDescription.setText(featured.getDescription());
                featuredContentId = featured.getId(); // 保存精选内容ID

                // 设置封面图片
                ImageLoader.load(getContext(), featured.getCoverImageUrl(), binding.ivFeatured, R.drawable.placeholder_image, R.drawable.placeholder_image);
            }
        });

        // 观察热门话题列表
        viewModel.getTopics().observe(getViewLifecycleOwner(), topics -> {
            topicAdapter.submitList(topics);
        });
    }

    private void getViewModel() {
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
    }
} 