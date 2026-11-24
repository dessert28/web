package com.zjsru.hospital.security;

import com.zjsru.hospital.domain.LoginUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 数据库登录用户在 Spring Security 中的表示。
 *
 * <p>支持两种数据来源，对应仓库里的两套账号模型：</p>
 * <ul>
 *   <li><b>双表模型</b>（本项目现状）：患者查 {@code tb_users}，医生查 {@code doctor_info}，
 *       角色由 {@link LoginUser#isDoctor()} 推导，见 {@link #DatabaseUserDetails(LoginUser)}；</li>
 *   <li><b>RBAC 三表模型</b>（参考工程 {@code 1117} 的 {@code sys_user} / {@code sys_role} /
 *       {@code sys_user_role}）：角色来自关联查询，见
 *       {@link #DatabaseUserDetails(LoginUser, String, String, Collection)}。</li>
 * </ul>
 *
 * <p>角色名统一转成 Spring Security 的 {@code ROLE_XXX} 形式：
 * {@code sys_role.name = 'DOCTOR'} 与 {@code LoginUser.role = DOCTOR} 最终都是 {@code ROLE_DOCTOR}，
 * 因此 {@code SecurityFilterChain} 里只写一次 {@code hasRole("DOCTOR")} 就能同时服务两套模型。</p>
 *
 * <p><b>关于密码</b>：双表模型下密码比对发生在 {@code AuthService} 的 SQL 条件里，
 * 构造时认证已通过，{@code password} 通常为 {@code null}；若将来切到
 * {@code sys_user} + {@code DaoAuthenticationProvider}，把库里的 BCrypt 哈希传进来即可，
 * {@code PasswordEncoder} 会负责比对。{@code null} 不会被本类做任何特殊处理，
 * 是否参与比对由认证器决定。</p>
 *
 * <p>账号状态：课程演示项目没有锁定/过期概念，{@code isXxxNonExpired} 等一律返回 {@code true}；
 * 若接入 {@code sys_user.enabled}，从构造参数扩展一个字段即可。</p>
 */
public class DatabaseUserDetails implements UserDetails, Serializable {

    private static final long serialVersionUID = 1L;

    private static final String ROLE_PREFIX = "ROLE_";

    /** 业务身份：患者为 tb_users 一行，医生为 doctor_info 一行。 */
    private final LoginUser loginUser;

    /** 登录名：患者是 tb_users.username，医生是 doctor_info.doctorName。 */
    private final String username;

    /** 库里的密码；双表模型下通常为 null，三表模型下传 BCrypt 哈希。 */
    private final String password;

    /** 规范化后的角色名，已去掉重复、已保证带 ROLE_ 前缀。 */
    private final Set<String> roleNames;

    /**
     * 双表模型：角色由 {@link LoginUser} 推导（医生 → DOCTOR，其余 → PATIENT）。
     */
    public DatabaseUserDetails(LoginUser loginUser) {
        this(loginUser, loginUser == null ? null : loginUser.getDisplayName(), null, null);
    }

    /**
     * 通用构造：角色显式给出，对应 {@code sys_user_role} 关联出来的 {@code sys_role.name}。
     *
     * @param roleNames 角色名集合，可带可不带 {@code ROLE_} 前缀；为空时回退到
     *                  {@link LoginUser} 推导，避免调用方漏传导致无权限
     */
    public DatabaseUserDetails(LoginUser loginUser, String username, String password,
                               Collection<String> roleNames) {
        this.loginUser = Objects.requireNonNull(loginUser, "loginUser 不能为空");
        this.username = username;
        this.password = password;
        this.roleNames = normalizeRoles(roleNames);
    }

    private Set<String> normalizeRoles(Collection<String> rawRoles) {
        Set<String> normalized = new LinkedHashSet<>();
        if (rawRoles != null) {
            for (String role : rawRoles) {
                if (role == null || role.isBlank()) {
                    continue;
                }
                String trimmed = role.trim();
                normalized.add(trimmed.startsWith(ROLE_PREFIX) ? trimmed : ROLE_PREFIX + trimmed);
            }
        }
        if (normalized.isEmpty()) {
            normalized.add(loginUser.isDoctor() ? ROLE_PREFIX + "DOCTOR" : ROLE_PREFIX + "PATIENT");
        }
        return Set.copyOf(normalized);
    }

    /** 业务身份，认证成功后直接使用，不需要再从 session 里拼。 */
    public LoginUser getLoginUser() {
        return loginUser;
    }

    /** 规范化后的角色名（含 ROLE_ 前缀），便于排查权限问题。 */
    public Set<String> getRoleNames() {
        return roleNames;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roleNames.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role))
                .toList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String toString() {
        return "DatabaseUserDetails{username='" + username + "', roles=" + roleNames + "}";
    }
}
