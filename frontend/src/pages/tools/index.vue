<template>
  <view class="tool-page">
    <text class="eyebrow">港易通 · 办事工具</text>
    <text class="title">从你的办事目的开始</text>
    <text class="description">四项工具正在建设中。先了解用途与未来需要准备的信息。</text>
    <button v-for="tool in tools" :key="tool.id" class="tool-card" @click="openToolIntro(tool.id)">
      <text class="section-title">{{ tool.title }}</text>
      <text class="description">{{ tool.purpose }}</text>
      <text class="availability">功能建设中 · 查看说明 →</text>
    </button>
    <button class="nav-button" @click="returnHome">返回首页</button>
  </view>
</template>
<script setup lang="ts">
import { tools } from '@/features/tools/content'
import { openToolIntro, returnHome } from '@/navigation'
</script>
<style scoped src="./tools.css"></style>
