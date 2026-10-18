# 架构

```text
frontend/src（uni-app + Vue 3 + TypeScript）
          ├── Vite → H5 Web
          └── Vite → 微信小程序
                     ↓ HTTP / RESTful JSON（后续业务接口）
backend（Java 21 + Spring Boot MVC）
                     ↓ Spring JDBC / Xerial JDBC
                  SQLite 文件
```

前后端独立运行和构建。当前前端包含首页框架和工具用途说明承接页，使用本地静态视图模型及 uni-app 导航 API；无业务 API、数据表或认证逻辑。页面路径集中在 `frontend/src/navigation/index.ts`，T02 注册首页、工具列表、工具说明及结果空状态四个页面，四 Tab 导航在后续 T05 统一。
后端包含应用入口、数据源配置和 `/api/health`；健康接口执行 SELECT 1，测试还检查数据库外键开关及故障时的 503 响应。
后续 API 以 /api 开头，使用 HTTP 方法及状态码表达资源操作，数据交换为 JSON。

SQLite 使用单连接池以简化并发写入，连接初始化启用外键约束。
默认数据库位于进程工作目录，可用 DATABASE_URL 修改；不自动创建业务表。
不引入 ORM、迁移框架、缓存或微服务。

后续联调时，H5 可使用 Vite 的 /api 开发代理；生产按部署方案选择同源反向代理或精确 CORS 配置。
小程序应配置可访问的 HTTPS API 地址及合法域名，真机不能将 localhost 作为开发电脑地址。
开发代理及业务请求封装尚未实现；生产反向代理见下述部署结构。

## 部署结构

第一阶段采用单机部署：Nginx HTTP 8000 提供 H5，`/api/` 反向代理到仅监听 127.0.0.1:9000 的 Java 服务。systemd 管理 Java 进程，SQLite 位于 `/srv/hk-handbook/data/handbook.db`，与发布目录分离。使用专用部署用户和应用用户。

CI 保存已验证的发布包，CD 手动选择 main 的成功 CI 运行编号。发布会短暂停机，执行数据库备份、版本切换、启动检查及应用回退。微信小程序发布不在此流水线内。首次安装及具体限制见 [部署说明](DEPLOYMENT.md)。
