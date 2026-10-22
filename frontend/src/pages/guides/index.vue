<template>
  <PageContainer>
    <text class="eyebrow">港易通 · 资讯</text>
    <text class="title">了解香港学校与政策变化</text>
    <text class="description intro">收录有明确来源和更新日期的学校与政策资讯。</text>
    <view class="categories" aria-label="资讯分类">
      <button v-for="item in newsCategories" :key="item.id" :class="['category', { selected: category === item.id }]" @click="category = item.id">{{ item.label }}</button>
    </view>
    <PageState v-if="!visibleItems.length" kind="empty" :title="category ? `暂无${categoryLabel}资讯` : '暂无已发布资讯'" description="真实来源、核验方式和更新机制确认后，内容会在这里显示。" />
    <template v-else>
      <button v-for="item in visibleItems" :key="item.id" class="news-card">
        <text class="eyebrow">{{ item.category === 'school' ? '香港学校' : '香港政策' }} · {{ item.publishedOn }}</text>
        <text class="section-title">{{ item.title }}</text><text class="description">{{ item.summary }}</text>
        <text class="meta">{{ item.publisher }} · 核验于 {{ item.verifiedOn }}</text>
      </button>
    </template>
    <text class="source-note">资讯页不使用未核验新闻，也不承诺实时更新。</text>
  </PageContainer>
</template>
<script setup lang="ts">
import PageContainer from '@/components/PageContainer.vue'
import PageState from '@/components/PageState.vue'
import { computed, ref } from 'vue'
import { newsCategories, newsItems, type NewsCategory } from '@/features/news/content'
const category = ref<NewsCategory>('')
const categoryLabel = computed(() => newsCategories.find(item => item.id === category.value)?.label ?? '')
const visibleItems = computed(() => category.value ? newsItems.filter(item => item.category === category.value) : newsItems)
</script>
<style scoped src="./guides.css"></style>
<style scoped>
.intro { color: #627570; }
.news-card { display: block; width: 100%; box-sizing: border-box; margin-top: 16px; padding: 20px; border: 1px solid #e3e9e5; border-radius: 14px; background: white; color: inherit; text-align: left; line-height: 1.7; }
.source-note { display: block; margin-top: 24px; font-size: 12px; line-height: 1.8; color: #627570; }
</style>
