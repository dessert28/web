# 根据将生成或运行容器的主机的操作系统，可能需要更改 FROM 语句中指定的映像。
# 有关详细信息，请参阅 https://aka.ms/containercompat

FROM mcr.microsoft.com/dotnet/framework/aspnet:4.8-windowsservercore-ltsc2019
ARG source
WORKDIR /inetpub/wwwroot
COPY ${source:-obj/Docker/publish} .
