package com.ailianlian.ablecisi.pojo.dto;

/**
 * ailianlian
 * com.ailianlian.ablecisi.pojo.dto
 * UserDTO <br>
 * 用户数据传输对象
 * @author Ablecisi
 * @version 1.0
 * 2025/4/25
 * 星期五
 * 23:00
 */
public class UserDTO {
    private String username;
    private String password;

    public UserDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public UserDTO() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
