# 手动部署 H5 与 Java

## 范围与状态

本方案面向当前 Ubuntu 24.04 主机：已有 Nginx／SSH，80、443 及开发工具端口已占用。配置预留独立 HTTP 8000，Java 仅监听 127.0.0.1:9000；公网 SSH 地址已确认，端口为 22，维护者已确认在 Azure 放行 TCP 8000；实际地址保存在 GitHub production 变量中。当前主机初始化和 GitHub production 配置已完成，应用尚未发布。具体执行状态见 [验证记录](VALIDATION.md)，首次发布需在 CD 配置合并后手动执行。

生产部署仅手动触发，合并 main 不自动上线。不发布微信小程序，不创建业务表或自动迁移数据。单实例更新存在短暂停机。

## 一次性准备服务器

先检查 `deploy/nginx.conf`、`deploy/hk-handbook.service`、`deploy/release.sh` 和 `deploy/setup-server.sh`。8000、9000 必须空闲；若改端口，需要同步 Nginx、初始化脚本的环境文件和端口检查、release.sh 的健康检查地址。

服务器应安装 Nginx、SSH、Python 3（包含 sqlite3）、curl、flock、sudo 和 JDK 21。当前服务器已有这些工具，JDK 在开发用户工具目录，初始化脚本会复制一份独立运行时到 `/opt/hk-handbook-java`。

```bash
sudo bash deploy/setup-server.sh /绝对路径/到/JDK21
```

脚本创建：

| 路径／用户 | 用途 |
| --- | --- |
| `hk-deploy` | SSH 上传和部署用户，仅可 sudo 停止／重启本项目服务 |
| `hk-handbook` | Java 运行用户，无登录 Shell |
| `/srv/hk-handbook/incoming` | 上传包，仅部署用户可访问 |
| `/srv/hk-handbook/releases` | 按提交版本和时间保存解包后的版本 |
| `/srv/hk-handbook/current` | 当前版本软链接，发布时原子替换 |
| `/srv/hk-handbook/data` | SQLite，应用可写，Nginx 不可访问 |
| `/srv/hk-handbook/backups` | 发布前的 SQLite 一致性备份 |
| `/srv/hk-handbook/deployments.log` | 成功发布的时间和提交 SHA |
| `/etc/hk-handbook/runtime.env` | 本机环境配置，不提交 Git |
| `/usr/local/bin/hk-handbook-release` | 经管理员安装的部署脚本 |

安装程序不会覆盖已有同名安装；如果中途失败，先检查已经创建的项目资源，再修复，不要直接删除整个 `/srv` 或覆盖现有 Nginx 配置。首次安装后没有 release，HTTP 返回 503，服务启动条件不会满足；第一次成功发布后服务随系统启动。

## 专用 SSH 密钥和服务器身份

为 CD 创建独立 SSH 密钥，不复用个人登录密钥。在管理员终端用 `ssh-keygen -t ed25519` 生成；将公钥写入 `hk-deploy` 家目录的 `.ssh/authorized_keys`（目录 700、文件 600、属主 hk-deploy），可在公钥前加 `restrict` 禁用转发和 PTY。私钥存入下述 GitHub secret，不写入仓库。

`DEPLOY_KNOWN_HOSTS` 应包含目标地址对应的、已经核对过的 SSH 主机公钥。可以在服务器读取 `/etc/ssh/ssh_host_ed25519_key.pub`，结合实际连接地址生成 known_hosts 行；非 22 端口使用 `[地址]:端口` 格式。不要在部署时自动信任未经核对的 `ssh-keyscan` 结果。

确保 GitHub 托管 runner 能连接该公网 SSH 地址，并由维护者放行 HTTP 8000；Java 9000 无需公网放行。主机当前 UFW 未启用，云端安全组仍需独立检查。

## 一次性配置 GitHub

创建 `production` Environment，并将部署分支限制为 `main`。配置：

| 类型 | 名称 | 值 |
| --- | --- | --- |
| Variable | `DEPLOY_HOST` | 公网 IPv4 或主机名；当前脚本不接受 IPv6 字面量 |
| Variable | `DEPLOY_PORT` | SSH 端口，默认 22 |
| Secret | `DEPLOY_SSH_KEY` | 部署专用私钥 |
| Secret | `DEPLOY_KNOWN_HOSTS` | 已核验的服务器主机密钥记录 |

workflow 固定使用 `hk-deploy` 用户，不使用 root SSH。无需额外 GitHub PAT，读取 CI 记录和产物使用当前仓库的只读 Actions token。环境分支限制仍需在 GitHub 设置，它不会随 workflow 自动创建。

维护者可用 GitHub UI 配置，或将密钥文件直接交给 gh CLI，避免打印内容：

```bash
gh secret set DEPLOY_SSH_KEY --env production < /安全路径/部署私钥
gh secret set DEPLOY_KNOWN_HOSTS --env production < /安全路径/known_hosts
```

## 每次发布

1. 合并经审核的 PR 到 main，等待 CI 的前后端、Deployment checks、Release package 全部通过。
2. 打开该次 **main** CI 运行，记下 URL 中 `/actions/runs/` 后的运行编号。确认有 `release-<SHA>` 产物。
3. Actions → Deploy → Run workflow，选择 main，输入 `ci_run_id`。
4. Deploy 会验证 CI 来源、成功状态、分支和事件，并下载对应版本发布包。PR CI 即使通过也不能部署。
5. 通过固定主机身份的 SSH 上传，服务器再次校验 SHA256 与包内版本，拒绝越界路径、链接及不完整的包。
6. 停止后端写入，使用 Python SQLite backup API 备份并验证完整性；切换版本，重启服务。
7. 检查后端数据库健康、Nginx 返回的 H5 是否与本次 index.html 一致，以及经过 Nginx 的健康接口。成功后记录提交 SHA。
8. 浏览器访问 `http://服务器地址:8000/`，健康检查为 `/api/health`。检查日志中的成功仅代表服务器本机验证通过，公网还需要实际访问确认。

首次加入 CD 前的 CI 没有发布产物，不能直接用于部署。产物保存 30 天，过期后不能再下载，需要重新构建并通过 CI 后选择新的有效产物。`workflow_dispatch` 入口需要配置先进入默认分支。

## 失败与回退

- 上传、来源验证或解包失败：不停止当前应用。
- 备份失败：停止发布并尝试恢复原服务。
- 新版启动或 HTTP 检查失败：恢复旧版本软链接、重启旧后端并检查其健康；流水线仍然标记失败。
- 首次部署没有旧版：失败后停止服务，移除 current 链接，不伪报回退成功。
- 回退失败：日志明确报告，需要维护者查看 `sudo journalctl -u hk-handbook.service`。
- 手动回到旧版：在 Deploy 输入那次仍保留产物的成功 main CI 编号即可。

自动回退只针对 H5／JAR，**不会恢复或覆盖数据库**。未来涉及表结构变更时，必须另行设计兼容性和数据恢复流程。

服务器保留发布版本和备份，不自动删除数据。当前剩余磁盘约 7 GB，维护者需定期检查使用量，在确认保留当前版本、回退版本和必要备份后清理旧上传包／旧版本，并另行安排异机备份。服务器上的备份不能抵御整台机器或磁盘丢失。

## 验证与维护

```bash
for script in deploy/*.sh; do bash -n "$script"; done
python3 deploy/test_release.py
# 如果已安装 actionlint
actionlint .github/workflows/ci.yml .github/workflows/deploy.yml
```

隔离测试模拟 systemctl 与 HTTP，真实操作临时 SQLite 和版本目录，覆盖首次失败、升级失败回退、备份数据保留、校验和篡改及路径越界。它不替代真实 SSH／systemd／Nginx 联调。

修改仓库里的 `release.sh` 不会自动更新服务器管理员安装的脚本；审核后通过管理员执行 `sudo install -m 755 deploy/release.sh /usr/local/bin/hk-handbook-release`。服务单元、Nginx 或运行时配置的变更也需要管理员安装并验证。
