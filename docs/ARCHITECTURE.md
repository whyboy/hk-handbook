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

前后端独立运行和构建，当前保留官方示例页面，无业务 API、数据表或认证逻辑。
后端只保留应用入口与数据源配置；测试实际执行 SELECT 1 并检查外键开关。
后续 API 以 /api 开头，使用 HTTP 方法及状态码表达资源操作，数据交换为 JSON。

SQLite 使用单连接池以简化并发写入，连接初始化启用外键约束。
默认数据库位于进程工作目录，可用 DATABASE_URL 修改；不自动创建业务表。
不引入 ORM、迁移框架、缓存或微服务。

后续联调时，H5 可使用 Vite 的 /api 开发代理；生产按部署方案选择同源反向代理或精确 CORS 配置。
小程序应配置可访问的 HTTPS API 地址及合法域名，真机不能将 localhost 作为开发电脑地址。
本次不预先实现代理、业务请求封装或生产配置。
