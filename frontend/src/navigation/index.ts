import type { RecordSectionId } from '@/features/profile/content'
import type { HomeActionId } from '@/features/home/content'
import type { ToolId } from '@/features/tools/content'

// pages.json owns page titles and the native tab bar configuration.
export const routes = {
  home: '/pages/index/index',
  profile: '/pages/profile/index',
  records: '/pages/profile/records',
  feedback: '/pages/profile/feedback',
  checklistDetail: '/pages/checklist/detail',
  guides: '/pages/guides/index',
  guideDetail: '/pages/guides/detail',
  tools: '/pages/tools/index',
  toolIntro: '/pages/tools/intro',
  toolResult: '/pages/tools/result',
} as const
function showNavigationError() {
  uni.showToast({ title: '页面暂时无法打开，请重试', icon: 'none' })
}
export function openToolIntro(scene: HomeActionId | ToolId) {
  uni.navigateTo({ url: `${routes.toolIntro}?scene=${scene}`, fail: showNavigationError })
}
export function openTools() {
  switchTab(routes.tools)
}
export function openToolResult(scene: ToolId) {
  uni.navigateTo({ url: `${routes.toolResult}?scene=${scene}`, fail: showNavigationError })
}
type TabRoute = typeof routes.home | typeof routes.tools | typeof routes.guides | typeof routes.profile
function switchTab(url: TabRoute) {
  uni.switchTab({ url, fail: showNavigationError })
}
function returnTo(url: TabRoute) {
  switchTab(url)
}
/* Secondary pages use navigateTo; top-level destinations always use switchTab.
   Tab page instances retain local display state without persistent storage. */
export function returnHome() { returnTo(routes.home) }
export function returnTools() { returnTo(routes.tools) }

export function openGuides() {
  switchTab(routes.guides)
}
export function openGuide(id: string) {
  uni.navigateTo({ url: `${routes.guideDetail}?id=${encodeURIComponent(id)}`, fail: showNavigationError })
}
export function returnGuides() { returnTo(routes.guides) }

export function openProfile() { switchTab(routes.profile) }
export function openRecords(type: RecordSectionId) {
  uni.navigateTo({ url: `${routes.records}?type=${type}`, fail: showNavigationError })
}
export function openFeedback() { uni.navigateTo({ url: routes.feedback, fail: showNavigationError }) }
export function openChecklistPreview() {
  uni.navigateTo({ url: `${routes.checklistDetail}?demo=layout`, fail: showNavigationError })
}
export function returnProfile() { returnTo(routes.profile) }
export function returnChecklists() {
  const pages = getCurrentPages()
  const target = pages.findIndex((page) => `/${page.route}` === routes.records &&
    (page as { options?: { type?: string } }).options?.type === 'checklists')
  const url = `${routes.records}?type=checklists`
  if (target >= 0 && target < pages.length - 1) {
    uni.navigateBack({ delta: pages.length - 1 - target,
      fail: () => uni.reLaunch({ url, fail: showNavigationError }) })
  } else {
    uni.reLaunch({ url, fail: showNavigationError })
  }
}
