# jsj2434 — 课程实验复刻 + 数据库认证

在 [`demo/`](../demo/) 登录示例的基础上复刻并扩展：登录改为查 MySQL，异常统一返回 JSON。
包名 `com.zjsru`，端口 8080。

- 技术栈：Spring Boot 4.0.8 · Java 21 · Thymeleaf · MyBatis · MySQL 8
- 演示账号：`admin` / `123`（`tb_users` 表内，另有 `15998815467` / `123` 等演示账号）

## 运行

```powershell
$env:JAVA_HOME = 'D:\pc\.jdks\graalvm-jdk-21.0.7'
.\mvnw.cmd spring-boot:run      # http://localhost:8080/
.\mvnw.cmd -B clean test        # 16 个用例
```

前置条件：本机 MySQL 8 已启动，`localhost:3306`，账号 `root/root`；库不存在时会按连接串里的 `createDatabaseIfNotExist` 自动创建。

## 数据库

项目使用 MySQL 库 `mybatis`，表结构与演示数据全部来自 [`src/main/resources/mybatis.sql`](src/main/resources/mybatis.sql)（从 Navicat 导出的完整脚本，含 `tb_users`、`tb_doctor`、`tb_patient` 等数十张表和演示记录）。

启动时由 `spring.sql.init.mode=always` + `spring.sql.init.schema-locations=classpath:mybatis.sql` 执行该脚本。脚本自带 `DROP TABLE IF EXISTS`，每次启动都会重建表并恢复演示数据。

登录使用 `tb_users` 表：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | INT | 自增主键 |
| username | VARCHAR(32) | 登录名 |
| password | VARCHAR(256) | 明文，仅课程演示 |
| rolename | VARCHAR(20) | 角色名（admin / ROLE_common / ROLE_vip） |
| status | VARCHAR(10) | 状态 |
| phone | VARCHAR(11) | 手机号 |

也可以手动执行：`mysql -uroot -p < src/main/resources/mybatis.sql`。

## 路由

| 方法 | 路径 | 行为 |
| --- | --- | --- |
| GET | `/` | 欢迎页 |
| GET | `/tologin` | 登录表单 |
| POST | `/login` | 查 `tb_users` 表校验，成功内部转发 `/toMain`，失败回 `login` |
| GET/POST | `/toMain` | 主页，模型带 `username`（模板读 `session.username`） |
| GET | `/logout` | 文本“退出成功” |
| GET | `/logout2` | 登录页 + `message=退出成功` |
| GET | `/testException` | 故意整数除零，演示异常 JSON |

## 异常处理

`GlobalExceptionHandler` 只处理 `com.zjsru.controller` 下的异常，按空指针 / 算术 / 其他分三类，统一返回：

```json
{"code": 500, "msg": "算术异常：/ by zero", "data": null}
```

HTTP 状态仍是 200（业务码与 HTTP 状态相互独立）。`/test-exceptions/*` 只存在于测试源码中，正式应用不提供。

## 测试

`.\mvnw.cmd -B clean test` 共 16 个用例：

| 测试类 | 用例数 | 覆盖 |
| --- | --- | --- |
| `UserControllerSecurityTest` | 6 | 页面可访问、登录转发、失败登录、模型与 session 差异、旧 URL 404、退出响应 |
| `UserPageTest` | 2 | 表单字段与链接、提示文本、旧样式 404 |
| `GlobalExceptionHandlerTest` | 5 | 三类异常 JSON、`Result.success`、`/testException` |
| `UserServiceTest` | 2 | Mockito 替换 Mapper，校验密码不一致 / 用户不存在 / 空用户名 |
| `Jsj2434ApplicationTests` | 1 | 上下文加载 |

## 注意

- 登录**不写 session**、退出**不销毁会话**，主页用户名显示为空——这是源示例的课程行为，复刻时保留。
- 密码明文比较与存储、数据源 `root/root` 明文配置，都只是教学用法。
