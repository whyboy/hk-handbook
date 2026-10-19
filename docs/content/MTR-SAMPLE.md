# 港铁真实攻略样稿：第一版编辑入口

对应 [E02-T02 #11](https://github.com/whyboy/hk-handbook/issues/11)，父 [Epic #20](https://github.com/whyboy/hk-handbook/issues/20)。**未发布草稿，人工事实核验、实操与创始人评审未完成。**

完整正文唯一编辑源为 [mtr-route.json](mtr-route.json)，ID 仍为 `mtr-route`，revision 2，分类改为生活落地 `life`，主题为交通。内容保留旧稿的起终点、换乘、首尾班查询说明、动态费用边界及 F1–F5 来源映射，没有添加票价、时刻或办理承诺。[阅读页](reading/mtr-route.html) 由 JSON 生成，[本地打开方法](MTR-READING.md) 无需业务服务。

## 选题与第一版决策

保留“第一次乘港铁，怎样查路线和尾班车？”这一具体问题。查询教程适合检查模板中的适用条件、材料不适用理由、步骤、动态费用及官方来源；不需要编造个人经历或固定查询结果。页面沿用移动端单列、前置适用范围、可跳转目录，不增加没有实现的业务按钮。

维护者在本会话要求自主决策、先完成第一版。本次据此完成草稿与内部阅读交付，不等同于人工签字或允许公开发布。人工核验字段、审核和发布记录全部保留 null；只有 AI 查阅记录更新至 2026-10-19。

## 历史保留

此前 [PR #17](https://github.com/whyboy/hk-handbook/pull/17) 已关闭未合并，不能算 main 已交付。[原版全文](https://github.com/whyboy/hk-handbook/blob/8fe94fe2a7275cf85814eab9fe4a96e2bf2354a1/docs/content/MTR-SAMPLE.md) 和 [Issue #11 折叠资料](https://github.com/whyboy/hk-handbook/issues/11) 保留 2026-10-17 的原始查阅与待办。新版将旧 S1–S4／F1–F5 映射为 JSON 的小写 s1–s4／f1–f5，文章 ID 不变。

[来源账本](MTR-SOURCES.md) 保留证据边界；[评审记录](MTR-REVIEW.md) 分开记录 AI 自查与未完成的人工项目。任何自动测试都不能把这些待办改写为“人工通过”。
