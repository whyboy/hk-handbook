<template>
  <view class="tool-page">
    <text class="eyebrow">港易通 · 工具结果</text>
    <text class="title">{{ tool ? `${tool.title} · 结果说明` : '未找到对应工具' }}</text>
    <view class="panel">
      <text class="section-title">{{ tool ? '暂无结果 · 功能建设中' : '入口暂不可用' }}</text>
      <text class="description">{{ tool ? '工具尚未开放，未执行匹配、初筛或清单生成。这不是一次测评的结果。' : '请返回工具页重新选择。' }}</text>
      <text v-if="tool" class="description">{{ tool.output }}</text>
    </view>
    <button class="nav-button secondary" @click="returnTools">返回工具页</button>
    <button class="nav-button secondary" @click="returnHome">返回首页</button>
  </view>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { findTool } from '@/features/tools/content'
import { returnTools, returnHome } from '@/navigation'
const tool = ref<ReturnType<typeof findTool>>()
onLoad((query) => { tool.value = findTool(query?.scene) })
</script>
<style scoped src="./tools.css"></style>
