# E02-T04 搜索调整验证（2026-10-20）

分支 `feature/e02-t04-search-relevance`。移除搜索页人群筛选，保留分类；正文检索、相关度排序、匹配片段及编辑推荐已接入。保留既有人群内容字段和旧 API 参数兼容，不新增依赖、数据库表或热度统计。此前 E03 草稿及 README、VALIDATION.md 未提交修改不纳入本次提交。

- `cd frontend && npm run type-check && npm run build:h5 && npm run build:mp-weixin` 全部通过，使用现有已安装依赖。
- Java 21 执行 `cd backend && ./mvnw clean verify` 通过。第一次 54 项测试通过；补充编辑推荐排序测试后重新执行完整检查，最终 55 项，0 failures/errors/skipped。覆盖正文命中、标题优先、关键词次级、分页前排序、分类交集、匹配片段、内部身份不检索、草稿隔离、推荐缺失／隐藏 ID 跳过及查询不受推荐顺序影响。
- 使用本次生产 JAR（内存 SQLite、127.0.0.1:18080）和 H5 构建产物，执行 `NODE_PATH=/tmp/hk-e01-browser/node_modules H5_TEST_BASE_URL=http://127.0.0.1:14173 node frontend/tests/guides.browser.cjs`，全部通过。
- 浏览器先访问真实空内容后端，确认首页／列表为空、草稿 404；有内容分支仅用浏览器请求拦截的合成样本。验证无人群筛选且请求不含 audience、分类与关键词、匹配片段、分页与失败重试、搜索竞态、详情与返回、错误状态和上下文入口；无页面脚本错误。
- 320／390／768／1280px 无横向溢出，查看 390px 列表截图确认分类和推荐文案。截图／临时服务器脚本／日志放在 /tmp，不提交。
- `git diff --check` 通过。未运行微信开发者工具／真机；未发布真实攻略、未执行人工内容核验或部署。当前编辑推荐列表为空，未来经审核的内容 ID 可按顺序填入 recommendations.txt；没有伪造热门数据。
