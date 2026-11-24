package com.zjsru.service;

import com.zjsru.domain.Users;
import com.zjsru.mapper.UserMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security 的数据库用户来源。
 *
 * <p>认证数据改由 {@link UserMapper} 的 XML 映射提供（对应老师给的三表）：</p>
 * <ul>
 *   <li>{@code findByUsername} 查 {@code sys_user}，并把 {@code enabled} 映射成
 *       {@link Users#getStatus()}；</li>
 *   <li>{@code findRolesByUserId} 三表联查，把 {@code sys_role.name} 别名为
 *       {@code username}，这里再拼上 {@code ROLE_} 前缀。</li>
 * </ul>
 *
 * <p>密码不在这里比对：框架用 {@code BCryptPasswordEncoder} 校验库里的 BCrypt 哈希。
 * 账号被禁用时通过 {@code disabled(true)} 拒绝登录。样本账号为
 * {@code admin} / {@code doctor} / {@code patient}，密码 {@code 1234}（不是 123）。</p>
 */
@Service
public class DataBaseUserDeatilsService implements UserDetailsService {

    private final UserMapper userMapper;

    public DataBaseUserDeatilsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = userMapper.findByUsername(username);
        if (user == null) {
            // 不区分「用户不存在」与「密码错误」，避免账号枚举
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        List<Users> roles = userMapper.findRolesByUserId(Integer.valueOf(user.getId()));
        List<GrantedAuthority> authorities = roles.stream()
                // XML 把 sys_role.name 别名成 username，所以这里拿到的是 ADMIN/DOCTOR/PATIENT
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role.getUsername()))
                .toList();

        return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .disabled(!Boolean.TRUE.equals(user.getStatus()))
                .build();
    }
}
