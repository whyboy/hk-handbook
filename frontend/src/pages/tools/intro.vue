<template>
  <PageContainer>
    <text class="eyebrow">港易通 · 工具说明</text>
    <text class="title">{{ tool?.title ?? (identity ? '申请身份' : '未找到对应工具') }}</text>
    <template v-if="tool">
      <ContentCard>
        <text class="section-title">用途</text><text class="description">{{ tool.purpose }}</text>
        <text class="section-title spaced">未来需要的信息</text><text class="description">{{ tool.input }}</text>
        <text class="section-title spaced">未来可以获得什么</text><text class="description">{{ tool.output }}</text>
      </ContentCard>
      <button class="nav-button" disabled>功能建设中</button>
      <text class="availability">目前仅提供用途说明，不收集答案、不执行规则。</text>
      <button class="nav-button secondary" @click="openToolResult(tool.id)">查看结果页面说明</button>
    </template>
    <template v-else-if="identity">
      <text class="description">高才与优才分别提供初筛说明，请选择想了解的路径。功能建设中。</text>
      <button v-for="item in talentTools" :key="item.id" class="tool-card" @click="openToolIntro(item.id)">
        <text class="section-title">{{ item.title }}</text><text class="description">{{ item.purpose }}</text>
      </button>
    </template>
    <ContentCard v-else><text class="description">入口暂不可用，请返回工具页重新选择。</text></ContentCard>
    <button class="nav-button secondary" @click="returnTools">返回工具页</button>
    <button class="nav-button secondary" @click="returnHome">返回首页</button>
  </PageContainer>
</template>
<script setup lang="ts">
import ContentCard from '@/components/ContentCard.vue'
import PageContainer from '@/components/PageContainer.vue'
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { findTool, tools } from '@/features/tools/content'
import { openToolIntro, openToolResult, returnTools, returnHome } from '@/navigation'
const tool = ref<ReturnType<typeof findTool>>()
const identity = ref(false)
const talentTools = tools.filter((item) => item.id === 'talent' || item.id === 'quality')
onLoad((query) => { tool.value = findTool(query?.scene); identity.value = query?.scene === 'identity' })
</script>
<style scoped src="./tools.css"></style>
