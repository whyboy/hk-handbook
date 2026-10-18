<template>
  <view class="profile-page">
    <text class="eyebrow">港易通 · 我的记录</text>
    <text class="title">{{ section?.title ?? '记录入口不可用' }}</text>
    <view class="panel">
      <text class="section-title">{{ section?.empty ?? '未找到对应记录类型' }}</text>
      <text class="description">{{ section?.description ?? '请返回我的页面重新选择。' }}</text>
    </view>
    <button v-if="section?.id === 'checklists'" class="nav-button" @click="openChecklistPreview">查看清单布局示例</button>
    <button class="nav-button" @click="returnProfile">返回我的</button>
    <button class="nav-button" @click="returnHome">返回首页</button>
  </view>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { findRecordSection } from '@/features/profile/content'
import { openChecklistPreview, returnProfile, returnHome } from '@/navigation'
const section = ref<ReturnType<typeof findRecordSection>>()
onLoad((query) => { section.value = findRecordSection(query?.type) })
</script>
<style scoped src="./profile.css"></style>
