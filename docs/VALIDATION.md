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

## E01-T01 首页框架（2026-10-18）

分支：`feat/e01-t01-home-framework`。仅实现首页展示、未开放提示及三个任务入口所需的最小说明页与导航；后端和依赖未修改。

- Node.js 24.21.0、npm 11.19.0 下 `cd frontend && npm ci`、`npm run type-check`、`npm run build:h5`、`npm run build:mp-weixin` 均成功。
- `npm ci` 仍报告 101 项上游依赖漏洞（20 low、23 moderate、58 high）；未修改依赖或执行自动修复。
- 检查微信编译产物：包含首页和工具说明两个页面，标题及路由配置正确；未把编译结果视为真机交互验证。
- 使用 `python3 -m http.server 4175 --bind 127.0.0.1 --directory frontend/dist/build/h5` 提供本机 H5 构建产物，Playwright／Chromium 走查通过：320、390、768、1280px 宽度无横向溢出；搜索未开放提示；三张卡片示例标记及攻略未开放提示；三个任务入口跳转和返回；三个有效场景与无效／缺失场景直接进入、刷新后返回首页。
- 浏览器走查未发现运行错误或 `/api/` 业务请求；已查看 390px 首页完整截图及说明页截图核对布局。验证脚本、截图及浏览器工具位于仓库外临时目录，不进入提交。
- `git diff --check` 和本次变更文档的相对链接检查通过。
- 未修改后端，未在本地重跑 `./mvnw clean verify`；PR CI 按原有配置执行后端检查。微信开发者工具、真机和产品负责人评审尚未执行；本次不合并或部署，也不开始 E01-T02。

## E01-T02 工具框架

- `cd frontend && npm ci`、`npm run type-check`、`npm run build:h5`、`npm run build:mp-weixin` 均通过。依赖未修改。
- H5 构建产物由本机 HTTP 服务提供，Playwright 检查四种屏宽、四工具说明与结果空状态、逐层和指定目标返回、直接进入／刷新、无效／缺失参数；具体结果在 PR 中记录。
- 后端未修改，未本地重跑 Maven；后端检查交由原有 PR CI。
- 微信开发者工具、真机及产品人工评审未执行。未部署，不自动关闭 Task；T02 合并前不开始 T03。
- H5 走查最终通过：320、390、768、1280px 工具列表无横向溢出；四工具导航、结果空状态、人才选择后的跨层返回、直接进入／刷新与异常参数均正常，无浏览器运行错误或业务 API 请求。首次脚本点击卡片内文字被 uni-button 截获，改为点击实际卡片按钮后通过，未使用强制点击。
- `git diff --check` 通过。浏览器脚本位于仓库外，未提交构建产物。

## E01-T03 攻略页与详情框架

分支：`feat/e01-t03-guides-framework`。仅新增攻略展示框架与首页导航，未修改依赖、后端或部署。

- `cd frontend && npm ci`、`npm run type-check`、`npm run build:h5`、`npm run build:mp-weixin` 均成功。npm 报告既有 101 项漏洞（20 low、23 moderate、58 high），本次未升级依赖。
- Playwright／Chromium 对实际 H5 产物走查通过：320、390、768、1280px 列表和详情无横向溢出；四分类筛选及返回保留选中项；首页三卡片与全部攻略入口；四篇详情直达／刷新后返回列表和首页；加载、空列表、失败、不存在状态；缺失／异常 ID 与未知 demo 参数。
- 浏览器无运行错误或业务 API 请求；已查看 390px 列表和详情完整截图。浏览器脚本与截图保存在仓库外。
- 检查微信 app.json 包含六个页面及攻略路由；`git diff --check` 通过。
- 后端未修改，本地未执行 `./mvnw clean verify`，原有 PR CI 执行后端检查。微信开发者工具、真机及产品人工评审未执行，不以编译成功替代。
- 未部署、未合并或自动关闭任务；T03 合并前不开始 T04。

## E01-T04 我的与记录框架

分支：`feat/e01-t04-profile-framework`。新增我的、记录空页面、清单布局与反馈表单布局，不修改依赖或后端。

- `cd frontend && npm ci`、`npm run type-check`、`npm run build:h5`、`npm run build:mp-weixin` 均通过。
- Playwright／Chromium 对 H5 构建产物走查通过：320、390、768、1280px 下我的、三类记录、反馈和清单布局无横向溢出；四入口、返回清单／我的／首页、直达／刷新和缺失／非法参数正常。
- 反馈输入与提交禁用；清单三阶段及禁用勾选可见。查看 390px 反馈及清单截图确认布局。未发现运行错误、业务 API 请求或浏览器 Storage 方法调用；源码无存储接口调用。
- `git diff --check` 通过。验证脚本和截图在仓库外，不提交产物。
- 后端未修改，本地未运行 `./mvnw clean verify`，原有 PR CI 执行后端检查。微信开发者工具、真机及产品人工验收未执行。
- 本次用户明确授权完成后合并，CI 通过后合并本 Task 并关闭 #29；未部署，不开始 T05。

## E01-T05 四 Tab 与公共组件（2026-10-19）

分支：`feat/e01-t05-navigation-framework`。继续验证已有四 Tab、公共页面容器及导航实现，保留工作区原有改动。

- `cd frontend && npm ci`、`npm run type-check`、`npm run build:h5`、`npm run build:mp-weixin` 全部通过。依赖审计报告既有 101 项漏洞（20 low、23 moderate、58 high），未修改依赖。
- 使用本机 HTTP 服务提供本次 H5 构建产物，Playwright／Chromium 走查通过：四 Tab 顺序与选中色、往返切换、二级页不显示 TabBar、攻略分类在详情返回及切换 Tab 后保留；二级页直达／刷新后返回相关 Tab 与首页；缺失／非法参数及公共加载、空、错误状态。
- 十个页面在 320、390、768、1280px 宽度下无横向溢出；走查期间未发现浏览器运行错误或业务 API 请求。脚本和截图位于仓库外临时目录。
- 微信编译产物检查通过：十个页面、四个 Tab 顺序及八个普通／选中图标文件齐全。`git diff --check` 通过。
- 本机 HTTP 服务首次启动使用 4179 端口失败（端口已被占用），改用独立 4183 端口完成走查，未停止原有服务。
- 后端未修改，本次未执行 `./mvnw clean verify`。微信开发者工具、真机、安全区及产品人工验收仍未执行，编译与浏览器检查不能替代这些验收。
- 本次未提交、推送、创建 PR、合并或部署。

### E01-T05 补充收尾检查（2026-10-19）

- Playwright 补测通过：从清单列表进入详情、直接进入布局示例、直接进入无效清单详情后，均可返回清单列表；连续三轮列表／详情往返及返回我的正常。
- 四 Tab 在 320、390、768、1280px 宽、600px 高的浏览器视口下滚动至页面底部后，最后一项内容未被 TabBar 遮挡；十个页面均接入 PageContainer。
- 首轮补测脚本使用了不存在的空状态文案导致超时，按实际文案修正；最初 scrollIntoView 只把元素对齐视口底部，尚未滚至页面末端，改为滚至文档底部后通过。两项均为测试脚本问题，未修改应用实现。

## 桌面底栏宽度调整（2026-10-19）

分支：`fix/desktop-tabbar-width`。用户提供桌面截图中仅露出工具、攻略。公网页面在自动化浏览器中四项均存在，未完整复现用户环境的裁切；确认原底栏在宽屏下全宽均分，首页和我的远离正文。此次将 H5 桌面四项入口居中限制在 760px 内，保留底栏全宽背景及移动端原生布局。

- `cd frontend && npm run type-check && npm run build:h5 && npm run build:mp-weixin` 全部通过。
- Playwright 对本次 H5 产物检查 320、390、753、1280、1920、2560px 六种宽度，四项边界均位于视口内；桌面四项总宽 760px，每项 190px；逐项点击及选中色通过。已查看 1280px 截图。
- 既有 T05 浏览器导航、分类保留、直达／刷新、异常参数、公共状态、四种屏宽和底部无遮挡检查通过；未发现运行错误或业务 API 请求。
- 微信 `app.wxss` 不包含 H5 底栏选择器；`git diff --check` 通过。未修改后端，未运行 Maven。
- 用户设备上的实际裁切原因与修复后效果仍需验证；未进行微信开发者工具或真机验证。本次未部署。

## E02-T01 内容契约与发布核验机制（2026-10-19）

分支：`automation/issue-10`。仅新增内容规范、JSON 字段契约、未发布模板及人工审核表，更新维护入口；未修改前后端、依赖或自动化配置。

- `python3 -m json.tool docs/content/template.json` 通过，模板无真实业务事实或人工签字。
- 本次 Markdown 本地链接、18 个顶层字段、5 个未核验章节、四分类与草稿状态检查通过。临时脚本首轮误将字段数写成 19，按契约核对为 18 并改用字段集合复验通过；不是生产模型校验测试。
- `git diff --check` 通过。单独完整 diff 与需求 review、公开资格边界推演记录见 [E02-T01 自查](content/E02-T01-REVIEW.md)；自查不是独立人工 APPROVED。
- 无前后端变更，本地未运行 npm／Maven 或 H5 浏览器走查；现有 PR CI 执行前后端、部署脚本及打包检查，实际结果以对应 head 的 GitHub Checks 为准。
- 运行时校验与查询尚未实现；未核验任何真实攻略事实，人工表为空表。#11 的人工实操、事实核验、创始人评审与 E01 微信真机／产品验收仍未完成；未部署。

## E02-T02 第一版内部样稿（2026-10-19）

分支：`automation/issue-11`。复用旧 PR #17 的真实港铁样稿与来源，归入 life；按 E02 契约整理 JSON 草稿并生成内部阅读 HTML，不修改生产前后端。

- `python3 docs/content/render_sample.py --check`、JSON／事实来源引用、人工字段保持 null、HTML 转义、拒绝不安全 URL／已发布状态／悬空引用、43 个 Markdown 本地链接及 `git diff --check` 均通过。
- Chromium 阅读页在 320／390／768／1280px 下草稿提示、四步骤、四来源、目录与返回顶部、无横向溢出和 JS 错误均通过；已检查 390px 截图。不是 uni-app 产品 H5 或微信真机验收。
- 官方 S1–S4 网页文本重新读取成功；浏览器访问 S3 返回 HTTP 403 / Access Denied，未执行动态路线查询，不将读取说明当作实操通过。
- 本地未执行 npm／Maven（无前后端变更），原有 CI 验证原工程。人工事实核验、真实查询及创始人评审仍未完成，保持 draft，未部署。详细自查见 [样稿评审记录](content/MTR-REVIEW.md)。

## E02-T02 阅读页事实追溯补齐（2026-10-19）

- 正文增加 F1–F5 事实锚点，新增逐条主张与适用条件，来源展示 JSON 中的原文位置；全部仍从同一 JSON 生成。
- `python3 docs/content/render_sample.py --check` 与 `git diff --check` 通过。
- 使用 Python HTMLParser 检查五个事实、四个来源、ID 唯一性及全部页内链接目标，均通过。
- 本轮未执行浏览器视觉走查；此前截图不能作为此次新增区域的视觉验收。未修改 frontend/backend，未运行 npm／Maven。未重新查询官方动态路线，人工核验仍待完成。

## E02-T02 事实追溯浏览器复验（2026-10-19）

- 使用现有 `/tmp/hk-e01-browser/node_modules/playwright` 与 Chromium，直接打开当前生成的阅读页，在 320、390、768、1280px 验证无横向溢出。
- 四种宽度均实际点击正文 F1 → 对应事实 → S1 来源 → 返回顶部，锚点跳转通过，无页面脚本错误。
- 首轮断言要求返回顶部后 scrollY ≤ 5，在 768px 失败；检查确认桌面 main 有 24px 顶部外边距，锚点定位正常。将断言改为 URL 为 #top 且目标顶部位于视口 0–24px 后，四种宽度重新执行全部通过；未为测试改动页面布局。
- 查看 390px 全页截图，新增事实、适用条件和来源位置正常换行，无遮挡。截图存于仓库外 `/tmp/mtr-reading-review-390.png`，不提交临时产物。
- 生成一致性与 `git diff --check` 再次通过。本次仅验证内部 HTML，不代表 uni-app H5、微信真机、官方路线实操或人工事实审核通过。

## E02-T03 内容查询服务第一版（2026-10-19）

分支：`feature/content-query-v1`，从当前工作分支创建并保留上一轮阅读页未提交修改。新增 Java 内容加载、结构校验、公开资格判断、公开投影和 `/api/guides` 列表／搜索／详情；未添加依赖、数据库表或部署设施。

- 首次 `./mvnw clean verify` 编译失败：使用了 Jackson 2 的 `TextNode` 类名，当前 Jackson 3 实际为 `StringNode`。按本机依赖 API 修正后重新执行完整命令通过。另一次文件写入因工作目录重复包含 backend 前缀失败，已在仓库根目录重写确认，未遗留嵌套目录。
- 最终以沙箱 JDK 21 执行 `cd backend && ./mvnw clean verify`，BUILD SUCCESS：53 tests，0 failures，0 errors，0 skipped（内容 50、既有应用／健康检查 3）。Mockito 动态 agent 警告为测试工具输出，未影响通过。
- 自动测试覆盖：结构／日期／未知字段／重复键／尾随对象／重复 ID／文件名／悬空引用；草稿与无效 published 隔离；当前 revision 与事实／链接／审核／发布条件；香港 00:00 到期、不含截止日、含生效日；公开字段去除内部身份和证据；分类＋人群＋关键词交集、稳定分页计数、空结果、非法参数、404／410 区分。
- 用生产 JAR 启动本机随机端口（127.0.0.1，SQLite 内存库），实际 HTTP 检查通过：health 200，guides 分类搜索 200 空列表，mtr-route 404，preview=true 400；攻略响应 Cache-Control 为 no-store。检查 JAR 不包含合成测试样本。检查后已停止临时进程，无部署、无 SQLite 落盘。
- `python3 docs/content/render_sample.py --check` 与 `git diff --check` 通过。README、架构、发布规范和 E02-T03 接口／运行说明已更新。
- 未修改 frontend，因此未重跑前端类型检查及两端构建；本轮未完成前端 API 接入、微信真机验收、港铁人工事实核验或真实路线查询。生产内容目录没有正式 JSON，列表为空符合预期；不伪造人工签字来填充内容。未提交、推送、创建 PR 或部署。

## E02-T04 攻略页面接入（2026-10-19）

在 `feature/content-query-v1` 继续，保留 T03 与内部阅读页的现有修改；新增共用攻略 API 客户端、真实列表／详情／首页入口、筛选分页及工具／清单上下文入口，未改依赖版本或后端代码。

- `cd frontend && npm ci` 成功；本次安装输出已有依赖审计 101 项（20 low、23 moderate、58 high），以及废弃包和 install-scripts 提示。没有运行自动升级或 audit fix，未将依赖审计称为通过。
- 最终 `npm run type-check`、`npm run build:h5`、`npm run build:mp-weixin` 全部通过。初轮类型检查因响应类型收窄后访问详情字段失败，已调整校验顺序；表单事件被类型库识别为 DOM Event 时，改为 unknown 入参并检查 detail.value.keyword 后通过。
- 浏览器首轮发现快速输入立即搜索会提交旧关键词；查阅本机 uni-h5 实现确认输入绑定事件有 100ms 节流。搜索改用 confirm 事件值和 form submit 实际字段值，快慢响应交错验证通过。
- 浏览器发现返回列表可能复用旧 Tab 状态而重置筛选；使用会话内存保存当前条件，每次 onShow 恢复后再消费上下文条件，回归通过。测试自身直达入口改为每例重新加载文档，避免把 hash 导航后的合法筛选保留误判为全新页面。
- 用当前生产 JAR（内存 SQLite、127.0.0.1 随机端口）和 H5 生产产物搭建仓库外临时测试服务，运行 `frontend/tests/guides.browser.cjs`。真实后端验证首页／列表空状态及港铁草稿 404；随后仅在浏览器拦截层使用测试样本验证 21 条分页、加载更多失败保留与重试、分类／人群交集、搜索竞态、分类保留、详情日期与正文、首页卡片、直达／刷新、404／410、503 重试、非法 ID／畸形响应、加载状态、工具和清单分类入口，全部通过，无页面 JS 错误。
- 320／390／768／1280px 列表与详情均无横向溢出；查看 390px 全页截图，正文、日期、来源和按钮换行正常。官方来源按钮在浏览器测试中打开预期 HTTPS 新标签，opener 为 null；来源站响应由测试拦截，不声称真实来源可访问。
- 临时 Python 静态服务器在页面切换取消下载时出现一次 BrokenPipeError；浏览器断言与退出状态均通过，未将其隐瞒为应用错误或重新测试的理由。所有临时服务器在检查后关闭；运行日志／截图只保存在 /tmp。
- 检查微信产物使用“复制来源链接”与 setClipboardData，不含 window.open 分支；H5 构建不含合成测试正文。`git diff --check` 与更新文档本地链接检查通过。
- 本轮后端未修改，未重跑 Maven，沿用上轮已记录的 53 项通过结果。微信仅完成构建及产物检查，未配置真实 HTTPS request 域名、运行微信开发者工具／真机或实测剪贴板权限失败。没有发布真实攻略，未完成 T02 人工核验／实操／产品评审，未提交、推送、创建 PR 或部署。

## E02-T06 首页六主题与规划调整（2026-10-20～21）

分支 `feature/e02-t06-home-topics`，基于已合并 #60 的 main，在独立 worktree 开发；原工作区 README、VALIDATION.md 和 E03 草稿保持原样。本次无后端及依赖版本变更。

- `cd frontend && npm ci` 成功；已有依赖审计报告 101 项（20 low、23 moderate、58 high），未自动升级依赖，不将审计称为通过。
- `npm run type-check`、`npm run build:h5`、`npm run build:mp-weixin` 全部通过。截图发现人才主题名称换行不整齐，改为“高才／优才”和“申请”两行后重新执行三项检查通过。
- 一次修改脚本因工作目录误加 frontend 前缀报 FileNotFoundError，未修改文件；改用绝对路径后成功。Project 描述首次更新使用不支持的 --short-description 参数，改为 --description 后成功。
- `topics.browser.cjs` 在 320／390／768／1280px 验证六入口两行三列、窄屏无横向溢出、六主题点击／直达／刷新／返回、搜索和四 Tab，全通过，无页面脚本错误；查看最终 390px 截图。主题测试只拦截攻略空列表，不提供假正文或投票。
- `guides.browser.cjs` 使用本次 H5 与既有已验证后端 JAR（127.0.0.1、内存 SQLite）回归，真实空内容／草稿 404、拦截样本分页／搜索／详情／失败／竞态及四屏宽均通过。后端未修改，未重跑 Maven；不把旧测试结果算作本轮后端测试。
- 修改文档的本地链接和 `git diff --check` 通过。临时服务器、截图和日志在仓库外；产物不提交。微信开发者工具／真机及人工产品验收未执行，未部署。
- #61 已关联 E02 #20 和 Project，状态 In Progress。按用户授权删除旧 E03～E08 的 6 个 Epic（#21～26）及 17 个子任务（#33～49），核对 Project 已无旧任务条目；README、PRD、ROADMAP、架构和 E02 父任务同步新规划。旧任务删除不代表业务实现完成。
