# jsj2434 — 课程实验复刻（User 登录/列表 + Doctor 查询/新增）

在 [`demo/`](../demo/) 登录示例的基础上扩展为两个模块。2026-11-03 按 [`jsj2434.rar`](../jsj2434.rar) 覆盖回退为课程原始实现（当时的改动详见 [HANDOFF.md](../HANDOFF.md)）。

包名 `com.zjsru`，端口 8080。

- 技术栈：Spring Boot 4.0.8 · Java 21 · Thymeleaf · MyBatis · MySQL 8
- 演示账号：`admin` / `123`
- 界面：Material Design（风格来源 [`material-design-style-pack/`](../material-design-style-pack/)），**全离线**，不依赖任何 CDN

## 运行

```powershell
$env:JAVA_HOME = 'D:\pc\.jdks\graalvm-jdk-21.0.7'
.\mvnw.cmd spring-boot:run      # http://localhost:8080/
.\mvnw.cmd -B clean compile     # 编译验证
.\mvnw.cmd -B clean test        # 1 个上下文加载测试（需 MySQL）
```

前置条件：本机 MySQL 8 已启动，`localhost:3306`，账号 `root/root`，库 `mybatis` 已建好 `tb_users`、`doctor_info` 表。全量建表 + 演示数据脚本在仓库根 [`mybatis.sql`](../mybatis.sql)（Navicat 导出，含数十张表），手动导入一次即可：

```
mysql -uroot -p < ..\mybatis.sql
```

> 当前 jsj2434 **没有**自动建表配置（`application.properties` 无 `spring.sql.init.*`），换机器请先建库。

## 路由

| 方法 | 路径 | 行为 |
| --- | --- | --- |
| GET | `/` | 欢迎页 `index.html` |
| GET | `/toLogin` | 登录表单 `login.html` |
| POST | `/login` | 校验 `admin/123` 成功后 `forward:/selectAllUsers`，失败回 `login` |
| GET/POST | `/toMain` | 主页，模型带 `username` |
| GET | `/logout` | 返回硬编码 `Users`（张三 / 123）的 JSON，不销毁会话 |
| GET | `/logout2` | 登录页 + `message=退出成功` |
| GET | `/test/{name}/{password}` | Restful 路径参数绑定演示，渲染 `main` |
| GET | `/selectAllUsers` | 查询 `tb_users` 全部用户，渲染 `main` 表格 |
| GET | `/queryUserById/{id}` | 按主键查询用户，返回 JSON |
| GET | `/queryUserByNameAndPassword/{username}/{password}` | 按用户名 + 明文密码查询，返回 JSON |
| GET | `/queryAllDoctors` | 查询 `doctor_info` 全部医生，返回 JSON |
| GET | `/doctorList` | 渲染 `doctorList.html` 医生表格 |
| GET | `/queryDoctorById?id=` | 按 id 查询医生，返回 JSON |
| POST | `/addDoctor` | 以表单参数新增医生，返回受影响行数 |
| GET | `/doctorQuery` | 条件查询，渲染 `doctorQuery.html`（条件回显 + 空结果提示） |
| GET | `/queryDoctorsByCondition` | 条件查询（JSON），按姓名 / 科室 / 职称 `like` 模糊 |
| GET | `/queryDoctorsBySingleCondition` | 单条件查询（JSON），`<choose>` 只取第一个成立的条件，都不填默认主任医师 |
| POST | `/updateDoctor` | 动态更新（JSON），只更新传入字段；id 为空或无可用字段时返回 0 |

## 代码结构

- `controller/UserController.java`：页面 / 登录 / 退出 / 用户查询路由
- `controller/DoctorController.java`：医生查询 / 列表 / 新增 / 条件查询 / 单条件查询 / 动态更新路由
- `mapper/UserMapper.java`：MyBatis 注解 SQL，操作 `tb_users`
- `mapper/DoctorMapper.java` + `resources/mapper/DoctorMapper.xml`：MyBatis XML 映射，操作 `doctor_info`（`<where>` 条件、`<choose>` 单条件、`<set>` 动态更新、`<foreach>` 批量查询）
- `service/`：`UserService`/`UserServiceImpl`、`DoctorService`/`DoctorServiceImpl`
- `domain/Users.java`、`domain/Doctor.java`：Lombok `@Data`
- `templates/`：`index` / `login` / `main` / `doctorList` / `doctorQuery`

## 前端资产（Material Design，全离线）

| 路径 | 作用 |
| --- | --- |
| `static/vendor/tailwindcss-3.4.16.js` | Tailwind Play 运行时本地副本（451 KB） |
| `static/vendor/md-material.config.js` | 主题扩展层：Material 色板、dp1–dp8 双层阴影、`cubic-bezier(0.4,0,0.2,1)` 缓动 |
| `static/vendor/md-roboto.css` | Roboto 400/500/700，base64 内嵌（496 KB），中文回退到 Microsoft YaHei |
| `static/css/md-material.css` | 组件层：涟漪、浮动标签、表格、app bar、焦点环、reduced-motion 降级 |

> 四个文件合计约 950 KB，换来断网可用。生产环境应改用 PostCSS 构建而非 Play CDN 运行时。

## 说明

- 登录不进数据库，`UserServiceImpl.login` 仍是硬编码 `admin/123`；登录不写 session、退出不销毁会话（课程原始行为）。
- `DoctorMapper.xml#queryDoctorById` 用单参数 `#{doctorid}`（对应方法参数名 `doctorid`），若改成多个参数需加 `@Param`。
- 无全局异常处理（`GlobalExceptionHandler`/`Result` 已随覆盖删除），异常走 Spring Boot 默认处理。
- 测试仅 `Jsj2434ApplicationTests.contextLoads`。
- 界面改造前的快照与回滚步骤见 [`bak/jsj2434-md-20261103-110723/ROLLBACK.md`](../bak/jsj2434-md-20261103-110723/ROLLBACK.md)。
