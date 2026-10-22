<template>
  <PageContainer>
    <view class="page-heading"><text class="eyebrow">港易通 / 实用工具</text><text class="edition">筹备中</text></view>
    <text class="title">实用小工具</text>
    <text class="description muted">算清收入，安排生活。</text>
    <view class="tool-list">
      <button v-for="tool in tools" :key="tool.id" class="tool-card" @click="openToolIntro(tool.id)">
        <text :class="['tool-icon', tool.id]">{{ tool.symbol }}</text>
        <view class="tool-copy"><text class="card-title">{{ tool.title }}</text><text class="description muted">{{ tool.brief }}</text></view>
        <text class="arrow">›</text>
      </button>
    </view>
    <view class="quiet-note"><text class="note-dot"></text><text>当前先确定工具入口，具体使用方式和计算规则后续设计。</text></view>
  </PageContainer>
</template>
<script setup lang="ts">
import PageContainer from '@/components/PageContainer.vue'
import { tools } from '@/features/tools/content'
import { openToolIntro } from '@/navigation'
</script>
<style scoped src="./tools.css"></style>
