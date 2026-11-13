# AGENTS.md — 在 hospital-registration 里工作的规则

本文件是**规则**（怎么做、不能做什么）。状态与进度看 [HANDOFF.md](HANDOFF.md)，全貌看 [README.md](README.md)。

接手任务前按 **本文件 → HANDOFF.md → README.md** 的顺序读完再动手。

适用范围：`D:\project\githubclone\web\hospital-registration\`。仓库级规则见仓库根 [AGENTS.md](../AGENTS.md)，仍然有效；本文件只补充本子项目的特殊约定。

---

## 1. 定位

- 本目录是仓库主项目 `../jsj2434` 的**干净副本**，用来完成「实验二：基于 Spring Boot 集成 MyBatis 实现患者挂号」。
- **与 `jsj2434` 没有代码依赖**，各自独立构建、独立运行（`jsj2434` :8080，本项目 :8082）。
- **不要为了本项目去改 `jsj2434`**；那边要保持原样。两边共用同一个 MySQL 库 `mybatis`，本项目只**新增**表/列，不改既有表结构、不删既有数据。

## 2. 硬红线

1. **不要改 `.mvn/`**：`settings.xml`（阿里云镜像）与 `maven.config`（`--settings`）是从 `jsj2434` 原样复制的，离线/内网环境下它们是能否解析依赖的关键。
2. **不要改 `db/experiment2-appointment.sql` 里既有的表结构语义**：它只允许「建 `tb_appointment`」「给 `doctor_info` 加 `specialty`/`password`」。要加新表新列另写脚本，并且必须**幂等**（可重复执行不报错），因为 `jsj2434` 与其他人可能共用这个库。
3. **不要重命名模板文件名与 URL 路径**：`templates/*.html` 的名字被控制器 `return "..."` 直接引用，路由字符串被测试断言引用。
4. **不要把 `doctorId` 做成请求参数**：医生端必须只从会话 `LoginUser.doctorId` 取身份，否则就是越权查别人患者。
5. **不要删除测试**。测试是契约说明书；断言与实现冲突时先判断谁对，再改其中一侧。
6. **不要提交构建产物**：`target/`、`run.log`、`run.err.log` 已在 `.gitignore` 里，提交前用 `git status` 逐条确认。
7. **不要执行 `git push --force`**，不要改写已推送历史。提交/推送前与用户确认。

## 3. 环境

| 项 | 内容 |
| --- | --- |
| JDK | **21**。本机实际路径 `D:\pc\.jdks\graalvm-jdk-21.0.7`（`jsj2434` 文档里写的 `D:\project\.jdks\...` 在本机**不存在**）。PATH 上的 JDK 15 会直接失败，跑 Maven 前必须 `$env:JAVA_HOME = 'D:\pc\.jdks\graalvm-jdk-21.0.7'` |
| Maven | 未安装 `mvn`，一律用 `.\mvnw.cmd` |
| MySQL | 8.x，`localhost:3306`，`root/root`，库 `mybatis`；CLI 在 `C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe` |
| 编码 | 源码/模板 UTF-8；PowerShell 看中文可能乱码，判断结果看 `BUILD SUCCESS/FAILURE`、`Tests run:`、退出码 |

## 4. 构建与验证

改完代码**必须**跑全量测试：

```powershell
cd D:\project\githubclone\web\hospital-registration
$env:JAVA_HOME = 'D:\pc\.jdks\graalvm-jdk-21.0.7'
.\mvnw.cmd -B clean test        # 期望 Tests run: 39, Failures: 0, Errors: 0 / BUILD SUCCESS
```

- 测试**真连 MySQL**（`RegistrationServiceTests` / `RegistrationFlowTests`），跑之前确认库和表在。
- 测试会自己造 `tb_appointment` 行，`@AfterEach` 按 `patient_name` 精确删除；新增用例也要同样自清理，**不要**用 `DELETE FROM tb_appointment` 全表清。
- 起服务冒烟：`.\mvnw.cmd spring-boot:run` → `http://127.0.0.1:8082/`。
- 交付说明里要区分「**我实跑过**」与「历史记录」。

## 5. 代码约定

- 分层：`controller` / `service`（接口 + Impl）/ `domain` / `mapper`；User 模块走注解 SQL，Doctor/Department/Appointment 走 XML。
- 模板复用 `templates/fragments/layout.html` 的片段：`head(title)`、`appbar(title, active)`、`alert(message, tone)`、`departmentGrid(departments)`、`searchResultGrid(results)`。新页面照抄现有页面的 Tailwind + `md-*` 配方，不要自己另发明一套样式。
- **`th:each` 不要和 `th:replace` 写在同一标签上**（`th:replace` 会连宿主标签一起替换），循环放到片段内部。
- 新增模板时，`th:href="@{...}"`、`th:action="@{...}"` 与测试里的字符串断言要保持一致。

## 6. 沟通与交付

- 交付给：改了哪些文件、**实跑**的命令与结果、仍未消除的风险。
- 发现与任务无关的问题（例如从 `jsj2434` 继承来的旧 bug）→ **记录到 HANDOFF.md**，不要顺手改掉。
- 用户用中文，交付与说明用中文；代码、命令、路径、报错原文保持原样。
