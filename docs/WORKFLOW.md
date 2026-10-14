# 研发流程

1. 阅读 PRD、架构和 AGENTS.md，检查工作区并确认任务范围。
2. 获取最新 main，在 feature/、fix/ 或 chore/ 分支开发；已有同名分支时先报告。
3. 仅修改任务相关文件，同步文档，不提交密钥、数据库或构建产物。
4. 前端执行 npm ci、npm run type-check、npm run build:h5、npm run build:mp-weixin；后端执行 ./mvnw clean verify。
5. 按用户授权提交与推送，使用 PR 模板记录变更、真实验证结果和未完成事项。
6. 人工审核后由维护者决定合并；禁止自动合并、强推和直接推送 main。

初始化改动通过 PR 交由人工审核，不自动合并、不部署。CI/CD 留待后续需求配置，当前检查手动执行。
依赖变更必须同步 package-lock.json；保留 Maven Wrapper 以固定 Maven 版本。
