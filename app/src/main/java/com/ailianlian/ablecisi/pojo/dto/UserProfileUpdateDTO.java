package com.ailianlian.ablecisi.pojo.dto;

public class UserProfileUpdateDTO {
    public String username;
    public String name;
    public String description;
    public String avatarUrl;

    public UserProfileUpdateDTO() {
    }

    public UserProfileUpdateDTO(String name, String description, String avatarUrl) {
        this.name = name;
        this.description = description;
        this.avatarUrl = avatarUrl;
    }
}
