package com.example.demo;

import com.example.demo.service.UserService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTests {
    @TempDir Path directory;

    @Test
    void registrationPersistsHashedPasswordAndReloadsProfile() throws Exception {
        UserService service = new UserService(new ObjectMapper(), directory.toString());
        var profile = service.register("alice", "alice@example.com", "secret1", "secret1", null);

        assertEquals("alice", profile.username());
        assertEquals("alice@example.com", profile.email());
        assertTrue(service.authenticate("alice", "secret1"));
        assertFalse(Files.readString(directory.resolve("data/users.json")).contains("secret1"));

        UserService reloaded = new UserService(new ObjectMapper(), directory.toString());
        assertTrue(reloaded.authenticate("alice", "secret1"));
        assertEquals("alice@example.com", reloaded.getProfile("alice").email());
    }

    @Test
    void rejectsDuplicateEmailAndInvalidEmail() {
        UserService service = new UserService(new ObjectMapper(), directory.toString());
        service.register("alice", "alice@example.com", "secret1", "secret1", null);
        assertEquals("邮箱已存在", assertThrows(IllegalArgumentException.class,
                () -> service.register("bob01", "ALICE@example.com", "secret1", "secret1", null)).getMessage());
        assertEquals("邮箱格式不正确", assertThrows(IllegalArgumentException.class,
                () -> service.register("bob01", "invalid", "secret1", "secret1", null)).getMessage());
    }

    @Test
    void savesValidAvatarAndRejectsInvalidBytes() throws Exception {
        UserService service = new UserService(new ObjectMapper(), directory.toString());
        byte[] png = new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 1};
        var profile = service.register("alice", "alice@example.com", "secret1", "secret1", png);
        assertTrue(profile.avatarUrl().startsWith("/uploads/avatars/"));
        assertArrayEquals(png, service.readAvatar(profile.avatarUrl().substring("/uploads/avatars/".length())));
        assertEquals("头像仅支持 PNG、JPG、GIF 或 WebP 图片", assertThrows(IllegalArgumentException.class,
                () -> service.register("bob01", "bob@example.com", "secret1", "secret1", new byte[] {1, 2, 3})).getMessage());
    }
}
