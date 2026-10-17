# 真实攻略样稿阅读入口

关联 [Issue #11](https://github.com/whyboy/hk-handbook/issues/11)。本文只提供内部样稿阅读方式，不进行公网部署或发布内容。

- 在 GitHub 直接阅读：[完整攻略样稿](MTR-SAMPLE.md)、[来源与核验账本](MTR-SOURCES.md)、[评审记录](MTR-REVIEW.md)。
- 浏览器查看长文排版：[HTML 源文件](reading/mtr-route.html)，本地打开方式如下。

```bash
python3 -m http.server 4174 --bind 127.0.0.1 --directory docs/content
```

在同机浏览器打开 `http://127.0.0.1:4174/reading/mtr-route.html`。结束后用 Ctrl+C 停止服务。也可直接用浏览器打开 HTML 文件；此阅读页不依赖 JavaScript、远程字体或图片。

页面包含真实样稿的摘要、适用条件、准备、完整步骤、费用耗时、注意事项和来源。编辑责任、选题理由及完整核验账本保存在 Markdown 中，阅读页提供入口；HTML 和 Markdown 的事实、日期与来源需要同步维护。

这是 #9 允许的“对应阅读示例”，不是生产前端，也不模拟尚未实现的收藏／分享／纠错。返回说明页和顶部始终可用；从 GitHub 直接打开 HTML 文件会看到源码，不会自动渲染网页。

设计依赖：[PR #15 内容规范](https://github.com/whyboy/hk-handbook/pull/15)、[PR #16 页面线框](https://github.com/whyboy/hk-handbook/pull/16)。本 PR 只新增样稿文件，可独立评审；建议按规范 → 线框 → 样稿顺序合并。
