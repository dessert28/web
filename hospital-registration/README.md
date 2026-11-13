# hospital-registration — 实验二：基于 Spring Boot 集成 MyBatis 实现患者挂号

独立子项目，从仓库主项目 [`jsj2434`](../jsj2434) 复制**干净副本**后改造而来，用于完成「实验二」。

- 与 `jsj2434` **没有代码依赖**，两者各自独立构建、独立运行。
- `jsj2434` 保持原样（内容未被本实验修改），本目录只是它的演进分支。
- 与 `jsj2434` 共用同一个 MySQL 库 `mybatis`：本实验只**新增**一张 `tb_appointment` 表、给 `doctor_info` **新增**两列 `specialty` / `password`，不改动、不删除任何既有表与数据。

---

## 1. 环境要求

| 项 | 要求 |
| --- | --- |
| JDK | **21**（`jsj2434` 同款；PATH 上的 JDK 15 会构建失败，必须显式设 `JAVA_HOME`） |
| Maven | 无需安装，用项目自带 `.\mvnw.cmd`（wrapper 3.3.4 / Maven 3.9.16） |
| MySQL | 8.x，`localhost:3306`，账号 `root/root`，库 `mybatis` |
| 端口 | **8082**（`jsj2434` 用 8080，可同时启动） |

依赖镜像配置 `.mvn/settings.xml` 与 `.mvn/maven.config` 从 `jsj2434` 原样复制，**不要改动**。

```powershell
$env:JAVA_HOME = 'D:\pc\.jdks\graalvm-jdk-21.0.7'   # 本机实际 JDK 位置
```

## 2. 数据库准备

`jsj2434` 的库来自仓库根 [`mybatis.sql`](../mybatis.sql)。本实验的表与列由脚本补齐：

```powershell
cd D:\project\githubclone\web\hospital-registration
& 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe' -uroot -proot mybatis < db\experiment2-appointment.sql
```

脚本是**幂等**的，可以重复执行：

- `CREATE TABLE IF NOT EXISTS tb_appointment`（挂号记录）
- 通过 `add_column_if_absent` 存储过程给 `doctor_info` 加 `specialty`、`password` 两列（MySQL 8 没有 `ADD COLUMN IF NOT EXISTS`）
- `UPDATE doctor_info SET password='123'`，并为 8 位医生补齐专业专长
- 结尾 `SELECT ... UNION ALL` 打印自检结果（`tb_appointment` 行数、`doctor_info` 行数）

## 3. 运行

```powershell
cd D:\project\githubclone\web\hospital-registration
$env:JAVA_HOME = 'D:\pc\.jdks\graalvm-jdk-21.0.7'
.\mvnw.cmd -B clean test        # 39 个测试，期望 Tests run: 39, Failures: 0, Errors: 0
.\mvnw.cmd spring-boot:run      # http://127.0.0.1:8082/
```

演示账号（都是查库校验，没有硬编码判断）：

| 角色 | 账号 | 密码 | 数据来源 |
| --- | --- | --- | --- |
| 患者 | `15998815467`（或 `admin`） | `123` | `tb_users.username` + `password` |
| 医生 | `张伟` | `123` | `doctor_info.doctorName` + `password` |

## 4. 路由

### 患者端

| 方法 | 路径 | 登录 | 说明 |
| --- | --- | --- | --- |
| GET | `/`、`/home` | 否 | 全部科室列表 + 搜索框 |
| GET | `/search?keyword=` | 否 | 按科室名/位置/电话或医生姓名/科室/职称/专长检索 |
| GET | `/department/{deptId}` | **是** | 该科室医生列表（姓名/性别/职称/专业专长） |
| GET | `/doctorDetail/{doctorId}` | **是** | 医生详情 + 挂号表单 |
| POST | `/appointment/submit` | **是** | 提交挂号 → 跳结果页 |
| GET | `/appointment/result/{id}` | **是** | 预约结果（只能看自己的） |
| GET | `/myAppointments` | **是** | 我的预约列表 |
| POST | `/appointment/cancel` | **是** | 取消预约（按 `user_id` 限定，幂等） |

### 医生端

| 方法 | 路径 | 登录 | 说明 |
| --- | --- | --- | --- |
| GET | `/doctor/appointments?date=&status=&range=week` | **仅医生** | 患者预约信息，按日期/状态/区间筛选 |
| GET | `/doctor/appointments/today` | **仅医生** | 今天（实际上是重定向到上面那条） |
| GET | `/doctor/appointments/query?date=` | **仅医生** | 兼容旧式表单提交，重定向 |

医生端**只从会话取 `doctorId`**，不接受请求参数里的医生 ID，因此不可能越权查询别的医生的患者。

### 登录/错误

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/toLogin` | 登录页（患者/医生切换） |
| POST | `/login` | 提交登录，失败回填账号并提示 |
| GET | `/logout` | 注销并回首页 |
| GET | `/forbidden` | 403 页面（患者访问医生端时由拦截器转发，状态码 403） |

## 5. 挂号业务规则

- 可预约日期：**今天** 至 **今天 + 14 天**（`AppointmentServiceImpl.MAX_ADVANCE_DAYS`）。
- 时段白名单 8 个，来自 `AppointmentServiceImpl.TIME_SLOTS`（上午 4 个、下午 4 个）。
- 同一医生 + 同一日期 + 同一时段**不允许重复占用**（`BookingResult.Code.DUPLICATE`）。
- 取消后该时段释放，可重新预约（因此库里**没有**唯一约束，查重在服务层）。
- 取消是幂等的：`UPDATE ... WHERE status = 0` 按影响行数判断，重复取消返回「无需重复操作」。
- 就诊人姓名、联系电话必填；身份证号、备注可选。

## 6. 代码结构

```
src/main/java/com/zjsru/hospital/
├── HospitalRegistrationApplication.java
├── config/WebConfig.java              # 注册拦截器，声明免登录路径
├── controller/
│   ├── DepartmentPageController.java  # 首页、检索、科室详情
│   ├── AuthController.java            # 登录/注销
│   ├── AppointmentController.java     # 医生详情、挂号、结果、我的预约、取消
│   ├── DoctorWorkbenchController.java # 医生端预约查询
│   └── ErrorPageController.java       # 403 页
├── domain/                            # Department/Doctor/Users/Appointment/...
├── interceptor/LoginInterceptor.java   # 会话校验 + 医生端角色校验
├── mapper/                            # MyBatis 接口（User 走注解 SQL，其余走 XML）
└── service/                           # 接口 + Impl
src/main/resources/
├── application.properties             # 端口 8082、数据源、MyBatis
├── mapper/{Doctor,Department,Appointment}Mapper.xml
├── static/                            # Material Design 离线资产（从 jsj2434 原样复制）
└── templates/
    ├── fragments/layout.html           # head / appbar / alert / departmentGrid / searchResultGrid
    ├── patientHome.html                # 首页（含检索结果）
    ├── login.html                      # 双角色登录
    ├── departmentDetail.html           # 科室医生列表
    ├── doctorDetail.html               # 医生详情 + 挂号表单
    ├── appointmentResult.html          # 挂号结果 + 取消
    ├── myAppointments.html             # 我的预约
    ├── doctorAppointments.html         # 医生工作台
    └── error/{403,404,500}.html
src/test/java/com/zjsru/hospital/
├── HospitalRegistrationApplicationTests.java  # 上下文加载 1 例
├── RegistrationServiceTests.java              # 业务层 20 例
└── RegistrationFlowTests.java                 # 路由/权限/端到端 18 例
```

## 7. 与 `jsj2434` 的差异

**新增**：挂号全链路（表、Mapper、Service、Controller、6 个模板）、双角色登录（查库）、登录拦截器、403/404/500 错误页、39 个测试、数据库脚本。

**移除**：`UserServiceImpl.login` 的硬编码 `admin/123` 判断；`jsj2434` 的历史演示页（`main`、`doctorList`、`doctorQuery`、`selectAllUsers`）没有迁移。

**修正**（顺带带来的改进，不影响 `jsj2434`）：

- `queryDoctorById` 的参数改为 `Integer` + `@Param`，传不存在/空 id 返回 `null` 而不是 500。
- `queryAllDepartments` 保留显式 camelCase 别名，避免 `deptName` 为 `null` 的旧 bug。
- 科室一对多映射保留 `<collection notNullColumn="doctor_id">`，避免空科室产出全 `null` 的 `Doctor` 行。

**保留的已知问题**（课程演示口径，见 `HANDOFF.md`）：密码明文存储与比较、`root/root` 写死在 `application.properties`、重复挂号只由服务层拦（库里没唯一约束）、`tb_users` 与医生/患者档案之间没有外键。

## 8. 许可

与仓库主体一致，见仓库根 `LICENSE`。
