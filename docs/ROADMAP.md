# 港易通研发计划

产品目标以 [PRD](PRD.md) 为准。本文只维护交付分工、依赖和 Issue 索引，具体实现机制在 Task 中。

## 日常浏览入口

- [Epic 总览：仅显示产品交付目标](https://github.com/whyboy/hk-handbook/issues?q=is%3Aissue%20is%3Aopen%20label%3A%22type%3Aepic%22)
- [Task 列表：仅显示具体实现任务](https://github.com/whyboy/hk-handbook/issues?q=is%3Aissue%20is%3Aopen%20label%3A%22type%3Atask%22)
- [E01 页面框架及其五个子任务](https://github.com/whyboy/hk-handbook/issues/8)

Epic 使用紫色 `type:epic` 标签，Task 使用蓝色 `type:task` 标签；这些标签只表示类型，不表示任务可领取或已完成。新增 Task 需设置 `type:task` 并关联原生父 Issue，Epic 使用 `type:epic`；不要只依赖标题编号表达层级。

现有 `task-ready`、`task-doing`、`task-done`、`task-failed` 属于自动化执行状态，保持独立。只有任务实际就绪时才按现有流程标记 ready，不能为了展示看板给全部 Task 批量加 ready。

## Projects 日常工作视图

[港易通 · 产品研发](https://github.com/users/whyboy/projects/4) 已关联本仓库，包含 8 个 Epic 和 26 个 Task。项目保持私有，需使用有权访问的 GitHub 账号登录。

| 视图 | 布局 | 过滤条件 | 展示与分组 |
| --- | --- | --- | --- |
| [产品总览](https://github.com/users/whyboy/projects/4/views/2) | Table | `repo:whyboy/hk-handbook is:issue label:"type:epic"` | 标题、状态、子任务完成进度；按标题排序 |
| [任务分解](https://github.com/users/whyboy/projects/4/views/3) | Table | `repo:whyboy/hk-handbook is:issue label:"type:task"` | 按 Parent issue 分组；显示标题、Labels、Status、Assignees |
| [执行看板](https://github.com/users/whyboy/projects/4/views/4) | Board | `repo:whyboy/hk-handbook is:issue label:"type:task"` | 按 Status 分列，卡片显示父 Issue 与状态标签 |

日常优先使用“任务分解”，按父 Epic 查看其 Task；“产品总览”展示目标与子任务完成进度；“执行看板”按 Todo、In Progress、Done 分列。新增 Epic 需加入本 Project；已在 Project 内的父 Issue 新增子任务时，由原生子任务自动添加工作流收录，并应检查类型标签。

初始化时，未带执行状态标签的开放 Issue 放入 Todo，表示待安排，不等于 `task-ready`，不会因此触发 Agent 领取。Project Status 与 `task-*` 标签尚未配置双向同步，实际执行状态仍以标签及验收结果为准；必要时手动同步看板状态。

保留 GitHub 默认的加入项目、Issue 关闭及子任务自动添加工作流。已移除新项目默认的“Auto-close issue”“Pull request linked to issue”“Pull request merged”工作流：拖动到 Done 不会自动关闭 Issue，关联或合并 PR 也不会自动宣布业务任务验收完成。Issue 仍按各自验收标准人工关闭。

视图能力参考 [GitHub 父 Issue 与子任务进度字段](https://docs.github.com/en/issues/planning-and-tracking-with-projects/understanding-fields/about-parent-issue-and-sub-issue-progress-fields)。

## 分层规则

- PRD 描述希望构建的目标产品，不记录任务迁移或单次 PR 状态。
- Epic 描述一个可验收的交付目标、范围和依赖。
- Task 描述具体实现机制、接口边界、验收标准与验证要求，使用 GitHub 原生子 Issue 关联父 Epic。
- 一个 Task 只负责一份核心能力；跨 Epic 通过约定的接口协作，避免重复实现存储、分享或内容服务。
- 当前计划包含 8 个 Epic、26 个 Task；创建任务不表示已实现或验收。

## Epic 总览

| Epic | 目标 | Task 数量 |
| --- | --- | --- |
| [E01 · 实现四 Tab 页面框架与基础导航](https://github.com/whyboy/hk-handbook/issues/8) | 让用户在 H5 和微信小程序上看到港易通的完整页面骨架，理解首页、工具、攻略、我的及二级页面的关系，并能完成基础导航。 | 5 |
| [E02 · 实现可核验攻略内容与浏览搜索](https://github.com/whyboy/hk-handbook/issues/20) | 让用户免登录找到、阅读并核实银行卡、手机卡、人才身份和生活落地攻略，形成后续工具与清单可复用的内容基础。 | 4 |
| [E03 · 实现赴港办事清单闭环](https://github.com/whyboy/hk-handbook/issues/21) | 让用户按赴港目的生成可执行清单，逐项准备并再次打开继续使用；优先验证首次赴港办理银行卡场景。 | 3 |
| [E04 · 实现银行开户匹配与方案比较](https://github.com/whyboy/hk-handbook/issues/22) | 让用户依据自身情况比较适合进一步了解的银行公开要求，识别材料缺口并继续准备清单。 | 3 |
| [E05 · 实现高才通 A/B/C 路径初筛](https://github.com/whyboy/hk-handbook/issues/23) | 让用户了解高才通各路径中已明确符合、暂不符合和仍待核实的条件，并获得材料缺口与官方入口。 | 3 |
| [E06 · 实现优才两种评核机制初筛](https://github.com/whyboy/hk-handbook/issues/24) | 让用户先理解基本资格，再分别了解综合计分制和成就计分制的明确条件、待核实信息及材料要求。 | 3 |
| [E07 · 实现个人记录、收藏与反馈](https://github.com/whyboy/hk-handbook/issues/25) | 让免登录用户从“我的”找回收藏、清单和测评记录，并能提交内容纠错或产品反馈。 | 3 |
| [E08 · 实现安全分享与增长效果观测](https://github.com/whyboy/hk-handbook/issues/26) | 让内容和清单能通过微信及 H5 分享进入正确页面，并可观察内容到工具到清单的使用和复访表现。 | 2 |

## Task 索引

### E01：实现四 Tab 页面框架与基础导航

仅交付布局、通用展示组件、路由和页面状态，不实现搜索、匹配、测评、清单生成、收藏保存、反馈提交或业务 API。示例内容明确标注，业务按钮显示“功能建设中”，不伪造结果或成功反馈。

依赖：无业务前置依赖；作为其余 Epic 的页面基础。

- [E01-T01 · 实现首页框架](https://github.com/whyboy/hk-handbook/issues/9)
- [E01-T02 · 实现工具页与工具说明框架](https://github.com/whyboy/hk-handbook/issues/27)
- [E01-T03 · 实现攻略页与详情框架](https://github.com/whyboy/hk-handbook/issues/28)
- [E01-T04 · 实现我的页面与记录列表框架](https://github.com/whyboy/hk-handbook/issues/29)
- [E01-T05 · 实现四 Tab、公共页面容器与导航状态](https://github.com/whyboy/hk-handbook/issues/30)

### E02：实现可核验攻略内容与浏览搜索

覆盖内容规范、人工核验、真实样稿、发布读取、分类搜索和详情。收藏存储归 E07，公共分享及统计归 E08；不建设独立后台或爬虫。

依赖：页面接入依赖 E01；内容规范和服务可独立推进。

- [E02-T01 · 定义内容模型与发布核验机制](https://github.com/whyboy/hk-handbook/issues/10)
- [E02-T02 · 制作真实攻略样稿并完成人工评审](https://github.com/whyboy/hk-handbook/issues/11)
- [E02-T03 · 实现内容发布校验与 REST 查询接口](https://github.com/whyboy/hk-handbook/issues/31)
- [E02-T04 · 接入攻略浏览搜索与上下文入口](https://github.com/whyboy/hk-handbook/issues/32)

### E03：实现赴港办事清单闭环

覆盖模板、生成、步骤操作、进度保存和模板更新提示。先支持直接选目的生成，后续银行／人才结果通过稳定契约接入。

依赖：E01 页面框架；模板核验沿用 E02 规范，直接生成不依赖银行或人才引擎。

- [E03-T01 · 定义并核验清单模板与生成契约](https://github.com/whyboy/hk-handbook/issues/33)
- [E03-T02 · 实现清单生成、步骤操作与进度保存](https://github.com/whyboy/hk-handbook/issues/34)
- [E03-T03 · 实现步骤复制转发与工具结果生成清单](https://github.com/whyboy/hk-handbook/issues/35)

### E04：实现银行开户匹配与方案比较

提供可解释匹配，不承诺开户成功；不代办开户，不引入商业排序或支付。

依赖：E01；核验规范使用 E02-T01；清单接入使用 E03 契约。

- [E04-T01 · 建立银行资料与匹配规则数据集](https://github.com/whyboy/hk-handbook/issues/36)
- [E04-T02 · 实现可解释银行匹配 API](https://github.com/whyboy/hk-handbook/issues/37)
- [E04-T03 · 实现匹配表单、结果比较与清单入口](https://github.com/whyboy/hk-handbook/issues/38)

### E05：实现高才通 A/B/C 路径初筛

独立高才规则，不与优才混算，不估算批准概率，不收集证件扫描件。

依赖：E01、E02 核验规范、E03 清单契约。

- [E05-T01 · 核验高才 A/B/C 规则与材料清单](https://github.com/whyboy/hk-handbook/issues/39)
- [E05-T02 · 实现独立高才规则引擎与初筛 API](https://github.com/whyboy/hk-handbook/issues/40)
- [E05-T03 · 实现高才初筛表单、解释结果与清单衔接](https://github.com/whyboy/hk-handbook/issues/41)

### E06：实现优才两种评核机制初筛

独立优才规则，以当前官方准则为依据，不复用历史分数表或高才判定，不预测获批概率。

依赖：E01、E02 核验规范、E03 清单契约。

- [E06-T01 · 核验优才基本资格及两套评核准则](https://github.com/whyboy/hk-handbook/issues/42)
- [E06-T02 · 实现独立优才初筛引擎与 API](https://github.com/whyboy/hk-handbook/issues/43)
- [E06-T03 · 实现优才表单、分层结果与清单衔接](https://github.com/whyboy/hk-handbook/issues/44)

### E07：实现个人记录、收藏与反馈

本设备存储和主动清除，不做账号、跨设备同步或证件采集；真实反馈提交必须有可核实结果。

依赖：E01；按 E02/E03/E04/E05/E06 的稳定资源标识接入。

- [E07-T01 · 实现本设备存储契约与收藏记录适配](https://github.com/whyboy/hk-handbook/issues/45)
- [E07-T02 · 接入我的收藏、清单与测评记录列表](https://github.com/whyboy/hk-handbook/issues/46)
- [E07-T03 · 实现反馈与内容纠错闭环](https://github.com/whyboy/hk-handbook/issues/47)

### E08：实现安全分享与增长效果观测

公共分享、参数化进入、最小渠道统计；不实现主动订阅消息、商业转介或敏感画像。

依赖：E01 路由；按各业务 Epic 提供的事件与资源接入。

- [E08-T01 · 实现跨端分享与公开落地协议](https://github.com/whyboy/hk-handbook/issues/48)
- [E08-T02 · 实现最小事件统计与增长指标口径](https://github.com/whyboy/hk-handbook/issues/49)

## 实施顺序与集成边界

1. E01 按 T01 → T02 → T03 → T04 → T05 的编码顺序逐个提交 PR，前一 PR 合并后才开始下一 Task。T01 先提供首页所需的最小路由和工具说明承接页，各页面沿用该导航约定；T05 再整合四 Tab、公共容器及导航状态。E01 验收只包含页面结构、导航和展示状态，业务入口明确提示建设中。
2. E02 内容规范与服务可提前推进，页面接入在 E01 后完成。E03 优先实现“选择目的 → 生成清单 → 勾选 → 保存 → 再次打开”，不等待匹配或人才引擎。
3. E03 与 E07-T01 先约定本地存储接口；清单实例由 E03 负责，E07 负责通用适配及“我的”聚合，避免两份进度数据。E08-T01 可先按公开资源契约实现分享适配，再与内容和清单联调。
4. E04、E05、E06 的规则整理和服务可分别推进，使用 E02 的核验原则；各自结果页按 E03 的生成契约接入清单，记录按 E07 的契约保存。
5. E07 完成所有记录与反馈入口接入；E08 完成公共分享和最小统计。完整漏斗验收在对应业务交付后进行，主动订阅消息不在本计划内。

## 验收约定

- 每个 Task 提供实现 PR、实际验证结果和未验证范围。前端变更执行类型检查及 H5／微信小程序构建；后端变更使用 Java 21 执行 `./mvnw clean verify`。
- 构建成功不能代替浏览器、微信开发者工具／真机交互验收；人工事实核验与产品评审不能由 AI 查阅代签。
- Epic 在子 Task 和目标流程完成后验收；文档 PR 不自动关闭功能任务，不自动合并或部署。

## 既有工作关联

- #8 调整为 E01 页面框架，#9 为 E01-T01 首页框架；旧版线框在任务的折叠记录中保留，以当前 Task 范围为准。
- #10 和 #11 分别归入 E02-T01 内容规范、E02-T02 真实样稿；保留原始成果、核验依据和未完成的人工评审记录。
- [PR #18](https://github.com/whyboy/hk-handbook/pull/18) 是在审内容与纠错服务，对应 E02-T03 和 E07-T03 的既有实现输入。后续任务须核对覆盖范围、分类与接口，避免重复实现；该 PR 不因重整计划而自动合并。
