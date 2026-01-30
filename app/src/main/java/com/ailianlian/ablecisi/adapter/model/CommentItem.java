package com.ailianlian.ablecisi.adapter.model;

import com.ailianlian.ablecisi.pojo.vo.CommentVO;

/**
 * ailianlian
 * com.ailianlian.ablecisi.pojo.entity
 * CommentItem <br>
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/9/1
 * 星期一
 * 09:37
 */
public class CommentItem {
    public final CommentVO vo;
    /**
     * 仅顶层使用：是否折叠其子孙
     */
    public boolean collapsedRoot = false;

    public CommentItem(CommentVO vo) {
        this.vo = vo;
    }

    public boolean isRoot() {
        return vo.depth == null || vo.depth == 0;
    }
}
