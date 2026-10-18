// Presentation-only content. No bank requirements or eligibility rules live here.
export const homeActions = [
  {
    id: 'bank',
    title: '办银行卡',
    subtitle: '了解开户准备',
    description: '这里将帮助你了解银行公开开户要求、比较方案，并整理需要准备的资料。',
  },
  {
    id: 'identity',
    title: '申请身份',
    subtitle: '了解人才路径',
    description: '这里将分别提供高才通与优才初筛，说明明确条件、待核实信息和材料缺口。',
  },
  {
    id: 'checklist',
    title: '赴港清单',
    subtitle: '梳理办事步骤',
    description: '这里将按赴港目的整理准备步骤，帮助你逐项确认出发前、办理中和完成后的事项。',
  },
] as const

export type HomeActionId = (typeof homeActions)[number]['id']

export function findHomeAction(value: unknown) {
  return homeActions.find((action) => action.id === value)
}

export const guidePreviews = [
  {
    id: 'bank-preparation',
    category: '银行开户',
    title: '香港银行卡开户，先准备什么？',
    summary: '从资料准备开始，了解出发前需要核实的事项。',
  },
  {
    id: 'talent-paths',
    category: '人才身份',
    title: '高才通 A / B / C 类路径说明',
    summary: '先了解不同申请路径，再查看适用条件。',
  },
  {
    id: 'first-visit',
    category: '生活落地',
    title: '第一次赴港，把准备事项列清楚',
    summary: '从出发前准备，到通信、交通与办事安排。',
  },
] as const
