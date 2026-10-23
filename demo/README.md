# demo — 课堂参考实现（登录模块）

老师给的 Spring Boot 示例，`jsj2434` 就是照着它复刻的。保持原样，不做业务扩展。

- 技术栈：Spring Boot 4.1.1 · Java 21 · Thymeleaf
- 账号：`admin` / `123`，写死在 `UserServiceimpl`
- 端口：8080

## 运行

```powershell
$env:JAVA_HOME = 'D:\pc\.jdks\graalvm-jdk-21.0.7'
.\mvnw.cmd spring-boot:run      # http://localhost:8080/
.\mvnw.cmd -B clean test        # 1 个用例：上下文加载
```

## 路由

| 方法 | 路径 | 行为 |
| --- | --- | --- |
| GET | `/` | 欢迎页 |
| GET | `/tologin` | 登录表单 |
| POST | `/login` | 成功内部转发 `/toMain`，失败回 `login` |
| GET/POST | `/toMain` | 主页，模型带 `username` |
| GET | `/logout` | 返回文本“退出成功” |
| GET | `/logout2` | 登录页 + `message=退出成功` |

## 注意

登录**没有**写 session，退出也**没有**销毁会话，所以主页上的 `session.username` 是空的。这是源示例的原有行为，复刻时一并保留。
