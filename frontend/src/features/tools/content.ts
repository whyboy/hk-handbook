// Presentation only: no rules, answers or generated results.
export const tools = [
  { id: 'checklist', symbol: '✓', label: '行前准备', brief: '把每一次赴港，准备得井井有条', note: '出发前反复核对，办理时随手查看', title: '赴港清单生成', purpose: '按赴港目的整理出发前、办理中与完成后的准备事项。', input: '未来将选择办事目的及相关准备情况。', output: '未来将展示准备步骤、材料与核实提示；当前不会生成或保存清单。' },
  { id: 'bank', symbol: '▤', label: '银行办事', brief: '开户前，先理清准备方向', note: '了解要求 · 比较方案 · 准备材料', title: '银行开户匹配', purpose: '了解银行公开开户要求，比较需要进一步核实的方案。', input: '未来将询问身份证持有情况、开户用途、能否亲临香港及跨境服务需要。', output: '未来将展示匹配理由、待核实条件、材料与官方入口，不承诺开户成功。' },
  { id: 'talent', symbol: 'A', label: '人才身份', brief: '了解高才通的申请路径', note: 'A / B / C 类路径分别了解', title: '高才资格初筛', purpose: '分别了解高才通 A、B、C 类路径的适用条件。', input: '未来将按路径了解收入、学历和工作经历；不收集证件扫描件。', output: '未来将展示明确条件、待核实项和材料缺口，不预测批准概率。' },
  { id: 'quality', symbol: 'Q', label: '人才身份', brief: '梳理优才的资格与条件', note: '基本资格与评核机制分别了解', title: '优才资格初筛', purpose: '了解优才基本资格及两种评核机制，与高才分开评估。', input: '未来将了解基本资格和所选评核机制相关情况；不收集证件扫描件。', output: '未来将分层说明条件和待核实信息，不提供获批承诺。' },

] as const
export type ToolId = (typeof tools)[number]['id']
export function findTool(value: unknown) { return tools.find((tool) => tool.id === value) }
