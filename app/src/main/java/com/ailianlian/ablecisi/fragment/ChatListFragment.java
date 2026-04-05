package com.ailianlian.ablecisi.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.app.Application;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.activity.CreateConversationActivity;
import com.ailianlian.ablecisi.activity.ChatDetailActivity;
import com.ailianlian.ablecisi.adapter.ConversationAdapter;
import com.ailianlian.ablecisi.constant.ExtrasConstant;
import com.ailianlian.ablecisi.databinding.FragmentChatListBinding;
import com.ailianlian.ablecisi.pojo.entity.Conversation;
import com.ailianlian.ablecisi.viewmodel.ChatListViewModel;

import java.util.List;

public class ChatListFragment extends Fragment {

    private FragmentChatListBinding binding;
    private ChatListViewModel viewModel;
    private ConversationAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChatListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        Application app = requireActivity().getApplication();
        viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(app))
                .get(ChatListViewModel.class);
        
        setupRecyclerView();
        setupSwipeRefresh();
        setupFab();
        setupEmptyView();
        setupSearchBar();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new ConversationAdapter();
        binding.rvChatSessions.setAdapter(adapter);

        // 跳转到聊天详情页
        adapter.setOnItemClickListener(this::navigateToChatDetail);
    }

    private void navigateToChatDetail(Conversation conversation) {
        // 使用Intent启动ChatDetailActivity
        Intent intent = new Intent(requireContext(), ChatDetailActivity.class);
        intent.putExtra(ExtrasConstant.EXTRA_CONVERSATION_ID, conversation.getId());
        startActivity(intent);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
        
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.refreshChatSessions();
        });
    }

    private void setupFab() {
        binding.fabNewChat.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CreateConversationActivity.class)));
    }

    private void setupEmptyView() {
        binding.btnStartChat.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CreateConversationActivity.class)));
    }
    
    private void setupSearchBar() {
        binding.etSearch.setOnClickListener(v -> {
            Toast.makeText(requireContext(), R.string.chat_search_coming_soon, Toast.LENGTH_SHORT).show();
        });
        
        binding.btnSlider.setOnClickListener(v -> {
            Toast.makeText(requireContext(), R.string.chat_search_coming_soon, Toast.LENGTH_SHORT).show();
        });
    }

    private void observeViewModel() {
        viewModel.getChatSessions().observe(getViewLifecycleOwner(), this::updateChatSessions);
        
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.swipeRefreshLayout.setRefreshing(isLoading);
        });
    }

    private void updateChatSessions(List<Conversation> conversations) {
        adapter.submitList(conversations);
        
        // 更新空视图的显示状态
        if (conversations == null || conversations.isEmpty()) {
            binding.emptyView.setVisibility(View.VISIBLE);
            binding.rvChatSessions.setVisibility(View.GONE);
        } else {
            binding.emptyView.setVisibility(View.GONE);
            binding.rvChatSessions.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}