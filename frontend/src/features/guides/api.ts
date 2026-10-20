import { categories, audiences, sections, type GuideFilters, type SectionId } from './content'
export interface Period { state: 'known' | 'not_stated'; from: string | null; until: string | null }
export interface GuideSummary {
  matchExcerpt?: string
  id: string; revision: number; title: string; summary: string; category: string; audiences: string[]
  keywords: string[]; editedOn: string; reviewDueOn: string; publishedOn: string; verifiedOn: string; effectivePeriod: Period
}
export interface GuideBlock { kind: 'official_fact' | 'editorial_advice' | 'personal_experience'; text: string; factIds: string[]; experienceContext: string | null }
export interface GuideSection { state: 'known' | 'not_applicable'; reason: string | null; blocks: GuideBlock[] }
export interface GuideSource { id: string; organization: string; title: string; url: string; consultedOn: string; effectivePeriod: Period }
export interface GuideDetail extends GuideSummary {
  sections: Record<SectionId, GuideSection>
  facts: { id: string; claim: string; conditions: string; sourceIds: string[] }[]
  sources: GuideSource[]
}
export interface GuidePage { items: GuideSummary[]; total: number; page: number; size: number }
export class GuideError extends Error {
  constructor(public status: number, message = '暂时无法加载，请检查网络后重试。') { super(message) }
}
const record = (v: unknown): v is Record<string, any> => typeof v === 'object' && v !== null && !Array.isArray(v)
const text = (v: unknown): v is string => typeof v === 'string' && v.trim().length > 0
const date = (v: unknown) => typeof v === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(v)
const strings = (v: unknown): v is string[] => Array.isArray(v) && v.every(text)
const period = (v: unknown) => record(v) && ['known', 'not_stated'].includes(v.state) && (v.from === null || date(v.from)) && (v.until === null || date(v.until))
function summary(v: unknown): v is GuideSummary {
  return record(v) && (v.matchExcerpt === undefined || text(v.matchExcerpt)) && text(v.id) && /^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(v.id) && Number.isInteger(v.revision) && v.revision > 0 && text(v.title) && text(v.summary)
    && categories.some(c => c.id !== '' && c.id === v.category) && strings(v.audiences) && v.audiences.length > 0 && v.audiences.every(a => audiences.some(item => item.id !== '' && item.id === a))
    && strings(v.keywords) && ['editedOn', 'reviewDueOn', 'publishedOn', 'verifiedOn'].every(key => date(v[key])) && period(v.effectivePeriod)
}
function detail(v: unknown): v is GuideDetail {
  return record(v) && record(v.sections) && sections.every(({ id }) => {
    const s = v.sections[id]
    return record(s) && ['known', 'not_applicable'].includes(s.state) && (s.reason === null || text(s.reason)) && Array.isArray(s.blocks) && s.blocks.every((b: unknown) => record(b) && ['official_fact', 'editorial_advice', 'personal_experience'].includes(b.kind) && text(b.text) && strings(b.factIds) && (b.experienceContext === null || text(b.experienceContext)))
  }) && Array.isArray(v.sources) && v.sources.every((s: unknown) => record(s) && ['id', 'title', 'organization'].every(k => text(s[k])) && safeSourceUrl(s.url) && date(s.consultedOn) && period(s.effectivePeriod))
    && Array.isArray(v.facts) && v.facts.every((f: unknown) => record(f) && ['id', 'claim', 'conditions'].every(k => text(f[k])) && strings(f.sourceIds)) && summary(v)
}
export function safeSourceUrl(value: unknown): value is string {
  return typeof value === 'string' && /^https:\/\/[^\s/@?#]+(?:[/?#][^\s]*)?$/.test(value)
}
function apiBase() {
  const base = (import.meta.env.VITE_API_BASE_URL ?? '').trim().replace(/\/+$/, '')
  // #ifdef MP-WEIXIN
  if (!/^https:\/\/[^\s]+$/.test(base) && !(import.meta.env.DEV && /^http:\/\/[^\s]+$/.test(base))) {
    throw new GuideError(0, '攻略服务暂不可用，请稍后再试。')
  }
  // #endif
  return base
}
async function request(path: string): Promise<unknown> {
  const base = apiBase()
  return new Promise((resolve, reject) => {
    uni.request({ url: `${base}/api/guides${path}`, method: 'GET', timeout: 10000,
      success: response => response.statusCode === 200 ? resolve(response.data) : reject(new GuideError(response.statusCode)),
      fail: () => reject(new GuideError(0)),
    })
  })
}
export async function getGuides(filters: Partial<GuideFilters> = {}, page = 1, size = 20): Promise<GuidePage> {
  const params = { category: filters.category ?? '', q: filters.q?.trim() ?? '', page, size }
  const query = Object.entries(params).map(([key, value]) => `${key}=${encodeURIComponent(value)}`).join('&')
  const data = await request(`?${query}`)
  if (!record(data) || !Array.isArray(data.items) || !data.items.every(summary) || !Number.isInteger(data.total) || data.total < 0 || data.page !== page || data.size !== size) throw new GuideError(0, '攻略数据暂不可用，请稍后重试。')
  return data as GuidePage
}
export async function getGuide(id: string): Promise<GuideDetail> {
  if (!/^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(id) || id.length > 80) throw new GuideError(404)
  const data = await request(`/${encodeURIComponent(id)}`)
  if (!detail(data) || data.id !== id) throw new GuideError(0, '攻略数据暂不可用，请稍后重试。')
  return data
}
export function openSource(url: string) {
  if (!safeSourceUrl(url)) { uni.showToast({ title: '来源地址暂不可用', icon: 'none' }); return }
  // #ifdef H5
  const opened = window.open(url, '_blank')
  if (opened) { opened.opener = null; return }
  // #endif
  uni.setClipboardData({ data: url,
    success: () => uni.showToast({ title: '已复制来源链接，请在浏览器打开', icon: 'none' }),
    fail: () => uni.showToast({ title: '复制失败，请长按下方链接复制', icon: 'none' }),
  })
}
export function periodLabel(value: Period) {
  return value.state === 'not_stated' ? '官方未注明统一生效日期' : `${value.from ?? '未注明起始日'} 起，${value.until ? `${value.until} 起失效` : '未注明失效日'}`
}
