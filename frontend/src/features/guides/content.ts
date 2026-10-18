// Layout samples only; never represent verified advice or published content.
export const categories = ['全部', '银行卡', '手机卡', '人才身份', '生活落地'] as const
export type Category = (typeof categories)[number]
export const guideSamples = [
  { id: 'bank-preparation', category: '银行卡', title: '香港银行卡开户，先准备什么？', summary: '示例摘要：这里将介绍开户准备的内容结构。' },
  { id: 'mobile-preparation', category: '手机卡', title: '赴港通信，先了解哪些事项？', summary: '示例摘要：这里将介绍通信准备的内容结构。' },
  { id: 'talent-paths', category: '人才身份', title: '高才通 A / B / C 类路径说明', summary: '示例摘要：这里将介绍人才路径的内容结构。' },
  { id: 'first-visit', category: '生活落地', title: '第一次赴港，把准备事项列清楚', summary: '示例摘要：这里将介绍首次赴港准备的内容结构。' },
] as const
export function findGuide(value: unknown) { return guideSamples.find((guide) => guide.id === value) }
export type DisplayState = 'ready' | 'loading' | 'empty' | 'error'
export function parseDisplayState(value: unknown): DisplayState {
  return value === 'loading' || value === 'empty' || value === 'error' ? value : 'ready'
}
