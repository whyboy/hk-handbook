export const categories = [
  { id: '', label: '全部' }, { id: 'bank', label: '银行卡' },
  { id: 'phone', label: '手机卡' }, { id: 'talent', label: '人才身份' },
  { id: 'life', label: '生活落地' },
] as const
export const audiences = [
  { id: '', label: '所有人群' }, { id: 'visitor', label: '游客' },
  { id: 'talent', label: '人才用户' }, { id: 'family', label: '家庭' },
] as const
export type Category = (typeof categories)[number]['id']
export type Audience = (typeof audiences)[number]['id']
export interface GuideFilters { category: Category; audience: Audience; q: string }
export const categoryLabel = (id: string) => categories.find(item => item.id === id)?.label ?? '攻略'
export const audienceLabel = (id: string) => audiences.find(item => item.id === id)?.label ?? id
export const sections = [
  { id: 'applicability', label: '适用条件' }, { id: 'materials', label: '准备事项与材料' },
  { id: 'steps', label: '操作步骤' }, { id: 'costsAndTime', label: '费用与耗时' },
  { id: 'warnings', label: '注意事项' },
] as const
export type SectionId = (typeof sections)[number]['id']

// switchTab cannot carry query parameters; consume this in-memory handoff once.
let pendingFilters: Partial<GuideFilters> | undefined
export function setGuideFilters(filters: Partial<GuideFilters>) { pendingFilters = { ...filters } }
export function takeGuideFilters() { const next = pendingFilters; pendingFilters = undefined; return next }
// Some platforms recreate Tab pages after returning from a directly opened page.
// Keep only the active filters in memory; never persist article bodies or search input.
let currentFilters: GuideFilters = { category: '', audience: '', q: '' }
export function rememberGuideFilters(filters: GuideFilters) { currentFilters = { ...filters } }
export function rememberedGuideFilters(): GuideFilters { return { ...currentFilters } }
