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
