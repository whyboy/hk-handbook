# 初始化验证记录

验证日期：2026-10-14。工作分支：`chore/initialize-project`。

| 项目 | 实际结果 |
| --- | --- |
| 官方初始化 | `npx --yes degit dcloudio/uni-preset-vue#vite-ts frontend` 成功 |
| 依赖安装 | `npm install` 成功，生成 package-lock.json |
| H5 | `npm run build:h5` 成功，生成 dist/build/h5 |
| 微信小程序 | `npm run build:mp-weixin` 成功，生成 dist/build/mp-weixin |
| TypeScript | `npm run type-check` 成功 |
| 后端 | Java 21 下 Maven Wrapper `clean verify` 成功，生成可执行 JAR |
| SQLite | 1 项测试通过，0 失败、0 错误、0 跳过；内存数据库 SELECT 1 和外键配置验证通过 |
| 启动检查 | 可执行 JAR 在临时端口 18080 启动，HTTP / 返回预期 404（无业务路由）；检查后已停止 |

环境：Node.js 24.21.0、npm 11.19.0、Temurin JDK 21.0.12.1、Maven Wrapper 固定 Maven 3.10.0。
原先环境没有 Java/Maven，Java 安装在用户工具目录，未加入仓库；Maven 由 Wrapper 下载。

## 未完成及已知事项

- 未进行微信开发者工具、真机或浏览器交互验收；需配置自己的微信 AppID。
- 未开发业务页面、业务 API 或数据库表，未进行前后端业务联调。
- npm 安装报告 101 项漏洞：22 low、23 moderate、56 high；存在官方模板上游弃用依赖。
  本次保留模板配套版本，未执行强制修复，后续需单独评估和验证依赖升级。
- 初始化器给出的 Spring Boot 4.1.1.RELEASE 在 Maven Central 返回 404，已修正为存在的 4.1.1。
- CI/CD 和生产部署未配置；初始化分支提交后通过 PR 交由人工审核，不自动合并。

## CI 接入验证（2026-10-16）

分支：`chore/add-ci`。本次仅增加 CI 和文档，不修改应用代码或依赖。

- 本地 Node.js 24.21.0 下 `npm ci`、`npm run type-check`、`npm run build:h5`、`npm run build:mp-weixin` 全部成功；CI 配置的 Node.js 22 由 PR 的 GitHub Actions 运行验证。
- 本地 Temurin Java 21.0.12.1 下 `./mvnw --batch-mode --no-transfer-progress clean verify` 成功，1 项测试通过，无失败、错误或跳过。
- `npm ci` 报告 101 项依赖漏洞（20 low、23 moderate、58 high）及上游弃用提示，本次未变更依赖或执行自动修复。
- 本地未安装 actionlint，未执行该工具检查；工作流的实际执行结果以 PR Checks 为准。
- 未进行浏览器交互、微信真机测试、部署或 GitHub 主分支保护设置。

## CD 配置本地验证（2026-10-16）

分支：`chore/add-cd`。增加手动发布 workflow、发布包归档、部署与初始化脚本、数据库健康接口及相关测试。

- Node.js 24.21.0 下 `npm ci`、`npm run type-check`、`npm run build:h5`、`npm run build:mp-weixin` 均通过；依赖审计仍报告 101 项（20 low、23 moderate、58 high），未修改依赖。
- Java 21 下 `./mvnw --batch-mode --no-transfer-progress clean verify` 通过，3 项测试通过。
- 启动真实 JAR，绑定 127.0.0.1:18080，使用临时 SQLite，HTTP `/api/health` 返回 200 和 UP；验证后已停止并清理临时数据库。
- `python3 deploy/test_release.py`：4 项测试通过，覆盖成功发布与 SQLite 备份、升级失败回退、首次发布失败、路径越界拒绝和校验和篡改拒绝。首次运行发现无旧版时错误判定回退目标，修正后复验通过。
- `deploy/package.sh` 使用全零测试 SHA 生成本地测试发布包，SHA256 校验通过；该包仅验证格式，不代表 main 的已验证发布版本，不用于上线。
- 所有部署 Shell 脚本通过 `bash -n`；两个 workflow 通过 actionlint。
- Nginx 独立临时配置通过 `nginx -t`，未加载到运行中的 Nginx。
- 未执行服务器初始化脚本，未安装 systemd 服务或部署密钥，未配置 GitHub production 环境，未验证公网 SSH／HTTP，未运行 GitHub CD；公网地址、SSH 端口、HTTP 端口与云安全组待确认。

## CD 服务器准备（2026-10-16）

维护者确认使用 HTTP 8000、Java 127.0.0.1:9000，SSH 端口 22，并已在 Azure 放行 TCP 8000。

- 已统一调整部署脚本、Nginx 和文档的端口。4 项部署脚本测试、Shell 语法检查、actionlint 和 `git diff --check` 均通过。
- 已执行 `sudo bash deploy/setup-server.sh /home/whyancool/.local/share/hk-handbook-toolchain/jdk-21.0.12.1+1`，安装独立 JDK、用户、目录、systemd 单元和 Nginx 站点。sudoers 与 Nginx 检查通过，原有站点保留。
- systemd 单元检查未报告本项目错误；工具额外输出了主机已有 v2ray 单元的 nobody 用户提示，未修改该无关服务。
- 已验证应用用户能写 data，不能写 releases。
- 已生成专用部署 SSH 密钥（位于仓库外），公钥带 restrict 限制；使用固定且与本机一致的主机密钥，通过目标公网 IP 验证 hk-deploy 登录、上传目录权限及仅限本项目停止／重启的 sudo 权限。
- 已创建 GitHub production 环境，限制分支 main，设置 DEPLOY_HOST／DEPLOY_PORT 及 DEPLOY_SSH_KEY／DEPLOY_KNOWN_HOSTS。没有将密钥或实际环境文件加入仓库。
- Nginx 正在监听 8000。从此服务器经公网 IP 请求 HTTP 得到预期 503（尚未有发布包）；这不等同于独立外网或 GitHub runner 连通性验证。
- Java 服务已设为开机启用，但尚未启动；9000 暂无监听。未部署本地测试包、未写入生产数据库。
- 待完成：CD 改动提交／审核／合并，main CI 构建发布包，手动触发首次 Deploy 并验证 GitHub runner SSH、真实 systemd 启动和外部访问。
