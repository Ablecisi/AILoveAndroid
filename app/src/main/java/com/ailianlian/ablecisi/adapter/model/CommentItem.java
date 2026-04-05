package com.ailianlian.ablecisi.adapter.model;

import com.ailianlian.ablecisi.pojo.vo.CommentVO;

/**
 * 评论列表行：普通评论，或某棵树的底部「展开/收起」条（共用根评论的 CommentVO 读总数等字段）。
 */
public class CommentItem {
    public final CommentVO vo;
    public final boolean expandBar;
    /**
     * 仅顶层评论行使用：内容加粗提示折叠（可选）
     */
    public boolean collapsedRoot = false;

    public CommentItem(CommentVO vo) {
        this.vo = vo;
        this.expandBar = false;
    }

    private CommentItem(CommentVO rootVo, boolean expandBar) {
        this.vo = rootVo;
        this.expandBar = expandBar;
    }

    public static CommentItem newExpandBar(CommentVO rootVo) {
        return new CommentItem(rootVo, true);
    }

    public boolean isRoot() {
        return !expandBar && (vo.depth == null || vo.depth == 0);
    }
}
