# AGENTS.md — 在本仓库工作的规则

本文件是**规则**（怎么做、不能做什么）。状态与进度看 [HANDOFF.md](HANDOFF.md)，全貌看 [README.md](README.md)。
接手任何任务前，请按 **本文件 → HANDOFF.md → README.md** 的顺序读完再动手。

适用范围：整个 `D:\project\githubclone\web` 仓库（Git 分支 `main`，remote `https://github.com/dessert28/web.git`，SSH 为 `git@github.com:dessert28/web.git`）。

**主项目是 `jsj2434/`**；`demo`、`shiyan2`、`Test34`、`sy1`、`material-design-style-pack` 已归档到 `archive/`，只作留档，不再维护（2026-11-09 整理，见 HANDOFF）。

---

## 1. 硬红线（不要做）

1. **不要删除或重写** `LICENSE`、`demo.zip`、`jsj2434.rar` —— 它们是仓库历史的一部分。`demo.zip` 已于 2026-10-20 恢复为原始快照（23,952 字节），不要再把它替换成本地新打的包。
2. **不要提交构建产物或 IDE 状态**：`target/`、`.mvn/wrapper/maven-wrapper.jar`、`.idea/`、`.vs/`、`*.suo`、`*.vsidx`、`*.user`、`demo/data/`、`demo/uploads/`。根目录 `.gitignore` 已兜底，提交前仍要用 `git status` 逐条确认。
3. **不要重命名 Java 包、`groupId`/`artifactId`、模板文件名或 URL 路径**（`com.example.demo`、`com.example.shiyan2`、`com.zjsru`、`/tologin`、`/toMain`、`/shiyan1/**` 等都被代码或测试断言引用）。
4. **不要删除或“顺手精简”现有测试**。测试断言是对外契约的说明书；断言与实现不一致时，先判断**谁是对的**（参见 HANDOFF 中 `logoutEndsSession` 的历史案例），再动其中一侧。
5. **不要同时启动 `archive/demo` 与 `jsj2434`**：两者默认端口都是 8080。需要并行时显式 `--server.port=8081`。
6. **不要把演示账号当认证设计**：`demo` 与 `jsj2434` 都是 `admin/123`，密码明文存储与比较，仅供课程演示，不要在此基础上扩展用户体系或当成生产凭据。
7. **不要绕过第 2 节的环境要求直接跑 Maven**（PATH 上的 JDK 15 会失败）。
8. **不要执行 `git push --force`、不要 rebase/改写已推送历史**。当前 `main` 与 `origin/main` 同步；推送前先与用户确认（用户明确要求推送的情况除外）。
9. **不要擅自扩大改动范围**：仓库里是多个独立作业，没有共享代码，改动应当局限在目标任务所在的子目录。

## 2. 环境规范（Windows + PowerShell）

| 规则 | 内容 |
| --- | --- |
| JDK | 主项目 `jsj2434` 用 **JDK 21**。主开发机：`D:\pc\.jdks\graalvm-jdk-21.0.7`；本机（2026-11-09 实测）：`D:\project\.jdks\graalvm-jdk-21.0.7`。归档工程 `archive/shiyan2` 需 JDK 17。**跑之前一律显式设 `JAVA_HOME`**，PATH 上未必有 `java` |
| Maven | 本机**未安装** `mvn`。一律使用项目自带的 `.\mvnw.cmd`（wrapper 3.3.4 / Maven 3.9.16）。首次构建需联网下载依赖（本机 `~\.m2` 无缓存） |
| 依赖镜像 | 不要删改 `.mvn/settings.xml` 的阿里云镜像与 `.mvn/maven.config` 的 `--settings`，离线/内网环境下它们是能否解析依赖的关键 |
| MySQL | 仅 `jsj2434` 需要：MySQL 8 于 `localhost:3306`，`root/root`，库 **`javaweb`**；表结构由仓库根 [`javaweb.sql`](javaweb.sql) 创建（6 张表，自带 `CREATE DATABASE IF NOT EXISTS`，幂等可重复导入），项目内无自动建表。旧 [`mybatis.sql`](mybatis.sql) 保留作对照 |
| 编码 | 源码与 HTML 为 UTF-8；PowerShell 查看中文输出可能乱码，判断构建结果看 `BUILD SUCCESS/FAILURE`、`Tests run:` 与退出码 |
| 沙箱 | 受限文件沙箱下原地构建可能因写 `*/target/**` 被拒而失败（`AccessDeniedException`）。这属于**会话环境限制，不是项目缺陷**；请切到完全权限或在仓库外复制一份再构建 |

## 3. 构建与验证规范

改任何代码后，**必须**跑对应项目的全量测试并把结果写进交付说明。日常只维护主项目：

```powershell
# jsj2434（主项目，Java 21，需要 MySQL 8 已启动、mybatis 库已建好）—— 期望 Tests run: 1, Failures: 0
cd D:\project\githubclone\web\jsj2434
$env:JAVA_HOME = 'D:\project\.jdks\graalvm-jdk-21.0.7'   # 主开发机改成 D:\pc\.jdks\...
.\mvnw.cmd -B clean test
```

归档工程（`archive/`）仅在确实改动它们时才验证：

```powershell
# archive/demo（Java 21）
cd D:\project\githubclone\web\archive\demo
$env:JAVA_HOME = 'D:\project\.jdks\graalvm-jdk-21.0.7'
.\mvnw.cmd -B clean test

# archive/shiyan2（Java 17）
cd D:\project\githubclone\web\archive\shiyan2
$env:JAVA_HOME = 'D:\project\.jdks\graalvm-jdk-17.0.12'
.\mvnw.cmd -B clean test

# archive/Test34（Java 21）—— 可编译，无测试
cd D:\project\githubclone\web\archive\Test34
$env:JAVA_HOME = 'D:\project\.jdks\graalvm-jdk-21.0.7'
.\mvnw.cmd -B clean compile
```

- 只跑相关测试类时用 `-Dtest=UserServiceTest`，但**交付前要跑一次全量**。
- `sy1` 是 ASP.NET 空模板，需要 Windows + VS/MSBuild，本仓库**没有可用的构建路径**；涉及它时先说明这一前提，不要假装验证过。
- 结论必须区分“**我实跑过**”与“**历史报告显示**”：`target/surefire-reports` 里的报告可能对应旧提交。
- `jsj2434` 的测试会真实连接 MySQL；换机器前先确认 MySQL 已启动、`javaweb` 库及 `tb_users`/`doctor_info`/`department` 表已建好。

## 4. 代码约定

- `demo` / `jsj2434`：分层为 `controller` / `service` / `domain`（`jsj2434` 另有 `mapper`）。控制器返回模板名或 `ModelAndView`；`@ResponseBody` 直接输出正文。
- `jsj2434`（2026-11-03 覆盖回退后）：**User 模块** 走 `UserService` → `UserServiceImpl` → `UserMapper`（MyBatis 注解 SQL，查 `tb_users`）；**Doctor 模块** 走 `DoctorService` → `DoctorServiceImpl` → `DoctorMapper`（XML 映射 `DoctorMapper.xml`，查 `doctor_info`）。登录校验仍是硬编码 `admin/123`。目前**没有** `GlobalExceptionHandler`/`Result`，异常走默认处理，改 SQL 时同步检查仓库根 `mybatis.sql` 的表结构与两个 Mapper。
- `shiyan2`（已归档到 `archive/shiyan2`）：数据在 `GoodsServiceImpl` 的静态集合里；模板用 `th:field` 绑定，字段名必须与 `Goods` 属性一致。
- 新增页面/接口时，模板里的 `th:href="@{...}"`、`th:action="@{...}"` 与测试中的字符串断言（如 `action="/login" method="post"`）要保持一致。
- 依赖版本保持项目现状（Spring Boot 4.x 系列），升级前先确认 JDK 与依赖坐标（注意 Boot 4 用的是 `spring-boot-starter-webmvc`、Jackson 3 的 `tools.jackson.*`）。

## 5. Git 与提交规范

- 提交信息沿用现有风格：`feat(jsj2434): ...` / `fix: ...` / `docs: ...` / `chore: ...` / `build(Test34): ...`，涉及具体项目时带作用域；正文写清“为什么改、改了哪几处、怎么验证的”。
- 提交前：`git status`（确认没有 `target/`、`.idea/`、`.vs/`、`data/`、`uploads/`）→ 跑受影响子项目测试 → 再 `git add` 具体文件（不要 `git add -A`）。
- 一个提交只做一类事：文档、依赖/构建、业务行为、仓库清理分开提交。
- 推送前确认远端状态；仓库是个人练习仓库，远端 `origin` 当前配置为 HTTPS（无凭据时 fetch/push 会失败），SSH 通道 `git@github.com:dessert28/web.git` 可用。

## 6. 沟通与交付约定

- 交付时给出：改了什么文件、**实跑**的验证命令与结果、以及仍未消除的风险。不要只写“已完成”。
- 发现与任务无关的问题（例如测试失败、过期报告、历史包袱）时，**记录到 HANDOFF.md 而不是顺手改掉**。
- 用户使用中文，交付与说明默认用中文；代码、命令、路径保持原样。
- 不确定项目归属或改哪一侧时，先问，不要猜。
