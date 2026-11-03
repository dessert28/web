# web — Java Web 课程练习仓库

《Web 开发技术》课程练习的合集：一份课堂参考实现、三个 Spring Boot 练习工程、一个 Maven 空壳工程和一个 ASP.NET 空模板，另有早期归档包与许可证。

各工程**互相独立**：没有共享模块、父子 POM 或统一构建，请分别打开、分别构建。

## 项目一览

| 目录 | 内容 | 技术栈 | 构建 / 测试（2026-10-23 实测） |
| --- | --- | --- | --- |
| [`demo/`](demo/) | 课堂参考实现：登录模块（index / login / main） | Spring Boot 4.1.1 · Java 21 · Thymeleaf | ✅ 1/1 通过（JDK 21） |
| [`jsj2434/`](jsj2434/) | 课程实验复刻 + User 登录/列表 + Doctor 增删改查与条件查询 | Spring Boot 4.0.8 · Java 21 · Thymeleaf · MyBatis · MySQL 8 | ✅ 编译通过（2026-11-03 实测） |
| [`shiyan2/`](shiyan2/) | 实验二：Thymeleaf 商品信息管理（添加 + 列表） | Spring Boot 4.1.1 · Java 17 · Thymeleaf | ✅ 1/1 通过（JDK 17） |
| [`Test34/`](Test34/) | Maven 练习空壳：Hello World + MySQL 驱动 | Java 21 · mysql-connector-j 8.4.0 | ⚙️ 可编译，无测试 |
| [`sy1/`](sy1/) | VS2022 ASP.NET Web 应用空模板 | .NET Framework 4.7.2 · IIS Express | ⛔ 未开发 |

## 目录结构

```
web/
├── demo/                     课堂参考实现（com.example.demo）
│   ├── src/main/java/com/example/demo/{controller,domain,service}
│   ├── src/main/resources/{application.properties,templates/{index,login,main}.html}
│   └── src/test/java/com/example/demo/DemoApplicationTests.java
├── jsj2434/                  课程实验复刻 + User / Doctor 模块（com.zjsru）
│   ├── src/main/java/com/zjsru/
│   │   ├── controller/{UserController,DoctorController}.java  页面 / 登录 / 查询 / 医生 CRUD 路由
│   │   ├── mapper/{UserMapper,DoctorMapper}.java              MyBatis 查询 tb_users / doctor_info
│   │   ├── service/{UserService,UserServiceImpl,DoctorService,DoctorServiceImpl}.java
│   │   └── domain/{Users,Doctor}.java
│   ├── src/main/resources/{application.properties,mapper/DoctorMapper.xml,templates/{index,login,main,doctorList,doctorQuery}.html}
│   └── src/test/java/com/zjsru/Jsj2434ApplicationTests.java   仅上下文加载用例
├── shiyan2/                  实验二：商品信息管理（com.example.shiyan2）
│   ├── src/main/java/com/example/shiyan2/{controller,model,service}
│   ├── src/main/resources/{templates/{addGoods,goodsList}.html,static/css/goods.css}
│   └── src/test/java/com/example/shiyan2/Shiyan2ApplicationTests.java
├── Test34/                   Maven 空壳工程（org.example）
├── sy1/                      ASP.NET Web 空模板
├── AGENTS.md                 在本仓库工作的规则（改代码前先读）
├── HANDOFF.md                交接状态与历史记录
├── README.md                 本文件
├── demo.zip                  demo 的早期快照（与当前 demo/ 不同源，勿当源码用）
├── jsj2434.rar               jsj2434 课程原始压缩包（2026-11-03 按它覆盖过 jsj2434/）
├── mybatis.sql               MySQL 库 mybatis 的全量建表 + 演示数据脚本（Navicat 导出，供 jsj2434 建表）
└── LICENSE                   GPL-3.0
```

## 环境要求

| 组件 | 说明 |
| --- | --- |
| JDK | `demo`、`jsj2434`、`Test34` 需要 **JDK 21**（`D:\pc\.jdks\graalvm-jdk-21.0.7`）；`shiyan2` 需要 **JDK 17**（`D:\pc\.jdks\graalvm-jdk-17.0.12`）。PATH 上的 JDK 15 不能用于构建 |
| Maven | 系统没有全局 `mvn`，一律用各项目自带的 `mvnw.cmd`（wrapper 3.3.4 / Maven 3.9.16）。`.mvn/settings.xml` 把依赖镜像到阿里云 |
| MySQL | 只有 `jsj2434` 需要：MySQL 8，`localhost:3306`，账号 `root/root`，库名 `mybatis`（由 `mybatis.sql` 初始化） |
| 端口 | `demo` 与 `jsj2434` 默认都占用 8080，不要同时启动；需要并行时加 `--server.port=8081` |

## 快速开始

```powershell
# demo：课堂参考实现
cd demo
$env:JAVA_HOME = 'D:\pc\.jdks\graalvm-jdk-21.0.7'
.\mvnw.cmd spring-boot:run        # http://localhost:8080/ ，账号 admin / 123

# jsj2434：实验复刻 + 数据库认证
cd jsj2434
$env:JAVA_HOME = 'D:\pc\.jdks\graalvm-jdk-21.0.7'
.\mvnw.cmd spring-boot:run        # 启动时自动执行 mybatis.sql 建表

# shiyan2：实验二
cd shiyan2
$env:JAVA_HOME = 'D:\pc\.jdks\graalvm-jdk-17.0.12'
.\mvnw.cmd spring-boot:run        # http://localhost:8080/shiyan1/toAdd

# 任意项目跑全量测试
cd <项目目录>
.\mvnw.cmd -B clean test
```

> 没有设置 `JAVA_HOME` 时 `mvnw` 会退回 PATH 上的 JDK 15，构建会直接失败。

## 各项目说明

### demo/ — 课堂参考实现

老师给出的登录示例，`jsj2434` 就是照着它复刻的。账号写死在 `UserServiceimpl` 里（`admin` / `123`）。

| 方法 | 路径 | 行为 |
| --- | --- | --- |
| GET | `/` | 欢迎页 `index.html` |
| GET | `/tologin` | 登录表单 `login.html` |
| POST | `/login` | 校验成功则内部转发 `/toMain`，失败回到 `login` |
| GET/POST | `/toMain` | 主页 `main.html`，模型带 `username`；模板实际读 `session.username` |
| GET | `/logout` | 返回文本“退出成功”，不销毁会话 |
| GET | `/logout2` | 回到登录页并带 `message=退出成功` |

原样保留了源项目的行为：登录不写 session，所以主页用户名是空的；两个退出接口都不销毁会话。

### jsj2434/ — 课程实验复刻（User + Doctor）

在 `demo/` 的基础上扩展为两个模块，2026-11-03 按 `jsj2434.rar` 覆盖回退到课程原始实现（细节见 HANDOFF）：

- **User 模块**：登录后内部转发 `/selectAllUsers`，主页 `main.html` 以表格循环展示 `tb_users`；`/queryUserById/{id}`、`/queryUserByNameAndPassword/{u}/{p}` 查询接口。
- **Doctor 模块**：`doctor_info` 表的查询 / 新增 / 列表页 / 条件查询页；`DoctorMapper.xml` 用 `<where>` 动态条件 + `like` 模糊匹配（姓名 / 科室 / 职称），新增 `doctorList.html`、`doctorQuery.html` 两个页面。
- **数据库**：MySQL 库 `mybatis`（`localhost:3306`，`root/root`），依赖现成 `tb_users`、`doctor_info` 表；全量建表脚本在仓库根 [`mybatis.sql`](mybatis.sql)（Navicat 导出，含数十张表），当前 jsj2434 内没有自动建表配置。
- **无全局异常处理**：`GlobalExceptionHandler`/`Result` 已删除，异常走 Spring Boot 默认处理。

| 方法 | 路径 | 行为 |
| --- | --- | --- |
| GET | `/` | 欢迎页 |
| GET | `/toLogin` | 登录表单 |
| POST | `/login` | 校验 `admin/123` 成功后 `forward:/selectAllUsers`，失败回 `login` |
| GET/POST | `/toMain` | 主页，模型带 `username` |
| GET | `/logout` | 返回硬编码 `Users`（张三 / 123）的 JSON |
| GET | `/logout2` | 登录页 + `message=退出成功` |
| GET | `/test/{name}/{password}` | Restful 路径参数绑定演示，渲染 `main` |
| GET | `/selectAllUsers` | 查询 `tb_users` 全部用户，渲染 `main` 表格 |
| GET | `/queryUserById/{id}` | 按主键查询用户，返回 JSON |
| GET | `/queryUserByNameAndPassword/{username}/{password}` | 按用户名 + 明文密码查询，返回 JSON |
| GET | `/queryAllDoctors` | 查询 `doctor_info` 全部医生，返回 JSON |
| GET | `/doctorList` | 渲染 `doctorList.html` 医生表格 |
| GET | `/queryDoctorById?id=` | 按 id 查询医生，返回 JSON |
| POST | `/addDoctor` | 新增医生，返回受影响行数 |
| GET | `/doctorQuery` | 条件查询，渲染 `doctorQuery.html`（条件回显） |
| GET | `/queryDoctorsByCondition` | 条件查询（JSON），按姓名 / 科室 / 职称 like 模糊 |

当前只有一个上下文加载测试（`Jsj2434ApplicationTests`）。

### shiyan2/ — 实验二：商品信息管理

Thymeleaf 表单绑定与列表渲染练习，数据保存在 `GoodsServiceImpl` 的静态 `ArrayList` 里（进程重启即清空，不涉及数据库）。

| 方法 | 路径 | 行为 |
| --- | --- | --- |
| GET | `/shiyan1/toAdd` | 渲染添加商品表单 `addGoods.html` |
| POST | `/shiyan1/addGoods` | 接收 `Goods`（名称、原价、折扣价），加入列表并渲染 `goodsList.html` |

![商品列表页面](shiyan2/goodsList.html.png)

添加页截图见 [`shiyan2/addGoods.html.png`](shiyan2/addGoods.html.png)；实验报告在 `shiyan2/` 下（`实验一实验报告.docx`、`实验二_Thymeleaf实验报告.doc`）。

### Test34/ — Maven 空壳工程

`org.example:Test34`，只有一个打印 `Hello and welcome!` 与 1..5 的 `Main.java`，依赖 `mysql-connector-j` 但没有任何连库代码。已补 Maven Wrapper，可以 `.\mvnw.cmd compile`，没有测试。

### sy1/ — ASP.NET Web 应用空模板

VS2022 新建的 ASP.NET Web Application（.NET Framework 4.7.2），只有 `AssemblyInfo.cs`、`Web.config` 和 Docker 配置，没有任何页面；需要 Windows + IIS Express，本仓库不做构建验证。

## 测试矩阵

| 项目 | 命令 | JDK | 结果 |
| --- | --- | --- | --- |
| `demo` | `.\mvnw.cmd -B clean test` | 21 | BUILD SUCCESS，Tests run: 1 |
| `jsj2434` | `.\mvnw.cmd -B clean compile` | 21 | BUILD SUCCESS（仅编译；测试 1 个上下文用例，需 MySQL） |
| `shiyan2` | `.\mvnw.cmd -B clean test` | 17 | BUILD SUCCESS，Tests run: 1 |
| `Test34` | `.\mvnw.cmd -B clean compile` | 21 | BUILD SUCCESS（无测试） |

`jsj2434` 的测试会连本机 MySQL，跑之前确认 MySQL 8 已启动并已用根 [`mybatis.sql`](mybatis.sql) 建好 `mybatis` 库。

## 已知限制

- `demo` 与 `jsj2434` 都是课程演示：密码明文比较/存储（jsj2434 登录仍硬编码 `admin/123`），数据源账号 `root/root` 写在配置里，没有 CSRF、会话固定防护，不要当真实站点使用。
- `jsj2434` 保留源示例行为：登录不写 session、退出不销毁会话。
- `jsj2434` 当前只有上下文加载测试，Doctor/User 各路由无自动化覆盖，改动需人工或补测试验证。
- `Test34` 只有 Hello World；`sy1` 只有模板，均非完整应用。
- `demo.zip` / `jsj2434.rar` 是更早的快照，内容与目录中的源码不一致，不要以它们为准；其中 `jsj2434.rar` 已于 2026-11-03 用作覆盖 `jsj2434/` 的基准。
- Windows 受限沙箱里直接构建可能因 `target/` 写入被拒而失败（`AccessDeniedException`），这是会话权限问题，不是工程缺陷。

## 文档与约定

- [AGENTS.md](AGENTS.md) — 在本仓库工作的规则与红线，改代码前先读。
- [HANDOFF.md](HANDOFF.md) — 交接状态、历史记录与遗留问题。
- 提交信息沿用 `feat(scope): ...`、`fix: ...`、`docs: ...` 的形式，涉及具体项目时带作用域。
- 推送前请先确认：远端为 `git@github.com:dessert28/web.git`，分支 `main`。

## 许可证

仓库根目录的 [LICENSE](LICENSE) 为 GPL-3.0 全文，各子项目未单独声明许可。
