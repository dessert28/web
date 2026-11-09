# login-demo（已归档）

## 这是什么

2026-11-09 整理仓库时在仓库根发现的 `login-demo/` 目录，来源不明，**目录树里没有任何文件**（`find login-demo -type f` = 0），只有 53 个空目录：

```
login-demo/
├── .idea/
├── .mvn/wrapper/
├── db/
├── _recon/            # 疑似某次 AI 识别/重建过程的产物目录名
├── _run/
├── src/
│   ├── main/java/com/example/logindemo/{config,security,user,web}/
│   ├── main/resources/static/
│   └── test/java/com/example/logindemo/
└── target/            # 构建产物目录也在，同样为空
```

## 为什么单独留一份说明

**Git 不跟踪空目录**：`git add` 对空目录不产生任何记录，`git mv` 也无法移动它。如果只把空目录挪进来而不留文件，下次 `git clone` 出来时这棵目录树会彻底消失。

所以这里放一个 `.gitkeep` 占位，让 `archive/login-demo/` 这个归档事实能被版本库承载；目录结构本身只存在于发现它的那台机器上。

## 处理原则

- 按用户 2026-11-09 的要求：「所有东西都留着」，故未删除，仅移入 `archive/`。
- 因为内容为空，不存在代码、配置或凭据被归档或泄露的风险。
- 若日后发现这棵目录树确实有用途，可在此目录下补充真实文件（`README.md` 可替换）。

## 如需复查

```bash
# 确认归档时它确实是空的
find archive/login-demo -type d | wc -l    # 53（仅在该机器上）
find archive/login-demo -type f            # 只有本 README.md 与 .gitkeep
```
