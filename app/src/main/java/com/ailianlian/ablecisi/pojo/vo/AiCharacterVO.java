package com.ailianlian.ablecisi.pojo.vo;

import com.ailianlian.ablecisi.pojo.entity.AiCharacter;

import java.time.LocalDateTime;

/**
 * ailianlian
 * com.ailianlian.ablecisi.pojo.vo
 * AiCharacterVO <br>
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/9/7
 * 星期日
 * 15:10
 */
public class AiCharacterVO {
    public Long id;
    public Long userId;
    public String name;
    public Long typeId;
    public String typeName;
    public Integer gender;
    public Integer age;
    public String imageUrl;
    public String traits;
    public String personaDesc;
    public String interests;
    public String backstory;
    public Integer online;
    public Integer status;
    public LocalDateTime createTime;
    public LocalDateTime updateTime;

    /*
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
    * */

    public AiCharacter getAiCharacter() {
        AiCharacter aiCharacter = new AiCharacter();
        aiCharacter.id = String.valueOf(this.id);
        aiCharacter.name = this.name;
        aiCharacter.type = this.typeName;
        aiCharacter.gender = this.gender == 0 ? "男" : "女";
        aiCharacter.age = this.age;
        aiCharacter.imageUrl = this.imageUrl;
        aiCharacter.personalityTraits = this.traits != null ? java.util.Arrays.asList(this.traits.split(",")) : new java.util.ArrayList<>();
        aiCharacter.description = this.personaDesc;
        aiCharacter.interests = this.interests != null ? java.util.Arrays.asList(this.interests.split(",")) : new java.util.ArrayList<>();
        aiCharacter.backgroundStory = this.backstory;
        aiCharacter.online = this.online != null && this.online == 1;
        aiCharacter.createdAt = this.createTime != null ? this.createTime : LocalDateTime.now();
        return aiCharacter;
    }
}
