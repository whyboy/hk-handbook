<template>
  <PageContainer>
    <view class="page-heading"><text class="eyebrow">港易通 / 工具预览</text><text class="edition">筹备中</text></view>
    <view class="intro-hero">
      <text :class="['tool-icon', tool?.id]">{{ tool?.symbol ?? '…' }}</text>
      <text class="title">{{ tool ? '准备好后，在这里相见' : '未找到对应工具' }}</text>
      <text class="description muted">{{ tool ? tool.title : '请返回工具页重新选择' }}</text>
    </view>
    <view class="preview-panel">
      <text class="section-title">{{ tool ? '暂无结果 · 功能筹备中' : '入口暂不可用' }}</text>
      <text class="description muted">{{ tool ? '工具尚未开放，当前没有生成任何匹配、初筛或清单结果。' : '这个链接可能有误，你可以返回工具页浏览现有工具。' }}</text>
    </view>
    <view v-if="tool" class="steps-panel result-preview">
      <text class="eyebrow">未来的结果内容</text>
      <text class="description muted">{{ tool.output }}</text>
    </view>
    <button class="nav-button secondary" @click="returnTools">返回工具页</button>
    <button class="nav-button secondary" @click="returnHome">返回首页</button>
  </PageContainer>
</template>
<script setup lang="ts">
import PageContainer from '@/components/PageContainer.vue'
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { findTool } from '@/features/tools/content'
import { returnTools, returnHome } from '@/navigation'
const tool = ref<ReturnType<typeof findTool>>()
onLoad((query) => { tool.value = findTool(query?.scene) })
</script>
<style scoped src="./tools.css"></style>
