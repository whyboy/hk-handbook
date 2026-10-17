# 内容服务运行说明

详细需求与内容规范保存在 [#10](https://github.com/whyboy/hk-handbook/issues/10)，样稿及人工评审事项在 [#11](https://github.com/whyboy/hk-handbook/issues/11)。

## 接口

- `GET /api/guides?q=港铁&category=transport&audience=family`：标题、摘要和关键词搜索，分类、人群条件同时生效；返回摘要数组。空结果是 `[]`，无效筛选返回 400。
- `GET /api/guides/{id}`：返回 `{guide, status, verifiedOn}`。未发布和不存在返回 404，下架返回 410；到期文章仍可阅读但状态为 `recheck`，不进入列表。
- `POST /api/guides/{id}/corrections`：JSON `{ "kind": "incorrect", "message": "问题说明", "referenceUrl": "" }`。类型为 `outdated` / `incorrect` / `other`，正文 1–2000 字符，来源可选（HTTPS，最多 500 字符）。持久化成功返回 201 和 `{id,status:"received"}`；不是自动修正或人工处理完成。

纠错表在启动时以 `CREATE TABLE IF NOT EXISTS` 创建；不改变现有表，不删除数据。纠错记录通过数据库由维护者查看，目前没有管理界面、通知或自动处理。请勿提交身份证件、账号、手机号等个人信息。匿名写入暂未实现限流；开放公网运营前需评估滥用和存储清理策略。

## 内容维护与本地预览

内容位于 `src/main/resources/guides/*.json`，通过 PR 人工修改并重新构建发布，不包含爬虫、AI 自动审核或 CMS。启动时校验字段、分类、人群、来源引用和发布条件；不合格内容使启动失败，避免静默发布。

当前仅有 `mtr-route` 待核验样稿，因此默认公开列表为空，详情返回 404。看样稿时仅在本地启动：

```bash
cd backend
HANDBOOK_PREVIEW_ENABLED=true ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.address=127.0.0.1
# 另一个终端
curl 'http://127.0.0.1:8080/api/guides/mtr-route?preview=true'
```

所有内容接口可加 `preview=true`；只有显式开启预览时有效。此开关不提供登录保护，**不得在公网服务启用**。预览纠错存储 `preview=1`，与公开提交区分。生产默认关闭，无需修改 CD。

样稿保留 AI 查阅日期，人工核验、发布日期为空。公开发布前，人工填写每项来源的 `verifiedBy` / `verifiedOn`、审核人 `approvedBy`、发布人 `publishedBy`、`publishedOn` 和 `reviewDueOn`，核实所有 `unverified` 段落再调整状态。程序校验字段完整性，不能判断签字真实性，也不能代替人工评审。

正文分段用纯文本展示；`official` 段落必须有有效 `sourceIds`，`editorial` / `experience` 与官方事实分开。必需的适用条件、准备事项、操作步骤、费用与耗时、注意事项不能遗漏。未知不能写成“不适用”。文章核验日期取来源核验日期的最早值，到复核日自动从公开列表移除。

## 验证

`./mvnw clean verify` 使用内存 SQLite，覆盖草稿隔离、预览、组合搜索、发布校验、到期/下架和纠错入库。首次运行需要 JDK 21。人工来源核验及真实设备验证仍待执行。
