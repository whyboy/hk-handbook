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
