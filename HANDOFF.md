# HANDOFF.md — 当前交接状态（2026-11-10）

> 本节是最新状态，优先于下面所有历史章节。
> 历史章节从「2026-11-03 覆盖事件」起保留供追溯，其中**状态类描述已不再准确**，不要据此操作。

## 一句话现状

主项目是 **`jsj2434/`**，其余工程已归档到 `archive/`；**`hospital-registration/`** 是 2026-11-13 新增的实验二独立子项目（:8082，39 测试，已提交推送，详见其自身文档）。
**2026-11-17 在 jsj2434 补全 Spring Security 7 认证改造**；**2026-11-20 把认证来源换成老师给的 1117 样本 `sys_user` 三表**，测试 14 → 20 全部实跑通过；改动均尚未提交（见第 0-新3 节）。

## 0-新4. 本次改动（2026-11-24）：登录统一到 Spring Security

- 按用户提供的 `AirPortal-1795488011760/UserMapper.java|xml` 接入认证：`UserMapper` 增加 `findByUsername` / `findRolesByUserId`；用其合法 MyBatis XML 替换误提交的空 `<beans>` 文件（该文件会让 `sqlSessionFactory` 解析失败、应用起不来）；`Users` 增加 `Boolean status` 映射 `enabled as status`。
- `DataBaseUserDeatilsService` 从 `JdbcTemplate` 改为注入 `UserMapper`；角色三表联查把 `r.name` 别名为 `username`，服务层拼 `ROLE_` 前缀；禁用判断 `!Boolean.TRUE.equals(status)`。类名保留原拼写以免改到配置与测试引用。
- 登录页地址从 `/toLogin` 改为 `/login`（`/toLogin` 保留兼容）；失败/注销跳 `/login?error`、`/login?logout`；删除 `UserService.login` / `UserServiceImpl.login` 的 `admin/123` 判断和 `/logout2` 提示页，模板链接同步改为 `/login`、`/logout`。
- 实跑 `cd jsj2434; $env:JAVA_HOME='D:\pc\.jdks\graalvm-jdk-21.0.7'; .\mvnw.cmd -B clean test` → Tests run: 23, Failures: 0, Errors: 0 / BUILD SUCCESS；`spring-boot:run` 冒烟：`/login`、`/toLogin` 均 200，未登录跳 `/login`，三种账号按角色跳转，注销回 `/login?logout`；临时实例已停止。`jsj2434/README.md` 已同步账号/测试数/路由。
- 改动均**尚未提交**。注意从 `jsj2434/` 目录执行 Maven，否则 `.mvn/maven.config` 的相对 `--settings` 找不到。

## 0-新3. 本次改动（2026-11-20）：jsj2434 接入 1117 样本的数据库认证

老师新给的 `1117/` 样本包含 `sys_user` / `sys_role` / `sys_user_role` 三张 RBAC 表（导出 SQL + 一份 `jsj2434` 旧快照）。核对结果：

- `mybatis` 库里**三张表已存在**，结构与样本一致（`utf8mb3`、唯一键、外键），账号 `admin`/`doctor`/`patient`、角色 `ADMIN`/`DOCTOR`/`PATIENT`、绑定 `1→1 / 2→2 / 3→3` 均已就位；
- 样本密码哈希实测对应明文 **`1234`**（对 `123` 全部不匹配），这是本次最容易被忽略的坑；
- 1117 的 Java 源码是旧快照（登录仍硬编码 `admin/123`），**没有任何 Security 配置**，真正可用的是三表数据模型。

据此改动：

- **`service/DataBaseUserDeatilsService.java`（空类 → 实现）**：`implements UserDetailsService`，用 `JdbcTemplate` 查 `sys_user`（含 `enabled`）与 `sys_user_role → sys_role`，返回 `User.withUsername(...).roles(...).disabled(!enabled)`；查不到用户抛 `UsernameNotFoundException`，不区分「账号不存在」与「密码错误」。
- **`config/WebAuthConfig.java`**：删除 `InMemoryUserDetailsManager` Bean，只保留 `BCryptPasswordEncoder` 与 `SecurityFilterChain`；由 Spring Security 自动装配 `DaoAuthenticationProvider`，避免容器里出现两个 `UserDetailsService`。
- **`controller/UserController.java`**：删除与过滤器冲突的 `/login`、`/logout` 方法（Security 已接管）；`/toLogin` 读取 `?error` / `?logout` 并翻译成页面提示，失败不再只是 URL 变化。
- **`pom.xml`**：按样本补 `spring-security-test`（本地 `7.0.7` 已缓存）。**未加** `thymeleaf-extras-springsecurity6`：本地仓库没有、与 Security 7 的兼容性未验证，页面暂未使用 `sec:` 标签。
- **`SecurityConfigTests.java`**：登录密码 `123` → `1234`；新增 6 个用例（`doctor` 数据库账号可登录、旧明文 `123` 被拒、未知账号被拒、登录页 error/logout 提示、userdata 角色映射、未知用户抛异常）。
- **`db/rbac-auth.sql`（新增，仓库根）**：按样本重建的幂等建表脚本，只把 `DROP TABLE` 改成 `CREATE TABLE IF NOT EXISTS`、跳过样本里 `id=4` 的空名脏角色；**未执行**，因为库里三表已存在，留作重装环境用。

### 实跑验证（本机 2026-11-20）

```
cd D:\project\githubclone\web\jsj2434
$env:JAVA_HOME = 'D:\pc\.jdks\graalvm-jdk-21.0.7'
.\mvnw.cmd -B -o clean test
→ Tests run: 20, Failures: 0, Errors: 0, Skipped: 0 / BUILD SUCCESS
```

`admin/1234` 能登录成功本身就是证据：旧的内存用户密码是 `123`，BCrypt 校验 `1234` 必然失败；现在 `doctor/1234` 也能登录，说明认证确实落到 `sys_user`。

### 风险 / 需知

1. **登录密码从 `123` 变成 `1234`**，账号为 `admin` / `doctor` / `patient`。旧的 `admin/123` 会失败。
2. 角色已参与授权（2026-11-20 追加）：`ADMIN` 可进用户管理；`ADMIN`/`DOCTOR` 可做医生增改；三种角色都可浏览科室与医生列表。登录后按角色跳转：`ADMIN → /selectAllUsers`、`DOCTOR → /doctorList`、`PATIENT → /departmentList`。
3. `UserService.login` 的硬编码判断、`UserController` 已删除的 `/login` 逻辑不再被调用；按 AGENTS 红线未删 `UserServiceImpl.login`。
4. `hospital-registration` 未受影响：它继续用 `tb_users` / `doctor_info`，39 个测试基线不变。

## 0-新. 本次改动（2026-11-17）：jsj2434 Spring Security 改造

接手时 `jsj2434` 的 `config/WebAuthConfig.java` 是**未写完的骨架**（`User.builder()` 处断掉，无法编译），`pom.xml` 已加 `spring-boot-starter-security`、`application.properties` 已加明文 `spring.security.user.*`，处于半成品状态。本次补全为真正可用的 Security：

- **`config/WebAuthConfig.java`（补全）**：`@EnableWebSecurity` + 三个 Bean —— `BCryptPasswordEncoder`、`InMemoryUserDetailsManager`（内存用户 `admin/123`，BCrypt 加密）、`SecurityFilterChain`。
  - 放行：`/`、`/toLogin`、`/login`、`/css/**`、`/vendor/**`、`/webjars/**`、`/favicon.ico`、`/error`；其余 `anyRequest().authenticated()`。
  - `formLogin`：loginPage `/toLogin`、processingUrl `/login`（字段 `username`/`password`）、success `/selectAllUsers`、failure `/toLogin?error`；`logout`：`/logout` → `/toLogin?logout`；CSRF 关闭（登录模板无 token）。
- **`application.properties`**：移除 `spring.security.user.name/password`（与自定义 UserDetailsService 冲突且误导），改为注释说明。
- **`DepartmentModuleTests.java`**：加 `@AutoConfigureMockMvc(addFilters = false)` —— 该类只测业务契约，不让 Security 拦截（**未改任何断言**）。
- **`SecurityConfigTests.java`（新增，6 个测试）**：走真实过滤器链，覆盖公开页、未登录重定向、登录成功/失败、带会话访问受保护路由、注销失效。
- **`jsj2434/README.md`**：技术栈、测试数（8→14）、`/login` 与 `/logout` 路由、说明同步。

### 实跑验证（本机 2026-11-17）

```
cd D:\project\githubclone\web\jsj2434
$env:JAVA_HOME = 'D:\pc\.jdks\graalvm-jdk-21.0.7'
.\mvnw.cmd -B -o clean test
→ Tests run: 14, Failures: 0, Errors: 0, Skipped: 0 / BUILD SUCCESS
```

改造前同命令为 **Tests run: 8, Failures: 4**（4 个 Department MockMvc 测试被 401 拦截）；补全后 14/14 通过。

### 行为变化 / 风险（需知）

1. **全站（除公开页）现在需要登录**，未登录访问业务路由 302 到 `/toLogin` —— 与旧版「随便访问」不同，属改造预期。
2. **`/logout` 行为被覆盖**：旧的 GET `/logout` 返回假「张三」JSON，现在被 Security LogoutFilter 前置拦截，销毁会话并重定向 `/toLogin?logout`。`UserController.logout()` 同名方法不再生效（源码保留未删）。
3. `UserServiceImpl.login` 的硬编码 `admin/123` 判断、`UserController.login` 仍是死代码；按 AGENTS 红线不删历史演示代码，仅注明。
4. CSRF 关闭（课程演示口径）；若将来上 CSRF 需给 `login.html` 表单加 token。
5. 改动**尚未提交、未推送**；按 AGENTS 第 5 节提交前先 `git status` 确认无 `target/`、IDE 状态。
6. 工作区里的 `jsj2434/domain/Users.java`（新增 `private String id;`）**非本次改造内容**，本会话未修改该文件，提交时请单独甄别归属。另有根目录下 `hospital-registration.zip`、`zero-one-his2607*` 等未跟踪文件，与本次无关。

## 0. 本次改动（2026-11-10）：Department 模块

按用户提供的截图新增「科室 + 医生」一对多关联查询。截图中是 `Department.java` / `DepartmentMapper.java` / `DepartmentMapper.xml`（`<resultMap>` + `<collection>`），**这套代码原本在仓库里完全不存在**，属新增而非回退。

### 0.1 新增文件

| 文件 | 作用 |
| --- | --- |
| `domain/Department.java` | `id`/`deptName`/`location`/`deptPhone`/`List<Doctor> doctorList` |
| `mapper/DepartmentMapper.java` | `queryAllDepartments()`、`queryDepartmentInfoAndDoctorsByDeptId(@Param("deptId"))` |
| `resources/mapper/DepartmentMapper.xml` | `<resultMap>` + `<collection notNullColumn="doctor_id">`，`doctor_department LEFT JOIN doctor_info` |
| `service/DepartmentService.java` / `DepartmentServiceImpl.java` | 传参守卫：`deptId == null` 直接返回 `null` |
| `controller/DepartmentController.java` | `/queryDepartmentInfoAndDoctorsByDeptId`（JSON）、`/departmentList`（页面） |
| `templates/departmentList.html` | 每个科室一张卡片 + 该科室医生表格，空科室显示「该科室暂无医生」 |
| `test/.../DepartmentModuleTests.java` | 7 个测试 |

### 0.2 修改文件

| 文件 | 改动 |
| --- | --- |
| `domain/Doctor.java` | 追加 `Integer departmentId`、`Department dept`（原 6 个字段与 `department` 文本列**未动**，避免断既有模板） |
| `mybatis.sql` | `doctor_department` 加 `dept_phone` 列 + 6 条科室种子数据；`doctor_info` 后追加按 `department` 文本回填 `department_id` 的 `UPDATE` |
| `templates/doctorList.html`、`doctorQuery.html` | 导航加「科室与医生」入口 |
| `jsj2434/README.md` | 路由表、代码结构、测试数、说明同步 |

### 0.3 实跑验证（本机，2026-11-10）

```
cd D:\project\githubclone\web\jsj2434
$env:JAVA_HOME = 'D:\pc\.jdks\graalvm-jdk-21.0.7'
.\mvnw.cmd -B clean test
→ Tests run: 8, Failures: 0, Errors: 0, Skipped: 0 / BUILD SUCCESS
```

`spring-boot:run` 冒烟：`/departmentList` 200 渲染 6 个科室 8 名医生（儿科 3 人）；`/queryDepartmentInfoAndDoctorsByDeptId?deptId=1` 返回嵌套 JSON；`deptId=9999` 与不传 `deptId` 均 200 空正文（**不 500**）；`/`、`/toLogin`、`/doctorList`、`/queryAllDoctors` 未受影响。测试用的临时科室已自动清理，`doctor_department` 回到 6 行。

### 0.4 两个实现坑（已修）

1. `queryAllDepartments` 用 `resultType` 自动映射时，`dept_name`/`dept_phone` 下划线列**装不上驼峰属性**，`deptName`/`deptPhone` 会是 `null`。已在 SQL 里改成显式别名。
2. `LEFT JOIN` 在「科室没有医生」时会产出一行 doc 列全 NULL 的记录，MyBatis 会把它当成一条**字段全 null 的 Doctor**，导致 `doctorList.size() == 1`、模板空状态判断失效。已用 `<collection notNullColumn="doctor_id">` 挡住，并补了回归测试。

### 0.5 数据库变更（需要手动同步）

`mybatis.sql` 已更新，但本机库是**直接 ALTER + INSERT + UPDATE** 落地的。换机器或用旧库时，只重导 `mybatis.sql` 会 `DROP` 重建全部 40 张表，要动生产数据请单独执行等价的三条 SQL（`ALTER TABLE ... ADD COLUMN dept_phone`、科室 `INSERT`、`department_id` 回填 `UPDATE`）。

## 1. 上一次整理（2026-11-09）

按用户要求「以 jsj2434 为主项目，其余归档」，完成了以下改动。

### 1.1 归档（`git mv`，历史保留）

| 原路径 | 新路径 |
| --- | --- |
| `demo/` | `archive/demo/` |
| `shiyan2/` | `archive/shiyan2/` |
| `Test34/` | `archive/Test34/` |
| `sy1/` | `archive/sy1/` |
| `material-design-style-pack/` + `material-design-style-pack.zip` | `archive/` 下同名 |

全部 67 项在 `git status` 中显示为 `R`（重命名），`git log --follow` 可追溯历史。**没有改写任何已推送历史。**

未移动：`bak/`（自带只读规则，且是回滚依据，移动会破坏文档路径）、`mybatis.sql`（主项目要用，留在根）、`demo.zip` / `jsj2434.rar` / `LICENSE`（AGENTS 硬红线）。

### 1.2 清理

- 删除根目录 `实验报告模板.doc` —— 与 `archive/shiyan2/实验报告模板.doc` 哈希完全相同（当年因文件被占用删不掉，本轮 ACL 修复后成功删除）。
- 删除 `archive/shiyan2/addGoods.html.png`（1.53 MB）—— 与 `goodsList.html.png` 重复，README 只引用后者。
- `git rm --cached archive/sy1/sy1.csproj.user` —— IDE 状态文件，本地保留，`.gitignore` 已覆盖 `*.user`。

### 1.3 文档

- 根 `README.md` 重写：首屏即 jsj2434，归档工程压缩成一张表。
- `AGENTS.md`：更新路径、JDK 路径、远端地址；补记本机与主开发机的 JDK 路径差异。
- `jsj2434/README.md`：修正指向归档目录的相对链接。

## 2. 环境事实（2026-11-10 实测，修正此前错误）

历次文档里的环境描述与实际不符，本节为**当前实机**核实结果（本机为主开发机）：

| 项 | 旧说法 | 实测（2026-11-10） |
| --- | --- | --- |
| JDK 路径 | 本机为 `D:\project\.jdks\` | **实为 `D:\pc\.jdks\graalvm-jdk-21.0.7`**（另有 `graalvm-jdk-17.0.12`）；`D:\project\.jdks` 不存在 |
| PATH 上的 java | 无 `java` | **有，但是 JDK 15.0.1**（`C:\Program Files\Common Files\Oracle\Java\javapath\java.exe`），跑 Boot 4 会失败。`$env:JAVA_HOME` 默认**为空**，必须每次显式设置 |
| 远端 | HTTPS，无凭据时失败 | 目前 `origin` 为 `git@github.com:dessert28/web.git`（SSH），可直接 fetch/push |
| Maven 缓存 | 无 Spring Boot 依赖 | 已有缓存，离线可构建 |
| 建表 | 曾称「启动时自动执行 mybatis.sql」 | **仍为错**。`application.properties` 无 `spring.sql.init.*`，必须手工导入根 `mybatis.sql`；库名是 `mybatis` |
| PowerShell | 未记录 | 本机为 **PowerShell 5.1**（无 `pwsh`）；`System.Drawing.Rectangle` 需用 `[System.Drawing.Rectangle]::new(...)` 而非 `New-Object` |

## 3. 提交状态与待办

本次整理的改动**已在工作区完成，尚未提交、未推送**。

接手后如果要落地，建议按 AGENTS.md 第 5 节分两笔提交：

| # | 提交信息 | 内容 |
| --- | --- | --- |
| A | `chore: archive the practice projects under archive/` | 67 项 `git mv` + 删除重复文件 + 取消跟踪 `sy1.csproj.user` |
| B | `docs: make jsj2434 the main project and refresh handoff` | `README.md`、`AGENTS.md`、`HANDOFF.md`、`jsj2434/README.md` |

> 提交前注意：本会话的沙箱**拦截对 `.git` 的写入**（`Unable to create '.git/index.lock': Permission denied`），即使 `.git` 的 ACL 已修好也如此。所有 `git mv` / `git rm` / `git commit` 都必须在**放开权限的执行**或用户自己的终端里完成。

## 4. 已知问题（只记录，未顺手修改）

按 AGENTS 第 6 节，以下问题**不在本次整理范围内**：

1. **测试覆盖仍然偏薄**：`jsj2434` 现有 8 个测试，其中 7 个只覆盖新增的 Department 模块；**User 与 Doctor 的全部路由依旧零自动化覆盖**，改动只能靠人工验证。这是仓库最大的质量风险。`archive/demo`、`archive/shiyan2` 各仍只有 1 个 `contextLoads`，`archive/Test34` 无测试。
2. **`archive/sy1` 无法构建**：ASP.NET 4.7.2 空模板，需 Windows + VS/MSBuild，本仓库没有构建路径。
3. **课程遗留行为**（按 AGENTS 红线第 6 条保留现状）：登录不写 session、`/logout` 返回假的「张三」JSON、密码明文比较与存储、数据源 `root/root` 写在配置文件里。
4. **`bak/` 体积**：55 个文件、约 1.79 MB，是仓库最大体积项之一。是否长期保留待用户决定，本轮未动。
5. **`archive/login-demo/`**：2026-11-09 在仓库根发现一个**未被 git 跟踪**的目录，整棵树 **0 个文件**、53 个空目录（`.idea`、`.mvn/wrapper`、`db`、`_recon`、`_run`、`src/main/java/com/example/logindemo/{config,security,user,web}`、`target/` 等），来源不明。按用户要求「所有东西都留着」，已移入 `archive/login-demo/` 并以 `README.md` + `.gitkeep` 留档（Git 不跟踪空目录，故目录结构本身无法入库）。
6. **`DoctorController.queryDoctorById` 用裸 `int id`**：不传参数实际返回 **500**（此前记为 400），因为 `int` 无法接受缺参。新增的 `DepartmentController` 已改用包装类型 `Integer` + `required = false` 规避同类问题；Doctor 侧未动。
7. **`doctor_info` 有两套科室表达**：`department`（varchar 文本）与 `department_id`（外键，2026-11-10 起已回填）。关联查询只认 `department_id`，新增医生时若只填文本列会导致关联查询查不到该医生。
8. **JSON 中文双重编码**：`/queryAllDoctors` 等接口返回中文呈 mojibake（`application.properties` 的数据源参数含 `characterEncoding=utf-8`，但响应体未按 UTF-8 输出）。Department 新接口同样存在，属历史问题，未在本轮修。

## 5. 历史：2026-11-03 覆盖事件及之后

> 以下为历史记录。**注意**：第 7 节「本轮未提交的 git 改动一览」所述改动后续已被提交并推送（`13f5bc6` → e9fa66d → 08c418c → 387288c → cf24f32），不再是未提交状态。

2026-11-03 上午按 `jsj2434.rar`（课程原始压缩包）覆盖了 `jsj2434/`，把上个版本（18 测试、全局异常、全量建表）回退成课程原始实现，覆盖前完整快照在 [`bak/jsj2434-20261103-100731/`](bak/jsj2434-20261103-100731/MANIFEST.md)。

覆盖后陆续做了三次推进：

| 提交 | 内容 |
| --- | --- |
| `13f5bc6` | 覆盖回退本身 |
| `e9fa66d` | 页面改 Material Design，vendor 资产全离线 |
| `08c418c` | Doctor 增加单条件查询（`<choose>`）、动态更新（`<set>`）、批量查询（`<foreach>`） |
| `387288c` | 入库 Material Design 风格包与回滚快照 |
| `cf24f32` | 同步 jsj2434 README |

当前 jsj2434 的实际能力（已按代码核实）：

- **User**：`/` `/toLogin` `/login` `/toMain` `/logout` `/logout2` `/test/{name}/{password}` `/selectAllUsers` `/queryUserById/{id}` `/queryUserByNameAndPassword/{u}/{p}`；登录成功 `forward:/selectAllUsers`。
- **Doctor**：`/queryAllDoctors` `/doctorList` `/queryDoctorById` `/addDoctor` `/doctorQuery` `/queryDoctorsByCondition` `/queryDoctorsBySingleCondition` `/updateDoctor` `/queryDoctorsByIds`。
- **异常处理**：`GlobalExceptionHandler`/`Result` 已删，异常走 Spring Boot 默认处理。
- **测试**：仅 `Jsj2434ApplicationTests.contextLoads`。

## 6. 关键路径速查

| 项 | 值 |
| --- | --- |
| 仓库 / 远端 | `D:\project\githubclone\web` / `github.com/dessert28/web`（HTTPS），分支 `main` |
| 主项目 | `jsj2434/`（`com.zjsru`，端口 8080，账号 `admin/123`） |
| JDK 21 | 本机 `D:\project\.jdks\graalvm-jdk-21.0.7`；主开发机 `D:\pc\.jdks\graalvm-jdk-21.0.7` |
| JDK 17 | 本机 `D:\project\.jdks\graalvm-jdk-17.0.12`（仅归档的 shiyan2 用） |
| Maven | 无全局 `mvn`，统一 `.\mvnw.cmd` |
| MySQL | `localhost:3306`，`root/root`，库 **`mybatis`**，用根 `mybatis.sql` 手工导入 |
| 只读备份区 | `bak/`（只允许读取和新建，不允许修改） |
