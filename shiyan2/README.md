# shiyan2 — 实验二：Thymeleaf 商品信息管理

Thymeleaf 表单绑定与列表渲染练习。商品保存在 `GoodsServiceImpl` 的静态 `ArrayList` 里，进程重启即清空，不涉及数据库。

- 技术栈：Spring Boot 4.1.1 · Java 17 · Thymeleaf
- 端口：8080

## 运行

```powershell
$env:JAVA_HOME = 'D:\pc\.jdks\graalvm-jdk-17.0.12'
.\mvnw.cmd spring-boot:run      # http://localhost:8080/shiyan1/toAdd
.\mvnw.cmd -B clean test        # 1 个用例：上下文加载
```

## 路由

| 方法 | 路径 | 行为 |
| --- | --- | --- |
| GET | `/shiyan1/toAdd` | 渲染添加表单 `addGoods.html`，模型属性 `goods` |
| POST | `/shiyan1/addGoods` | 接收 `Goods`（`goodsName`、`originalPrice`、`discountPrice`），加入列表后渲染 `goodsList.html` |

`Goods` 的 `id` 由服务层自增，价格字段用 `BigDecimal`。

## 页面

![商品列表](goodsList.html.png)

添加页：[`addGoods.html.png`](addGoods.html.png)

## 实验报告

- `实验一实验报告.docx`
- `实验二_Thymeleaf实验报告.doc`
- `实验报告模板.doc`

`report-work/` 是写报告时的中间产物（截图、渲染稿、旧版本），**未纳入版本库**，只保留在本地。
