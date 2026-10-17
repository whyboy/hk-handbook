# hk-handbook

项目基础工程：微信小程序与 H5 共用前端代码，Java 后端提供 RESTful JSON API。
当前前端仍为基础页面；后端已提供攻略内容接口、样稿本地预览和纠错存储。详见 [内容服务运行说明](backend/README.md)。

## 技术栈

- 前端：uni-app、Vue 3、TypeScript、Vite；npm lockfile 固定依赖。
- 后端：Java 21、Spring Boot 4.1.1、Maven Wrapper。
- 数据库：SQLite，Xerial SQLite JDBC 3.53.4.0；直接 JDBC，无 ORM。
- 接口约定：RESTful JSON，后续接口统一使用 `/api` 前缀。

## 目录

```text
frontend/       uni-app 前端，共用 src 编译 H5 与微信小程序
backend/        Spring Boot、Maven Wrapper、SQLite 配置及集成测试
docs/           产品需求、架构、研发流程和验证记录
.github/        PR 模板和 GitHub Actions CI
AGENTS.md       AI 开发规范
openhands-test.md  保留的 GitHub 集成测试文件
```

## 环境与运行

准备 Node.js 22 LTS 或 24、npm、JDK 21，并设置 JAVA_HOME。Maven Wrapper 首次运行需要网络下载 Maven 和依赖，无需全局安装 Maven。

前端：

```bash
cd frontend
npm ci
npm run dev:h5
# 或启动微信小程序增量编译
npm run dev:mp-weixin
```

H5 地址以终端输出为准。微信开发者工具导入 `frontend/dist/dev/mp-weixin`，
并在 `frontend/src/manifest.json` 的 `mp-weixin.appid` 中配置自己的小程序 AppID。
构建不需要 AppID；开发者工具预览和真机联调需要开发者自行配置。

后端（独立终端）：

```bash
cd backend
./mvnw spring-boot:run
# Windows 使用 .\mvnw.cmd spring-boot:run
```

默认监听 8080，`PORT` 可覆盖端口。内容接口位于 `/api/guides`，访问 `/` 返回 404 属于预期；`GET /api/health` 检查 SQLite 连接，健康时返回 200 和 `{"status":"UP"}`，失败时返回 503。
SQLite 默认在后端工作目录生成 `hk-handbook.db`（首次建立数据库连接时）；
可通过 `DATABASE_URL=jdbc:sqlite:/绝对路径/handbook.db` 覆盖，父目录需要提前存在。
数据库文件不会进入 Git。测试使用内存 SQLite，不污染本地数据。

## 构建与测试

```bash
cd frontend
npm ci
npm run type-check
npm run build:h5
npm run build:mp-weixin
cd ../backend
./mvnw clean verify
# 可执行包
java -jar target/hk-handbook-0.0.1-SNAPSHOT.jar
```

产物：`frontend/dist/build/h5`、`frontend/dist/build/mp-weixin`、`backend/target/*.jar`。
实际执行结果见 [验证记录](docs/VALIDATION.md)。微信产物构建成功不代表完成真机测试。

## 自动检查（CI）

向 `main` 提交 PR、更新 PR 或推送到 `main` 时，GitHub Actions 自动并行执行前端类型检查、H5／微信小程序构建和后端构建测试。CI 使用 Node.js 22、Temurin Java 21，无需生产服务器或部署密钥。

在 PR 的 Checks 或仓库 Actions 页面查看结果和失败日志；修复后推送即可重新检查。主分支保护需另行在 GitHub 设置，详见 [研发流程](docs/WORKFLOW.md)。CI 额外验证部署脚本，并保存 H5、JAR 和完整发布包；仅 `main` 上成功的 CI 发布包可用于生产部署。

## 手动发布（CD）

首次配置服务器和 GitHub 后，在 Actions → Deploy → Run workflow 选择 `main`，输入成功的 main CI 运行编号 `ci_run_id`。流水线下载对应发布包，通过 SSH 上传，备份 SQLite、切换 H5／JAR、重启后端并检查健康；失败则回退应用版本。合并 main 不会自动部署。

目标结构为 Nginx HTTP 8000 → H5 与本机 Java 9000；SQLite 独立保存。完整的首次安装、GitHub 配置和回退说明见 [部署说明](docs/DEPLOYMENT.md)。当前服务器与 GitHub production 环境已准备；配置文件合并到 main 并生成成功的 CI 发布包后，即可手动发布。

## 初始化来源

前端按 [uni-app 官方 CLI 文档](https://uniapp.dcloud.net.cn/quickstart-cli.html) 执行：
`npx degit dcloudio/uni-preset-vue#vite-ts frontend`。
保留官方模板及平台脚本，本项目验收范围仅 H5 与微信小程序。
后端通过 [Spring Initializr](https://start.spring.io/) 生成 Maven / Java 21 / Web / JDBC 工程。
Initializr 返回的 `4.1.1.RELEASE` 坐标在 Maven Central 不存在，已修正为正式发布坐标 `4.1.1`。

产品范围见 [PRD](docs/PRD.md)，设计见 [架构](docs/ARCHITECTURE.md)，协作见 [研发流程](docs/WORKFLOW.md)。

当前开发沙箱已在用户目录准备 JDK 21，可在此环境执行：

```bash
export JAVA_HOME="$HOME/.local/share/hk-handbook-toolchain/jdk-21.0.12.1+1"
export PATH="$JAVA_HOME/bin:$PATH"
```

此路径仅适用于当前沙箱；其他开发者应使用各自安装的 JDK 21。
