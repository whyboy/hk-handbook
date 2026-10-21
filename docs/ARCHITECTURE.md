# 架构

```text
frontend/src（uni-app + Vue 3 + TypeScript）
          ├── Vite → H5 Web
          └── Vite → 微信小程序
                     ↓ HTTP / RESTful JSON
backend（Java 21 + Spring Boot MVC）
                     ↓ Spring JDBC / Xerial JDBC
                  SQLite 文件
```

前后端独立运行和构建。当前攻略页已接入内容 API，首页仅提供搜索与主题导航，不请求攻略数据；工具及我的／记录仍保留框架，使用 uni-app 导航 API，无业务数据表或认证逻辑。页面路径集中在 `frontend/src/navigation/index.ts`，页面共用 PageContainer，卡片与展示状态由公共组件复用；pages.json 集中页面标题及原生四 Tab 配置。顶层导航使用 switchTab，二级页使用 navigateTo，直接进入二级页可切回对应 Tab。
后端包含应用入口、数据源配置和 `/api/health`；健康接口执行 SELECT 1，测试还检查数据库外键开关及故障时的 503 响应。
后续 API 以 /api 开头，使用 HTTP 方法及状态码表达资源操作，数据交换为 JSON。

SQLite 使用单连接池以简化并发写入，连接初始化启用外键约束。
默认数据库位于进程工作目录，可用 DATABASE_URL 修改；不自动创建业务表。
不引入 ORM、迁移框架、缓存或微服务。

H5 已使用 Vite 的 /api 开发代理（默认本机 8080）；生产按部署方案选择同源反向代理或精确 CORS 配置。
小程序应配置可访问的 HTTPS API 地址及合法域名，真机不能将 localhost 作为开发电脑地址。
攻略请求统一使用 uni.request，10 秒超时和响应格式检查，配置与回归方法见 [E02-T04](implementation/E02-T04.md)；生产反向代理见下述部署结构。

## 主题导航

首页六宫格读取 `features/topics/content.ts`，通过 `openTopic` 进入 `pages/topics/` 下六个独立路由；空页面复用 TopicPlaceholder，不发业务 API、不显示投票。主题与攻略四分类独立。工具页仍为历史框架，其计算类功能后续单独实现。公开有用／无用票数仅为产品设计，当前没有投票表或接口。

## 部署结构

第一阶段采用单机部署：Nginx HTTP 8000 提供 H5，`/api/` 反向代理到仅监听 127.0.0.1:9000 的 Java 服务。systemd 管理 Java 进程，SQLite 位于 `/srv/hk-handbook/data/handbook.db`，与发布目录分离。使用专用部署用户和应用用户。

CI 保存已验证的发布包，CD 手动选择 main 的成功 CI 运行编号。发布会短暂停机，执行数据库备份、版本切换、启动检查及应用回退。微信小程序发布不在此流水线内。首次安装及具体限制见 [部署说明](DEPLOYMENT.md)。

## 内容契约（E02-T01）

[JSON 字段契约](content/MODEL.md) 定义四分类、正文、事实与来源、版本和人工签字，以及银行／人才／清单的稳定引用。[发布规范](content/STANDARD.md) 要求统一公开资格判断；E02-T03 在 Java 实现校验与查询，E02-T04 已在当前分支接入页面；模板留在 docs，不自动作为业务数据加载。

当前分支已实现 [E02-T03 内容接口](implementation/E02-T03.md)：生产内容来自版本管理的 classpath `content/*.json`，启动严格校验结构，每次查询重新判断公开资格，REST 响应使用公开字段投影；没有新增数据库表。生产内容包当前为空，港铁草稿仍留在 docs。
