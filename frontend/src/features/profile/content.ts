// Display metadata only. No persisted records or storage access.
export const recordSections = [
  { id: 'favorites', title: '我的收藏', empty: '暂无收藏展示', description: '收藏功能建设中，当前未读取或保存任何收藏。' },
  { id: 'checklists', title: '办事清单', empty: '暂无清单展示', description: '清单功能建设中，当前未生成、读取或保存任何清单。' },
  { id: 'assessments', title: '测评记录', empty: '暂无测评记录展示', description: '测评功能建设中，当前未执行测评或读取历史记录。' },
] as const
export type RecordSectionId = (typeof recordSections)[number]['id']
export function findRecordSection(value: unknown) { return recordSections.find((section) => section.id === value) }
export const checklistStages = [
  { title: '出发前', step: '准备事项将在这里列出' },
  { title: '办理中', step: '办理步骤将在这里列出' },
  { title: '完成后', step: '后续核实事项将在这里列出' },
] as const
