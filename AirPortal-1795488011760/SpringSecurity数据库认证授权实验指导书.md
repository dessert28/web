# Spring Security 数据库认证与授权实验指导书

> 实验目标：给已有 Spring Boot 项目接入 **Spring Security**，实现"从数据库读取用户 + BCrypt 密码校验 + 按角色授权"的完整登录体系；理解认证（你是谁）与授权（你能访问什么）的区别，以及两者如何配合。  
> 前置知识：Spring Boot 基础、MyBatis 基础、REST 接口、HTML 表单。  
> 环境要求：JDK 21、Maven、IntelliJ IDEA、MySQL（可用项目已配的数据库）。

---

## 实验任务总览

本实验把"没有登录门禁的系统"改造成"数据库认证 + 角色授权"的系统。共 9 个阶段：

| 阶段  | 任务           | 核心产出             | 对应文件                              |
| --- | ------------ | ---------------- | --------------------------------- |
| 一   | 修改版本号        | 兼容的 Boot 3 基础    | `pom.xml`                         |
| 二   | 引入依赖         | Security 能力      | `pom.xml`                         |
| 三   | 建库建表         | 用户/角色/中间表 + 测试账号 | MySQL（`sys_*` 三张表）                |
| 四   | 添加实体与 Mapper | 能查数据库的用户通道       | `Users.java`、`UserMapper.java`    |
| 五   | 添加密码加密器      | BCrypt Bean      | `WebAuthConfig.java`              |
| 六   | 添加认证服务       | 从数据库加载用户         | `DataBaseUserDetailsService.java` |
| 七   | 添加安全配置       | 放行/授权/登录/登出      | `SecurityConfig.java`             |
| 八   | 添加页面         | 登录页 + 首页展示角色     | `login.html`、`dashboard.html`     |
| 九   | 编译与验证        | 跑通的完整认证体系        | ——                                |

**先记住这张核心链路图**（后面每一步都在填这条链的某个环节）：

```
① 浏览器提交表单 POST /login（username + password）
   ▼
② SecurityConfig 的 formLogin 拦截请求，交给 AuthenticationManager
   ▼
③ DataBaseUserDetailsService.loadUserByUsername() 查数据库：
     sys_user 取用户名+密码；sys_user_role + sys_role 取角色 → ROLE_XXX
   ▼
④ 框架用 BCryptPasswordEncoder 比对：表单明文 vs 数据库 BCrypt 哈希
   ▼
⑤ 校验通过 → SecurityContext 写入登录态，跳转 /dashboard
   ▼
⑥ 访问 /doctor/** /patient/** 时，SecurityConfig 的 hasRole 按角色放行或拒绝
   ▼
⑦ 页面用 sec:authorize 按角色显示菜单；Controller 从 SecurityContext 取当前用户
```

---

## 阶段一：修改版本号

### 1.1 为什么要做

本项目原先由 **Spring Boot 4.0.x** 生成，已在本课程前一实验统一降到 **3.5.9**。本实验沿用这个版本基线，原因：

- Spring Boot 3.5.9 **内置管理** Spring Security **6.5.7**（无需手写版本号）；
- Boot 4 的一批 starter 改了 artifactId（如 `starter-web` → `starter-webmvc`），与本课程其他依赖不兼容；
- 版本错配时典型报错：`找不到或无法加载主类`、Bean 注入失败。

### 1.2 学生要做什么

确认 `pom.xml` 的父版本与 Java 版本：

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.9</version>
    <relativePath/>
</parent>

<properties>
    <java.version>21</java.version>
</properties>
```

> **注意**：Security 依赖**不要手写 `<version>`**，由父 POM 统一管理（本实验实际解析到 6.5.7）。手写版本号反而容易和其他 Spring 组件冲突。

### 1.3 怎么验证

```bash
./mvnw clean compile -DskipTests
```

看到 `BUILD SUCCESS` 即通过。

---

## 阶段二：引入依赖

### 2.1 学生要做什么

在 `pom.xml` 的 `<dependencies>` 里加入三个依赖（**均不写 version**）：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>


<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>


<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

### 2.2 三个依赖各自的作用

| 依赖                                 | 作用                    | 少了会怎样                         |
| ---------------------------------- | --------------------- | ----------------------------- |
| `spring-boot-starter-security`     | 认证/授权核心、默认登录页、密码编码器   | 完全没有安全保护                      |
| `spring-security-test`             | 测试时模拟登录用户             | 写 `@WithMockUser` 测试会报错（教学可选） |
| `thymeleaf-extras-springsecurity6` | 让模板支持 `sec:authorize` | 页面里 `sec:authorize` 不生效       |

> **版本对应**：thymeleaf-extras 用的是 `springsecurity6`（不是 5）。Boot 3.x 用 Spring Security 6，选错成 5 会导致标签解析失败。

### 2.3 引入后立刻生效的"默认行为"

只加 starter、**什么配置都不写**，Spring Security 也会自动保护所有请求，并：

- 生成一个随机密码（控制台日志里打印 `Using generated security password: ...`）；
- 默认用户名 `user`；
- 提供默认登录页 `/login`。

这正是"先加依赖、后再定制"的教学起点——后面的阶段就是把这个默认行为替换成我们自己的数据库认证。

验证依赖是否进来：

```bash
./mvnw dependency:tree | grep -i "spring-security"
```

---

## 阶段三：建库建表与测试账号

### 3.1 为什么要三张表

数据库认证需要把"用户"和"角色"分开存，两者是多对多关系（一个用户可有多个角色），所以需要中间表：

| 表               | 作用      | 关键字段                                  |
| --------------- | ------- | ------------------------------------- |
| `sys_user`      | 存账号与密码  | `id`、`username`、`password`（BCrypt 哈希） |
| `sys_role`      | 存角色     | `id`、`name`（如 `DOCTOR`）               |
| `sys_user_role` | 用户-角色关联 | `user_id`、`role_id`                   |

### 3.2 学生要做什么（建表 SQL）

```sql
-- 用户表（密码字段必须能放下 60 位的 BCrypt 哈希）
CREATE TABLE sys_user (
    id       INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL
);

-- 角色表：name 存 ADMIN / DOCTOR / PATIENT（不带 ROLE_ 前缀）
CREATE TABLE sys_role (
    id   INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 用户-角色中间表（多对多）
CREATE TABLE sys_user_role (
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);
```

### 3.3 插入测试账号（密码必须 BCrypt 加密）

**关键点**：Spring Security 注入了 `BCryptPasswordEncoder`，它会把表单提交的**明文密码**与数据库中存的**BCrypt 哈希**比对。所以数据库里**绝不能存明文**，必须存哈希。

下面 3 条哈希都是明文 `1234` 的 BCrypt 结果（用本项目的编码器实际生成，可直接用）：

```sql
INSERT INTO sys_role (id, name) VALUES
  (1, 'ADMIN'), (2, 'DOCTOR'), (3, 'PATIENT');

INSERT INTO sys_user (id, username, password) VALUES
  (1, 'admin',   '$2a$10$lPUMpKv4BQ7/hxPMw6TF7u3zl9IvM/qoX/7Qqk9KD.gPXGbZcCWhq'),
  (2, 'doctor',  '$2a$10$u73CgZ2AwleWb8fwdt0I7e6eb4l3Am58.4VRpKjIwUO41G48IRfLm'),
  (3, 'patient', '$2a$10$c0uz7rMop8U9vSpxu0eJUOmKRdONKOd5N7j.Bm0FFjgqA3E.d3oE6');

INSERT INTO sys_user_role (user_id, role_id) VALUES
  (1, 1),  -- admin  → ADMIN
  (2, 2),  -- doctor → DOCTOR
  (3, 3);  -- patient→ PATIENT
```

> 测试账号（明文）：`admin / 1234`、`doctor / 1234`、`patient / 1234`。

### 3.4 如何自己生成 BCrypt 密码

不要手写哈希（BCrypt 每次结果都不同，但都能校验成功）。用项目里的编码器生成，可用 JShell 或临时 Java 类调用 `new BCryptPasswordEncoder().encode("你的明文")`。

> **事实核验提示**：上面 3 条哈希由本项目实际依赖 `spring-security-crypto 6.5.7` 的 `BCryptPasswordEncoder` 对明文 `1234` 生成（2026-11-24），可直接用于本实验。换密码时重新生成即可。

---

## 阶段四：添加实体类与 Mapper

### 4.1 学生要做什么

**① 实体类** `src/main/java/com/zjsru/domain/Users.java`（项目已有）：

```java
package com.zjsru.domain;

import lombok.Data;

@Data
public class Users {
    private Integer id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String address;
}
```

**② Mapper 接口** `src/main/java/com/zjsru/mapper/UserMapper.java`——认证需要两个方法：

```java
package com.zjsru.mapper;

import com.zjsru.domain.Users;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {

    // ① 根据用户名查询用户（认证第一步）
    @Select("select * from sys_user where username = #{username}")
    Users selectUserByName(String username);

    // ② 根据用户ID查询角色（认证第二步，三表联查）
    @Select("select r.name from sys_user u " +
            "join sys_user_role ur on u.id = ur.user_id " +
            "join sys_role r on ur.role_id = r.id " +
            "where u.id = #{id}")
    List<String> selectRolesByUserId(Integer userId);
}
```

### 4.2 逐点理解

| 点                     | 说明                                                  |
| --------------------- | --------------------------------------------------- |
| `@Mapper`             | 标记为 MyBatis 接口，Spring 会生成实现类并注入                     |
| `selectUserByName`    | 查的是 **`sys_user`** 表（Security 认证专用），不是旧的 `tb_users` |
| `selectRolesByUserId` | 三表联查，返回角色名列表（`ADMIN`/`DOCTOR`…）                     |
| `#{username}`         | MyBatis 参数占位，防 SQL 注入，**不要写成 `${}`**                |

> **本项目真实代码说明**：实际 `UserMapper.java` 里还有 `selectAllUsers`、`selectUserById` 等方法（查 `tb_users`），那是旧功能的遗留。认证只用上面这两个查 `sys_*` 表的方法，不要在教学中把它们混为一谈。

### 4.3 数据源配置

确认 `application.properties` 里的数据源指向正确的库（本项目已配）：

```properties
spring.datasource.url=jdbc:mysql://172.20.54.63:3306/javaweb?...(省略参数)
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
mybatis.mapper-locations=classpath:mapper/*.xml
```

> 用注解 SQL（`@Select`）时，`mapper-locations` 不是必需的，但保留无害。

---

## 阶段五：添加密码加密器

### 5.1 学生要做什么

新建 `src/main/java/com/zjsru/config/WebAuthConfig.java`：

```java
package com.zjsru.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class WebAuthConfig {
    /** 密码加密器：Spring Security 会自动用它校验数据库里的 BCrypt 密码。 */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 5.2 为什么必须有这个 Bean

- 框架在校验密码时，会找容器里的 `PasswordEncoder` Bean；
- 不配这个 Bean，用明文密码会报 `There is no PasswordEncoder mapped for the id "null"`；
- 注册新用户时，也要**手动调用** `passwordEncoder.encode(明文)` 再入库（本项目注册流程尚未实现，见 9.3 思考题）。

> **记忆点**：数据库存 BCrypt 哈希 → 登录时框架自动比对；注册时自己 `encode()` 后再存。一存一取，两端都要加密意识。

---

## 阶段六：添加认证服务（数据库加载用户）

### 6.1 学生要做什么

新建 `src/main/java/com/zjsru/service/DataBaseUserDetailsService.java`：

```java
package com.zjsru.service;

import com.zjsru.domain.Users;
import com.zjsru.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DataBaseUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 按用户名查用户
        Users user = userMapper.selectUserByName(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        // 2. 查该用户的角色
        List<String> roles = userMapper.selectRolesByUserId(user.getId());

        // 3. 角色转成 Spring Security 的 GrantedAuthority，加 ROLE_ 前缀
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));

        // 4. 组装成 UserDetails 返回（框架据此校验密码与权限）
        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }
}
```

### 6.2 六个步骤逐条讲

1. **实现 `UserDetailsService` 接口**：这是 Spring Security 查用户的标准入口，框架会自动调用它。
2. **查用户**：`selectUserByName` 返回 `null` 就抛 `UsernameNotFoundException`。
3. **查角色**：用用户 id 去中间表联查。
4. **`ROLE_` 前缀**：框架的角色授权要求权限字符串以 `ROLE_` 开头。`hasRole("DOCTOR")` 内部会拼成 `ROLE_DOCTOR` 去匹配，所以这里必须加，否则永远授权失败。
5. **组装 `UserDetails`**：用 Spring Security 自带的 `User.builder()`（**不是**你自己的 `Users` 类）。
6. **`@RequiredArgsConstructor`**：Lombok 生成构造器，实现 `UserMapper` 的构造器注入（final 字段必须这样注入）。

### 6.3 两个高频错误

| 错误                | 现象                         | 原因                           |
| ----------------- | -------------------------- | ---------------------------- |
| 角色不加 `ROLE_`      | 登录成功但访问 `/doctor/**` 仍 403 | `hasRole` 匹配的是 `ROLE_DOCTOR` |
| 用了自己的 `Users` 类返回 | 编译不通过                      | 必须返回 `UserDetails` 类型        |

---

## 阶段七：添加安全配置（授权 + 登录 + 登出）

### 7.1 学生要做什么

新建/修改 `src/main/java/com/zjsru/config/SecurityConfig.java`：

```java
package com.zjsru.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // ① 放行：匿名可访问
                .requestMatchers("/", "/login", "/ai-consult", "/api/public/**",
                                 "/static/**", "/css/**", "/js/**", "/images/**",
                                 "/logout").permitAll()
                // ② 授权：按角色拦截
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/doctor/**").hasRole("DOCTOR")
                .requestMatchers("/patient/**").hasRole("PATIENT")
                .requestMatchers("/common/**").hasAnyRole("DOCTOR", "PATIENT")
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")                    // 自定义登录页
                .defaultSuccessUrl("/dashboard", true)  // 成功跳首页，true=总是跳
                .failureForwardUrl("/login?error=true") // 失败回登录页带错误标记
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
            )
            .csrf(csrf -> csrf.disable());              // 关闭 CSRF（教学简化）

        return http.build();
    }
}
```

### 7.2 三个核心概念

**① 放行 vs 授权**

| 写法                                | 含义                    |
| --------------------------------- | --------------------- |
| `.permitAll()`                    | 任何人可访问（登录页、静态资源、公开接口） |
| `.hasRole("DOCTOR")`              | 必须有 `ROLE_DOCTOR` 权限  |
| `.hasAnyRole("DOCTOR","PATIENT")` | 两者任一即可                |
| `.authenticated()`                | 只要登录就行，不限角色           |

**② `hasRole` 和 `ROLE_` 的对应**

`hasRole("ADMIN")` 等价于检查权限 `ROLE_ADMIN`。数据库 `sys_role.name` 存 `ADMIN`（不带前缀），是阶段六的 `DataBaseUserDetailsService` 给它加上前缀的。**两处必须约定一致。**

**③ 规则顺序：先具体后宽泛**

`.anyRequest().permitAll()` 必须放最后。若放前面，后面的角色规则永远不会生效（匹配到第一条就停）。

### 7.3 表单登录的三个路径

| 配置                                       | 作用                        |
| ---------------------------------------- | ------------------------- |
| `loginPage("/login")`                    | 自定义登录页地址（GET 显示表单）        |
| 表单 `action="/login"` method=post         | POST 提交，由框架拦截认证           |
| `defaultSuccessUrl("/dashboard", true)`  | 认证成功跳转；`true` 表示忽略原来访问的地址 |
| `failureForwardUrl("/login?error=true")` | 失败转发回登录页，页面据此显示错误         |

> **为什么要关闭 CSRF**：Spring Security 默认开启 CSRF 防护，POST 表单必须带 token，否则 403。教学为了简化先 `csrf.disable()`。生产环境**不要**这样，应在表单里加 `<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">`。

---

## 阶段八：添加页面与获取当前用户

### 8.1 登录页 `templates/login.html`（关键部分）

```html
<!-- 错误/登出提示：读取 URL 参数 -->
<div th:if="${param.error}" class="alert error">用户名或密码错误！</div>
<div th:if="${param.logout}" class="alert success">您已成功退出登录！</div>

<!-- 表单：字段名必须是 username / password，action 指向 /login -->
<form th:action="@{/login}" method="post">
    <input type="text"     name="username" required>
    <input type="password" name="password" required>
    <button type="submit">登录系统</button>
</form>
```

> **字段名必须叫 `username` 和 `password`**——这是 Spring Security 表单登录的默认参数名。改成 `user`/`pwd` 框架就读不到，登录永远失败。

### 8.2 首页按角色显示菜单 `templates/dashboard.html`

页面 `<html>` 需声明 `xmlns:sec="http://www.w3.org/1999/xhtml"`（项目已有），然后用 `sec:authorize` 控制：

```html
<li sec:authorize="hasRole('DOCTOR')"><a href="/doctor/patients">患者管理</a></li>
<li sec:authorize="hasRole('PATIENT')"><a href="/patient/records">我的病历</a></li>
```

效果：医生只看到医生菜单，患者只看到患者菜单，且后端 `SecurityConfig` 的 `hasRole` 也真正拦截 URL——**前端隐藏 + 后端拦截，双保险**。

### 8.3 Controller 获取当前登录用户

`UserController` 的 `/dashboard` 方法（项目已有）：

```java
@RequestMapping("/dashboard")
public String dashboard(Model model) {
    // 从 SecurityContext 拿当前认证对象
    Authentication authentication =
        SecurityContextHolder.getContext().getAuthentication();
    String userName = authentication.getName();
    String role = authentication.getAuthorities().toString();

    model.addAttribute("userName", userName);
    model.addAttribute("role", role.contains("DOCTOR") ? "Doctor" : "Patient");
    return "dashboard";
}
```

> 登录成功后，用户信息存在 `SecurityContext` 里，任何 Controller 都能用 `SecurityContextHolder` 取到——**不用自己往 session 里塞**。

---

## 阶段九：编译与验证

### 9.1 编译启动

```bash
./mvnw clean compile -DskipTests
# IDEA 里 Run 主启动类，或：
./mvnw spring-boot:run
```

启动后控制台**不应**再出现 `Using generated security password`（出现说明自定义认证没生效，框架还在用默认用户）。

### 9.2 四层验证（缺一不可）

**① 未登录访问受限资源 → 跳转登录页**

```bash
curl -s -o /dev/null -w "HTTP %{http_code} → %{redirect_url}\n" http://localhost/doctor/patients
# 期望：302 跳转到 /login
```

**② 登录页能打开**

```bash
curl -s -o /dev/null -w "HTTP %{http_code}\n" http://localhost/login
# 期望：200
```

**③ 用测试账号登录成功并跳转**

浏览器打开 `http://localhost/login`，输入 `doctor / 1234`。期望：认证成功，跳到 `/dashboard`，页面只显示医生菜单。

> 命令行可用 curl 带 cookie 验证（需先关 CSRF，本项目已关）：
> 
> ```bash
> curl -c cookie.txt -X POST http://localhost/login -d "username=doctor&password=1234" -D -
> # 期望：302 Location: /dashboard
> curl -b cookie.txt http://localhost/dashboard   # 带 cookie 访问首页
> ```

**④ 角色越权被拒**

用 `patient / 1234` 登录后访问 `/doctor/patients`，应得到 **403**（或统一错误页）——证明授权生效，不是只靠前端隐藏菜单。

### 9.3 思考题（课堂上机）

1. 把数据库里 doctor 的密码改成明文 `1234`，再登录会怎样？为什么？
2. 把 `DataBaseUserDetailsService` 里的 `"ROLE_" + role` 去掉，登录后访问 `/doctor/**` 会怎样？
3. 本项目 `register.html` 表单存在，但没有对应的注册 Controller 和加密入库逻辑。请补全：注册时用 `passwordEncoder.encode()` 加密密码后调用 `insertUser()` 入库。
4. 为什么关闭 CSRF 后表单能提交？开启 CSRF 后需要给表单加什么？

---

## 附录 A：两套登录并存——本项目最大的坑

本项目里其实有**两套登录逻辑**，学生极易混淆：

| 对比项  | 真正生效的（本实验主线）                                | 遗留的（旧代码，勿用）                               |
| ---- | ------------------------------------------- | ----------------------------------------- |
| 认证入口 | `SecurityConfig.formLogin` 拦截 `POST /login` | `UserController.login()` 里的 `/login` 方法   |
| 用户来源 | `sys_user` 表（数据库认证）                         | `UserServiceImpl.login()` 硬编码 `admin/123` |
| 密码校验 | BCrypt 自动比对哈希                               | 字符串直接比较明文                                 |
| 角色   | `sys_role` 三表联查 + `ROLE_` 前缀                | 无角色概念                                     |
| 是否生效 | ✅ 生效                                        | ❌ 被 Security 拦截，基本不会被调用                   |

**结论**：启用 Spring Security 后，`POST /login` 由框架接管，`UserController.login()` 那套手工判断形同虚设。教学时**以 `DataBaseUserDetailsService` 这条线为准**，旧的 `tb_users` 表、`admin/123` 硬编码、注释掉转发的那行，都是历史遗留，不要照着学。

## 附录 B：全程踩坑速查表

| 现象                                                     | 根因                          | 解决                                           |
| ------------------------------------------------------ | --------------------------- | -------------------------------------------- |
| `Using generated security password`                    | 自定义认证没生效，用默认用户              | 检查 `UserDetailsService` Bean 是否被扫描到          |
| `There is no PasswordEncoder mapped for the id "null"` | 没配 `PasswordEncoder` Bean   | 加 `WebAuthConfig` 的 BCrypt Bean              |
| 登录成功但 `/doctor/**` 仍 403                               | 角色没加 `ROLE_` 前缀             | `new SimpleGrantedAuthority("ROLE_" + role)` |
| 表单提交后登录永远失败                                            | 字段名不是 `username`/`password` | 改表单 `name` 属性                                |
| POST /login 返回 403                                     | CSRF 开启且表单没 token           | 教学先 `csrf.disable()`，或加隐藏 token 字段           |
| `sec:authorize` 不生效                                    | 缺 thymeleaf-extras 依赖       | 加 `thymeleaf-extras-springsecurity6`         |
| 角色规则写了不生效                                              | `.anyRequest()` 放到了前面       | 把宽泛规则移到最后                                    |
| 角色查出来为空                                                | `sys_user_role` 中间表没数据      | 补插关联记录                                       |
| 数据库密码是明文，登录失败                                          | BCrypt 校验要求哈希               | 用 `encode()` 生成哈希入库                          |
| 找不到主类 / Bean 注入失败                                      | Boot 4 依赖artifactId不兼容      | 确认 parent 3.5.9、依赖无手写 version                |

## 附录 C：核心命令清单

```bash
# 编译
./mvnw clean compile -DskipTests

# 核对依赖版本（应为 6.5.7，由父POM管理）
./mvnw dependency:tree | grep -i "spring-security"

# 未登录访问受限资源（期望 302 → /login）
curl -s -o /dev/null -w "HTTP %{http_code} → %{redirect_url}\n" http://localhost/doctor/patients

# 带 cookie 登录并访问（CSRF 已关）
curl -c cookie.txt -X POST http://localhost/login -d "username=doctor&password=1234" -D -
curl -b cookie.txt http://localhost/dashboard
```

## 附录 D：事实核验提示

| 信息                                                 | 依据                                                      | 时效性                          |
| -------------------------------------------------- | ------------------------------------------------------- | ---------------------------- |
| Spring Security 6.5.7                              | 本地 Maven 依赖，由 Boot parent 3.5.9 管理（2026-11-24 实测）       | 随 Boot 版本变动，升级后重新核对          |
| 三张表结构                                              | 由 `UserMapper.java` 的 `@Select` SQL 反推（项目未提供 `.sql` 脚本） | 以实际数据库表为准，字段名可能需微调           |
| BCrypt 哈希对应明文 `1234`                               | 用项目依赖 `spring-security-crypto 6.5.7` 的编码器实际生成           | 哈希固定可用；换密码需重新生成              |
| `sec:authorize` 需 thymeleaf-extras-springsecurity6 | 项目 `pom.xml` 已含该依赖                                      | Spring Security 5/6 后缀不同，勿混用 |

---

**实验完成标准**：能独立走完阶段一到九，实现"未登录跳登录页 → 数据库账号登录成功 → 按角色显示菜单且越权 URL 被拒"；能说清"认证与授权的区别、`ROLE_` 前缀为什么必须、密码为什么不能存明文、两套登录哪套生效"四个问题。
