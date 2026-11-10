# HANDOFF.md — 当前交接状态（2026-11-09）

> 本节是最新状态，优先于下面所有历史章节。
> 历史章节从「2026-11-03 覆盖事件」起保留供追溯，其中**状态类描述已不再准确**，不要据此操作。

## 一句话现状（2026-11-06）

jsj2434 的数据源从 `mybatis` 库切到新的 `javaweb` 库，仓库根新增 [`javaweb.sql`](javaweb.sql)（自带建库、幂等、6 张表）。`doctor_info` 不再有 `department` 文本列，科室一律走 `department_id` → `department` 外键，`DoctorMapper.xml` 全部改为 `left join department` 取科室名，模板零改动。实跑验证：`javaweb.sql` 连续导入两次成功，`.\mvnw.cmd -B clean test` 为 `Tests run: 1, Failures: 0`，8081 端口起服务后逐条打完 22 条路由。所有改动未提交、未推送。

### 2026-11-06 javaweb 库迁移（本轮）

**背景**：`javaweb.sql` 原是一份 Navicat 直接导出的快照，有四个硬伤：① 没有 `CREATE DATABASE` / `USE`，导入即报 `No database selected`；② `DROP TABLE` + 裸 `INSERT`，重放会丢数据；③ 完全没有 `tb_users`，User 模块在新库上无表可查；④ 医生科室的 `department_id` 值与科室名对不上（李娜标 `department_id=2` 但科室写「外科」，实际在儿科）。

**改动清单**：

| 文件 | 改动 |
| --- | --- |
| `javaweb.sql`（新增，未跟踪） | 重写：`CREATE DATABASE IF NOT EXISTS javaweb` + `USE`；全部 `CREATE TABLE IF NOT EXISTS` 且去掉 `DROP`；`INSERT IGNORE` 保证幂等；表按依赖顺序 `department → doctor_info → account/patient/visit_record`；补 `tb_users`（5 行演示数据）；字符集 `utf8mb4`（原 `utf8` 在 MySQL 8 是 `utf8mb3`）；`department_id` 改 `NOT NULL` + 外键；医生 PK 重排 1..5 并修正科室映射 |
| `domain/Doctor.java` | 新增 `Integer departmentId`（对应表列）；`department` 保留为 join 出来的展示字段 |
| `resources/mapper/DoctorMapper.xml` | 新增 `<sql id="doctorColumns">` 与 `queryDepartmentIdByName`；5 条查询全部 `left join department` 并显式 `AS departmentId / AS department`（没开下划线映射）；`addDoctor` / `updateDoctor` 用 `<choose>` 支持传 `departmentId` 或中文科室名 |
| `mapper/DoctorMapper.java` | 新增 `queryDepartmentIdByName(@Param("deptName") String)` |
| `service/DoctorService.java`、`DoctorServiceImpl.java` | 同步新增该方法，纯透传 |
| `controller/DoctorController.java` | 新增 `resolveDepartmentId`：科室归一，查不到就返回 0；`hasAnyUpdateField` 补 `departmentId != null` |
| `resources/application.properties` | 库名 `javaweb`（此前已是工作区改动）；顺手修掉两行历史乱码注释 |
| `jsj2434/README.md`、`README.md` | 库名、建库脚本、科室存储方式、测试矩阵同步；修掉根 README「启动时自动执行 mybatis.sql 建表」这句**错误描述**（项目实际无 `spring.sql.init.*`） |

**实跑验证（2026-11-06）**：

- `javaweb.sql` 连续 `source` 两次，两次 `exit=0`；导入后 `department=6 / doctor_info=5 / account=4 / patient=5 / visit_record=5 / tb_users=5`。
- 外键完整性：`doctor_info` 无悬空 `department_id`（0 行），`visit_record` 无悬空 `doctor_id`/`patient_id`（0 行）。
- `cd jsj2434; $env:JAVA_HOME='D:\pc\.jdks\graalvm-jdk-21.0.7'; .\mvnw.cmd -B clean test` → **BUILD SUCCESS**，`Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`（surefire 报告时间对应本次构建）。
- 起服务 `--server.port=8081`（8080 可能被 demo 占），逐条实打：`/`(200)、`/toLogin`(200)、`POST /login`(200)、`/selectAllUsers`(5 用户)、`/queryUserById/1`、`/doctorList`(5 行，科室显示中文名)、`/doctorQuery`、`/queryAllDoctors`、`/queryDoctorById?id=2`、`/queryDoctorsByCondition`（姓名/科室两路都对）、`/queryDoctorsBySingleCondition`（空条件走 `主任医师`）、`/queryDoctorsByIds?ids=1,2`、`/queryDoctorsByIds?ids=`（空集合返回 `[]`）、`POST /addDoctor`（中文科室名=1、`departmentId`=1、不传=0、错科室=0）、`POST /updateDoctor`（只改职称=1、只改电话=1、只改科室=1、无字段=0、错科室=0）。

**过程中发现并修掉的真缺陷**：`/updateDoctor` 最初无条件调用 `resolveDepartmentId`，导致「只改职称/电话」的请求因没传科室而被判 0。改成仅在本次请求确实带了科室信息时才解析。修复后重跑上述 5 种情形全部符合预期。

**已知行为（非缺陷）**：`title` 条件是 `like '%主任医师%'`，会同时匹配「**副**主任医师」——这是课程原始 LIKE 设计，`/queryDoctorsByCondition?title=主任医师` 返回 5 条属正确表现；要精确匹配得改成 `=`。

**未消除的风险**：① `javaweb`（utf8mb4）与 `mybatis`（utf8mb3）字符集不一致，跨库同步需统一；② `INSERT IGNORE` 会静默跳过主键冲突行，改数据请直接改表；③ Doctor/User 各路由仍无自动化测试，上述验证是手工实打，改动后需重跑；④ `mybatis.sql` 与 `javaweb.sql` 的 `doctor_info` 结构已分叉（前者有 `department` 列，后者只有 `department_id`），切换数据源时要一起换代码。

主项目是 **`jsj2434/`**，其余工程已归档到 `archive/`。GitHub 远端 `main` = 本地 `main`，工作区干净。
2026-11-09 完成了一次仓库整理（归档 + 去重 + 文档重写），**已提交但尚未推送**（详见第 3 节）。

## 1. 本次整理（2026-11-09）

按用户要求「以 jsj2434 为主项目，其余归档」，完成了以下改动。

### 1.1 归档（`git mv`，历史保留）

| 原路径 | 新路径 |
| --- | --- |
| `demo/` | `archive/demo/` |
| `shiyan2/` | `archive/shiyan2/` |
| `Test34/` | `archive/Test34/` |
| `sy1/` | `archive/sy1/` |
| `material-design-style-pack/` + `material-design-style-pack.zip` | `archive/` 下同名 |

全部 67 项在 `git status` 中显示为 `R`（重命名），`git log --follow` 可追溯历史。**没有改写任何已推送历史。**

未移动：`bak/`（自带只读规则，且是回滚依据，移动会破坏文档路径）、`mybatis.sql`（主项目要用，留在根）、`demo.zip` / `jsj2434.rar` / `LICENSE`（AGENTS 硬红线）。

### 1.2 清理

- 删除根目录 `实验报告模板.doc` —— 与 `archive/shiyan2/实验报告模板.doc` 哈希完全相同（当年因文件被占用删不掉，本轮 ACL 修复后成功删除）。
- 删除 `archive/shiyan2/addGoods.html.png`（1.53 MB）—— 与 `goodsList.html.png` 重复，README 只引用后者。
- `git rm --cached archive/sy1/sy1.csproj.user` —— IDE 状态文件，本地保留，`.gitignore` 已覆盖 `*.user`。

### 1.3 文档

- 根 `README.md` 重写：首屏即 jsj2434，归档工程压缩成一张表。
- `AGENTS.md`：更新路径、JDK 路径、远端地址；补记本机与主开发机的 JDK 路径差异。
- `jsj2434/README.md`：修正指向归档目录的相对链接。

## 2. 环境事实修正（重要）

历次文档里的环境描述与实际不符，本次已核实并更正：

| 项 | 文档旧说法 | 实测（2026-11-09） |
| --- | --- | --- |
| JDK 路径 | `D:\pc\.jdks\...` | **本机为 `D:\project\.jdks\`**（`graalvm-jdk-17.0.12` / `graalvm-jdk-21.0.7` / `corretto-18.0.2`）；`D:\pc\.jdks` 不存在 |
| PATH 上的 java | JDK 15.0.1，会失败 | **PATH 上根本没有 `java`**；`JAVA_HOME` 已指向 `D:\project\.jdks\graalvm-jdk-21.0.7` |
| 远端 | SSH `git@github.com:...` | **`origin` 实际配置为 HTTPS**（`https://github.com/dessert28/web.git`），无凭据时 fetch/push 失败；SSH 通道本身可用 |
| Maven 缓存 | 3.9.16 已缓存 | `~\.m2` 存在但**无 Spring Boot 依赖缓存**，首次构建需联网下载 |
| 建表 | README 称「启动时自动执行 mybatis.sql 建表」 | **错**。`application.properties` 无 `spring.sql.init.*`，必须手工导入根 `mybatis.sql`；库名是 `mybatis` |

**本机不是主开发机**（用户明确说明），因此本次整理**没有做构建验证**。

## 3. 提交状态与待办

本次整理的改动**已在工作区完成，尚未提交、未推送**。

接手后如果要落地，建议按 AGENTS.md 第 5 节分两笔提交：

| # | 提交信息 | 内容 |
| --- | --- | --- |
| A | `chore: archive the practice projects under archive/` | 67 项 `git mv` + 删除重复文件 + 取消跟踪 `sy1.csproj.user` |
| B | `docs: make jsj2434 the main project and refresh handoff` | `README.md`、`AGENTS.md`、`HANDOFF.md`、`jsj2434/README.md` |

> 提交前注意：本会话的沙箱**拦截对 `.git` 的写入**（`Unable to create '.git/index.lock': Permission denied`），即使 `.git` 的 ACL 已修好也如此。所有 `git mv` / `git rm` / `git commit` 都必须在**放开权限的执行**或用户自己的终端里完成。

## 4. 已知问题（只记录，未顺手修改）

按 AGENTS 第 6 节，以下问题**不在本次整理范围内**：

1. **测试覆盖极薄**：`jsj2434`、`archive/demo`、`archive/shiyan2` 各只有 1 个 `contextLoads`；`archive/Test34` 无测试。jsj2434 的 Doctor/User 全部路由**零自动化覆盖**，改动只能靠人工验证。这是仓库最大的质量风险。
2. **`archive/sy1` 无法构建**：ASP.NET 4.7.2 空模板，需 Windows + VS/MSBuild，本仓库没有构建路径。
3. **课程遗留行为**（按 AGENTS 红线第 6 条保留现状）：登录不写 session、`/logout` 返回假的「张三」JSON、密码明文比较与存储、数据源 `root/root` 写在配置文件里。
4. **`bak/` 体积**：55 个文件、约 1.79 MB，是仓库最大体积项之一。是否长期保留待用户决定，本轮未动。
5. **`archive/login-demo/`**：2026-11-09 在仓库根发现一个**未被 git 跟踪**的目录，整棵树 **0 个文件**、53 个空目录（`.idea`、`.mvn/wrapper`、`db`、`_recon`、`_run`、`src/main/java/com/example/logindemo/{config,security,user,web}`、`target/` 等），来源不明。按用户要求「所有东西都留着」，已移入 `archive/login-demo/` 并以 `README.md` + `.gitkeep` 留档（Git 不跟踪空目录，故目录结构本身无法入库）。
6. **`DoctorController.queryDoctorById` 用裸 `int id`**：不传参数返回 400，属可接受行为，仅记录。

## 5. 历史：2026-11-03 覆盖事件及之后

> 以下为历史记录。**注意**：第 7 节「本轮未提交的 git 改动一览」所述改动后续已被提交并推送（`13f5bc6` → e9fa66d → 08c418c → 387288c → cf24f32），不再是未提交状态。

2026-11-03 上午按 `jsj2434.rar`（课程原始压缩包）覆盖了 `jsj2434/`，把上个版本（18 测试、全局异常、全量建表）回退成课程原始实现，覆盖前完整快照在 [`bak/jsj2434-20261103-100731/`](bak/jsj2434-20261103-100731/MANIFEST.md)。

覆盖后陆续做了三次推进：

| 提交 | 内容 |
| --- | --- |
| `13f5bc6` | 覆盖回退本身 |
| `e9fa66d` | 页面改 Material Design，vendor 资产全离线 |
| `08c418c` | Doctor 增加单条件查询（`<choose>`）、动态更新（`<set>`）、批量查询（`<foreach>`） |
| `387288c` | 入库 Material Design 风格包与回滚快照 |
| `cf24f32` | 同步 jsj2434 README |

当前 jsj2434 的实际能力（已按代码核实）：

- **User**：`/` `/toLogin` `/login` `/toMain` `/logout` `/logout2` `/test/{name}/{password}` `/selectAllUsers` `/queryUserById/{id}` `/queryUserByNameAndPassword/{u}/{p}`；登录成功 `forward:/selectAllUsers`。
- **Doctor**：`/queryAllDoctors` `/doctorList` `/queryDoctorById` `/addDoctor` `/doctorQuery` `/queryDoctorsByCondition` `/queryDoctorsBySingleCondition` `/updateDoctor` `/queryDoctorsByIds`。
- **异常处理**：`GlobalExceptionHandler`/`Result` 已删，异常走 Spring Boot 默认处理。
- **测试**：仅 `Jsj2434ApplicationTests.contextLoads`。

## 6. 关键路径速查

| 项 | 值 |
| --- | --- |
| 仓库 / 远端 | `D:\project\githubclone\web` / `github.com/dessert28/web`（HTTPS），分支 `main` |
| 主项目 | `jsj2434/`（`com.zjsru`，端口 8080，账号 `admin/123`） |
| JDK 21 | 本机 `D:\project\.jdks\graalvm-jdk-21.0.7`；主开发机 `D:\pc\.jdks\graalvm-jdk-21.0.7` |
| JDK 17 | 本机 `D:\project\.jdks\graalvm-jdk-17.0.12`（仅归档的 shiyan2 用） |
| Maven | 无全局 `mvn`，统一 `.\mvnw.cmd` |
| MySQL | `localhost:3306`，`root/root`，库 **`mybatis`**，用根 `mybatis.sql` 手工导入 |
| 只读备份区 | `bak/`（只允许读取和新建，不允许修改） |
