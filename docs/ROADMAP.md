# 香港百事通研发计划

产品依据：[PRD](PRD.md)。采用按板块逐步规划的方式：先确认当前板块，再创建对应 Epic 和 Task；不预先创建全部远期业务任务。

## 活跃规划

[研发 Project](https://github.com/users/whyboy/projects/4) · [任务分解](https://github.com/users/whyboy/projects/4/views/3) · [执行看板](https://github.com/users/whyboy/projects/4/views/4)

| Epic | 任务 |
| --- | --- |
| [E01 页面框架 #8](https://github.com/whyboy/hk-handbook/issues/8) | T01 #9、T02 #27、T03 #28、T04 #29、T05 #30；历史交付与剩余验收保留 |
| [E02 内容与浏览 #20](https://github.com/whyboy/hk-handbook/issues/20) | 下表；包含首页搜索与资讯页面框架 |

| Task | 范围 |
| --- | --- |
| [E02-T01 #10](https://github.com/whyboy/hk-handbook/issues/10) | 内容模型与发布核验机制 |
| [E02-T02 #11](https://github.com/whyboy/hk-handbook/issues/11) | 真实样稿、人工核验、实操与产品评审 |
| [E02-T03 #31](https://github.com/whyboy/hk-handbook/issues/31) | 内容校验和 REST 查询 |
| [E02-T04 #32](https://github.com/whyboy/hk-handbook/issues/32) | 跨端攻略浏览与上下文入口 |
| [E02-T05 #59](https://github.com/whyboy/hk-handbook/issues/59) | 无人群筛选、正文搜索、相关度和编辑推荐 |
| [E02-T06 #61](https://github.com/whyboy/hk-handbook/issues/61) | 首页六主题导航、独立空页面及产品规划同步 |
| [E02-T07 #63](https://github.com/whyboy/hk-handbook/issues/63) | 精简首页为搜索与主题入口 |
| [E02-T08 #68](https://github.com/whyboy/hk-handbook/issues/68) | 搜索结果保留在首页 Tab，原攻略 Tab 改为学校／政策资讯框架 |

## 规划调整

按产品负责人要求，原 E03～E08 的 6 个 Epic 和 17 个子 Task 从 GitHub 删除，取消预建清单、银行匹配、高才／优才初筛、个人记录及增长统计的完整远期排期。删除的是任务规划，不删除已合并代码、E01／E02 Issue 或未提交草稿。旧版本需求可从 Git 历史追溯，不能继续作为当前排期。

后续围绕首页各办事与生活板块逐个讨论，确认后新建 Epic；工具页未来计算类能力也按具体需求创建。公开有用／无用票数是已确认的设计方向，但当前空主题不实现投票，不提前创建完整业务 Epic。

## Project 与验收规则

Epic 使用 `type:epic`，Task 使用 `type:task` 并关联原生父 Issue；加入现有 Project。类型不表示完成。Project Status 使用 Todo / In Progress / Done，按实际交付和验收证据维护。`task-*` 自动执行标签保持独立，不能为展示看板而触发未授权开发。

保留未完成的人工内容核验、微信实际交互和产品验收；不能因 PR 合并而替人工签字。每项提交实际验证结果，前端执行类型检查与 H5／微信构建，后端变更执行 Java 21 `./mvnw clean verify`。不自动合并或部署。
