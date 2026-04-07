package com.ailianlian.ablecisi.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;
import java.util.List;

public class ChatDetailActivity extends BaseActivity<ActivityChatDetailBinding> {

    private ChatDetailViewModel viewModel;
    private MessageAdapter adapter;
    private LinearLayoutManager layoutManager;
    private String conversationId;
    private String userId;

    /** 首次历史加载完成后滚到底（无动画） */
    private boolean pendingInitialScroll = true;
    /** 用户是否停留在列表底部附近（用于流式是否自动跟随） */
    private boolean pinnedToBottom = true;
    private final StringBuilder pendingUiDelta = new StringBuilder();

    @Override
    protected ActivityChatDetailBinding getViewBinding() {
        return ActivityChatDetailBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        if (getIntent().hasExtra(ExtrasConstant.EXTRA_CONVERSATION_ID)) {
            conversationId = getIntent().getStringExtra(ExtrasConstant.EXTRA_CONVERSATION_ID);
        } else {
            showToast("会话ID不能为空");
            finish();
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
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnMore.setOnClickListener(v ->
                Toast.makeText(this, R.string.feature_not_available, Toast.LENGTH_SHORT).show());

        binding.btnPhone.setOnClickListener(v ->
                Toast.makeText(this, R.string.feature_not_available, Toast.LENGTH_SHORT).show());

        binding.btnVideo.setOnClickListener(v ->
                Toast.makeText(this, R.string.feature_not_available, Toast.LENGTH_SHORT).show());
    }

    private void setupMessageList() {
        String userAv = LoginInfoUtil.getAvatarUrl(this);
        adapter = new MessageAdapter(this, "", userAv != null ? userAv : "", message -> {
            String text = message.getContent() != null ? message.getContent() : "";
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("chat", text));
            }
            Toast.makeText(this, R.string.article_copied, Toast.LENGTH_SHORT).show();
        });

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvMessages.setLayoutManager(layoutManager);
        binding.rvMessages.setAdapter(adapter);

        binding.rvMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                if (lm == null) {
                    return;
                }
                int first = lm.findFirstVisibleItemPosition();
                if (first <= 2 && dy < 0 && viewModel.hasMoreOlder()) {
                    viewModel.loadOlderMessages();
                }
                updatePinnedState();
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updatePinnedState();
                }
            }
        });
    }

    private void updatePinnedState() {
        if (layoutManager == null || adapter == null) {
            return;
        }
        int n = adapter.getItemCount();
        if (n == 0) {
            pinnedToBottom = true;
            return;
        }
        int last = n - 1;
        int lastVis = layoutManager.findLastVisibleItemPosition();
        pinnedToBottom = lastVis >= last - 1;
    }

    private void setupInputArea() {
        binding.btnSend.setOnClickListener(v -> {
            String message = binding.etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                pinnedToBottom = true;
                viewModel.sendMessage(message);
                binding.etMessage.setText("");
            }
        });
    }

    private void observeViewModel() {
        viewModel.getCharacter().observe(this, this::updateCharacterInfo);
        viewModel.getMessages().observe(this, this::updateMessages);
        viewModel.getIsLoading().observe(this, isLoading ->
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE));
        viewModel.getIsTyping().observe(this, isTyping ->
                binding.typingIndicator.setVisibility(isTyping ? View.VISIBLE : View.GONE));
        viewModel.getStreamDelta().observe(this, this::applyStreamDelta);
        viewModel.getStreamStage().observe(this, stage -> {
            if (stage == null) {
                return;
            }
            if ("ack".equals(stage) || "start".equals(stage) || "preprocess_done".equals(stage)) {
                binding.typingIndicator.setVisibility(View.VISIBLE);
            }
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

        Glide.with(this)
                .load(aiCharacter.getImageUrl())
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(binding.ivAvatar);

        String userAv = LoginInfoUtil.getAvatarUrl(this);
        adapter.setAvatars(aiCharacter.getImageUrl(), userAv != null ? userAv : "");
    }

    private void updateMessages(List<Message> messages) {
        if (messages == null) {
            messages = new ArrayList<>();
        }

        final List<Message> newList = new ArrayList<>(messages);
        boolean hasStreaming = false;
        for (Message m : newList) {
            if (ChatDetailViewModel.STREAMING_MESSAGE_ID.equals(m.getId())) {
                hasStreaming = true;
                break;
            }
        }
        if (!hasStreaming) {
            adapter.clearStreamingOverride(ChatDetailViewModel.STREAMING_MESSAGE_ID);
        }
        final List<Message> oldList = new ArrayList<>(adapter.getCurrentList());
        final int oldSize = oldList.size();
        final String oldFirstId = oldList.isEmpty() ? null : oldList.get(0).getId();

        final int oldFirstVis = layoutManager != null ? layoutManager.findFirstVisibleItemPosition()
                : RecyclerView.NO_POSITION;
        final View oldTopView = layoutManager != null && oldFirstVis != RecyclerView.NO_POSITION
                ? layoutManager.findViewByPosition(oldFirstVis) : null;
        final int oldOffset = oldTopView != null ? oldTopView.getTop() : 0;

        adapter.submitList(newList, () -> binding.rvMessages.post(() -> {
            flushPendingUiDelta();
            if (newList.isEmpty()) {
                return;
            }
            int newSize = newList.size();
            boolean prepended = oldFirstId != null && newSize > oldSize
                    && !oldFirstId.equals(newList.get(0).getId());

            if (prepended && layoutManager != null && oldFirstVis != RecyclerView.NO_POSITION) {
                int added = newSize - oldSize;
                layoutManager.scrollToPositionWithOffset(oldFirstVis + added, oldOffset);
                updatePinnedState();
                return;
            }

            boolean shouldScroll = pendingInitialScroll || pinnedToBottom;
            if (shouldScroll && layoutManager != null) {
                layoutManager.scrollToPosition(newSize - 1);
            }
            if (pendingInitialScroll) {
                pendingInitialScroll = false;
            }
            updatePinnedState();
        }));
    }

    private void applyStreamDelta(ChatDetailViewModel.StreamDelta delta) {
        if (delta == null || delta.text == null || delta.text.isEmpty()) {
            return;
        }
        boolean ok = adapter.appendStreamingText(delta.messageId, delta.text);
        if (!ok) {
            synchronized (pendingUiDelta) {
                pendingUiDelta.append(delta.text);
            }
            binding.rvMessages.postDelayed(this::flushPendingUiDelta, 30L);
        }
        if (pinnedToBottom && layoutManager != null) {
            int n = adapter.getItemCount();
            if (n > 0) {
                layoutManager.scrollToPosition(n - 1);
            }
        }
    }

    private void flushPendingUiDelta() {
        String pending;
        synchronized (pendingUiDelta) {
            if (pendingUiDelta.length() == 0) {
                return;
            }
            pending = pendingUiDelta.toString();
            pendingUiDelta.setLength(0);
        }
        boolean ok = adapter.appendStreamingText(ChatDetailViewModel.STREAMING_MESSAGE_ID, pending);
        if (!ok) {
            synchronized (pendingUiDelta) {
                pendingUiDelta.insert(0, pending);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
