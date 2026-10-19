<template>
  <PageContainer>
    <text class="eyebrow">港易通 · 攻略</text>
    <text class="title">赴港之前，先了解</text>
    <text class="notice">框架示例 · 尚未核验，不作为办事依据。攻略内容功能建设中。</text>
    <view class="categories">
      <button v-for="item in categories" :key="item" :class="['category', { selected: category === item }]" @click="category = item">{{ item }}</button>
    </view>
    <PageState v-if="state !== 'ready'" :kind="state" :title="stateTitles[state]" description="当前为展示状态示例，未连接内容服务。">
      <button class="nav-button" @click="state = 'ready'">返回示例列表</button>
    </PageState>
    <template v-else>
      <button v-for="guide in visibleGuides" :key="guide.id" class="guide-card" @click="openGuide(guide.id)">
        <text class="eyebrow">{{ guide.category }}</text>
        <text class="section-title">{{ guide.title }}</text>
        <text class="description">{{ guide.summary }}</text>
        <text class="sample-label">示例 · 尚未核验</text>
      </button>
    </template>
    <button class="nav-button" @click="returnHome">返回首页</button>
  </PageContainer>
</template>
<script setup lang="ts">
import PageState from '@/components/PageState.vue'
import PageContainer from '@/components/PageContainer.vue'
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { categories, guideSamples, parseDisplayState, type Category, type DisplayState } from '@/features/guides/content'
import { openGuide, returnHome } from '@/navigation'
const category = ref<Category>('全部')
const state = ref<DisplayState>('ready')
const stateTitles = { loading: '加载中（示例）', empty: '暂无攻略（示例）', error: '加载失败（示例）' }
const visibleGuides = computed(() => guideSamples.filter((guide) => category.value === '全部' || guide.category === category.value))
onLoad((query) => { state.value = parseDisplayState(query?.demo) })
</script>
<style scoped src="./guides.css"></style>
