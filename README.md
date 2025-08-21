# UIToolkit

简体中文说明 | English: See below

UIToolkit 是一个基于 Fabric 的 Minecraft 客户端/模组工具包，提供 UI 相关的工具和素材以便在模组或资源包中复用。

仓库结构（节选）

- `src/` - 源代码（Fabric mod）
- `assets/uitoolkit/` - UI 贴图与纹理
- `build.gradle`, `gradle/` - Gradle 构建配置

主要功能

- 提供可复用的 UI 组件与贴图素材
- 与 Fabric Loader / Fabric API 兼容的示例实现

快速开始（开发者）

先决条件

- Java 17 或更高（请参阅项目中的 Gradle 配置以确认精确版本）
- Git
- Gradle Wrapper（仓库包含 `gradlew`/`gradlew.bat`）
- Fabric Loader + Fabric API（用于在 Minecraft 中运行构建产物）

开发与运行（Windows PowerShell 示例）

1. 构建项目并生成 mod jar：

```powershell
./gradlew build
```

2. 在开发环境中运行 Minecraft 客户端（调试用）：

```powershell
./gradlew runClient
```

3. 将生成的 jar 文件复制到 Minecraft 的 `mods/` 目录进行测试：

```powershell
Copy-Item -Path .\build\libs\*.jar -Destination "$env:APPDATA\.minecraft\mods\"
```

生成的 jar 通常位于 `build/libs/`。

安装到 Minecraft

1. 在 `build/libs/` 找到 `UIToolkit-<version>.jar`。
2. 将 jar 放入 Minecraft 的 `mods/` 文件夹。
3. 启动使用相同 Fabric loader 版本的 Minecraft 客户端。

贡献

- 欢迎提交 Issue 与 Pull Request。
- 修改前建议先开 Issue 简述你的计划，以便讨论与协调。

许可

本项目遵循仓库根目录中的 `LICENSE.txt`（请参阅该文件了解详细条款）。

联系方式与致谢

- 作者/维护者: 仓库所有者 (lxyesa)
- 感谢 Fabric 社区与所有开源贡献者。

-----

English (short)

UIToolkit - a small UI toolkit for Minecraft (Fabric).

Quick build:

```powershell
./gradlew build
```

Run client:

```powershell
./gradlew runClient
```

See `LICENSE.txt` for license details.

