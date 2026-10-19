<template>
  <PageContainer>
    <text class="eyebrow">港易通 · 攻略详情</text>
    <text class="notice">框架示例 · 尚未核验，不作为办事依据。</text>
    <PageState v-if="state === 'loading' || state === 'error'" :kind="state" :title="state === 'loading' ? '加载中（示例）' : '加载失败（示例）'" description="当前为展示状态示例，未连接内容服务。">
      <button class="nav-button" @click="state = 'ready'">返回示例详情</button>
    </PageState>
    <template v-else-if="guide && state !== 'empty'">
      <text class="title">{{ guide.title }}</text>
      <text class="eyebrow">{{ guide.category }} · 示例 · 尚未核验</text>
      <ContentCard>
        <text class="section-title">摘要</text><text class="description">{{ guide.summary }}</text>
      </ContentCard>
      <ContentCard>
        <text class="section-title">正文示例</text>
        <text class="description">此处将说明适用情境、准备事项、办理步骤和注意事项。当前没有真实办理建议、费用、材料要求或资格结论。</text>
        <text class="description">正式内容需补充依据并完成核验后发布。</text>
      </ContentCard>
      <ContentCard>
        <text class="section-title">来源与核验</text>
        <text class="description">尚未提供来源，尚未核验。正式攻略将在此展示来源链接与实际核验日期。</text>
      </ContentCard>
    </template>
    <PageState v-else kind="unavailable" title="攻略不存在或暂不可用" description="请返回攻略列表选择示例。此处不会展示旧正文。" />
    <button class="nav-button" @click="returnGuides">返回攻略列表</button>
    <button class="nav-button" @click="returnHome">返回首页</button>
  </PageContainer>
</template>
<script setup lang="ts">
import PageState from '@/components/PageState.vue'
import ContentCard from '@/components/ContentCard.vue'
import PageContainer from '@/components/PageContainer.vue'
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { findGuide, parseDisplayState, type DisplayState } from '@/features/guides/content'
import { returnGuides, returnHome } from '@/navigation'
const guide = ref<ReturnType<typeof findGuide>>()
const state = ref<DisplayState>('ready')
onLoad((query) => { guide.value = findGuide(query?.id); state.value = parseDisplayState(query?.demo) })
</script>
<style scoped src="./guides.css"></style>
