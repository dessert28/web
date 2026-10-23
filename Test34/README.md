# Test34 — Maven 练习空壳

`org.example:Test34:1.0-SNAPSHOT`，Java 21。只有一个 `Main.java`（打印 `Hello and welcome!` 和 1..5），依赖 `mysql-connector-j 8.4.0` 但没有任何连库代码。

```powershell
$env:JAVA_HOME = 'D:\pc\.jdks\graalvm-jdk-21.0.7'
.\mvnw.cmd -B clean compile     # BUILD SUCCESS，无测试
```

已补 Maven Wrapper（复制自 `jsj2434`）与阿里云镜像配置，可以直接构建。
