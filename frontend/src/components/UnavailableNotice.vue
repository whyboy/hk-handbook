<template>
  <view class="unavailable-notice">
    <text class="notice-title">功能建设中</text>
    <text class="notice-description">{{ description }}</text>
  </view>
</template>
<script setup lang="ts">
defineProps<{ description: string }>()
</script>
<style scoped>
.unavailable-notice { margin: 16px 0; padding: 16px; border-radius: 12px; background: #eaf2ee; }
.notice-title { display: block; font-size: 15px; font-weight: 600; }
.notice-description { display: block; margin-top: 6px; font-size: 14px; line-height: 1.7; }
</style>
