# 研发流程

1. 阅读 PRD、架构和 AGENTS.md，检查工作区并确认任务范围。
2. 获取最新 main，在 feature/、fix/ 或 chore/ 分支开发；已有同名分支时先报告。
3. 仅修改任务相关文件，同步文档，不提交密钥、数据库或构建产物。
4. 前端执行 npm ci、npm run type-check、npm run build:h5、npm run build:mp-weixin；后端执行 ./mvnw clean verify。
5. 按用户授权提交与推送，使用 PR 模板记录变更、真实验证结果和未完成事项。
6. 人工审核后由维护者决定合并；禁止自动合并、强推和直接推送 main。

改动通过 PR 交由人工审核，不自动合并。生产发布由维护者手动触发 Deploy，合并不触发部署。CI 自动运行以下检查，本地仍可执行相同命令提前验证。
依赖变更必须同步 package-lock.json；保留 Maven Wrapper 以固定 Maven 版本。

## GitHub Actions CI

配置文件：`.github/workflows/ci.yml`。

- 触发：目标为 `main` 的 PR（创建、重新打开、更新提交）、`main` 的 push，以及手动触发。手动入口在 workflow 合并到默认分支后可用。
- `Frontend checks`：Node.js 22，执行 `npm ci`、`npm run type-check`、`npm run build:h5`、`npm run build:mp-weixin`。
- `Backend checks`：Temurin Java 21，执行 `./mvnw --batch-mode --no-transfer-progress clean verify`，包含内存 SQLite 集成测试。
- 前后端任务并行运行，每个任务最多 15 分钟；部署脚本检查与发布包任务最多 5 分钟；同一 PR／分支有新运行时取消旧运行。
- 缓存 npm 和 Maven 下载依赖，仍执行完整安装／构建；Actions 固定到提交 SHA，令牌仅允许读取仓库，不使用生产密钥。
- 不设置路径过滤，文档 PR 也生成完整检查结果，避免必需检查一直等待。

失败时打开 PR → Checks → 失败任务，查看具体步骤日志，使用相同命令在本地复现，修复后推送。CI 通过不代表微信真机、浏览器交互或全部业务功能已经验证。依赖审计现有问题见验证记录，本次不引入额外审计门禁。

## GitHub 网站配置（维护者操作）

1. 确认仓库 Settings → Actions → General 允许运行本工作流使用的 GitHub 官方 Actions。
2. 首次 PR 的 CI 运行后，在 Settings → Rules → Rulesets 或 Branches 中为 `main` 配置保护规则（可用功能取决于仓库可见性和 GitHub 套餐）。
3. 要求通过 PR 合并，配置人工审核；如果只有一位维护者，需考虑作者不能批准自己的 PR，避免设置无法满足的审核人数。
4. 启用合并前必须通过状态检查，选择 `Frontend checks` 和 `Backend checks`，建议要求分支与最新 `main` 保持同步。
5. 限制直接推送、强推和删除主分支；按团队权限控制规则绕过。

仅提交 workflow 不会自动设置分支保护。CI 不上传小程序，也不会部署服务器。

## 发布包与 CD

- `Deployment checks` 执行 Shell 语法检查，以及隔离环境中的发布、SQLite 备份、首次失败、升级失败回退和异常归档拒绝测试。
- `Release package` 等待前端、后端和部署检查均通过后生成 `release-<commit SHA>`，保存 30 天；中间 H5／JAR 产物保存 7 天。缓存不作为发布包使用。
- PR 也构建发布包以验证打包流程，但 Deploy 只允许来自本仓库 main、指定 CI workflow 且成功完成的 push／手动 CI 运行。
- 建议将 `Deployment checks` 和 `Release package` 一并加入必需检查。
- Deploy 仅支持从 main 手动触发，按生产环境串行执行，不取消正在进行的发布。GitHub 并发控制不是持久队列，避免连续提交多个发布请求。
- 服务器初始化及生产环境 secrets／variables 见 [部署说明](DEPLOYMENT.md)。
