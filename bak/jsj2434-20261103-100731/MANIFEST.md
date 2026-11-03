# 备份：jsj2434 覆盖前快照

- 备份时间：2026-11-03 10:07:31
- 来源仓库：`D:\project\githubclone\web`，分支 `main`
- 来源 commit：`7560a92 jsj2434_20261023_01`（覆盖前的 HEAD）
- 触发原因：按 `jsj2434.rar` 覆盖 `jsj2434/`，回退到压缩包版本

## 内容

| 路径 | 说明 |
| --- | --- |
| `worktree/` | 覆盖前磁盘上的 jsj2434 源码树（排除 `target/`、`.idea/`、`.acl-recovery/`），38 个文件 |
| `head.tar` | `git archive HEAD jsj2434` 的完整 tar 快照（含被删的测试与文档） |
| `untracked/mybatis_dump.sql` | 覆盖前未跟踪文件，从 `jsj2434/` 迁出 |
| `untracked/doctor-crud-test.html` | 覆盖前未跟踪文件，原位于 `jsj2434/src/main/resources/static/` |
| `MANIFEST.md` | 本文件 |

## 覆盖时被删除的 10 个 git 跟踪文件

```
jsj2434/README.md
jsj2434/src/main/java/com/zjsru/domain/Result.java
jsj2434/src/main/java/com/zjsru/exception/GlobalExceptionHandler.java
jsj2434/src/main/resources/mybatis.sql
jsj2434/src/main/resources/schema.sql
jsj2434/src/main/resources/static/crud-test.html
jsj2434/src/test/java/com/zjsru/controller/GlobalExceptionHandlerTest.java
jsj2434/src/test/java/com/zjsru/controller/UserControllerSecurityTest.java
jsj2434/src/test/java/com/zjsru/controller/UserPageTest.java
jsj2434/src/test/java/com/zjsru/service/UserServiceTest.java
```

## 还原方式

```powershell
cd D:\project\githubclone\web
# 方式一：从 git 恢复跟踪文件到覆盖前状态
git checkout 7560a92 -- jsj2434
# 方式二：从本备份拷回整棵树（含未跟踪文件）
robocopy bak\jsj2434-20261103-100731\worktree jsj2434 /E /XD target .idea .acl-recovery
Copy-Item bak\jsj2434-20261103-100731\untracked\mybatis_dump.sql jsj2434\mybatis_dump.sql
Copy-Item bak\jsj2434-20261103-100731\untracked\doctor-crud-test.html jsj2434\src\main\resources\static\doctor-crud-test.html
```
