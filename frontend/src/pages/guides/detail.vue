<template>
  <PageContainer>
    <text class="eyebrow">港易通 · 攻略详情</text>
    <PageState v-if="state === 'loading'" kind="loading" title="正在加载攻略" description="请稍候…" />
    <PageState v-else-if="state === 'error'" kind="error" title="攻略加载失败" :description="errorMessage">
      <button class="nav-button" @click="load">重试</button>
    </PageState>
    <PageState v-else-if="state === 'withdrawn'" kind="unavailable" title="这篇攻略已下架" description="请返回攻略列表查看其他已发布内容。" />
    <PageState v-else-if="!guide" kind="unavailable" title="攻略不存在或暂不可用" description="内容可能正在复核，请返回攻略列表。" />
    <template v-else>
      <text class="title">{{ guide.title }}</text>
      <text class="eyebrow">{{ categoryLabel(guide.category) }} · {{ guide.audiences.map(audienceLabel).join(' / ') }}</text>
      <ContentCard>
        <text class="description">{{ guide.summary }}</text>
        <text class="meta">事实核验：{{ guide.verifiedOn }} · 发布：{{ guide.publishedOn }}</text>
        <text class="meta">编辑：{{ guide.editedOn }} · 复核截止：{{ guide.reviewDueOn }}（不含当天）</text>
        <text class="meta">{{ periodLabel(guide.effectivePeriod) }}</text>
      </ContentCard>
      <ContentCard v-for="section in sections" :key="section.id">
        <text class="section-title">{{ section.label }}</text>
        <text v-if="guide.sections[section.id].state === 'not_applicable'" class="description">不适用：{{ guide.sections[section.id].reason }}</text>
        <view v-for="(block, index) in guide.sections[section.id].blocks" :key="index" class="content-block">
          <text class="description" selectable>{{ section.id === 'steps' ? `${index + 1}. ` : '' }}{{ block.text }}</text>
          <text class="meta">{{ blockLabels[block.kind] }}{{ block.factIds.length ? ` · 事实依据：${block.factIds.join('、').toUpperCase()}` : '' }}</text>
          <text v-if="block.experienceContext" class="meta">{{ block.experienceContext }}</text>
        </view>
      </ContentCard>
      <ContentCard>
        <text class="section-title">事实依据与适用条件</text>
        <view v-for="fact in guide.facts" :key="fact.id" class="content-block">
          <text class="description">{{ fact.id.toUpperCase() }} · {{ fact.claim }}</text>
          <text class="meta">适用条件：{{ fact.conditions }}</text>
          <text class="meta">来源：{{ fact.sourceIds.join('、').toUpperCase() }}</text>
        </view>
      </ContentCard>
      <ContentCard>
        <text class="section-title">官方来源</text>
        <view v-for="source in guide.sources" :key="source.id" class="content-block">
          <text class="description">{{ source.id.toUpperCase() }} · {{ source.organization }} · {{ source.title }}</text>
          <text class="meta">查阅：{{ source.consultedOn }} · {{ periodLabel(source.effectivePeriod) }}</text>
          <text class="source-url" selectable>{{ source.url }}</text>
          <!-- #ifdef H5 -->
          <button class="nav-button" @click="openSource(source.url)">打开官方来源</button>
          <!-- #endif -->
          <!-- #ifndef H5 -->
          <button class="nav-button" @click="openSource(source.url)">复制来源链接</button>
          <!-- #endif -->
        </view>
      </ContentCard>
      <text class="notice">出发或办事前，请再次核对官方最新说明及适用条件。</text>
    </template>
    <button class="nav-button" @click="returnGuides">返回攻略列表</button>
    <button class="nav-button" @click="returnHome">返回首页</button>
  </PageContainer>
</template>
<script setup lang="ts">
import PageState from '@/components/PageState.vue'
import ContentCard from '@/components/ContentCard.vue'
import PageContainer from '@/components/PageContainer.vue'
import { ref } from 'vue'
import { onLoad, onShow, onHide, onUnload } from '@dcloudio/uni-app'
import { sections, categoryLabel, audienceLabel } from '@/features/guides/content'
import { getGuide, GuideError, openSource, periodLabel, type GuideDetail } from '@/features/guides/api'
import { returnGuides, returnHome } from '@/navigation'
const guide = ref<GuideDetail>(), state = ref<'loading' | 'ready' | 'error' | 'withdrawn' | 'unavailable'>('loading')
const errorMessage = ref('')
const blockLabels = { official_fact: '官方说明', editorial_advice: '编辑建议', personal_experience: '个人经验' }
let id = '', generation = 0
async function load() {
  const requestId = ++generation; guide.value = undefined; state.value = 'loading'
  try {
    const result = await getGuide(id)
    if (requestId !== generation) return
    guide.value = result; state.value = 'ready'
  } catch (error) {
    if (requestId !== generation) return
    state.value = error instanceof GuideError && error.status === 410 ? 'withdrawn' : error instanceof GuideError && error.status === 404 ? 'unavailable' : 'error'
    errorMessage.value = error instanceof GuideError ? error.message : '暂时无法加载，请重试。'
  }
}
onLoad(query => { id = typeof query?.id === 'string' ? query.id : '' })
onShow(load)
function invalidate() { generation++; guide.value = undefined; state.value = 'loading' }
onHide(invalidate); onUnload(invalidate)
</script>
<style scoped src="./guides.css"></style>
