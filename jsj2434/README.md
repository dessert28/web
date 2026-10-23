# jsj2434 — 课程实验复刻 + 数据库认证

在 [`demo/`](../demo/) 登录示例的基础上复刻并扩展：登录改为查 MySQL，异常统一返回 JSON，并提供 `tb_users` 的增删改查接口与临时测试页。
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
| GET | `/logout` | 返回硬编码 `Users`（张三 / 123）的 JSON，不销毁会话 |
| GET | `/logout2` | 登录页 + `message=退出成功` |
| GET | `/testException` | 故意整数除零，演示异常 JSON |
| GET | `/selectAllUsers` | 查询 `tb_users` 全部用户，返回 JSON 数组 |
| GET | `/queryUserById/{id}` | 按主键查询用户，返回 JSON |
| GET | `/queryUserByNameAndPassword/{username}/{password}` | 按用户名 + 明文密码查询，返回 JSON |
| POST | `/insertUser` | 新增用户，返回受影响行数 |
| PUT | `/updateUser` | 按 id 全字段更新用户，返回受影响行数 |
| DELETE | `/deleteUserById/{id}` | 按 id 删除用户，返回受影响行数 |
| GET | `/crud-test.html` | 临时可视化 CRUD 测试页（仅本地联调） |

## 异常处理

`GlobalExceptionHandler` 只处理 `com.zjsru.controller` 下的异常，按空指针 / 算术 / 其他分三类，统一返回：

```json
{"code": 500, "msg": "算术异常：/ by zero", "data": null}
```

HTTP 状态仍是 200（业务码与 HTTP 状态相互独立）。`/test-exceptions/*` 只存在于测试源码中，正式应用不提供。

## 临时 CRUD 测试页

[`src/main/resources/static/crud-test.html`](src/main/resources/static/crud-test.html) 是纯前端联调页面，启动应用后访问 <http://localhost:8080/crud-test.html>。

- 页面加载即调用 `GET /selectAllUsers` 展示全部用户，操作后自动刷新列表
- 新增表单 → `POST /insertUser`；修改表单 → `PUT /updateUser`；列表行内按钮 → `DELETE /deleteUserById/{id}`
- “填入修改”按钮把该行数据带入修改表单；底部输出区显示每次请求的 HTTP 状态与响应内容
- 修改为全字段覆盖，表单未填写的字段会被更新为 `NULL`
- **仅为本地联调临时页面，验证完可直接删除该文件**；重启应用后 `tb_users` 会按 `mybatis.sql` 重置，测试数据不会保留

## 测试

`.\mvnw.cmd -B clean test` 共 18 个用例：

| 测试类 | 用例数 | 覆盖 |
| --- | --- | --- |
| `UserControllerSecurityTest` | 7 | 页面可访问、登录转发、失败登录、模型与 session 差异、Restful 路径参数、旧 URL 404、退出响应 |
| `UserPageTest` | 2 | 表单字段与链接、提示文本、旧样式 404 |
| `GlobalExceptionHandlerTest` | 5 | 三类异常 JSON、`Result.success`、`/testException` |
| `UserServiceTest` | 3 | Mockito 替换 Mapper，登录校验与增删改委托 |
| `Jsj2434ApplicationTests` | 1 | 上下文加载 |

## 注意

- `insertUser` 不写 id，由数据库自增；`updateUser` 为全字段覆盖式更新。
- 临时测试页仅用于本地联调，验证完可删除，不影响正式接口。
- 登录**不写 session**、退出**不销毁会话**，主页用户名显示为空——这是源示例的课程行为，复刻时保留。
- 密码明文比较与存储、数据源 `root/root` 明文配置，都只是教学用法。
