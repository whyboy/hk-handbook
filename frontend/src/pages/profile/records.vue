<template>
  <PageContainer>
    <text class="eyebrow">香港百事通 · 我的记录</text>
    <text class="title">{{ section?.title ?? '记录入口不可用' }}</text>
    <PageState :kind="section ? 'empty' : 'unavailable'" :title="section?.empty ?? '未找到对应记录类型'" :description="section?.description ?? '请返回我的页面重新选择。'" />
    <button v-if="section?.id === 'checklists'" class="nav-button" @click="openChecklistPreview">查看清单布局示例</button>
    <button class="nav-button" @click="returnProfile">返回我的</button>
    <button class="nav-button" @click="returnHome">返回首页</button>
  </PageContainer>
</template>
<script setup lang="ts">
import PageState from '@/components/PageState.vue'
import PageContainer from '@/components/PageContainer.vue'
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { findRecordSection } from '@/features/profile/content'
import { openChecklistPreview, returnProfile, returnHome } from '@/navigation'
const section = ref<ReturnType<typeof findRecordSection>>()
onLoad((query) => { section.value = findRecordSection(query?.type) })
</script>
<style scoped src="./profile.css"></style>
