# 第一版内部阅读入口

关联 [E02-T02 #11](https://github.com/whyboy/hk-handbook/issues/11)。完整内容：[JSON 草稿](mtr-route.json)；[样稿说明](MTR-SAMPLE.md)、[来源账本](MTR-SOURCES.md)、[评审记录](MTR-REVIEW.md)。

无需 npm 或后端，仓库根目录执行：

```bash
python3 docs/content/render_sample.py --check
python3 -m http.server 4187 --bind 127.0.0.1 --directory docs/content
```

在同机浏览器访问 `http://127.0.0.1:4187/reading/mtr-route.html`。如端口已占用，选择其他空闲端口，不停止其他服务。也可以用浏览器直接打开 [HTML 文件](reading/mtr-route.html)。退出本地服务用 Ctrl+C。不部署、不接入公开列表；noindex 只是索引提示，不是访问控制，因此该目录不得作为公开业务站点托管。

仅在 `mtr-route.json` 编辑正文，然后运行 `python3 docs/content/render_sample.py` 重新生成阅读页；`--check` 检查生成结果一致性。渲染器只接受固定 ID 的 draft，转义文本，只允许港铁 HTTPS 来源链接；它不是通用内容服务或发布校验器。正文、日期、来源从 JSON 生成，防止页面与编辑稿漂移。

第一版支持章节目录、来源定位、官方链接与返回顶部，移动／桌面阅读；没有收藏、公开分享、纠错业务或假成功反馈。H5 产品页面、微信小程序接入仍归后续 Task，本页不能代替两端正式验收。
