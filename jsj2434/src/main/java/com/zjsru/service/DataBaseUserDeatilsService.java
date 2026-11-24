package com.zjsru.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security 的数据库用户来源。
 *
 * <p>对应老师给的 1117 样本三表：</p>
 * <ul>
 *   <li>{@code sys_user}：账号、BCrypt 密码、{@code enabled}；</li>
 *   <li>{@code sys_role}：{@code ADMIN} / {@code DOCTOR} / {@code PATIENT}；</li>
 *   <li>{@code sys_user_role}：用户与角色的多对多关联。</li>
 * </ul>
 *
 * <p>密码不在这里比对：本类只把库里的 BCrypt 哈希交给
 * {@code DaoAuthenticationProvider}，由 {@code WebAuthConfig} 里的
 * {@code BCryptPasswordEncoder} 完成校验。账号被禁用时通过
 * {@code disabled(true)} 让 Security 直接拒绝登录。</p>
 *
 * <p>注意：样本种子密码是 {@code 1234}（不是 {@code 123}），
 * 账号为 {@code admin} / {@code doctor} / {@code patient}。</p>
 */
@Service
public class DataBaseUserDeatilsService implements UserDetailsService {

    private final JdbcTemplate jdbcTemplate;

    public DataBaseUserDeatilsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<SysUserRow> rows = jdbcTemplate.query(
                "select id, username, password, enabled from sys_user where username = ?",
                (rs, rowNum) -> new SysUserRow(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getBoolean("enabled")),
                username);

        if (rows.isEmpty()) {
            // 不区分「用户不存在」与「密码错误」，避免账号枚举
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        SysUserRow row = rows.get(0);
        List<String> roleNames = jdbcTemplate.queryForList(
                "select r.name from sys_role r "
                        + "join sys_user_role ur on ur.role_id = r.id "
                        + "where ur.user_id = ?",
                String.class, row.id());

        return User.withUsername(row.username())
                .password(row.password())
                // roles(...) 会自动补 ROLE_ 前缀，hasRole("DOCTOR") 可直接匹配
                .roles(roleNames.toArray(new String[0]))
                .disabled(!row.enabled())
                .build();
    }

    /** 只在本类内部使用的行映射，不暴露成 domain。 */
    private record SysUserRow(long id, String username, String password, boolean enabled) {
    }
}
