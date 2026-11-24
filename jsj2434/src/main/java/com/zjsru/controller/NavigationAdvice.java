package com.zjsru.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * 把当前登录人的角色与用户名注入所有页面模型，供公共页头按角色显示导航。
 *
 * <p>项目未引入 thymeleaf-extras-springsecurity，模板里不能直接写 sec:authorize，
 * 所以在这里把 {@code navRole}（ADMIN / DOCTOR / PATIENT / 空）与 {@code navUsername}
 * 放进 model，页面用普通的 th:if 判断即可。未登录或匿名请求时两者都是空字符串。</p>
 */
@ControllerAdvice
public class NavigationAdvice {

    @ModelAttribute
    public void addNavigation(Model model) {
        String role = "";
        String username = "";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {
            username = auth.getName();
            role = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(value -> value.startsWith("ROLE_"))
                    .map(value -> value.substring("ROLE_".length()))
                    .findFirst()
                    .orElse("");
        }
        model.addAttribute("navRole", role);
        model.addAttribute("navUsername", username);
    }
}
