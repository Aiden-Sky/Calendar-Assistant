package com.aiden.desine.model;

public class User {
    private int id;
    private String username;
    private String password; // 仅用于注册时传递明文密码
    private String phone;
    private String email;
    private String profilePicturePath;  // 新增字段：存储头像路径

    // 默认构造函数
    public User() {}

    // 兼容旧代码的构造函数，profilePicturePath 默认 null
    public User(String username, String password, String phone, String email) {
        this(username, password, phone, email, null);
    }

    // 包含 profilePicturePath 的构造函数
    public User(String username, String password, String phone, String email, String profilePicturePath) {
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.email = email;
        this.profilePicturePath = profilePicturePath;
    }

    // Getter 和 Setter 方法
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfilePicturePath() { return profilePicturePath; }
    public void setProfilePicturePath(String profilePicturePath) { this.profilePicturePath = profilePicturePath; }
}
