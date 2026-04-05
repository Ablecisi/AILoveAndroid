package com.ailianlian.ablecisi.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import java.util.List;
import java.util.Map;

/**
 * 评论：一层根评论 + 缩进子评论；展开批次 5→10→20→50→100→全部；根下「展开N条回复」/「展开更多」/「收起」。
 */
public class CommentListAdapter extends BaseAdapter<CommentItem, CommentListAdapter.VH> {

    /** 第 1 次拉 5 条，第 2 次 10 条…第 6 次起一次性拉全 */
    private static final int[] EXPAND_BATCH_SIZES = {5, 10, 20, 50, 100};

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
    private final int indentPerDepth;

    private final Map<Long, String> rootNextCursor = new HashMap<>();
    /** 当前根评论已完成的加载轮数（用于下一批条数） */
    private final Map<Long, Integer> rootExpandRounds = new HashMap<>();

    public CommentListAdapter(Context context, Listener listener) {
        super(context, new ArrayList<>());
        this.listener = listener;
        this.basePadding = dp(0);
        this.indentPerDepth = dp(16);
        setHasStableIds(true);
    }

    private int dp(int v) {
        return Math.round(context.getResources().getDisplayMetrics().density * v);
    }

    @Override
    public long getItemId(int position) {
        CommentItem it = getItem(position);
        return it == null || it.vo == null || it.vo.id == null ? RecyclerView.NO_ID : it.vo.id;
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
        int[] range = findRootRange(rootId);
        int insertPos = (range == null) ? dataList.size() : range[1] + 1;
        int count = 0;
        for (CommentVO vo : more) {
            if (vo.depth == null || vo.depth < 1) {
                vo.depth = 1;
            }
            dataList.add(insertPos + count, new CommentItem(vo));
            count++;
        }
        notifyItemRangeInserted(insertPos, count);

        String next = more.get(more.size() - 1).pathCursor;
        if (next != null) {
            rootNextCursor.put(rootId, next);
        }

        int rootIdx = getRootIndex(rootId);
        if (rootIdx >= 0) {
            notifyItemChanged(rootIdx);
        }
    }

    public void collapseRoot(long rootId) {
        int[] range = findRootRange(rootId);
        if (range == null) {
            return;
        }
        int start = range[0] + 1;
        int end = range[1];
        if (end >= start) {
            int count = end - start + 1;
            for (int i = 0; i < count; i++) {
                dataList.remove(start);
            }
            notifyItemRangeRemoved(start, count);
        }
        rootExpandRounds.put(rootId, 0);
        rootNextCursor.remove(rootId);
        int rootIdx = range[0];
        if (rootIdx >= 0) {
            notifyItemChanged(rootIdx);
        }
    }

    private int countShownDescendants(long rootId) {
        int[] range = findRootRange(rootId);
        if (range == null) {
            return 0;
        }
        return Math.max(0, range[1] - range[0]);
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

    private long getRootId(CommentVO vo) {
        return (vo.rootId != null && vo.rootId > 0) ? vo.rootId : (vo.id == null ? -1L : vo.id);
    }

    private int indexOfCommentId(Long id) {
        if (id == null) {
            return -1;
        }
        for (int i = 0; i < dataList.size(); i++) {
            CommentItem it = dataList.get(i);
            if (it.vo != null && id.equals(it.vo.id)) {
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
            int d = next.vo.depth == null ? 0 : next.vo.depth;
            if (d <= parentDepth) {
                break;
            }
            end++;
        }
        return end;
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
        int insertPos = findSubtreeEndIndex(parentIdx) + 1;
        dataList.add(insertPos, new CommentItem(reply));
        notifyItemInserted(insertPos);
        CommentItem parentItem = dataList.get(parentIdx);
        if (parentItem.vo.replyCount == null) {
            parentItem.vo.replyCount = 0;
        }
        parentItem.vo.replyCount += 1;
        notifyItemChanged(parentIdx);
    }

    private int getRootIndex(long rootId) {
        int[] range = findRootRange(rootId);
        return range == null ? -1 : range[0];
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflateView(parent, R.layout.item_comment);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        CommentItem item = getItem(position);
        if (item == null) {
            return;
        }
        CommentVO vo = item.vo;

        h.ivUserAvatar.setScaleX(1f);
        h.ivUserAvatar.setScaleY(1f);

        int depth = vo.depth == null ? 0 : vo.depth;
        h.root.setPadding(
                basePadding + indentPerDepth * depth,
                h.root.getPaddingTop(),
                h.root.getPaddingRight(),
                h.root.getPaddingBottom()
        );

        ImageLoader.loadCircle(context, vo.avatarUrl, h.ivUserAvatar);
        if (!item.isRoot()) {
            h.ivUserAvatar.setScaleX(0.88f);
            h.ivUserAvatar.setScaleY(0.88f);
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

        if (item.isRoot()) {
            long rootId = getRootId(vo);
            int shown = countShownDescendants(rootId);
            int total = vo.replyCount == null ? 0 : vo.replyCount;
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
                item.collapsedRoot = false;
                notifyItemChanged(position);
                String cursor = rootNextCursor.get(rootId);
                int size = batchSizeForRound(r);
                if (listener != null) {
                    listener.onLoadMoreDepth(rootId, cursor, size);
                }
            });

            h.btnLoadMoreReplies.setOnClickListener(v -> {
                int r = rootExpandRounds.getOrDefault(rootId, 0) + 1;
                rootExpandRounds.put(rootId, r);
                notifyItemChanged(position);
                String cursor = rootNextCursor.get(rootId);
                int size = batchSizeForRound(r);
                if (listener != null) {
                    listener.onLoadMoreDepth(rootId, cursor, size);
                }
            });

            h.btnCollapseReplies.setOnClickListener(v -> {
                collapseRoot(rootId);
                item.collapsedRoot = true;
                notifyItemChanged(position);
            });
        } else {
            h.btnExpandReplies.setVisibility(View.GONE);
            h.btnLoadMoreReplies.setVisibility(View.GONE);
            h.btnCollapseReplies.setVisibility(View.GONE);
        }

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

    static class VH extends RecyclerView.ViewHolder {
        ViewGroup root;
        ImageView ivUserAvatar;
        TextView tvUserName, tvCommentTime, tvCommentContent, tvLikeCount, btnReply;
        TextView btnExpandReplies, btnLoadMoreReplies, btnCollapseReplies;

        VH(@NonNull View itemView) {
            super(itemView);
            root = (ViewGroup) itemView;
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
            tvCommentContent = itemView.findViewById(R.id.tvCommentContent);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            btnReply = itemView.findViewById(R.id.btnReply);
            btnExpandReplies = itemView.findViewById(R.id.btnExpandReplies);
            btnLoadMoreReplies = itemView.findViewById(R.id.btnLoadMoreReplies);
            btnCollapseReplies = itemView.findViewById(R.id.btnCollapseReplies);
        }
    }
}
