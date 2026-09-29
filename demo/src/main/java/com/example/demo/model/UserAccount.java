package com.example.demo.model;

public record UserAccount(String username, String email, String passwordHash, String avatarFilename) {
}
