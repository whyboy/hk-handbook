<template>
  <PageContainer>
    <text class="eyebrow">港易通 · 主题板块</text>
    <text class="title">{{ title }}</text>
    <PageState kind="empty" title="内容整理中" description="本板块内容正在准备，敬请期待。" />
    <button class="nav-button" @click="returnHome">返回首页</button>
  </PageContainer>
</template>
<script setup lang="ts">
import PageContainer from '@/components/PageContainer.vue'
import PageState from '@/components/PageState.vue'
import { returnHome } from '@/navigation'
defineProps<{ title: string }>()
</script>
<style scoped>
.nav-button { margin-top: 24px; background: #eaf2ee; color: #286456; font-size: 14px; }
</style>
