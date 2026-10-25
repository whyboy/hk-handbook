<template>
  <PageContainer>
    <text class="eyebrow">香港百事通 · 我的</text>
    <text class="title">你的赴港准备</text>
    <UnavailableNotice description="功能建设中，当前仅展示页面框架，不读取或保存个人记录。" />
    <button v-for="section in recordSections" :key="section.id" class="entry" @click="openRecords(section.id)">
      <text class="section-title">{{ section.title }}</text><text class="description">查看页面说明 →</text>
    </button>
    <button class="entry" @click="openFeedback"><text class="section-title">反馈</text><text class="description">查看反馈表单说明 →</text></button>
    <text class="description">未来收藏、清单进度与测评记录计划保存在本设备，不跨设备同步；清理数据可能导致记录丢失。目前尚未开放保存功能。</text>
    <button class="nav-button" @click="returnHome">返回首页</button>
  </PageContainer>
</template>
<script setup lang="ts">
import PageContainer from '@/components/PageContainer.vue'
import UnavailableNotice from '@/components/UnavailableNotice.vue'
import { recordSections } from '@/features/profile/content'
import { openRecords, openFeedback, returnHome } from '@/navigation'
</script>
<style scoped src="./profile.css"></style>
