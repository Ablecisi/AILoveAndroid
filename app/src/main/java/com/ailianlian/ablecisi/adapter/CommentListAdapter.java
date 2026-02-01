package com.ailianlian.ablecisi.adapter;

import android.content.Context;
import android.graphics.Typeface;
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
 * 评论列表适配器（扁平渲染 + depth 缩进 + 折叠文案「展开X条回复」 + root增量加载 + 每次展开更多）
 */
public class CommentListAdapter extends BaseAdapter<CommentItem, CommentListAdapter.VH> {

    /**
     * 每个顶层首次展开要拉的条数，可按需改为 3/5/10
     */
    private static final int BASE_STEP = 5;

    int showCount = 0;
    int totalCount = 0;
    int remainCount = 0;

    public interface Listener {
        void onReplyClick(CommentVO target);

        void onLikeClick(CommentVO target);

        void onAvatarClick(CommentVO target);

        /**
         * 新接口：支持按需 size 拉取（适配“展开次数越多，拉得越多”）
         */
        default void onLoadMoreDepth(long rootId, String afterPathCursor, int size) {
            // 若你暂时没实现带 size 的仓库方法，可降级到固定 size
            onLoadMoreDepth(rootId, afterPathCursor);
        }

        /**
         * 兼容旧接口（固定 size）——可在 Repository 内部固定 size=100 等
         */
        default void onLoadMoreDepth(long rootId, String afterPathCursor) {
        }
    }

    private final Listener listener;
    private final int basePadding;     // 顶层左边距
    private final int indentPerDepth;  // 每层缩进

    /**
     * rootId -> afterPath 游标（下一页）
     */
    private final Map<Long, String> rootNextCursor = new HashMap<>();
    /**
     * rootId -> 已点击“展开/更多”的次数（决定本次 size = BASE_STEP * times）
     */
    private final Map<Long, Integer> rootExpandClicks = new HashMap<>();

    public CommentListAdapter(Context context, Listener listener) {
        super(context, new ArrayList<>());
        this.listener = listener;
        this.basePadding = dp(0);
        this.indentPerDepth = dp(52);
        setHasStableIds(true);
    }

    /**
     * dp 转 px
     *
     * @param v dp 值
     * @return px 值
     */
    private int dp(int v) {
        return Math.round(context.getResources().getDisplayMetrics().density * v);
    }

    @Override
    public long getItemId(int position) {
        CommentItem it = getItem(position);
        return it == null || it.vo == null || it.vo.id == null ? RecyclerView.NO_ID : it.vo.id;
    }

    // ================= 对外 API =================

    /**
     * 用 /bundle 刷新整页 —— 只加入“顶层”，不展示子孙
     */
    public void setDataFromBundle(PageResult<RootTreeVO> bundlePage) {
        List<CommentItem> fresh = new ArrayList<>();
        if (bundlePage != null && bundlePage.getRecords() != null) {
            for (RootTreeVO group : bundlePage.getRecords()) {
                if (group.root != null) {
                    // 强制作为顶层显示
                    group.root.depth = 0;
                    group.root.parentId = null;
                    if (group.root.rootId == null || group.root.rootId <= 0) {
                        group.root.rootId = group.root.id; // 顶层 rootId=自身
                    }
                    fresh.add(new CommentItem(group.root));
                }
            }
        }
        this.dataList = fresh;
        notifyDataSetChanged();
        rootNextCursor.clear();
        rootExpandClicks.clear();
    }

    /**
     * 仓库 getTree(...) 回来后，外层调用本方法把“更多”插入到该 root 下
     */
    public void onMoreLoaded(long rootId, List<CommentVO> more) {
        appendTree(rootId, more);
    }

    /**
     * 向某个 root 追加 /tree 返回的更深层（窗口增量）
     */
    public void appendTree(long rootId, List<CommentVO> more) {
        if (more == null || more.isEmpty()) return;
        int[] range = findRootRange(rootId);
        int insertPos = (range == null) ? dataList.size() : range[1] + 1;
        int count = 0;
        for (CommentVO vo : more) {
            if (vo.depth == null || vo.depth < 1) vo.depth = 1;
            dataList.add(insertPos + count, new CommentItem(vo));
            count++;
        }
        notifyItemRangeInserted(insertPos, count);

        String next = more.get(more.size() - 1).pathCursor;
        if (next != null) rootNextCursor.put(rootId, next);

        // ★ 关键：刷新顶层一项，让“剩余条数/按钮文案”重算
        int rootIdx = getRootIndex(rootId);
        if (rootIdx >= 0) notifyItemChanged(rootIdx);
    }

    /**
     * 折叠某个 root：移除其所有已显示的子孙
     */
    public void collapseRoot(long rootId) {
        int[] range = findRootRange(rootId);
        if (range == null) return;
        int start = range[0] + 1, end = range[1];
        if (end >= start) {
            int count = end - start + 1;
            for (int i = 0; i < count; i++) dataList.remove(start);
            notifyItemRangeRemoved(start, count);
        }
        // 展开次数清零，等待下次从 BASE_STEP 起步
        rootExpandClicks.put(rootId, 0);
        // 折叠后刷新顶层一项，让文案回到“展开X条回复”
        int rootIdx = range[0];
        if (rootIdx >= 0) notifyItemChanged(rootIdx);
    }


    // ================= 内部工具 =================

    /**
     * 当前 root 已显示多少子孙（不含顶层本身）
     */
    private int countShownDescendants(long rootId) {
        int[] range = findRootRange(rootId);
        if (range == null) return 0;
        int diff = range[1] - range[0];
        return Math.max(0, diff); // range 包含 root 本身，所以子孙数=diff
    }

    /**
     * 返回该 root 在 dataList 中的 [rootIndex, subtreeEndIndex]，若不存在返回 null
     */
    private int[] findRootRange(long rootId) {
        int start = -1;
        for (int i = 0; i < dataList.size(); i++) {
            CommentItem it = dataList.get(i);
            if (it.isRoot() && getRootId(it.vo) == rootId) {
                start = i; // 记录当前顶层评论的下标
                break; // 找到后退出循环
            }
        }
        if (start == -1) return null;
        int end = start; // 从顶层评论开始往后找 -> 子孙的结束下标
        while (end + 1 < dataList.size()) {
            CommentItem next = dataList.get(end);
            if (next.isRoot()) break; // 遇到下一个顶层评论就停止
            // 中间的非顶层评论都是前一个顶层评论的子孙评论
            end++;
        }
        return new int[]{start, end};
    }

    private long getRootId(CommentVO vo) {
        return (vo.rootId != null && vo.rootId > 0) ? vo.rootId : (vo.id == null ? -1L : vo.id);
    }

    private int indexOfCommentId(Long id) {
        if (id == null) return -1;
        for (int i = 0; i < dataList.size(); i++) {
            CommentItem it = dataList.get(i);
            if (it.vo != null && id.equals(it.vo.id)) return i;
        }
        return -1;
    }

    private int findSubtreeEndIndex(int parentIdx) {
        if (parentIdx < 0 || parentIdx >= dataList.size()) return parentIdx;
        int parentDepth = dataList.get(parentIdx).vo.depth == null ? 0 : dataList.get(parentIdx).vo.depth;
        int end = parentIdx;
        while (end + 1 < dataList.size()) {
            CommentItem next = dataList.get(end + 1);
            int d = next.vo.depth == null ? 0 : next.vo.depth;
            if (d <= parentDepth) break;
            end++;
        }
        return end;
    }

    // ====== 乐观插入 ======

    public void insertTop(CommentVO newTop, boolean scrollToTop, RecyclerView rvOpt) {
        if (newTop == null) return;
        newTop.depth = 0;
        newTop.parentId = null;
        newTop.rootId = newTop.id;
        CommentItem item = new CommentItem(newTop);
        dataList.add(0, item);
        notifyItemInserted(0);
        if (scrollToTop && rvOpt != null) rvOpt.scrollToPosition(0);
    }

    public void insertReply(CommentVO parent, CommentVO reply) {
        if (parent == null || reply == null) return;
        int parentIdx = indexOfCommentId(parent.id);
        if (parentIdx < 0) return;
        if (reply.parentId == null) reply.parentId = parent.id;
        if (reply.depth == null) reply.depth = (parent.depth == null ? 0 : parent.depth) + 1;
        if (reply.rootId == null || reply.rootId <= 0) reply.rootId = getRootId(parent);
        int insertPos = findSubtreeEndIndex(parentIdx) + 1;
        dataList.add(insertPos, new CommentItem(reply));
        notifyItemInserted(insertPos);
        CommentItem parentItem = dataList.get(parentIdx);
        if (parentItem.vo.replyCount == null) parentItem.vo.replyCount = 0;
        parentItem.vo.replyCount += 1;
        notifyItemChanged(parentIdx);
    }

    // 放到 CommentListAdapter 内部
    private int getRootIndex(long rootId) {
        int[] range = findRootRange(rootId);
        return range == null ? -1 : range[0];
    }

    // ================= RecyclerView =================

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflateView(parent, R.layout.item_comment);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        CommentItem item = getItem(position);
        if (item == null) return;
        CommentVO vo = item.vo;

        // 缩进
        int d = (vo.depth == null ? 0 : 1);
        h.root.setPadding(basePadding + indentPerDepth * d,
                h.root.getPaddingTop(),
                h.root.getPaddingRight(),
                h.root.getPaddingBottom()
        );

        // 基本信息
        ImageLoader.loadCircle(context, vo.avatarUrl, h.ivUserAvatar);
        h.tvUserName.setText(vo.userName == null ? "" : vo.userName);
        h.tvCommentTime.setText(TimeAgoUtil.toTimeAgo(vo.createTime));
        h.tvCommentContent.setText(vo.content == null ? "" : vo.content);
        h.tvLikeCount.setText(String.valueOf(vo.likeCount == null ? 0 : vo.likeCount));

        // 顶层视觉：折叠时加粗
        h.tvCommentContent.setTypeface(null, (item.isRoot() && item.collapsedRoot) ? Typeface.BOLD : Typeface.NORMAL);

        // 行内交互
        h.btnReply.setOnClickListener(v -> {
            if (listener != null) listener.onReplyClick(vo);
        });
        h.tvLikeCount.setOnClickListener(v -> {
            if (listener != null) listener.onLikeClick(vo);
        });
        h.ivUserAvatar.setOnClickListener(v -> {
            if (listener != null) listener.onAvatarClick(vo);
        });

        // ===== 顶层的 三个按钮逻辑 =====
        if (item.isRoot()) {
            long rootId = getRootId(vo);
            showCount = countShownDescendants(rootId);                   // 当前已显示子孙数
            totalCount = vo.replyCount == null ? 0 : vo.replyCount;       // 后端给的总回复数
            remainCount = Math.max(0, totalCount - showCount);

            if (showCount == 0 && remainCount > 0) {
                h.btnExpandReplies.setVisibility(View.VISIBLE);
                h.btnLoadMoreReplies.setVisibility(View.GONE);
                h.btnCollapseReplies.setVisibility(View.GONE);
                h.btnExpandReplies.setText("展开" + remainCount + "条回复");
            } else if (showCount > 0 && remainCount > 0) {
                int clicks = rootExpandClicks.getOrDefault(rootId, 0);
                int step = BASE_STEP * Math.max(1, clicks); // 已经点过了，至少 1
                int next = Math.min(step, remainCount);
                h.btnExpandReplies.setVisibility(View.GONE);
                h.btnLoadMoreReplies.setVisibility(View.VISIBLE);
                h.btnCollapseReplies.setVisibility(View.VISIBLE);
                h.btnLoadMoreReplies.setText("再展开" + next + "条");
            } else if (showCount > 0) {
                h.btnExpandReplies.setVisibility(View.GONE);
                h.btnLoadMoreReplies.setVisibility(View.GONE);
                h.btnCollapseReplies.setVisibility(View.VISIBLE);
                h.btnCollapseReplies.setText("收起回复");
            } else {
                // 没有任何回复
                h.btnExpandReplies.setVisibility(View.GONE);
                h.btnLoadMoreReplies.setVisibility(View.GONE);
                h.btnCollapseReplies.setVisibility(View.GONE);
            }


            // 点击事件
            h.btnExpandReplies.setOnClickListener(v -> {
                showCount = countShownDescendants(rootId);                   // 当前已显示子孙数
                totalCount = vo.replyCount == null ? 0 : vo.replyCount;       // 后端给的总回复数
                remainCount = Math.max(0, totalCount - showCount);

                // 1) 增加展开次数（第一次至少为 1）
                int cur = rootExpandClicks.getOrDefault(rootId, 0);
                cur = Math.max(cur, 0) + 1;
                rootExpandClicks.put(rootId, cur);

                // 2) 立即切换本项 UI：显示“再展开N条/收起回复”，隐藏“展开X条”
                item.collapsedRoot = false;
                notifyItemChanged(position); // 立刻重算 shown/remain 并刷新文案

                // 3) 发起加载
                String cursor = rootNextCursor.getOrDefault(rootId, null);
                int size = BASE_STEP * cur; // 次数越多拉得越多
                if (listener != null) listener.onLoadMoreDepth(rootId, cursor, size);
            });


            h.btnLoadMoreReplies.setOnClickListener(v -> {

                int cur = rootExpandClicks.getOrDefault(rootId, 0) + 1;
                rootExpandClicks.put(rootId, cur);

                notifyItemChanged(position); // 先刷新按钮文案

                String cursor = rootNextCursor.getOrDefault(rootId, null);
                int size = BASE_STEP * cur;
                if (listener != null) listener.onLoadMoreDepth(rootId, cursor, size);
            });

            h.btnCollapseReplies.setOnClickListener(v -> {
                collapseRoot(rootId);
                // 标记视觉为折叠
                item.collapsedRoot = true;
                notifyItemChanged(position);
                // 折叠后三键状态会在下一次 onBind 自动刷新
            });

        } else {
            // 非顶层，隐藏三键
            h.ivUserAvatar.setScaleX(0.6f);
            h.ivUserAvatar.setScaleY(0.6f);
            h.btnExpandReplies.setVisibility(View.GONE);
            h.btnLoadMoreReplies.setVisibility(View.GONE);
            h.btnCollapseReplies.setVisibility(View.GONE);
        }

        // 可选：把整行点击/长按透传给你的基类监听
        h.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) onItemClickListener.onItemClick(item, position);
        });
        h.itemView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null)
                return onItemLongClickListener.onItemLongClick(item, position);
            return false;
        });
    }

    // ================= ViewHolder =================

    static class VH extends RecyclerView.ViewHolder {
        ViewGroup root;
        ImageView ivUserAvatar;
        TextView tvUserName, tvCommentTime, tvCommentContent, tvLikeCount, btnReply;
        TextView btnExpandReplies, btnLoadMoreReplies, btnCollapseReplies;

        public VH(@NonNull View itemView) {
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
