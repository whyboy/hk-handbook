<template>
  <PageContainer>
    <text class="eyebrow">港易通 · 清单详情</text>
    <text class="title">{{ preview ? '清单布局示例' : '清单暂不可用' }}</text>
    <template v-if="preview">
      <text class="notice">框架示例 · 非个人清单。功能建设中，未生成实际步骤，不支持勾选或保存。</text>
      <ContentCard v-for="stage in checklistStages" :key="stage.title">
        <text class="section-title">{{ stage.title }}</text>
        <view class="step"><checkbox disabled :checked="false" /><text class="description">{{ stage.step }}</text></view>
      </ContentCard>
      <button class="nav-button" disabled>功能建设中</button>
    </template>
    <text v-else class="notice">没有可显示的清单。请返回清单列表查看布局示例。</text>
    <button class="nav-button" @click="openGuides({ category: 'life' })">查看生活落地攻略</button>
    <button class="nav-button" @click="returnChecklists">返回清单列表</button>
    <button class="nav-button" @click="returnProfile">返回我的</button>
    <button class="nav-button" @click="returnHome">返回首页</button>
  </PageContainer>
</template>
<script setup lang="ts">
import ContentCard from '@/components/ContentCard.vue'
import PageContainer from '@/components/PageContainer.vue'
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { checklistStages } from '@/features/profile/content'
import { openGuides, returnChecklists, returnProfile, returnHome } from '@/navigation'
const preview = ref(false)
onLoad((query) => { preview.value = query?.demo === 'layout' && !query?.id })
</script>
<style scoped src="../profile/profile.css"></style>
