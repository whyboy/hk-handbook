# JSON 字段契约 v1

配套 [规范](STANDARD.md)、[模板](template.json) 和 [审核表](CHECKLIST.md)。UTF-8 JSON，每文件一个对象，文件名为 `<id>.json`；本目录模板仅供复制，不作为生产内容加载。所有下表字段必填，未知只在明确允许处用 null；不允许额外字段。文本为纯文本，不接受 HTML／脚本，渲染层仍须转义。长度按 Unicode 码点计算，输入先去首尾空白；非空文本不允许全空白。

## 顶层

| 字段 | 类型与规则 |
| --- | --- |
| schemaVersion | 整数，固定 1；不支持的版本拒绝加载 |
| id | 1–80 字符，`^[a-z0-9]+(?:-[a-z0-9]+)*$`；全库唯一，不随标题变动或下架复用 |
| revision | 正整数；同 ID 内容修改必须递增 |
| status | draft / pending_review / published / review_due / withdrawn |
| title、summary | 非空文本，分别最多 120、500 字符 |
| category | bank / phone / talent / life |
| audiences | visitor / talent / family 的非空去重数组 |
| keywords | 1–20 个去重非空词，每词最多 40 字符 |
| editedOn | 有效 ISO 日期 YYYY-MM-DD，不得晚于当前香港日期 |
| effectivePeriod | 下述 Period；文章适用有效期 |
| reviewDueOn | 日期或 null；草稿可未知，公开候选必须有未来复核日期 |
| sections | 包含 applicability、materials、steps、costsAndTime、warnings 五个 Section |
| facts | Fact 数组，最多 200 条；ID 在文章内唯一，公开候选至少一条 |
| sources | Source 数组，最多 100 条；ID 在文章内唯一，公开候选非空 |
| review | null 或 Review，针对当前 revision |
| publication | null 或 Publication，针对当前 revision |
| related | Reference 数组，最多 20 条，按 type + id 去重；无关联为 [] |

日期比较统一使用 Asia/Hong_Kong 的日历日期。Period 的 from 为含当天的起始日，until 为不含当天的失效日；若两者都有值则 from < until。复核截止日同样不含当天。日期不能用空字符串、编辑日期替代。

## Period

固定字段：`state`（known / not_stated / unknown）、`from`（日期或 null）、`until`（日期或 null）、`note`（非空文本，最多 1000 字符）。known 至少有一个日期；not_stated／unknown 两日期都为 null。not_stated 表示实际查阅后确认官方未注明，并在事实核验中确认；unknown 表示尚未查清，阻塞公开。note 解释适用范围和未注明／未知原因。不得捏造统一官方有效期。

## Section 与 Block

Section 固定字段：`state`（known / not_applicable / unknown）、`reason`（文本或 null）、`blocks`（Block 数组，最多 100 条）。known 要求至少一条 block 且 reason=null；其余状态 blocks=[]，reason 为 1–1000 字符解释。steps 与 applicability 公开时必须 known；其他章节可 not_applicable，但人工审核须确认理由。unknown 章节阻塞发布。

Block 固定字段：`kind`（official_fact / editorial_advice / personal_experience）、`text`（1–5000 字符）、`factIds`（去重事实 ID 数组）、`experienceContext`（文本或 null，最多 1000 字符）。official_fact 至少引用一个已定义事实；建议无事实时可 []，涉及事实必须引用；个人经验必须给时间、情境及匿名化作者，其余 kind 的 experienceContext=null。事实 ID 引用在全篇内解析，不可悬空；每条 Fact 必须被正文引用。

## Fact

| 字段 | 类型与规则 |
| --- | --- |
| id | 同顶层 ID 格式，文章内唯一 |
| claim、conditions | 非空文本，各最多 2000 字符；陈述和适用对象／例外分开 |
| sourceIds | 非空去重来源 ID 数组；全部指向本文章 sources |
| verification | null 或 Verification；未核验为 null，不能填 AI |

所有 Fact 都按关键事实处理，不设置可降级的 critical=false 开关。Verification 固定字段：`human`（非空责任人标识）、`verifiedOn`（实际日期）、`result`（passed / conflict / pending）、`evidence`（非空核验记录定位）、`revision`（本文章正整数版本）、`reviewDueOn`（未来复核日期）。human 最多 120 字符，evidence 最多 2000 字符，指向本 PR 的逐条账本或不可变 commit 路径，可引用实操记录。passed 才允许公开；verifiedOn 不得在未来，不得晚于文章审核日期。只有人工可以填写。

## Source

| 字段 | 类型与规则 |
| --- | --- |
| id | 同顶层 ID 格式，文章内唯一 |
| organization、title | 非空文本，各最多 200 字符 |
| url | 最多 2048 字符的绝对 HTTPS URL，无用户信息；精确指向原始依据页面，不是搜索结果 |
| locator、scope | 非空文本，各最多 2000 字符；支持事实的段落／位置和适用情境 |
| consultedOn | 日期或 null；实际查阅日期，不是核验日期 |
| consultedBy | 非空文本或 null；与 consultedOn 同时存在或同时 null，AI 查阅明确填 AI |
| effectivePeriod | Period，记录官方生效范围，不用查阅时间补齐 |
| linkCheck | null 或 LinkCheck；查阅可由 AI 完成，有效性人工确认单独记录 |

LinkCheck 固定字段：`human`（1–120 字符）、`checkedOn`（实际日期）、`result`（valid / broken / unknown）、`evidence`（1–2000 字符）、`reviewDueOn`（复核截止日）。公开时须 valid、未到复核日；链接虽能打开但机构／产品／段落不符也不能记 valid。公开候选所有来源必须被事实使用且有实际查阅、人工检查记录。所有人工日期不得在未来。

## Review、Publication 与 Reference

Review：`human`、`reviewedOn`、`result`（approved / changes_requested）、`evidence`、`revision`、`rolesNote`。human／evidence 使用上文长度约束，rolesNote 为 1–1000 字符，说明分工或兼任原因。审核不得早于编辑、逐条核验、来源查阅与链接检查日期；人工核验和链接检查必须对应当前正文及来源。publication 仅在人工作出允许发布的决定后填写。

Publication：`human`、`publishedOn`、`evidence`、`revision`，类型和长度同上，日期不早于审核且不在未来。这里记录当前 revision 获准进入公开数据的日期，不等同于服务器实际部署日期；实际上线另由授权的发布记录追溯。重复发布保留 Git 历史，不能伪造上线记录。文章级“事实核验日期”从所有 Fact 的 verifiedOn 取最早值，禁止编辑手填派生值。

Reference：`type`（bank / talent_rule / checklist）、`id`（稳定 ID，规则同顶层）、`version`（正整数）。例如银行资料、独立人才规则集及清单模板的版本引用；模型只存引用，不存用户输入、推荐权重、初筛条件或执行规则。目标服务负责解析可用性；未知版本不回退到任意最新版，隐藏执行入口并显示尚未开放状态。

## 结构与跨字段校验分工

本 Task 固定完整结构与语义；#31 在 Java 中实现结构、日期顺序、引用完整性、版本和公开资格校验及边界测试，不另建 Python 生产校验服务。结构校验失败应拒绝该数据集生效并保留错误定位，不部分加载以掩盖编辑错误；结构有效但未通过公开门槛的草稿可保留在内部，公开结果为空是合法状态。生产包只读取经过版本管理的内容，不能扫描模板目录自动发布。
