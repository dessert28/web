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

前置条件：本机 MySQL 8 已启动，`localhost:3306`，账号 `root/root`，库 **`javaweb`**（由仓库根 [`javaweb.sql`](../javaweb.sql) 创建，自带 `CREATE DATABASE IF NOT EXISTS` 与 6 张表）。导入一次即可，脚本幂等，重复执行不会报错、不会覆盖已改数据：

```
mysql -uroot -proot --default-character-set=utf8mb4 < ..\javaweb.sql
```

> 当前 jsj2434 **没有**自动建表配置（`application.properties` 无 `spring.sql.init.*`），换机器请先导入该脚本。要切回旧的 `mybatis` 库，只改 `spring.datasource.url` 的库名即可，两库互不影响。

### 科室的存储方式（2026-11-06 起）

`javaweb.doctor_info` **没有** `department` 文本列，科室只存 `department_id`，外键指向 `department.id`。因此：

- 查询：`DoctorMapper.xml` 统一用 `left join department` 把 `dept_name` 别名成 `Doctor.department`，所以模板里的 `${doctor.department}` 不用改。
- 新增 / 更新：`departmentId`（数字）与 `department`（中文科室名）两种写法都接受，控制器 `resolveDepartmentId` 会统一成 `department_id`；两者都没给、或给了 `department` 表里不存在的科室名，直接返回 `0`。

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
- `controller/DoctorController.java`：医生查询 / 列表 / 新增 / 条件查询 / 单条件查询 / 动态更新路由；科室名→`department_id` 的归一在 `resolveDepartmentId`
- `mapper/UserMapper.java`：MyBatis 注解 SQL，操作 `tb_users`
- `mapper/DoctorMapper.java` + `resources/mapper/DoctorMapper.xml`：MyBatis XML 映射，操作 `doctor_info`（`<where>` 条件、`<choose>` 单条件、`<set>` 动态更新、`<foreach>` 批量查询，全部 `left join department` 取科室名）
- `service/`：`UserService`/`UserServiceImpl`、`DoctorService`/`DoctorServiceImpl`
- `domain/Users.java`、`domain/Doctor.java`：Lombok `@Data`；`Doctor` 的 `departmentId` 对应表列，`department` 是 join 出来的展示字段
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
- `title` 条件用的是 `like '%主任医师%'`，会把「**副**主任医师」也算进来；这是课程原始 LIKE 设计，不是筛选失效。要精确匹配需改成 `=`。
- 无全局异常处理（`GlobalExceptionHandler`/`Result` 已随覆盖删除），异常走 Spring Boot 默认处理。
- 测试仅 `Jsj2434ApplicationTests.contextLoads`（1 个用例，`Tests run: 1, Failures: 0`）；Doctor/User 各路由没有自动化覆盖。
- 界面改造前的快照与回滚步骤见 [`bak/jsj2434-md-20261103-110723/ROLLBACK.md`](../bak/jsj2434-md-20261103-110723/ROLLBACK.md)。
