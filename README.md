# web — Java Web 课程练习仓库

主项目是 **[`jsj2434/`](jsj2434/)**（课程实验的完整实现：User 登录/列表 + Doctor 查询/新增/更新，MyBatis + MySQL）。
其余工程是早期练习，已整体移入 **[`archive/`](archive/)**，只作留档，不再维护。

## 主项目：jsj2434

Spring Boot 4.0.8 · Java 21 · Thymeleaf · MyBatis · MySQL 8，包名 `com.zjsru`，端口 8080。

| 模块 | 内容 |
| --- | --- |
| User | 登录（硬编码 `admin` / `123`）、用户列表、按 id / 用户名+密码查询 |
| Doctor | 列表、按 id 查询、新增、条件查询（`<where>`）、单条件（`<choose>`）、动态更新（`<set>`）、批量查询（`<foreach>`） |
| 界面 | Material Design，**全离线**（Tailwind 运行时 + 内嵌 Roboto，约 950 KB，不依赖 CDN） |

```powershell
cd jsj2434
$env:JAVA_HOME = 'D:\project\.jdks\graalvm-jdk-21.0.7'   # 本机实际路径；主开发机见 AGENTS.md
.\mvnw.cmd spring-boot:run        # http://localhost:8080/ ，账号 admin / 123
.\mvnw.cmd -B clean compile       # 编译验证
```

**数据库前置条件（不满足则启动即失败）**：MySQL 8 于 `localhost:3306`，账号 `root/root`，库名 **`mybatis`**，需要 `tb_users`、`doctor_info` 两张表。建表脚本是仓库根的 [`mybatis.sql`](mybatis.sql)（Navicat 导出，含数十张表），**需手动导入一次**：

```
mysql -uroot -p < mybatis.sql
```

> jsj2434 **没有**自动建表配置（`application.properties` 无 `spring.sql.init.*`），换机器一定要先建库。

路由表、代码结构、前端资产说明见 **[jsj2434/README.md](jsj2434/README.md)**。

## 归档工程（`archive/`）

以下工程保留完整历史（`git mv` 迁移，可用 `git log --follow` 追溯），仅作留档：

| 目录 | 内容 | 技术栈 | 状态 |
| --- | --- | --- | --- |
| [`archive/demo/`](archive/demo/) | 课堂参考实现，jsj2434 照它复刻 | Spring Boot 4.1.1 · Java 21 | 只读留档 |
| [`archive/shiyan2/`](archive/shiyan2/) | 实验二：商品信息管理（静态 List） | Spring Boot 4.1.1 · Java 17 | 只读留档 |
| [`archive/Test34/`](archive/Test34/) | Maven 空壳，仅 Hello World | Java 21 | 只读留档 |
| [`archive/sy1/`](archive/sy1/) | ASP.NET Web 应用空模板 | .NET Framework 4.7.2 | 未开发，本仓库无法构建 |
| [`archive/material-design-style-pack/`](archive/material-design-style-pack/) | jsj2434 界面风格包（含同名 `.zip`） | CSS / Tailwind 令牌 | 参考资产 |

归档工程各自的 README 在各自目录下；它们的构建命令已不纳入日常验证。

## 仓库其他内容

| 路径 | 说明 |
| --- | --- |
| [`mybatis.sql`](mybatis.sql) | MySQL 库 `mybatis` 的全量建表 + 演示数据脚本，**主项目要用** |
| [`bak/`](bak/) | 只读快照区：2026-11-03 覆盖前的整树、Material Design 改造前的回滚材料。内有说明「只允许读取和新建，不允许修改」 |
| `demo.zip` / `jsj2434.rar` | 早期压缩快照，内容与目录源码**不一致**，勿当源码用（AGENTS.md 硬红线：不可删改） |
| `LICENSE` | GPL-3.0 |

## 环境要求

| 组件 | 说明 |
| --- | --- |
| JDK | 主项目需要 **JDK 21**（`graalvm-jdk-21.0.7`）。归档工程另有 Java 17 需求。PATH 上未必有 `java`，跑之前先设 `JAVA_HOME` |
| Maven | 无全局 `mvn`，一律用各项目自带的 `.\mvnw.cmd`（wrapper 3.3.4 / Maven 3.9.16） |
| MySQL | 仅主项目需要，见上文前置条件 |
| 端口 | 主项目占用 8080 |

> JDK 的具体路径因机器而异（本机在 `D:\project\.jdks\`，主开发机见 [AGENTS.md](AGENTS.md) 第 2 节）。首次构建需要联网下载依赖。

## 文档与约定

- [AGENTS.md](AGENTS.md) — 在本仓库工作的规则与红线，改代码前先读。
- [HANDOFF.md](HANDOFF.md) — 交接状态、历史记录与已知问题。
- 提交信息沿用 `feat(scope): ...`、`fix: ...`、`docs: ...` 的形式。
- 远端为 `github.com/dessert28/web`，分支 `main`；推送前先与用户确认。
