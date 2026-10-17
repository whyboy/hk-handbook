# E01 交互原型

关联 #8、#9、#10、#11。独立静态 HTML/CSS/JavaScript，仅用于产品评审，不修改 uni-app 或 Java 业务代码，不进入现有生产发布包。

从仓库根目录运行：

```bash
python3 -m http.server 4173 --bind 127.0.0.1 --directory docs/prototype
```

在同一开发机打开 `http://127.0.0.1:4173`。也可直接打开 index.html，但文件协议下剪贴板/存储行为受浏览器限制。
这不是公网部署，分享的本地地址只在能访问该地址的环境有效。

## 页面与流程

| 页面 | 入口与动作 | 输出 |
| --- | --- | --- |
| 首页 | 银行卡/电话卡/交通入口、搜索入口 | 对应分类攻略库 |
| 攻略库/搜索结果 | 分类、人群、关键词组合 | 样稿或明确的空状态 |
| 详情 | `#article/mtr-route` 深链接 | 样稿、来源、日期、返回首页/库 |
| 我的 | 本地收藏列表 | 详情或无收藏状态 |
| 纠错 | 详情进入、填写问题 | 明确模拟成功，实际不发送 |
| 异常状态 | 页面底部评审面板 | 加载失败/重试、不存在、下架 |

当前仅交通有真实样稿，另外两类显示无内容，避免用虚构业务内容填充。样稿适用于全部已定义人群，故三个人群筛选均可命中同一篇。
收藏存于浏览器 localStorage，存储拒绝时显示失败；清理数据丢失，不登录、不跨端同步。
分享复制包含文章 ID 的 hash 链接；复制权限受限时显示可手动复制的 URL。
纠错只演示结果表达，真实后台接收、审核和通知属于后续 Epic。

## H5 与微信小程序差异（后续实现约束）

H5 使用 URL 深链接与复制降级；微信使用页面 path + article ID 和原生分享按钮。
后续 uni-app 使用本地存储 API，实现两端各自的设备收藏，不能承诺 H5 与微信互通。
微信外部来源链接需按平台能力提供复制/浏览器查看引导，不能假设可直接跳转任意网页。
此 HTML 原型只在浏览器评审，不代表已验证微信原生能力。

## 阅读字段与待确认问题

页面字段遵循 [内容规范](../content/STANDARD.md)，完整样稿与事实记录见 [MTR-SAMPLE.md](../content/MTR-SAMPLE.md)。
产品名称“香港手册”和颜色仅为评审方案；需确认名称、阅读密度、人群标签文案、纠错接收方式及审核责任人。
评审签字、反馈和验证记录见 [REVIEW.md](REVIEW.md)。

## 浏览器自检

测试工具独立安装在临时目录，不改变业务工程依赖。需要 Node.js 20+、Python 3 和 Chromium 所需系统库：

```bash
npm install --prefix /tmp/hk-prototype-test --no-audit --no-fund playwright@1.64.1
/tmp/hk-prototype-test/node_modules/.bin/playwright install chromium
# 如缺少系统库，按本机权限安装 Playwright Chromium 系统依赖
NODE_PATH=/tmp/hk-prototype-test/node_modules node docs/prototype/smoke.cjs
```

请保持 4173 端口空闲。脚本启动并关闭自己的本地 HTTP 服务，验证实际浏览器交互，不访问生产服务。
