<template>
  <PageContainer wide>
    <GuideSearch v-if="searchMode" :key="searchKey" @close="closeSearch" />
    <template v-else>
    <view class="brand">
      <view class="brand-symbol" aria-hidden="true"><text>HK</text></view>
      <view class="brand-line">
        <text class="brand-name">香港百事通</text>
        <text class="brand-english">HONG KONG LIFE GUIDE</text>
        <text class="slogan">赴港生活大小事，一站轻松了解</text>
      </view>
    </view>

    <button class="search button-reset" hover-class="pressed" @click="openSearch">
      <view class="search-icon" aria-hidden="true" />
      <text class="search-label">搜索办事攻略与生活指南</text>
      <text class="search-status">搜索攻略</text>
    </button>

    <view class="task-section">
      <view class="section-eyebrow">
        <view class="status-dot" aria-hidden="true" />
        <text>香港生活服务</text>
      </view>
      <text class="section-title">热门办事指南</text>
      <text class="section-description">从出行到安居，快速找到你需要的信息</text>
      <view class="task-grid">
        <button
          v-for="action in topics"
          :key="action.id"
          class="task-card button-reset"
          hover-class="pressed"
          @click="openTopic(action.id)"
        >
          <view :class="['task-icon', `task-icon-${action.id}`]" aria-hidden="true">{{ action.mark }}</view>
          <text class="task-title">{{ action.title }}</text>
          <text class="task-subtitle">{{ action.subtitle }}</text>
        </button>
      </view>
    </view>
    </template>
  </PageContainer>
</template>

<script setup lang="ts">
import PageContainer from '@/components/PageContainer.vue'
import GuideSearch from '@/components/GuideSearch.vue'
import { topics } from '@/features/topics/content'
import { rememberGuideFilters, takeGuideFilters } from '@/features/guides/content'
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { openTopic } from '@/navigation'

const searchMode = ref(false)
const searchKey = ref(0)
function openSearch() {
  rememberGuideFilters({ category: '', q: '' })
  searchKey.value++
  searchMode.value = true
}
function closeSearch() { searchMode.value = false }
onShow(() => {
  const next = takeGuideFilters()
  if (!next) return
  rememberGuideFilters({ category: next.category ?? '', q: next.q ?? '' })
  searchKey.value++
  searchMode.value = true
})
</script>

<style scoped>

.brand { display: flex; align-items: center; gap: 14px; margin: 4px 0 26px; }
.brand-symbol { display: flex; flex: 0 0 auto; align-items: center; justify-content: center; width: 52px; height: 52px; border-radius: 17px 17px 17px 5px; color: #fff; background: linear-gradient(145deg, #e54c4c, #bd2838); box-shadow: 0 8px 18px rgba(190, 42, 56, .22); }
.brand-symbol text { font-size: 16px; font-weight: 800; letter-spacing: 1px; }
.brand-line { display: flex; min-width: 0; flex-direction: column; }
.brand-name { color: #173a38; font-size: 27px; font-weight: 800; letter-spacing: 1.5px; line-height: 1.2; }
.brand-english { margin-top: 3px; font-size: 9px; font-weight: 700; letter-spacing: 1.2px; color: #a83943; }
.slogan { margin-top: 5px; font-size: 12px; color: #6e7f7b; }
.button-reset {
  margin: 0;
  padding: 0;
  border: 0;
  border-radius: 0;
  font: inherit;
  color: inherit;
  line-height: 1.5;
  text-align: left;
  background: transparent;
}
.button-reset::after { border: 0; }
.button-reset:focus-visible { outline: 2px solid #26766a; outline-offset: 3px; }
.search {
  display: flex;
  align-items: center;
  width: 100%;
  min-height: 52px;
  padding: 12px 16px;
  box-sizing: border-box;
  border: 1px solid rgba(36, 93, 86, .12);
  border-radius: 18px;
  background: #fff;
  color: #6e7f7b;
  box-shadow: 0 8px 24px rgba(29, 69, 64, .07);
}
.search-icon { position: relative; flex-shrink: 0; width: 13px; height: 13px; margin-right: 12px; border: 1.5px solid currentColor; border-radius: 50%; }
.search-icon::after { content: ''; position: absolute; width: 6px; height: 1.5px; right: -5px; bottom: -2px; background: currentColor; transform: rotate(45deg); }
.search-label { flex: 1; min-width: 0; font-size: 13px; }
.search-status { flex-shrink: 0; margin-left: 10px; padding: 4px 8px; border-radius: 9px; color: #28685f; font-size: 11px; background: #eaf5f1; }
.task-section { margin-top: 24px; padding: 23px 14px 16px; border: 1px solid rgba(36, 93, 86, .08); border-radius: 24px; background: linear-gradient(155deg, #edf7f3 0%, #f7f4eb 100%); box-shadow: 0 12px 30px rgba(29, 69, 64, .06); }
.section-eyebrow { display: flex; align-items: center; gap: 7px; margin-bottom: 10px; font-size: 12px; font-weight: 600; color: #a83943; }
.status-dot { width: 6px; height: 6px; border-radius: 50%; background: #d7444e; box-shadow: 0 0 0 4px rgba(215, 68, 78, .1); }
.section-title { display: block; color: #173a38; font-size: 22px; font-weight: 800; line-height: 1.4; }
.section-description { display: block; margin-top: 5px; font-size: 13px; color: #6e7f7b; }
.task-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; margin-top: 20px; }
.task-card { display: flex; min-width: 0; min-height: 128px; flex-direction: column; align-items: center; justify-content: center; padding: 14px 5px 12px; border: 1px solid rgba(42, 91, 84, .08); border-radius: 16px; background: rgba(255, 255, 255, .94); text-align: center; box-shadow: 0 5px 16px rgba(29, 69, 64, .06); }
.task-title { margin-top: 10px; color: #244744; font-size: 15px; font-weight: 700; line-height: 1.35; white-space: nowrap; }
.task-subtitle { margin-top: 4px; font-size: 10px; color: #81918d; }
.task-icon { display: flex; align-items: center; justify-content: center; width: 42px; height: 42px; border-radius: 14px; color: #28685f; font-size: 17px; font-weight: 800; background: #e3f2ed; }
.task-card:nth-child(3n + 2) .task-icon { color: #a83943; background: #f9e8e7; }
.task-card:nth-child(3n) .task-icon { color: #826a2d; background: #f6efd9; }
.pressed { opacity: .7; }
/* #ifdef H5 */
.task-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
/* #endif */
@media (min-width: 640px) {
  .task-section { padding: 26px; }
  .task-grid { gap: 14px; }
  .task-card { min-height: 142px; padding-top: 20px; padding-bottom: 18px; }
}
@media (max-width: 350px) {

  .task-section { padding-left: 10px; padding-right: 10px; }
  .section-title { font-size: 19px; }
  .task-title { font-size: 12px; }
  .search { padding-left: 12px; padding-right: 12px; }
  .search-label { font-size: 12px; }
  .search-status { margin-left: 6px; }
}
</style>
