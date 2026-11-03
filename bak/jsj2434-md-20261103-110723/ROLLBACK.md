# Material Design 改造回滚说明（jsj2434）

生成时间：2026-11-03 11:07 (+08:00) · 基线 HEAD：`7560a92`

## 一、备份内容

本目录是**改造前**的完整快照：

- `templates/`：5 个原始模板（`index` / `login` / `main` / `doctorList` / `doctorQuery`）
- `vendor-src/`：改造过程中生成的字体 base64、Roboto CSS 与 Tailwind 运行时（便于重建）
- `md-material.config.js`：改造时新增的 Tailwind 配置层（改造前不存在，回滚即删除）

## 二、一键回滚（PowerShell，仓库根执行）

```powershell
cd D:\project\githubclone\web

# 1) 还原 5 个模板
#    shell 对 jsj2434/src/** 无写权限时，改用文件工具把 bak 内同名文件覆盖回去
Copy-Item 'bak\jsj2434-md-20261103-110723\templates\*' 'jsj2434\src\main\resources\templates\' -Force

# 2) 删除本次新增的样式与运行时
Remove-Item 'jsj2434\src\main\resources\static\vendor' -Recurse -Force
Remove-Item 'jsj2434\src\main\resources\static\css\md-material.css' -Force

# 3) 清理临时目录
Remove-Item 'jsj2434\vendor-src' -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item '.tmp-fetch' -Recurse -Force -ErrorAction SilentlyContinue
```

## 三、git 级回滚（更彻底）

```powershell
git checkout -- jsj2434/src/main/resources/templates
git clean -fdn jsj2434/src/main/resources/static   # 先预演
git clean -fd  jsj2434/src/main/resources/static   # 确认无误再执行
```

`git clean` 会连 `static/` 下**所有未跟踪文件**一起删掉，执行前务必先跑 `-fdn` 预演。

## 四、改造前已存在的差异（不是本次引入，回滚后仍会存在）

- `static/crud-test.html` 在索引里标记为 `D`（覆盖 `jsj2434.rar` 时删除）
- `templates/index.html`、`login.html`、`main.html` 相对 HEAD 已有修改
- `templates/doctorList.html`、`doctorQuery.html` 是覆盖后手工新增的未跟踪文件

## 五、结论

「本备份还原 + `git checkout`」可回到 2026-11-03 11:07 的精确状态。
