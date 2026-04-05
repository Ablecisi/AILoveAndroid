package com.ailianlian.ablecisi.pojo.entity;

/**
 * 本地浏览历史项（文章或帖子）
 */
public class BrowseHistoryEntry {
    public static final String KIND_ARTICLE = "article";
    public static final String KIND_POST = "post";

    public String kind;
    public String id;
    public String title;
    public long visitedAt;

    public BrowseHistoryEntry() {
    }

    public BrowseHistoryEntry(String kind, String id, String title, long visitedAt) {
        this.kind = kind;
        this.id = id;
        this.title = title;
        this.visitedAt = visitedAt;
    }
}
