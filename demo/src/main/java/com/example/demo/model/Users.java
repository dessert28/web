package com.example.demo.model;

/**
 * 接口请求对象：POST /api/register、POST /api/login 用 JSON 请求体传参。
 * 请求示例：{"username":"alice","password":"secret1","confirmPassword":"secret1"}
 */
public class Users {

    private String username;
    private String password;
    private String confirmPassword;

    public Users() {
    }

    public Users(String username, String password, String confirmPassword) {
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
