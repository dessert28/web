package com.zjsru.domain;

import lombok.Data;
import lombok.ToString;

@Data
public class Users {
    private String username;
    @ToString.Exclude
    private String password;
    private String email;
    private String phone;
    private String address;

}

