<template>
  <PageContainer wide>
    <view class="brand">
      <view class="brand-line">
        <text class="brand-name">港易通</text>
        <text class="brand-english">HONG KONG GUIDE</text>
      </view>
      <text class="slogan">第一次去香港，从这里开始。</text>
    </view>

    <button class="search button-reset" hover-class="pressed" @click="openGuides({})">
      <view class="search-icon" aria-hidden="true" />
      <text class="search-label">搜索：香港银行卡 / 高才 / 手机卡</text>
      <text class="search-status">搜索攻略</text>
    </button>

    <view class="task-section">
      <view class="section-eyebrow">
        <view class="status-dot" aria-hidden="true" />
        <text>办事与生活 · 从这里开始</text>
      </view>
      <text class="section-title">你想了解香港的哪些事？</text>
      <text class="section-description">从一件事开始，把准备做得更清楚。</text>
      <view class="task-grid">
        <button
          v-for="action in topics"
          :key="action.id"
          class="task-card button-reset"
          hover-class="pressed"
          @click="openTopic(action.id)"
        >
          <view :class="['task-icon', `task-icon-${action.id}`]" aria-hidden="true">
            <view class="icon-detail" />
          </view>
          <view class="task-title">
            <text>{{ action.id === 'talent' ? '高才／优才' : action.title }}</text>
            <text v-if="action.id === 'talent'">申请</text>
          </view>
          <text class="task-subtitle">{{ action.subtitle }}</text>
        </button>
      </view>
    </view>


    <view class="guides-section">
      <view class="section-heading">
        <text class="section-title">推荐攻略</text>
        <text class="preview-label">已发布内容</text>
      </view>
      <text class="guides-note">了解适用条件，查看官方来源，出发前再核实。</text>
      <PageState v-if="guideState === 'loading'" kind="loading" title="正在加载攻略" description="请稍候…" />
      <PageState v-else-if="guideState === 'error'" kind="error" title="攻略加载失败" description="请检查网络后重试。">
        <button class="all-tools" @click="loadGuides">重试</button>
      </PageState>
      <PageState v-else-if="!guidePreviews.length" kind="empty" title="暂无已发布攻略" description="攻略正在整理与核验中，发布后会在这里显示。" />
      <button
        v-for="guide in guidePreviews"
        :key="guide.id"
        class="guide-card button-reset"
        hover-class="pressed"
        @click="openGuide(guide.id)"
      >
        <view class="guide-copy">
          <text class="guide-category">{{ categoryLabel(guide.category) }}</text>
          <text class="guide-title">{{ guide.title }}</text>
          <text class="guide-summary">{{ guide.summary }}</text>
          <text class="guide-status">事实核验：{{ guide.verifiedOn }}</text>
        </view>
        <view class="chevron" aria-hidden="true" />
      </button>
    </view>
    <button class="all-tools" @click="openGuides({})">查看全部攻略 →</button>
    <button class="all-tools" @click="openProfile">我的收藏与记录 →</button>
    <text class="footer-note">赴港办事，先了解，再出发。</text>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow, onHide, onUnload } from '@dcloudio/uni-app'
import PageContainer from '@/components/PageContainer.vue'
import PageState from '@/components/PageState.vue'
import { topics } from '@/features/topics/content'
import { categoryLabel } from '@/features/guides/content'
import { getGuides, type GuideSummary } from '@/features/guides/api'
import { openTopic, openGuide, openGuides, openProfile } from '@/navigation'
const guidePreviews = ref<GuideSummary[]>([])
const guideState = ref<'loading' | 'ready' | 'error'>('loading')
let generation = 0
async function loadGuides() {
  const requestId = ++generation; guidePreviews.value = []; guideState.value = 'loading'
  try {
    const result = await getGuides({}, 1, 3)
    if (requestId !== generation) return
    guidePreviews.value = result.items; guideState.value = 'ready'
  } catch { if (requestId === generation) guideState.value = 'error' }
}
onShow(loadGuides)
function invalidate() { generation++; guidePreviews.value = []; guideState.value = 'loading' }
onHide(invalidate); onUnload(invalidate)
</script>

<style scoped>

.brand { margin-bottom: 24px; }
.brand-line { display: flex; flex-wrap: wrap; align-items: baseline; gap: 10px; }
.brand-name { font-size: 26px; font-weight: 700; letter-spacing: 1px; }
.brand-english { font-size: 10px; font-weight: 600; letter-spacing: 1.5px; color: #627570; }
.slogan { display: block; margin-top: 8px; font-size: 14px; color: #627570; }
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
  border: 1px solid #e3e9e5;
  border-radius: 16px;
  background: #fff;
  color: #627570;
}
.search-icon { position: relative; flex-shrink: 0; width: 13px; height: 13px; margin-right: 12px; border: 1.5px solid currentColor; border-radius: 50%; }
.search-icon::after { content: ''; position: absolute; width: 6px; height: 1.5px; right: -5px; bottom: -2px; background: currentColor; transform: rotate(45deg); }
.search-label { flex: 1; min-width: 0; font-size: 13px; }
.search-status { flex-shrink: 0; margin-left: 10px; font-size: 11px; }
.task-section { margin-top: 24px; padding: 22px 16px 16px; border-radius: 20px; background: #eaf2ee; }
.section-eyebrow { display: flex; align-items: center; gap: 7px; margin-bottom: 12px; font-size: 12px; color: #38685e; }
.status-dot { width: 5px; height: 5px; border-radius: 50%; background: #38685e; }
.section-title { display: block; font-size: 21px; font-weight: 700; line-height: 1.5; }
.section-description { display: block; margin-top: 6px; font-size: 13px; color: #627570; }
.task-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 8px; margin-top: 22px; }
.task-card { display: flex; flex: 1; min-width: 0; flex-direction: column; align-items: center; padding: 20px 4px 14px; border-radius: 12px; background: #fff; text-align: center; }
.task-title { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 45px; margin-top: 14px; font-size: 15px; font-weight: 600; overflow-wrap: anywhere; }
.task-subtitle { margin-top: 5px; font-size: 11px; color: #627570; }
.task-status { margin-top: 12px; font-size: 10px; color: #627570; }
.task-icon { position: relative; box-sizing: border-box; width: 26px; height: 22px; color: #38685e; }
.task-icon-bank { border: 1.8px solid currentColor; border-radius: 5px; }
.task-icon-bank::before { content: ''; position: absolute; top: 5px; left: 0; right: 0; height: 2px; background: currentColor; }
.task-icon-bank .icon-detail { position: absolute; width: 5px; height: 2px; bottom: 4px; left: 4px; background: currentColor; }
.task-icon-talent::before { content: ''; position: absolute; top: 0; left: 3px; width: 18px; height: 12px; border: 1.8px solid currentColor; border-radius: 2px; transform: rotate(-30deg) skewX(30deg) scaleY(.8); }
.task-icon-talent .icon-detail { position: absolute; left: 5px; top: 11px; width: 15px; height: 8px; border: 1.8px solid currentColor; border-top: 0; border-radius: 0 0 6px 6px; }
.task-icon-checklist { width: 19px; border: 1.8px solid currentColor; border-radius: 4px; }
.task-icon-checklist::before { content: ''; position: absolute; top: -4px; left: 4px; width: 7px; height: 4px; border: 1.5px solid currentColor; border-radius: 2px; background: #fff; }
.task-icon-checklist .icon-detail { position: absolute; top: 8px; left: 5px; width: 7px; height: 4px; border-left: 1.8px solid currentColor; border-bottom: 1.8px solid currentColor; transform: rotate(-45deg); }
.all-tools { margin-top: 16px; font-size: 14px; color: #286456; background: #eaf2ee; }
.guides-section { margin-top: 30px; }
.section-heading { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.preview-label { padding: 3px 8px; border: 1px solid #dce5df; border-radius: 5px; font-size: 11px; color: #627570; }
.guides-note { display: block; margin: 6px 0 16px; font-size: 12px; color: #627570; }
.guide-card { display: flex; align-items: center; width: 100%; box-sizing: border-box; margin-top: 10px; padding: 17px 18px; border: 1px solid #e6ebe7; border-radius: 14px; background: #fff; }
.guide-copy { flex: 1; min-width: 0; }
.guide-category { display: block; font-size: 11px; font-weight: 600; color: #38685e; }
.guide-title { display: block; margin-top: 5px; font-size: 16px; font-weight: 600; }
.guide-summary { display: block; margin-top: 6px; font-size: 12px; color: #627570; }
.guide-status { display: block; margin-top: 10px; font-size: 10px; color: #75674c; }
.chevron { width: 6px; height: 6px; margin-left: 14px; flex-shrink: 0; border-top: 1.5px solid #627570; border-right: 1.5px solid #627570; transform: rotate(45deg); }
.footer-note { display: block; margin-top: 28px; font-size: 11px; text-align: center; letter-spacing: 1px; color: #627570; }
.pressed { opacity: .7; }
@media (min-width: 640px) {

  .task-section { padding: 26px; }
  .task-grid { gap: 14px; }
  .task-card { padding-top: 24px; padding-bottom: 20px; }
}
@media (max-width: 350px) {

  .task-section { padding-left: 10px; padding-right: 10px; }
  .section-title { font-size: 19px; }
  .task-title { font-size: 13px; }
  .search { padding-left: 12px; padding-right: 12px; }
  .search-label { font-size: 12px; }
  .search-status { margin-left: 6px; }
}
</style>

<style scoped>
.task-icon-phone { width: 16px; border: 1.8px solid currentColor; border-radius: 3px; }
.task-icon-phone::after { content: ''; position: absolute; bottom: 3px; left: 5px; width: 3px; height: 2px; background: currentColor; }
.task-icon-transport { border: 1.8px solid currentColor; border-radius: 5px; }
.task-icon-transport::before { content: ''; position: absolute; inset: 4px 3px 8px; border: 1px solid currentColor; }
.task-icon-transport::after { content: '• •'; position: absolute; bottom: -4px; left: 4px; font-size: 13px; }
.task-icon-attractions { width: 21px; height: 21px; border: 1.8px solid currentColor; border-radius: 50% 50% 50% 0; transform: rotate(-45deg); }
.task-icon-attractions::after { content: ''; position: absolute; inset: 5px; border: 1.5px solid currentColor; border-radius: 50%; }
.task-icon-tax { border: 1.8px solid currentColor; border-radius: 3px; }
.task-icon-tax::before { content: ''; position: absolute; top: 4px; left: 4px; right: 4px; height: 4px; border: 1px solid currentColor; }
.task-icon-tax::after { content: '···'; position: absolute; bottom: -3px; left: 5px; font-weight: 700; }
</style>
