package com.ailianlian.ablecisi.pojo.dto;

public class ArticleToggleDTO {
    public Long articleId;
    public Boolean active;

    public ArticleToggleDTO(Long articleId, Boolean active) {
        this.articleId = articleId;
        this.active = active;
    }
}
