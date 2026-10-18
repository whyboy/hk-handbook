import type { HomeActionId } from '@/features/home/content'

// T01 registers only the routes needed for its navigation loop.
// T05 will extend this contract when the four-tab navigation is introduced.
export const routes = {
  home: '/pages/index/index',
  toolIntro: '/pages/tools/intro',
} as const

function showNavigationError() {
  uni.showToast({ title: '页面暂时无法打开，请重试', icon: 'none' })
}

export function openToolIntro(scene: HomeActionId) {
  uni.navigateTo({
    url: `${routes.toolIntro}?scene=${scene}`,
    fail: showNavigationError,
  })
}

export function returnHome() {
  // Normal entry preserves the home page (including its scroll position).
  // Direct links and browser refreshes have no home page in the page stack.
  if (getCurrentPages().length > 1) {
    uni.navigateBack({
      delta: 1,
      fail: () => uni.reLaunch({ url: routes.home, fail: showNavigationError }),
    })
  } else {
    uni.reLaunch({ url: routes.home, fail: showNavigationError })
  }
}
