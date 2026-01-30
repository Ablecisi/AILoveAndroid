package com.ailianlian.ablecisi.pojo.dto;

/**
 * ailianlian
 * com.ailianlian.ablecisi.pojo.dto
 * AiCharacterCreateDTO <br>
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/9/7
 * 星期日
 * 15:14
 */
public class AiCharacterCreateDTO {
    public String name;
    public Long userId;        // 系统内置可为 null
    public Long typeId;
    public Integer gender;     // 默认 2
    public Integer age;
    public String imageUrl;
    public String traits;        // JSON 字符串或逗号分隔
    public String personaDesc;
    public String interests;     // JSON 字符串或逗号分隔
    public String backstory;
    public Integer status;       // 默认 1
}
