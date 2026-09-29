package com.example.demo.service;

import com.example.demo.model.UserAccount;
import com.example.demo.model.UserProfile;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UserService {
    private static final int MAX_AVATAR_BYTES = 2 * 1024 * 1024;
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern AVATAR_NAME = Pattern.compile("[a-f0-9-]{36}\\.(png|jpg|gif|webp)");

    private final ObjectMapper mapper;
    private final Path usersFile;
    private final Path avatarDirectory;
    private final Map<String, UserAccount> users = new LinkedHashMap<>();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(ObjectMapper mapper, @Value("${app.storage-root:.}") String storageRoot) {
        this.mapper = mapper;
        Path root = Path.of(storageRoot).toAbsolutePath().normalize();
        this.usersFile = root.resolve("data/users.json");
        this.avatarDirectory = root.resolve("uploads/avatars");
        if (Files.exists(usersFile)) {
            try {
                for (UserAccount account : mapper.readValue(usersFile.toFile(), new TypeReference<ArrayList<UserAccount>>() {})) {
                    users.put(account.username(), account);
                }
            } catch (Exception e) {
                throw new IllegalStateException("无法读取用户数据", e);
            }
        }
    }

    public synchronized UserProfile register(String username, String email, String password,
                                             String confirmPassword, byte[] avatar) {
        String name = username == null ? "" : username.trim();
        String address = email == null ? "" : email.trim();
        if (name.length() < 3 || name.length() > 20) throw new IllegalArgumentException("用户名长度必须为 3–20 位");
        if (address.length() > 100 || !EMAIL.matcher(address).matches()) throw new IllegalArgumentException("邮箱格式不正确");
        if (password == null || password.length() < 6) throw new IllegalArgumentException("密码至少需要 6 位");
        if (!password.equals(confirmPassword)) throw new IllegalArgumentException("两次密码不一致");
        if (users.keySet().stream().anyMatch(existing -> existing.equalsIgnoreCase(name))) throw new IllegalArgumentException("用户名已存在");
        if (users.values().stream().anyMatch(existing -> existing.email().equalsIgnoreCase(address))) throw new IllegalArgumentException("邮箱已存在");

        String extension = avatarExtension(avatar);
        String avatarName = extension == null ? null : UUID.randomUUID() + "." + extension;
        try {
            if (avatarName != null) {
                Files.createDirectories(avatarDirectory);
                Files.write(avatarDirectory.resolve(avatarName), avatar);
            }
            UserAccount account = new UserAccount(name, address, passwordEncoder.encode(password), avatarName);
            users.put(name, account);
            try {
                persist();
            } catch (IOException e) {
                users.remove(name);
                if (avatarName != null) Files.deleteIfExists(avatarDirectory.resolve(avatarName));
                throw e;
            }
            return toProfile(account);
        } catch (IOException e) {
            throw new IllegalStateException("保存用户数据失败", e);
        }
    }

    public synchronized boolean authenticate(String username, String password) {
        if (username == null || password == null) return false;
        UserAccount account = findAccount(username);
        return account != null && passwordEncoder.matches(password, account.passwordHash());
    }

    public synchronized UserProfile getProfile(String username) {
        UserAccount account = findAccount(username);
        return account == null ? null : toProfile(account);
    }

    public byte[] readAvatar(String filename) throws IOException {
        if (filename == null || !AVATAR_NAME.matcher(filename).matches()) throw new IllegalArgumentException("头像文件名无效");
        return Files.readAllBytes(avatarDirectory.resolve(filename));
    }

    private UserAccount findAccount(String username) {
        return users.values().stream().filter(account -> account.username().equalsIgnoreCase(username.trim())).findFirst().orElse(null);
    }

    private UserProfile toProfile(UserAccount account) {
        String url = account.avatarFilename() == null ? null : "/uploads/avatars/" + account.avatarFilename();
        return new UserProfile(account.username(), account.email(), url);
    }

    private String avatarExtension(byte[] avatar) {
        if (avatar == null || avatar.length == 0) return null;
        if (avatar.length > MAX_AVATAR_BYTES) throw new IllegalArgumentException("头像大小不能超过 2 MB");
        if (startsWith(avatar, new int[] {0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a})) return "png";
        if (startsWith(avatar, new int[] {0xff, 0xd8, 0xff})) return "jpg";
        if (startsWith(avatar, "GIF87a".getBytes()) || startsWith(avatar, "GIF89a".getBytes())) return "gif";
        if (avatar.length >= 12 && startsWith(avatar, "RIFF".getBytes()) && avatar[8] == 'W' && avatar[9] == 'E' && avatar[10] == 'B' && avatar[11] == 'P') return "webp";
        throw new IllegalArgumentException("头像仅支持 PNG、JPG、GIF 或 WebP 图片");
    }

    private boolean startsWith(byte[] bytes, int[] prefix) {
        if (bytes.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) if ((bytes[i] & 0xff) != prefix[i]) return false;
        return true;
    }

    private boolean startsWith(byte[] bytes, byte[] prefix) {
        if (bytes.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) if (bytes[i] != prefix[i]) return false;
        return true;
    }

    private void persist() throws IOException {
        Files.createDirectories(usersFile.getParent());
        Path temporary = Files.createTempFile(usersFile.getParent(), "users-", ".tmp");
        try {
            mapper.writeValue(temporary.toFile(), new ArrayList<>(users.values()));
            Files.move(temporary, usersFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            throw new IOException("当前文件系统不支持原子保存用户数据", e);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
