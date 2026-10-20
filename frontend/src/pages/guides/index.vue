<template>
  <PageContainer>
    <text class="eyebrow">港易通 · 攻略</text>
    <text class="title">赴港之前，先了解</text>
    <form @submit="submitSearch">
    <view class="search-row">
      <input v-model="keyword" name="keyword" class="search-input" placeholder="搜索银行卡、手机卡、人才或生活攻略" aria-label="搜索攻略" :maxlength="100" confirm-type="search" @confirm="confirmSearch" />
      <button class="search-button" form-type="submit">搜索</button>
    </view>
    </form>
    <view class="categories" aria-label="攻略分类">
      <button v-for="item in categories" :key="item.id" :class="['category', { selected: filters.category === item.id }]" @click="chooseCategory(item.id)">{{ item.label }}</button>
    </view>
    <button v-if="filtered" class="nav-button" @click="clearFilters">清除筛选</button>
    <PageState v-if="state === 'loading'" kind="loading" title="正在查找攻略" description="请稍候…" />
    <PageState v-else-if="state === 'error'" kind="error" title="攻略加载失败" :description="errorMessage">
      <button class="nav-button" @click="load()">重试</button>
    </PageState>
    <PageState v-else-if="!items.length" kind="empty" :title="filtered ? '没有找到相关攻略' : '暂无已发布攻略'" :description="filtered ? '试试其他关键词，或清除筛选查看全部攻略。' : '攻略正在整理与核验中，发布后会在这里显示。'" />
    <template v-else>
      <text class="eyebrow result-count">{{ filters.q ? '搜索结果' : '推荐攻略' }} · 共 {{ total }} 篇攻略</text>
      <button v-for="guide in items" :key="guide.id" class="guide-card" @click="openGuide(guide.id)">
        <text class="eyebrow">{{ categoryLabel(guide.category) }}</text>
        <text class="section-title">{{ guide.title }}</text>
        <text class="description">{{ guide.matchExcerpt || guide.summary }}</text>
        <text class="sample-label">事实核验：{{ guide.verifiedOn }}</text>
      </button>
      <text v-if="moreError" class="notice">加载更多失败，已有结果保留，请重试。</text>
      <button v-if="items.length < total" class="nav-button" :disabled="loadingMore" @click="load(true)">{{ loadingMore ? '正在加载…' : moreError ? '重试加载更多' : '加载更多' }}</button>
    </template>
    <button class="nav-button" @click="returnHome">返回首页</button>
  </PageContainer>
</template>
<script setup lang="ts">
import PageState from '@/components/PageState.vue'
import PageContainer from '@/components/PageContainer.vue'
import { computed, reactive, ref } from 'vue'
import { onLoad, onShow, onHide, onUnload } from '@dcloudio/uni-app'
import { categories, categoryLabel, takeGuideFilters, rememberGuideFilters, rememberedGuideFilters, type Category, type GuideFilters } from '@/features/guides/content'
import { getGuides, GuideError, type GuideSummary } from '@/features/guides/api'
import { openGuide, returnHome } from '@/navigation'
const filters = reactive<GuideFilters>(rememberedGuideFilters())
const keyword = ref(''), items = ref<GuideSummary[]>([]), total = ref(0), page = ref(0)
const state = ref<'loading' | 'ready' | 'error'>('loading')
const errorMessage = ref(''), loadingMore = ref(false), moreError = ref(false)
const filtered = computed(() => Boolean(filters.category || filters.q))
let generation = 0
async function load(more = false) {
  if (more && (loadingMore.value || state.value !== 'ready')) return
  rememberGuideFilters(filters)
  const requestId = ++generation, targetPage = more ? page.value + 1 : 1
  moreError.value = false; loadingMore.value = more
  if (!more) { state.value = 'loading'; items.value = []; total.value = 0; page.value = 0 }
  try {
    const result = await getGuides({ ...filters }, targetPage)
    if (requestId !== generation) return
    items.value = more ? [...items.value, ...result.items.filter(item => !items.value.some(old => old.id === item.id))] : result.items
    total.value = result.total; page.value = targetPage; state.value = 'ready'
  } catch (error) {
    if (requestId !== generation) return
    if (more) moreError.value = true
    else { state.value = 'error'; errorMessage.value = error instanceof GuideError ? error.message : '暂时无法加载，请重试。' }
  } finally { if (requestId === generation) loadingMore.value = false }
}
function search(value: string) { keyword.value = value; filters.q = value.trim(); load() }
function confirmSearch(event: { detail: { value: string } }) { search(event.detail.value) }
function submitSearch(event: unknown) {
  const value = (event as { detail?: { value?: { keyword?: unknown } } }).detail?.value?.keyword
  if (typeof value === 'string') search(value)
}
function chooseCategory(value: Category) { filters.category = value; load() }
function clearFilters() { Object.assign(filters, { category: '', q: '' }); keyword.value = ''; load() }
onLoad(query => {
  if (query?.category !== undefined) filters.category = categories.find(c => c.id === query.category)?.id ?? ''
  keyword.value = typeof query?.q === 'string' ? [...query.q].slice(0, 100).join('') : filters.q
  filters.q = keyword.value.trim()
  rememberGuideFilters(filters)
})
onShow(() => {
  Object.assign(filters, rememberedGuideFilters()); keyword.value = filters.q
  const next = takeGuideFilters()
  if (next) { Object.assign(filters, { category: '', q: '' }, next); keyword.value = filters.q }
  load()
})
function invalidate() { generation++; items.value = []; total.value = 0; loadingMore.value = false; state.value = 'loading' }
onHide(invalidate); onUnload(invalidate)
</script>
<style scoped src="./guides.css"></style>
