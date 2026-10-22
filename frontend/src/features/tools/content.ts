// Presentation only: no rules, answers or generated results.
export const tools = [
  { id: 'exchange', symbol: '⇄', label: '金额换算', title: '汇率换算', brief: '换算港币与人民币金额', purpose: '按明确的汇率来源换算港币与人民币金额。', input: '未来将输入金额并选择换算方向。', output: '未来将展示换算金额、汇率来源及更新时间。' },
  { id: 'net-salary', symbol: '$', label: '薪资工具', title: '税后工资估算', brief: '估算全年收入、税款与月均可支配收入', purpose: '估算全年收入扣除预计税款和强积金后的可支配收入。', input: '未来将输入收入、适用税年及必要的估算条件。', output: '未来将分别展示年收入、预计税款、强积金及月均可支配收入。' },
  { id: 'offer', symbol: '≠', label: '薪资工具', title: 'Offer 收入对比', brief: '比较不同薪资和奖金方案', purpose: '将不同 Offer 的月薪、发薪月数和奖金放在统一口径下比较。', input: '未来将分别输入每份 Offer 的薪资和奖金条件。', output: '未来将展示各方案的预计全年收入和金额差异。' },
  { id: 'living-budget', symbol: '∴', label: '生活工具', title: '生活预算计算', brief: '看看每月开支后还能剩多少', purpose: '汇总每月收入和生活开支，估算可剩余金额。', input: '未来将输入每月收入、房租、交通、饮食及其他开支。', output: '未来将展示每月总开支、结余和结余比例。' },
  { id: 'rental-budget', symbol: '⌂', label: '租房工具', title: '租房入住预算', brief: '汇总租金、押金及其他入住支出', purpose: '汇总租房入住时需要准备的各项现金支出。', input: '未来将由用户填写租金、押金、中介费及其他费用。', output: '未来将展示预计入住总支出，不预设统一收费标准。' },
] as const
export type ToolId = (typeof tools)[number]['id']
export function findTool(value: unknown) { return tools.find((tool) => tool.id === value) }
