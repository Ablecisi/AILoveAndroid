package com.ailianlian.ablecisi.pojo.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义AI角色数据模型
 */
public class AiCharacter {
    public String id; // 角色ID
    public String name; // 角色名称
    public String type; // 角色类型，例如：AI助手、虚拟偶像等
    public String gender;
    public Integer age;
    public String imageUrl; // 角色头像URL
    public List<String> personalityTraits; // 角色个性特征列表
    public String description; // 角色个性描述
    public List<String> interests; // 角色兴趣爱好列表
    public String backgroundStory; // 角色背景故事
    public Boolean online; // 角色是否在线
    public LocalDateTime createdAt; // 角色创建时间，使用时间戳表示


    public AiCharacter(String id, String name, String type, String imageUrl, Boolean online) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.imageUrl = imageUrl;
        this.online = online;
    }

    public AiCharacter() {
        this.personalityTraits = new ArrayList<>();
        this.interests = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }

    public AiCharacter(String id, String name, String type, String gender, int age, String imageUrl, List<String> personalityTraits, String description, List<String> interests, String backgroundStory, Boolean online, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.gender = gender;
        this.age = age;
        this.imageUrl = imageUrl;
        this.personalityTraits = personalityTraits;
        this.description = description;
        this.interests = interests;
        this.backgroundStory = backgroundStory;
        this.online = online;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getPersonalityTraits() {
        return personalityTraits;
    }

    public void setPersonalityTraits(List<String> personalityTraits) {
        this.personalityTraits = personalityTraits;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public String getBackgroundStory() {
        return backgroundStory;
    }

    public void setBackgroundStory(String backgroundStory) {
        this.backgroundStory = backgroundStory;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void addPersonalityTrait(String trait) {
        if (this.personalityTraits == null) {
            this.personalityTraits = new ArrayList<>();
        }
        this.personalityTraits.add(trait);

    }

    public void addInterest(String interest) {
        if (this.interests == null) {
            this.interests = new ArrayList<>();
        }
        this.interests.add(interest);
    }
}