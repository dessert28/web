# HANDOFF.md — hospital-registration 交接记录

状态与进度（规则见 [AGENTS.md](AGENTS.md)，全貌见 [README.md](README.md)）。

---

## 0. 本次交付（2026-11-13）

### 0.1 做了什么

把仓库主项目 `jsj2434` 复制成**干净副本** `hospital-registration/`，在副本里完成实验二：

- 包名 `com.zjsru` → `com.zjsru.hospital`，启动类 `Jsj2434Application` → `HospitalRegistrationApplication`
- `artifactId` → `hospital-registration`，端口 → **8082**（与 `jsj2434` 的 8080 并存）
- 登录从 `UserServiceImpl.login` 的**硬编码 `admin/123`** 改为**查库**：患者查 `tb_users`，医生查 `doctor_info`（新增 `password` 列）
- 新增挂号全链路：`tb_appointment` 表、`AppointmentMapper`(+XML)、`AppointmentService`、患者端 5 个路由 + 医生端 3 个路由
- 新增登录拦截器：未登录 → `/toLogin?redirect=`；患者访问 `/doctor/**` → 403
- 新增 8 个模板（含 `fragments/layout.html` 5 个片段）与 3 个错误页
- 新增 39 个测试

### 0.2 实跑验证结果

```powershell
$env:JAVA_HOME = 'D:\pc\.jdks\graalvm-jdk-21.0.7'
cd D:\project\githubclone\web\hospital-registration
.\mvnw.cmd -B -o clean test
```

实际输出（2026-11-13 实跑）：

```
Tests run: 1,  Failures: 0, Errors: 0 -- HospitalRegistrationApplicationTests
Tests run: 18, Failures: 0, Errors: 0 -- RegistrationFlowTests
Tests run: 20, Failures: 0, Errors: 0 -- RegistrationServiceTests
Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

`spring-boot:run` 在 :8082 启动后，用 `HttpClient` 走真实 HTTP 冒烟，全部符合预期：

| 请求 | 期望 | 实测 |
| --- | --- | --- |
| `GET /home` | 200 | 200 |
| `GET /search?keyword=儿` | 200 | 200 |
| `GET /department/1`（匿名） | 302 → `/toLogin?redirect=%2Fdepartment%2F1` | 一致 |
| `GET /myAppointments`（匿名） | 302 | 一致 |
| `POST /login`（患者 `15998815467/123`） | 302 → `/home` | 一致 |
| `GET /department/1`（已登录） | 200 | 200 |
| `GET /department/9999` | 200（提示不存在） | 200 |
| `GET /doctor/appointments`（患者） | 403 | 403 |
| `POST /appointment/submit` | 302 → `/appointment/result/{id}` | 一致 |
| `POST /appointment/submit`（重复） | 200 + 「已被预约」 | 一致 |
| `POST /appointment/submit`（昨天） | 200 + 「不能早于今天」 | 一致 |
| `POST /appointment/cancel` | 200 | 200 |
| `POST /appointment/cancel`（再来一次） | 200（幂等） | 200 |
| `POST /login`（医生 `张伟/123`） | 302 → `/doctor/appointments` | 一致 |
| `GET /doctor/appointments?date=昨天` | 200（空列表） | 200 |
| `GET /doctor/appointments?range=week` | 200 | 200 |

冒烟产生的 `tb_appointment` 行已按 `patient_name` 精确删除，测试用例自清理，**验证后库里没有残留**。

### 0.3 数据库

`db/experiment2-appointment.sql` 已导入 `mybatis` 库，并**重复执行验证幂等**（两次都 exit=0）。导入后自检：`tb_appointment` 存在、`doctor_info` 8 行且 `specialty`/`password` 就位。

## 1. 已知问题（课程演示口径，故意没改）

1. **密码明文**存储与比较（`tb_users.password`、`doctor_info.password` 都是 `123`）。课程要求，不引入 BCrypt。
2. `application.properties` 里数据源写死 `root/root`，与 `jsj2434` 一致。
3. **重复挂号只在服务层拦**：`countActiveBySlot` 查 `status = 0` 的行数，库里没有唯一约束。并发下仍可能插入两条；因为取消后要能重新预约同一时段，所以没有加 `(doctor_id, visit_date, time_slot)` 唯一键。真要治本需要「唯一键 + 取消时把 `status` 复合进唯一列」或「逻辑删除 + 物化占位行」。
4. `tb_users` 与医生/患者档案之间**没有外键**，挂号记录只用 `user_id` 记身份，患者姓名/电话是**快照**（提交时抄一份进 `tb_appointment`）。这是刻意的：患者改档案不应该影响历史预约。
5. `doctor_info.gender` 是 `tinyint`，而 `Doctor.gender` 是 `String`；靠 `getGenderLabel()` 兜（`1`→男、`0`→女、`2`→其他、`null`→`-`）。
6. `UserMapper.insertUser` 仍指向**已不存在**的 `tb_users.email` 列（从 `jsj2434` 原样继承），当前无人调用。要启用注册功能必须先修这条 SQL。
7. `tb_users` 里 `rolename = 'admin'` 的账号在本项目里也会被当作**患者**（`Role` 枚举只有 `PATIENT`/`DOCTOR`），登录后只能挂号，不能进医生端。

## 2. 过程中踩到的坑（避免重犯）

1. **Thymeleaf `th:replace` 会连宿主标签一起替换**，所以 `th:each` 不能和 `th:replace` 挤在同一个标签上——循环变量在片段里解析不到，报 `Exception evaluating SpringEL expression: "dept.id"`。解决办法：把 `th:each` 写在**片段内部**的卡片标签上（见 `fragments/layout.html` 的 `departmentGrid` / `searchResultGrid`）。第一版写法就是在 `<a th:each>` 上加 `th:replace`，测试里 `/home` 直接 500。
2. `MockHttpSession` 在 `/logout` 里被 `invalidate()` 后**不能再 `getAttribute`**（抛 `IllegalStateException`），断言要改成 `session.isInvalid()`。
3. PowerShell 5.1 的 `Invoke-WebRequest` **没有** `-SkipHttpErrorCheck`，且 `MaximumRedirection 0` 遇到 302 会抛异常、拿不到 `Location`。冒烟改用 `System.Net.Http.HttpClient`（`AllowAutoRedirect = $false`）。
4. `powershell -File xxx.ps1` 按 **GBK** 读脚本，UTF-8 中文会炸成语法错误。冒烟脚本要么全 ASCII，要么用 `[char]0x5F20` 这类转义拼中文。
5. Tomcat 把 `;jsessionid=` 拼在重定向 URL 上，从 `Location` 里取 id 前要 `-replace ';jsessionid=[^?&/]*',''`。
6. 函数名 `R` 与内置别名 `Invoke-History` 冲突（`r` 是 `Invoke-History` 的别名），脚本里别用单字母函数名。

## 3. 后续可做

- 患者自助取消的时间窗限制（现在随时可取消，包括就诊当天）。
- 医生端按状态统计（今日待就诊/已取消数量）目前只在页面算，可以下推到 SQL。
- 号源概念：现在没有「每个时段限 N 个号」，只有「同一时段不能被重复占用」。
- `tb_users` 增加 `patient_id` 外键，把就诊人档案真正建模。
