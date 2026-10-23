# sy1 — ASP.NET Web 应用空模板

VS2022 新建的 **ASP.NET Web Application（.NET Framework 4.7.2）**，带 Docker 支持与 IIS Express 配置。`<Compile>` 里只有 `Properties/AssemblyInfo.cs`，没有任何页面（`.aspx` / `Global.asax` / `Site.Master` 均不存在），等同于新建后未开发的模板。

- 构建需要 Windows + Visual Studio / MSBuild + .NET Framework 4.7.2 开发工具包，本仓库不做构建验证。
- Dockerfile 基于 `mcr.microsoft.com/dotnet/framework/aspnet:4.8-windowsservercore-ltsc2019`（Windows 容器）。
- `.vs/` IDE 状态已从版本库移除，本地保留。
