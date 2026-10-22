<template>
  <PageContainer>
    <view class="page-heading"><text class="eyebrow">港易通 / {{ tool?.label ?? '实用工具' }}</text><text class="edition">工具预览</text></view>
    <template v-if="tool">
      <view class="intro-hero"><text :class="['tool-icon', tool.id]">{{ tool.symbol }}</text><text class="title">{{ tool.title }}</text><text class="description muted">{{ tool.brief }}</text><text class="status-badge hero-status">筹备中 · 暂未开放</text></view>
      <view class="preview-panel"><text class="eyebrow">为你的下一步，提前做准备</text><text class="section-title spaced">{{ tool.purpose }}</text></view>
      <view class="section-heading"><text class="section-title">未来可以这样使用</text><text class="section-caption">功能规划</text></view>
      <view class="steps-panel">
        <view class="preview-step"><text class="step-number">01</text><view class="step-copy"><text class="card-title">梳理你的情况</text><text class="description muted">{{ tool.input }}</text></view></view>
        <view class="preview-step"><text class="step-number">02</text><view class="step-copy"><text class="card-title">找到准备方向</text><text class="description muted">{{ tool.output }}</text></view></view>
      </view>
      <button class="nav-button" disabled>正在准备，敬请期待</button>
      <text class="availability centered">当前仅为页面预览，不收集信息或生成结果。</text>
    </template>
    <view v-else class="panel"><text class="section-title">未找到对应工具</text><text class="description">入口暂不可用，请返回工具页重新选择。</text></view>
    <button class="text-button" @click="returnTools">返回工具页 →</button>
  </PageContainer>
</template>
<script setup lang="ts">
import PageContainer from '@/components/PageContainer.vue'
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { findTool } from '@/features/tools/content'
import { returnTools } from '@/navigation'
const tool = ref<ReturnType<typeof findTool>>()
onLoad((query) => { tool.value = findTool(query?.scene) })
</script>
<style scoped src="./tools.css"></style>
