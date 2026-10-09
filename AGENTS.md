# AGENTS.md — 在本仓库工作的规则

本文件是**规则**（怎么做、不能做什么）。状态与进度看 [HANDOFF.md](HANDOFF.md)，全貌看 [README.md](README.md)。
接手任何任务前，请按 **本文件 → HANDOFF.md → README.md** 的顺序读完再动手。

适用范围：整个 `D:\project\githubclone\web` 仓库（Git 分支 `main`，remote `git@github.com:dessert28/web.git`）。

---

## 1. 硬红线（不要做）

1. **不要删除或重写** `LICENSE`、`demo.zip`、`jsj2434.rar` —— 它们是仓库历史的一部分。
2. **不要提交构建产物或 IDE 状态**：`target/`、`.mvn/wrapper/maven-wrapper.jar`、`.idea/`、`.vs/`、`*.suo`、`*.vsidx`、`demo/data/`、`demo/uploads/`。仓库根**没有** `.gitignore`，各子项目自己的 `.gitignore` 才是防线，提交前用 `git status` 逐条确认。
3. **不要重命名 Java 包、`groupId`/`artifactId`、模板文件名或 URL 路径**（`com.example.demo`、`com.zjsru`、`/api/**`、`/uploads/**`、`/toLogin`、`/main` 等都被代码或测试断言引用）。
4. **不要删除或“顺手精简”现有测试**。测试断言是对外契约的说明书；断言与实现不一致时，先判断**谁是对的**（见 HANDOFF 中的 `logoutEndsSession` 案例），再动其中一侧。
5. **不要同时启动 demo 与 jsj2434**：两者默认端口都是 8080。需要并行时显式 `--server.port=8081`。
6. **不要把 `jsj2434` 的 `admin/123` 当成可用的认证设计**，也不要把它当作生产凭据依据；它是教学演示用的硬编码账号。
7. **不要绕过步骤 3 的环境要求直接跑 Maven**（PATH 上的 JDK 15 会失败，见第 3 节）。
8. **不要执行 `git push --force`、不要 rebase/改写已推送历史**。当前 `main` 领先 `origin/main` 5 个提交，推送前先与用户确认。
9. **不要擅自扩大改动范围**：本仓库是多个独立作业的合集，没有共享代码，改动应当局限在目标任务所在的子目录。

## 2. 环境规范（Windows + PowerShell）

| 规则 | 内容 |
| --- | --- |
| JDK | 跑 Maven **必须**先设 `JAVA_HOME`：`demo` → `D:\pc\.jdks\graalvm-jdk-17.0.12`；`jsj2434` / `Test34` → `D:\pc\.jdks\graalvm-jdk-21.0.7`。PATH 上的 `java` 是 **15.0.1**，不可用于构建 |
| Maven | 本机**未安装** `mvn`。一律使用项目自带的 `.\mvnw.cmd`（wrapper 3.3.4，发行版 3.9.16 已缓存） |
| 依赖镜像 | 不要删改 `.mvn/settings.xml` 的阿里云镜像与 `.mvn/maven.config` 的 `--settings`，离线/内网环境下它们是能否解析依赖的关键 |
| 编码 | 源码与 HTML 为 UTF-8；在 PowerShell 里查看含中文的输出可能乱码，判断构建结果看 `BUILD SUCCESS/FAILURE`、`Tests run:` 与退出码，不要靠乱码文本判断 |
| 沙箱 | 若会话运行在受限文件沙箱中，直接构建可能因写 `*/target/**` 被拒而失败（`AccessDeniedException`）。这属于**会话环境限制，不是项目缺陷**；请在仓库根复制一份再构建，或先请用户放开权限。详见 HANDOFF 第 6 节 |

## 3. 构建与验证规范

改任何代码后，**必须**跑对应子项目的全量测试并把结果写进交付说明：

```powershell
# demo（Java 17）—— 期望 Tests run: 9, Failures: 0, Errors: 0
cd D:\project\githubclone\web\demo
$env:JAVA_HOME = 'D:\pc\.jdks\graalvm-jdk-17.0.12'
.\mvnw.cmd -B test

# jsj2434（Java 21）—— 期望 Tests run: 12, Failures: 0（当前 HEAD 实际是 11 passed / 1 failed）
cd D:\project\githubclone\web\jsj2434
$env:JAVA_HOME = 'D:\pc\.jdks\graalvm-jdk-21.0.7'
.\mvnw.cmd -B test
```

- 只跑相关测试类时用 `-Dtest=UserControllerTests`，但**交付前要跑一次全量**。
- `Test34`、`sy1` 目前**没有可用的构建/运行路径**（前者缺 Wrapper 与系统 Maven，后者是空模板）；若任务涉及它们，先说明这一前提，不要假装验证过。
- 不要改动 `run-tests.bat` 并声称它能用：它是 bash 脚本却带 `.bat` 后缀，且调用未安装的 `mvn`。要么改成真正的 `mvnw.cmd` 调用，要么在文档里标注不可用。
- 结论必须区分“**我实跑过**”与“**历史报告显示**”：`target/surefire-reports` 里的报告可能对应旧提交（demo 的报告就与当前源码不符）。

## 4. 代码约定

- `demo`：分层为 `controller` / `service` / `model` / `config`；持久化是**本地 JSON 文件**（无数据库），新增写操作要保持 `synchronized` + 原子写入 + 失败回滚的既有风格；错误以 `IllegalArgumentException` 抛出、由控制器转成页面提示或 `ApiResult.fail`。
- `jsj2434`：认证交给 Spring Security（`SecurityConfig` + `UserService implements UserDetailsService`），页面用 Thymeleaf + `static/css/app.css`；改安全规则时同步检查 `UserControllerSecurityTest` 与 `UserPageTest`。
- 新增页面/接口时，模板里的 `th:href="@{...}"` 与测试中的字符串断言要保持一致（`UserPageTest` 就按 `action="/logout"`、`method="post"` 这类字面量断言）。
- 依赖版本保持项目现状（Spring Boot 4.x 系列），升级前先确认 JDK 与依赖坐标（注意 Boot 4 用的是 `spring-boot-starter-webmvc`、Jackson 3 的 `tools.jackson.*`）。

## 5. Git 与提交规范

- 提交信息沿用现有风格：`feat(jsj2434): ...` / `fix: ...` / `chore: ...` / `build(jsj2434): ...`，涉及具体项目时带作用域。
- 提交前：`git status`（确认没有 `target/`、`.idea/`、`.vs/`、`data/`、`uploads/`）→ 跑受影响子项目测试 → 再 `git add` 具体文件（不要 `git add -A`）。
- 推送前必须让用户确认：`main` 领先 `origin/main` 5 个提交（jsj2434 相关工作尚未推送）。
- 不要为了“干净”而清理已入库的 `.vs/`、`.idea/` 历史文件——那是独立的任务，需要单独说明影响。

## 6. 沟通与交付约定

- 交付时给出：改了什么文件、**实跑**的验证命令与结果、以及仍未消除的风险。不要只写“已完成”。
- 发现与任务无关的问题（例如测试失败、过期报告、历史包袱）时，**记录到 HANDOFF.md 而不是顺手改掉**。
- 用户使用中文，交付与说明默认用中文；代码、命令、路径保持原样。
- 不确定项目归属或改哪一侧时，先问，不要猜。
