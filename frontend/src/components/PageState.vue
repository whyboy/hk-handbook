<template>
  <ContentCard>
    <view :class="['page-state', `state-${kind}`]" role="status">
      <text class="state-title">{{ title }}</text>
      <text class="state-description">{{ description }}</text>
      <slot />
    </view>
  </ContentCard>
</template>
<script setup lang="ts">
import ContentCard from './ContentCard.vue'
defineProps<{ kind: 'empty' | 'loading' | 'error' | 'unavailable'; title: string; description: string }>()
</script>
<style scoped>
.state-title { display: block; font-size: 18px; font-weight: 600; }
.state-description { display: block; margin-top: 10px; font-size: 14px; line-height: 1.8; }
</style>
