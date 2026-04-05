package com.ailianlian.ablecisi.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.adapter.model.CommentItem;
import com.ailianlian.ablecisi.baseclass.BaseAdapter;
import com.ailianlian.ablecisi.pojo.vo.CommentVO;
import com.ailianlian.ablecisi.pojo.vo.RootTreeVO;
import com.ailianlian.ablecisi.result.PageResult;
import com.ailianlian.ablecisi.utils.ImageLoader;
import com.ailianlian.ablecisi.utils.TimeAgoUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 评论：根评论 + 子评论；每棵树底部一条「展开/收起」；展开批次 5→10→20→50→100→全部。
 */
public class CommentListAdapter extends BaseAdapter<CommentItem, RecyclerView.ViewHolder> {

    private static final int TYPE_COMMENT = 0;
    private static final int TYPE_TREE_FOOTER = 1;

    private static final int[] EXPAND_BATCH_SIZES = {5, 10, 20, 50, 100};

    public static int firstExpandBatchSize() {
        return EXPAND_BATCH_SIZES[0];
    }

    private static int batchSizeForRound(int round1Based) {
        int i = round1Based - 1;
        if (i < 0) {
            i = 0;
        }
        if (i < EXPAND_BATCH_SIZES.length) {
            return EXPAND_BATCH_SIZES[i];
        }
        return 50_000;
    }

    public interface Listener {
        void onReplyClick(CommentVO target);

        void onLikeClick(CommentVO target);

        void onAvatarClick(CommentVO target);

        default void onLoadMoreDepth(long rootId, String afterPathCursor, int size) {
            onLoadMoreDepth(rootId, afterPathCursor);
        }

        default void onLoadMoreDepth(long rootId, String afterPathCursor) {
        }
    }

    private final Listener listener;
    private final int basePadding;
    private final int childIndent;

    private final Map<Long, String> rootNextCursor = new HashMap<>();
    private final Map<Long, Integer> rootExpandRounds = new HashMap<>();

    public CommentListAdapter(Context context, Listener listener) {
        super(context, new ArrayList<>());
        this.listener = listener;
        this.basePadding = dp(0);
        this.childIndent = dp(16);
        setHasStableIds(true);
    }

    private int dp(int v) {
        return Math.round(context.getResources().getDisplayMetrics().density * v);
    }

    @Override
    public int getItemViewType(int position) {
        CommentItem it = getItem(position);
        if (it != null && it.expandBar) {
            return TYPE_TREE_FOOTER;
        }
        return TYPE_COMMENT;
    }

    @Override
    public long getItemId(int position) {
        CommentItem it = getItem(position);
        if (it == null || it.vo == null || it.vo.id == null) {
            return RecyclerView.NO_ID;
        }
        if (it.expandBar) {
            return -it.vo.id;
        }
        return it.vo.id;
    }

    private int replyTotalForRoot(CommentVO rootVo) {
        if (rootVo == null) {
            return 0;
        }
        if (rootVo.descendantCount != null && rootVo.descendantCount > 0) {
            return rootVo.descendantCount;
        }
        return rootVo.replyCount == null ? 0 : rootVo.replyCount;
    }

    public void setDataFromBundle(PageResult<RootTreeVO> bundlePage) {
        List<CommentItem> fresh = new ArrayList<>();
        if (bundlePage != null && bundlePage.getRecords() != null) {
            for (RootTreeVO group : bundlePage.getRecords()) {
                if (group.root != null) {
                    group.root.depth = 0;
                    group.root.parentId = null;
                    if (group.root.rootId == null || group.root.rootId <= 0) {
                        group.root.rootId = group.root.id;
                    }
                    fresh.add(new CommentItem(group.root));
                    if (replyTotalForRoot(group.root) > 0) {
                        fresh.add(CommentItem.newExpandBar(group.root));
                    }
                }
            }
        }
        this.dataList = fresh;
        notifyDataSetChanged();
        rootNextCursor.clear();
        rootExpandRounds.clear();
    }

    public void onMoreLoaded(long rootId, List<CommentVO> more) {
        appendTree(rootId, more);
    }

    public void appendTree(long rootId, List<CommentVO> more) {
        if (more == null || more.isEmpty()) {
            return;
        }
        int insertPos = indexOfExpandBar(rootId);
        if (insertPos < 0) {
            int[] range = findRootRange(rootId);
            insertPos = range == null ? dataList.size() : range[1] + 1;
        }
        int count = 0;
        for (CommentVO vo : more) {
            if (vo.depth == null || vo.depth < 1) {
                vo.depth = 1;
            }
            dataList.add(insertPos + count, new CommentItem(vo));
            count++;
        }
        notifyItemRangeInserted(insertPos, count);

        rootNextCursor.remove(rootId);
        String next = more.get(more.size() - 1).pathCursor;
        if (next != null && !next.isEmpty()) {
            rootNextCursor.put(rootId, next);
        }

        int foot = indexOfExpandBar(rootId);
        if (foot >= 0) {
            notifyItemChanged(foot);
        }
    }

    public void collapseRoot(long rootId) {
        int[] range = findRootRange(rootId);
        if (range == null) {
            return;
        }
        CommentVO rootVo = dataList.get(range[0]).vo;
        int start = range[0] + 1;
        int end = range[1];
        if (end >= start) {
            int removeCount = end - start + 1;
            for (int i = 0; i < removeCount; i++) {
                dataList.remove(start);
            }
            notifyItemRangeRemoved(start, removeCount);
        }
        rootExpandRounds.put(rootId, 0);
        rootNextCursor.remove(rootId);
        if (replyTotalForRoot(rootVo) > 0) {
            dataList.add(range[0] + 1, CommentItem.newExpandBar(rootVo));
            notifyItemInserted(range[0] + 1);
        }
        int foot = indexOfExpandBar(rootId);
        if (foot >= 0) {
            notifyItemChanged(foot);
        }
    }

    private int countShownDescendants(long rootId) {
        int[] range = findRootRange(rootId);
        if (range == null) {
            return 0;
        }
        int n = 0;
        for (int i = range[0] + 1; i <= range[1]; i++) {
            CommentItem it = dataList.get(i);
            if (it.expandBar) {
                continue;
            }
            int d = it.vo.depth == null ? 0 : it.vo.depth;
            if (d >= 1) {
                n++;
            }
        }
        return n;
    }

    private int[] findRootRange(long rootId) {
        int start = -1;
        for (int i = 0; i < dataList.size(); i++) {
            CommentItem it = dataList.get(i);
            if (it.isRoot() && getRootId(it.vo) == rootId) {
                start = i;
                break;
            }
        }
        if (start == -1) {
            return null;
        }
        int end = start;
        while (end + 1 < dataList.size()) {
            CommentItem next = dataList.get(end + 1);
            if (next.isRoot()) {
                break;
            }
            end++;
        }
        return new int[]{start, end};
    }

    private int indexOfExpandBar(long rootId) {
        int[] range = findRootRange(rootId);
        if (range == null) {
            return -1;
        }
        for (int i = range[0] + 1; i <= range[1]; i++) {
            if (dataList.get(i).expandBar) {
                return i;
            }
        }
        return -1;
    }

    private long getRootId(CommentVO vo) {
        return (vo.rootId != null && vo.rootId > 0) ? vo.rootId : (vo.id == null ? -1L : vo.id);
    }

    private int indexOfCommentId(Long id) {
        if (id == null) {
            return -1;
        }
        for (int i = 0; i < dataList.size(); i++) {
            CommentItem it = dataList.get(i);
            if (!it.expandBar && it.vo != null && id.equals(it.vo.id)) {
                return i;
            }
        }
        return -1;
    }

    private int findSubtreeEndIndex(int parentIdx) {
        if (parentIdx < 0 || parentIdx >= dataList.size()) {
            return parentIdx;
        }
        int parentDepth = dataList.get(parentIdx).vo.depth == null ? 0 : dataList.get(parentIdx).vo.depth;
        int end = parentIdx;
        while (end + 1 < dataList.size()) {
            CommentItem next = dataList.get(end + 1);
            if (next.expandBar) {
                break;
            }
            int d = next.vo.depth == null ? 0 : next.vo.depth;
            if (d <= parentDepth) {
                break;
            }
            end++;
        }
        return end;
    }

    public boolean hasRoot(long rootId) {
        return findRootRange(rootId) != null;
    }

    /** 当前列表中已加载过至少一条子评论的根 id（用于本地持久化） */
    public Set<Long> getExpandedRootIds() {
        Set<Long> set = new LinkedHashSet<>();
        for (int i = 0; i < dataList.size(); i++) {
            CommentItem it = dataList.get(i);
            if (!it.isRoot()) {
                continue;
            }
            long rid = getRootId(it.vo);
            if (countShownDescendants(rid) > 0) {
                set.add(rid);
            }
        }
        return set;
    }

    public void insertTop(CommentVO newTop, boolean scrollToTop, RecyclerView rvOpt) {
        if (newTop == null) {
            return;
        }
        newTop.depth = 0;
        newTop.parentId = null;
        newTop.rootId = newTop.id;
        dataList.add(0, new CommentItem(newTop));
        notifyItemInserted(0);
        if (replyTotalForRoot(newTop) > 0) {
            dataList.add(1, CommentItem.newExpandBar(newTop));
            notifyItemInserted(1);
        }
        if (scrollToTop && rvOpt != null) {
            rvOpt.scrollToPosition(0);
        }
    }

    public void insertReply(CommentVO parent, CommentVO reply) {
        if (parent == null || reply == null) {
            return;
        }
        int parentIdx = indexOfCommentId(parent.id);
        if (parentIdx < 0) {
            return;
        }
        if (reply.parentId == null) {
            reply.parentId = parent.id;
        }
        if (reply.depth == null) {
            reply.depth = (parent.depth == null ? 0 : parent.depth) + 1;
        }
        if (reply.rootId == null || reply.rootId <= 0) {
            reply.rootId = getRootId(parent);
        }
        long rootId = getRootId(reply);
        int eb = indexOfExpandBar(rootId);
        int insertPos = eb >= 0 ? eb : findSubtreeEndIndex(parentIdx) + 1;
        dataList.add(insertPos, new CommentItem(reply));
        notifyItemInserted(insertPos);
        CommentItem parentItem = dataList.get(parentIdx);
        if (parentItem.vo.replyCount == null) {
            parentItem.vo.replyCount = 0;
        }
        parentItem.vo.replyCount += 1;
        notifyItemChanged(parentIdx);
        int rootIdx = getRootIndex(rootId);
        if (rootIdx >= 0) {
            CommentVO rootVo = dataList.get(rootIdx).vo;
            if (rootVo.descendantCount != null) {
                rootVo.descendantCount = rootVo.descendantCount + 1;
                notifyItemChanged(rootIdx);
            }
        }
        int foot = indexOfExpandBar(rootId);
        if (foot >= 0) {
            notifyItemChanged(foot);
        }
    }

    private void applyCommentLikeUi(CommentVH h, CommentVO vo) {
        boolean liked = Boolean.TRUE.equals(vo.liked);
        int drawableRes = liked ? R.drawable.ic_heart_filled : R.drawable.ic_heart;
        int colorRes = liked ? R.color.primary : R.color.text_secondary;
        Drawable d = AppCompatResources.getDrawable(context, drawableRes);
        if (d != null) {
            d = DrawableCompat.wrap(d.mutate());
            DrawableCompat.setTint(d, ContextCompat.getColor(context, colorRes));
            h.tvLikeCount.setCompoundDrawablesRelativeWithIntrinsicBounds(d, null, null, null);
        }
        h.tvLikeCount.setCompoundDrawablePadding(dp(4));
        h.tvLikeCount.setTextColor(ContextCompat.getColor(context, colorRes));
    }

    private int getRootIndex(long rootId) {
        int[] range = findRootRange(rootId);
        return range == null ? -1 : range[0];
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_TREE_FOOTER) {
            View v = inflateView(parent, R.layout.item_comment_tree_footer);
            return new FooterVH(v);
        }
        View v = inflateView(parent, R.layout.item_comment);
        return new CommentVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CommentItem item = getItem(position);
        if (item == null) {
            return;
        }
        if (holder instanceof FooterVH) {
            bindFooter((FooterVH) holder, item, position);
            return;
        }
        if (holder instanceof CommentVH) {
            bindComment((CommentVH) holder, item, position);
        }
    }

    private void bindComment(CommentVH h, CommentItem item, int position) {
        CommentVO vo = item.vo;

        h.ivUserAvatar.setScaleX(1f);
        h.ivUserAvatar.setScaleY(1f);

        int depth = vo.depth == null ? 0 : vo.depth;
        int leftPad = basePadding + (depth >= 1 ? childIndent : 0);
        h.root.setPadding(
                leftPad,
                h.root.getPaddingTop(),
                h.root.getPaddingRight(),
                h.root.getPaddingBottom()
        );

        ImageLoader.loadCircle(context, vo.avatarUrl, h.ivUserAvatar);
        if (!item.isRoot()) {
            h.ivUserAvatar.setScaleX(0.82f);
            h.ivUserAvatar.setScaleY(0.82f);
        }

        if (item.isRoot()) {
            h.tvUserName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
            h.tvUserName.setTypeface(null, Typeface.BOLD);
            h.tvCommentContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
            h.tvCommentTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            h.tvLikeCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            h.btnReply.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        } else {
            h.tvUserName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            h.tvUserName.setTypeface(null, Typeface.NORMAL);
            h.tvCommentContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
            h.tvCommentTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
            h.tvLikeCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
            h.btnReply.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        }

        String displayName;
        if (!item.isRoot()
                && depth >= 2
                && !TextUtils.isEmpty(vo.parentUserName)
                && !TextUtils.isEmpty(vo.userName)) {
            displayName = vo.userName + " → " + vo.parentUserName;
        } else {
            displayName = vo.userName == null ? "" : vo.userName;
        }
        h.tvUserName.setText(displayName);
        h.tvCommentTime.setText(TimeAgoUtil.toTimeAgo(vo.createTime));
        h.tvCommentContent.setText(vo.content == null ? "" : vo.content);
        h.tvLikeCount.setText(String.valueOf(vo.likeCount == null ? 0 : vo.likeCount));
        applyCommentLikeUi(h, vo);

        h.tvCommentContent.setTypeface(null, (item.isRoot() && item.collapsedRoot) ? Typeface.BOLD : Typeface.NORMAL);

        h.btnReply.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReplyClick(vo);
            }
        });
        h.tvLikeCount.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLikeClick(vo);
            }
        });
        h.ivUserAvatar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAvatarClick(vo);
            }
        });

        h.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(item, position);
            }
        });
        h.itemView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                return onItemLongClickListener.onItemLongClick(item, position);
            }
            return false;
        });
    }

    private void bindFooter(FooterVH h, CommentItem item, int position) {
        CommentVO vo = item.vo;
        long rootId = getRootId(vo);
        int shown = countShownDescendants(rootId);
        int total = replyTotalForRoot(vo);
        int remain = Math.max(0, total - shown);
        int rounds = rootExpandRounds.getOrDefault(rootId, 0);
        int nextBatch = batchSizeForRound(rounds + 1);

        if (shown == 0 && remain > 0) {
            int showInLabel = Math.min(remain, batchSizeForRound(1));
            h.btnExpandReplies.setVisibility(View.VISIBLE);
            h.btnExpandReplies.setText(context.getString(R.string.comment_expand_replies_count, showInLabel));
            h.btnLoadMoreReplies.setVisibility(View.GONE);
            h.btnCollapseReplies.setVisibility(View.GONE);
        } else if (shown > 0 && remain > 0) {
            h.btnExpandReplies.setVisibility(View.GONE);
            h.btnLoadMoreReplies.setVisibility(View.VISIBLE);
            h.btnLoadMoreReplies.setText(R.string.comment_expand_more);
            h.btnCollapseReplies.setVisibility(View.VISIBLE);
            h.btnCollapseReplies.setText(R.string.comment_collapse_all);
        } else if (shown > 0) {
            h.btnExpandReplies.setVisibility(View.GONE);
            h.btnLoadMoreReplies.setVisibility(View.GONE);
            h.btnCollapseReplies.setVisibility(View.VISIBLE);
            h.btnCollapseReplies.setText(R.string.comment_collapse_all);
        } else {
            h.btnExpandReplies.setVisibility(View.GONE);
            h.btnLoadMoreReplies.setVisibility(View.GONE);
            h.btnCollapseReplies.setVisibility(View.GONE);
        }

        h.btnExpandReplies.setOnClickListener(v -> {
            int r = rootExpandRounds.getOrDefault(rootId, 0) + 1;
            rootExpandRounds.put(rootId, r);
            CommentItem rootItem = dataList.get(getRootIndex(rootId));
            rootItem.collapsedRoot = false;
            notifyItemChanged(getRootIndex(rootId));
            int footIdx = indexOfExpandBar(rootId);
            if (footIdx >= 0) {
                notifyItemChanged(footIdx);
            }
            String cursor = rootNextCursor.get(rootId);
            int size = batchSizeForRound(r);
            if (listener != null) {
                listener.onLoadMoreDepth(rootId, cursor, size);
            }
        });

        h.btnLoadMoreReplies.setOnClickListener(v -> {
            int r = rootExpandRounds.getOrDefault(rootId, 0) + 1;
            rootExpandRounds.put(rootId, r);
            int footIdx = indexOfExpandBar(rootId);
            if (footIdx >= 0) {
                notifyItemChanged(footIdx);
            }
            String cursor = rootNextCursor.get(rootId);
            int size = batchSizeForRound(r);
            if (listener != null) {
                listener.onLoadMoreDepth(rootId, cursor, size);
            }
        });

        h.btnCollapseReplies.setOnClickListener(v -> {
            collapseRoot(rootId);
            int rootIdx = getRootIndex(rootId);
            if (rootIdx >= 0) {
                dataList.get(rootIdx).collapsedRoot = true;
                notifyItemChanged(rootIdx);
            }
        });

        h.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(item, position);
            }
        });
        h.itemView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                return onItemLongClickListener.onItemLongClick(item, position);
            }
            return false;
        });
    }

    static class CommentVH extends RecyclerView.ViewHolder {
        ViewGroup root;
        ImageView ivUserAvatar;
        TextView tvUserName, tvCommentTime, tvCommentContent, tvLikeCount, btnReply;

        CommentVH(@NonNull View itemView) {
            super(itemView);
            root = (ViewGroup) itemView;
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
            tvCommentContent = itemView.findViewById(R.id.tvCommentContent);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            btnReply = itemView.findViewById(R.id.btnReply);
        }
    }

    static class FooterVH extends RecyclerView.ViewHolder {
        TextView btnExpandReplies, btnLoadMoreReplies, btnCollapseReplies;

        FooterVH(@NonNull View itemView) {
            super(itemView);
            btnExpandReplies = itemView.findViewById(R.id.btnExpandReplies);
            btnLoadMoreReplies = itemView.findViewById(R.id.btnLoadMoreReplies);
            btnCollapseReplies = itemView.findViewById(R.id.btnCollapseReplies);
        }
    }
}
