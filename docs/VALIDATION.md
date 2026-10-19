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
