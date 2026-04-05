package com.ailianlian.ablecisi.activity;

import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.adapter.MessageAdapter;
import com.ailianlian.ablecisi.baseclass.BaseActivity;
import com.ailianlian.ablecisi.constant.ExtrasConstant;
import com.ailianlian.ablecisi.databinding.ActivityChatDetailBinding;
import com.ailianlian.ablecisi.pojo.entity.AiCharacter;
import com.ailianlian.ablecisi.pojo.entity.Message;
import com.ailianlian.ablecisi.pojo.vo.AiCharacterVO;
import com.ailianlian.ablecisi.utils.LoginInfoUtil;
import com.ailianlian.ablecisi.viewmodel.ChatDetailViewModel;
import com.bumptech.glide.Glide;

import java.util.List;

public class ChatDetailActivity extends BaseActivity<ActivityChatDetailBinding> {

    private ChatDetailViewModel viewModel;
    private MessageAdapter adapter;
    private String conversationId;
    private String userId;

    @Override
    protected ActivityChatDetailBinding getViewBinding() {
        return ActivityChatDetailBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        // 获取会话ID参数
        if (getIntent().hasExtra(ExtrasConstant.EXTRA_CONVERSATION_ID)) {
            conversationId = getIntent().getStringExtra(ExtrasConstant.EXTRA_CONVERSATION_ID);
        } else {
            showToast("会话ID不能为空");
            finish(); // 关闭Activity，返回上一页
        }
        userId = LoginInfoUtil.getUserId(this);
        viewModel = new ViewModelProvider(this).get(ChatDetailViewModel.class);
    }

    @Override
    protected void initData() {
        viewModel.init(conversationId, userId);
        setupMessageList();
        observeViewModel();
    }

    @Override
    protected void setListeners() {
        setupToolbar();
        setupInputArea();
    }

    private void setupToolbar() {
        binding.btnBack.setOnClickListener(v -> {
            finish(); // 关闭Activity，返回上一页
        });

        binding.btnMore.setOnClickListener(v ->
                Toast.makeText(this, R.string.feature_not_available, Toast.LENGTH_SHORT).show());

        binding.btnPhone.setOnClickListener(v ->
                Toast.makeText(this, R.string.feature_not_available, Toast.LENGTH_SHORT).show());

        binding.btnVideo.setOnClickListener(v ->
                Toast.makeText(this, R.string.feature_not_available, Toast.LENGTH_SHORT).show());

    }

    private void setupMessageList() {
        String userAv = LoginInfoUtil.getAvatarUrl(this);
        adapter = new MessageAdapter(this, "", userAv != null ? userAv : "");

        // 设置布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.rvMessages.setLayoutManager(layoutManager);

        // 设置适配器
        binding.rvMessages.setAdapter(adapter);
    }

    private void setupInputArea() {
        binding.btnSend.setOnClickListener(v -> {
            String message = binding.etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                viewModel.sendMessage(message);
                binding.etMessage.setText("");
            }
        });
    }

    private void observeViewModel() {
        // 观察AI角色信息
        viewModel.getCharacter().observe(this, this::updateCharacterInfo);

        // 观察消息列表
        viewModel.getMessages().observe(this, this::updateMessages);

        // 观察加载状态
        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // 观察输入状态
        viewModel.getIsTyping().observe(this, isTyping -> {
            binding.typingIndicator.setVisibility(isTyping ? View.VISIBLE : View.GONE);
        });

        viewModel.getStreamError().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                viewModel.clearStreamError();
            }
        });
    }

    private void updateCharacterInfo(AiCharacterVO characterVO) {
        AiCharacter aiCharacter = characterVO.getAiCharacter();
        binding.tvName.setText(aiCharacter.getName());
        binding.tvStatus.setText(aiCharacter.getOnline() ? getString(R.string.online) : getString(R.string.offline));

        // 加载头像
        Glide.with(this)
                .load(aiCharacter.getImageUrl())
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(binding.ivAvatar);

        String userAv = LoginInfoUtil.getAvatarUrl(this);
        adapter.setAvatars(aiCharacter.getImageUrl(), userAv != null ? userAv : "");
    }

    private void updateMessages(List<Message> messages) {
        adapter.submitList(messages); // 提交新列表

        // 滚动到最新消息
        if (messages != null && !messages.isEmpty()) {
            binding.rvMessages.smoothScrollToPosition(messages.size() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
} 