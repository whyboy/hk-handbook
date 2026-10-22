<template>
  <PageContainer>
    <view class="page-heading"><text class="eyebrow">港易通 / 实用工具</text><text class="edition">筹备中</text></view>
    <text class="title">赴港办事，心里有数</text>
    <text class="description muted">从行前准备到人生新计划，把复杂的事慢慢理清。</text>
    <button class="featured" @click="openToolIntro(primary.id)">
      <view class="card-top"><text class="light-label">行前准备 · 优先入口</text><text class="light-badge">筹备中</text></view>
      <view class="feature-body"><view><text class="feature-title">赴港清单</text><text class="feature-subtitle">{{ primary.brief }}</text></view><text class="feature-icon">✓</text></view>
      <view class="feature-tags"><text>出发前</text><text>办理中</text><text>完成后</text></view>
      <view class="feature-footer"><text>先看看未来能帮你做什么</text><text>查看介绍 ↗</text></view>
    </button>
    <view class="section-heading"><text class="section-title">办事准备</text><text class="section-caption">有计划，再出发</text></view>
    <button class="tool-card bank-card" @click="openToolIntro(bank.id)">
      <view class="card-top"><text class="tool-icon">{{ bank.symbol }}</text><text class="status-badge">筹备中</text></view>
      <text class="card-title">{{ bank.title }}</text><text class="description muted">{{ bank.brief }}</text>
      <view class="card-footer"><text>{{ bank.note }}</text><text class="arrow">↗</text></view>
    </button>
    <view class="section-heading"><text class="section-title">人才身份</text><text class="section-caption">按你的计划，了解不同路径</text></view>
    <view class="talent-grid">
      <button v-for="tool in talentTools" :key="tool.id" class="tool-card talent-card" @click="openToolIntro(tool.id)">
        <view class="card-top"><text :class="['tool-icon', tool.id]">{{ tool.symbol }}</text><text class="status-badge">筹备中</text></view>
        <text class="card-title">{{ tool.title }}</text><text class="description muted">{{ tool.brief }}</text>
        <view class="card-footer"><text>查看介绍</text><text class="arrow">↗</text></view>
      </button>
    </view>
    <view class="quiet-note"><text class="note-dot"></text><text>工具正在逐步完善，目前可浏览介绍，功能尚未开放。</text></view>
  </PageContainer>
</template>
<script setup lang="ts">
import PageContainer from '@/components/PageContainer.vue'
import { tools } from '@/features/tools/content'
import { openToolIntro } from '@/navigation'
const primary = tools[0]
const bank = tools[1]
const talentTools = tools.filter(tool => tool.id === 'talent' || tool.id === 'quality')
</script>
<style scoped src="./tools.css"></style>
