<template>
  <PageContainer>
    <view class="page-heading"><text class="eyebrow">港易通 / {{ tool?.label ?? (identity ? '人才身份' : '实用工具') }}</text><text class="edition">工具预览</text></view>
    <template v-if="tool">
      <view class="intro-hero"><text :class="['tool-icon', tool.id]">{{ tool.symbol }}</text><text class="title">{{ tool.title }}</text><text class="description muted">{{ tool.brief }}</text><text class="status-badge hero-status">筹备中 · 暂未开放</text></view>
      <view class="preview-panel"><text class="eyebrow">为你的下一步，提前做准备</text><text class="section-title spaced">{{ tool.purpose }}</text><text class="description muted">{{ tool.note }}</text></view>
      <view class="section-heading"><text class="section-title">未来可以这样使用</text><text class="section-caption">功能规划</text></view>
      <view class="steps-panel">
        <view class="preview-step"><text class="step-number">01</text><view class="step-copy"><text class="card-title">梳理你的情况</text><text class="description muted">{{ tool.input }}</text></view></view>
        <view class="preview-step"><text class="step-number">02</text><view class="step-copy"><text class="card-title">找到准备方向</text><text class="description muted">{{ tool.output }}</text></view></view>
      </view>
      <button class="nav-button" disabled>正在准备，敬请期待</button>
      <text class="availability centered">当前仅为页面预览，不收集信息或生成结果。</text>
    </template>
    <template v-else-if="identity">
      <text class="title">为下一段生活，了解更多</text><text class="description muted">高才与优才是不同的申请路径，先选择你想了解的一项。</text>
      <button v-for="item in talentTools" :key="item.id" class="tool-card" @click="openToolIntro(item.id)"><view class="card-top"><text :class="['tool-icon', item.id]">{{ item.symbol }}</text><text class="status-badge">筹备中</text></view><text class="card-title">{{ item.title }}</text><text class="description muted">{{ item.brief }}</text><view class="card-footer"><text>查看介绍</text><text>↗</text></view></button>
    </template>
    <view v-else class="panel"><text class="section-title">未找到对应工具</text><text class="description">入口暂不可用，请返回工具页重新选择。</text></view>
    <button v-if="tool || identity" class="nav-button secondary" @click="openGuides({ category: tool?.id === 'bank' ? 'bank' : tool?.id === 'checklist' ? 'life' : 'talent' })">查看相关攻略</button>
    <button class="text-button" @click="returnTools">返回工具页 →</button>
  </PageContainer>
</template>
<script setup lang="ts">
import PageContainer from '@/components/PageContainer.vue'
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { findTool, tools } from '@/features/tools/content'
import { openToolIntro, openGuides, returnTools } from '@/navigation'
const tool = ref<ReturnType<typeof findTool>>()
const identity = ref(false)
const talentTools = tools.filter((item) => item.id === 'talent' || item.id === 'quality')
onLoad((query) => { tool.value = findTool(query?.scene); identity.value = query?.scene === 'identity' })
</script>
<style scoped src="./tools.css"></style>
