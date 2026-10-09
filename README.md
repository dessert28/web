# web — 多项目练习仓库（总览）

> **这是什么**：一个 Git 仓库，里面并排放了 4 个互不相关的练习/作业项目，外加 2 个早期快照压缩包和一份 GPL-3.0 许可证。
> 各项目之间**没有**共享代码、父子模块或统一构建，只是一个存放目录，请按“独立项目”对待。
>
> **交接入口**：[AGENTS.md](AGENTS.md)（规则与红线）→ [HANDOFF.md](HANDOFF.md)（当前状态、已知问题、下一步）→ 本文（全貌）。

| 项目 | 位置 | 技术栈 | 状态 |
| --- | --- | --- | --- |
| 用户注册/登录示例 | `demo/` | Spring Boot 4.1.1 · Java 17 · Thymeleaf · BCrypt | ✅ 完成，**9/9 测试通过**（2026-10-09 实测） |
| 安全登录站点 | `jsj2434/` | Spring Boot 4.0.8 · Java 21 · Spring Security · Lombok | ⚠️ 功能完成，**12 个测试中 1 个失败** |
| Maven 空壳工程 | `Test34/` | Java 21 · mysql-connector-j 8.4.0 | ⬜ 只有 Hello World，无 Maven Wrapper |
| ASP.NET 空模板 | `sy1/` | .NET Framework 4.7.2 · IIS Express · Docker | ⬜ 模板未写任何页面 |
| 早期快照 | `demo.zip`、`jsj2434.rar` | — | 📦 与当前目录内容**不一致**，勿当源码用 |
| 许可证 | `LICENSE` | GPL-3.0 全文 | — |

---

## 1. 目录地图

```
D:\project\githubclone\web\        ← git 仓库根（remote: git@github.com:dessert28/web.git，分支 main）
├── demo\                          Spring Boot 用户注册/登录示例（Java 17）
│   ├── src\main\java\com\example\demo\
│   │   ├── DemoApplication.java          启动类
│   │   ├── controller\UserController.java     页面：登录/注册/主页/退出
│   │   ├── controller\UserApiController.java  JSON：/api/register|login|me|logout
│   │   ├── service\UserService.java           校验 + BCrypt + JSON 持久化
│   │   ├── model\{UserAccount,UserProfile,Users,ApiResult}.java
│   │   └── config\WebConfig.java              映射 /uploads/** 静态资源
│   ├── src\main\resources\{application.properties, templates\, static\}
│   ├── src\test\java\com\example\demo\    4 个测试类 / 9 个用例
│   ├── .mvn\{settings.xml, maven.config, wrapper\}   阿里云镜像 + Maven Wrapper
│   ├── mvnw / mvnw.cmd / pom.xml / run-tests.bat
│   └── target\                   构建产物（已被 .gitignore 忽略）
├── jsj2434\                       Spring Security 登录站点（Java 21）
│   ├── src\main\java\com\zjsru\
│   │   ├── Jsj2434Application.java        启动类
│   │   ├── config\SecurityConfig.java     安全规则（见 3.2）
│   │   ├── controller\UserController.java 3 个页面路由
│   │   ├── service\UserService.java       内存 UserDetailsService（硬编码 admin）
│   │   └── domain\Users.java              领域对象（当前未被登录流程使用）
│   ├── src\main\resources\templates\{index,login,main}.html + static\css\app.css
│   ├── src\test\java\com\zjsru\           4 个测试类 / 12 个用例
│   └── target\jsj2434-0.0.1-SNAPSHOT.jar  已打包的可执行 jar（2026-09-29）
├── Test34\                        Maven 空壳：pom.xml + Main.java + .mvn\.gitkeep（无 mvnw）
├── sy1\                           VS2022 ASP.NET Web 应用模板（.NET Framework 4.7.2）+ Docker 支持
├── demo.zip                       53 项的早期 demo 快照（旧包结构，与 demo\ 不一致）
├── jsj2434.rar                    早期 jsj2434 快照（本机无解压工具，内容未核对）
├── LICENSE                        GPL-3.0
├── README.md / AGENTS.md / HANDOFF.md   本套交接文档
├── .git\ / .gitignore（无，仓库根没有 .gitignore）
└── .acl-recovery\                 2026-10-09 会话修复 Windows 文件权限时的备份与回滚脚本（见 HANDOFF 第 6 节；不属于项目，可删）
```

## 2. 项目详情

### 2.1 `demo/` — 用户注册 / 登录示例

| 项 | 说明 |
| --- | --- |
| 技术栈 | Spring Boot **4.1.1**（parent）、Java **17**、spring-boot-starter-webmvc + thymeleaf、spring-security-crypto（只用 BCrypt，未引入完整 Spring Security） |
| 启动类 | `com.example.demo.DemoApplication` |
| 默认端口 | 8080（`application.properties` 未覆盖） |
| 数据存储 | 进程工作目录下的 `data/users.json` + `uploads/avatars/`，由 `app.storage-root=.` 决定；两项均被 `.gitignore` 忽略 |

**页面路由**（`UserController`）

| 方法 | 路径 | 行为 |
| --- | --- | --- |
| GET | `/`、`/login` | 登录页；支持 `?error` / `?registered` / `?logout` 三种提示 |
| POST | `/login` | 校验成功后把用户名放进 session（key `LOGIN_USERNAME`），跳 `/home` |
| GET/POST | `/register` | 注册；失败时回填用户名/邮箱并显示错误 |
| GET | `/home` | 未登录跳 `/login`；已登录渲染个人主页 |
| GET/POST | `/logout` | 销毁 session，跳 `/login?logout` |

**JSON 接口**（`UserApiController`，前缀 `/api`）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/register` | 请求体 `Users`（`avatarBase64` 支持 `data:` 前缀），返回 `ApiResult` |
| POST | `/api/login` | 成功后写入同一个 session 属性 |
| GET | `/api/me` | 未登录返回 `success=false` |
| POST | `/api/logout` | 退出 |

**业务规则**（`UserService`）：用户名 3–20 位、邮箱正则且 ≤100 字符、密码 ≥6 位、两次密码一致、用户名/邮箱**不区分大小写**查重；头像 ≤2 MB 且按**魔数**判定 PNG/JPG/GIF/WebP（不信任扩展名），存为 `UUID.扩展名`；`users.json` 用“临时文件 + `ATOMIC_MOVE`”原子写入，写失败会回滚内存与头像文件；所有写方法 `synchronized`。密码只存 BCrypt 哈希。

**测试**（4 个类 / 9 个用例，2026-10-09 实测全绿）

| 测试类 | 用例数 | 覆盖 |
| --- | --- | --- |
| `DemoApplicationTests` | 1 | 上下文加载 |
| `UserControllerTests` | 3 | 页面注册→登录→主页→退出全流程、重复邮箱/密码不一致、头像上传 |
| `UserApiTests` | 2 | JSON 注册/登录/me/退出、非法注册与错误密码 |
| `UserServiceTests` | 3 | 密码哈希落盘与重载、重复/非法邮箱、头像字节校验（用 `@TempDir`） |

> ⚠️ `run-tests.bat` 名字像批处理，内容其实是 **bash 脚本**（`#!/bin/bash` 且调用 `mvn`），在 Windows 上不能直接运行；请用下面的 `mvnw.cmd` 命令。

### 2.2 `jsj2434/` — Spring Security 登录站点

| 项 | 说明 |
| --- | --- |
| 技术栈 | Spring Boot **4.0.8**、Java **21**、thymeleaf + webmvc + **spring-boot-starter-security**、lombok（optional） |
| 启动类 | `com.zjsru.Jsj2434Application` |
| 默认端口 | 8080 |
| 演示凭据 | 用户名 `admin`，密码 `123`（`UserService` 中硬编码的 BCrypt 哈希，**仅供演示**） |

**`SecurityConfig` 规则**

| 规则 | 内容 |
| --- | --- |
| 放行 | `/`、`/toLogin`、`/css/**`、`/error` |
| 其余 | 一律需要认证 |
| 表单登录 | `loginPage=/toLogin`，提交 `POST /login`，成功 `/main`，失败 `/toLogin?error` |
| 退出 | `POST /logout`（CSRF 校验），成功后跳 **`/toLogin?logout`**，销毁 session 并删除 `JSESSIONID` |

**页面**：`index.html`（欢迎页）→ `login.html`（登录表单；`param.logout` 非空时隐藏表单并显示“已安全退出”）→ `main.html`（需登录，含 `POST /logout` 表单）；共用 `static/css/app.css`。

**测试**（12 个用例）：`UserControllerSecurityTest`(7)、`UserPageTest`(2)、`Jsj2434ApplicationTests`(1)、`UserServiceTest`(2)。

> ❗ **当前 HEAD 下 12 个用例有 1 个失败**：`UserControllerSecurityTest.logoutEndsSession` 断言 `redirectedUrl("/")`，但配置返回 `/toLogin?logout`。最后一个提交（41986fc）只改了测试断言与 `login.html`，没有同步配置。详见 [HANDOFF.md](HANDOFF.md)。

### 2.3 `Test34/` — Maven 空壳工程

- `pom.xml`：`org.example:Test34:1.0-SNAPSHOT`，`maven.compiler.source/target=21`，唯一依赖 `com.mysql:mysql-connector-j:8.4.0`。
- 唯一源码 `src/main/java/org/example/Main.java`：IDEA 模板生成的 `Hello and welcome!` + 打印 1..5；**没有任何代码连接 MySQL**。
- **没有 `mvnw`/`mvnw.cmd`**（只有 `.mvn/.gitkeep`），而本机 `mvn` 未安装 → 现状下无法构建，需要先补 Wrapper 或装 Maven。
- 提交里还带着 `.idea/` 配置（含 Copilot 迁移文件）。

### 2.4 `sy1/` — ASP.NET Web 应用模板（空）

- VS2022（`Visual Studio Version 17`）的 **ASP.NET Web Application (.NET Framework 4.7.2)**，带 Docker 支持与 IIS Express 配置（`https://localhost:44324/`）。
- `<Compile>` 只有 `Properties/AssemblyInfo.cs`，`<Content>` 只有 `Web.config`：**没有 .aspx / Global.asax / Site.Master 等任何页面**，等价于新建后未开发的模板，无需运行。
- `Dockerfile` 基于 `mcr.microsoft.com/dotnet/framework/aspnet:4.8-windowsservercore-ltsc2019`（Windows 容器；镜像 4.8 与项目的 targetFramework 4.7.2 并不完全对应）。
- ⚠️ `.vs/`（含 `v17/.suo`、`FileContentIndex/*.vsidx`、`applicationhost.config`）已被**提交进 Git**；`.dockerignore`、`.gitignore` 只忽略 `bin/obj/packages`。建议后续从版本库移除 IDE 状态文件。

### 2.5 压缩包与许可证

- `demo.zip`（23,952 B，53 项）：早期 demo 快照，包结构是 `com/example/controller/UserController.java`、没有 service/model 分层，`login.html` 只有 139 B → **与 `demo/` 不是同一份代码**。
- `jsj2434.rar`（17,134 B）：推测为早期 jsj2434 快照；本机未安装 7-Zip/WinRAR，**内容未核对**。
- `LICENSE`：GPL-3.0 全文（各子项目未单独声明许可）。

## 3. 本机工具链（2026-10-09 核对）

| 组件 | 现状 |
| --- | --- |
| `java`（PATH） | Oracle **JDK 15.0.1**（`C:\Program Files\Common Files\Oracle\Java\javapath\java.exe`）——**过旧，两个 Spring Boot 项目都不能用它构建** |
| `JAVA_HOME` | **未设置** |
| 可用 JDK | `D:\pc\.jdks\graalvm-jdk-17.0.12`（给 demo）、`D:\pc\.jdks\graalvm-jdk-21.0.7`（给 jsj2434 / Test34）；另有 `C:\Users\dessert\.jdks\corretto-18.0.2` |
| `mvn` | **未安装**，不在 PATH |
| Maven Wrapper | `demo`、`jsj2434` 各有 `mvnw.cmd`（wrapper 3.3.4，`distributionType=only-script`，无需 jar）；发行版 **apache-maven-3.9.16** 已缓存在 `C:\Users\dessert\.m2\wrapper\dists\` |
| 本地仓库 | `C:\Users\dessert\.m2\repository` 已预热 |
| 依赖镜像 | 两个项目的 `.mvn/settings.xml` 都把 `*` 镜像到 `https://maven.aliyun.com/repository/public`，并由 `.mvn/maven.config` 自动加载（`--settings=.mvn/settings.xml`） |
| IDE | IntelliJ IDEA 2025.3.2（`D:\pc\JetBrains`）；`.idea/misc.xml` 记录的 SDK：demo=`21`(languageLevel 17)、jsj2434=`graalvm-jdk-21`、Test34=`graalvm-21` |

## 4. 常用命令（PowerShell）

```powershell
# demo：跑测试（JDK 17）
cd D:\project\githubclone\web\demo
$env:JAVA_HOME = 'D:\pc\.jdks\graalvm-jdk-17.0.12'
.\mvnw.cmd test                # 预期：Tests run: 9, Failures: 0

# demo：启动（http://localhost:8080/ → 登录页）
.\mvnw.cmd spring-boot:run

# jsj2434：跑测试（JDK 21）
cd D:\project\githubclone\web\jsj2434
$env:JAVA_HOME = 'D:\pc\.jdks\graalvm-jdk-21.0.7'
.\mvnw.cmd test                # 现状：Tests run: 12, Failures: 1（见 HANDOFF）

# jsj2434：启动（http://localhost:8080/ ，登录 admin / 123）
.\mvnw.cmd spring-boot:run
```

> 两个应用都占用 **8080**，不要同时启动；确需并行时用 `--server.port=8081` 覆盖。
> 没有 `JAVA_HOME` 时 `mvnw` 会退回 PATH 上的 JDK 15，构建会以编译错误告终。

## 5. 已知坑（速查）

1. **JDK 版本**：demo 要 17、jsj2434/Test34 要 21；PATH 上的 15.0.1 不合格，必须显式设 `JAVA_HOME`。
2. **没有全局 Maven**：一律用项目自带 `mvnw.cmd`；`Test34` 没有 Wrapper，先补。
3. **`run-tests.bat` 是 bash 脚本**，Windows 下不可用（且它调用未安装的 `mvn`）。
4. **`target/surefire-reports` 可能是过期报告**：demo 的历史报告只有 3 个测试类（缺 `UserServiceTests`）且方法数与当前源码不符，**以实跑结果为准**。
5. **端口冲突**：两个应用默认都是 8080。
6. **沙箱写入限制**：在受限文件沙箱的会话里，直接构建会往 `demo/target`、`jsj2434/target` 写文件而被拒绝（`AccessDeniedException`）；可先在仓库根建一份副本再构建（详见 [HANDOFF.md](HANDOFF.md) 第 6 节）。
7. **`sy1` 的 `.vs/` 已入库**；`Test34` 的 `.idea/` 也已入库，属于可清理的历史包袱。
8. **归档包与目录不同源**：`demo.zip` / `jsj2434.rar` 是更早的快照，改代码时不要以它们为准。

## 6. 文档与协作

- [AGENTS.md](AGENTS.md) — 在本仓库里工作必须遵守的规则与红线（改代码/跑测试/提交前先读）。
- [HANDOFF.md](HANDOFF.md) — 交接主文档：当前状态、已完成事项、已知问题、验证证据、下一步任务。
- 许可证：GPL-3.0（见 [LICENSE](LICENSE)）。
