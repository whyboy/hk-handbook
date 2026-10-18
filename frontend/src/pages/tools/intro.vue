<template>
  <view class="intro">
    <text class="eyebrow">港易通 · 办事工具</text>
    <text class="title">{{ action ? action.title : '未找到对应工具' }}</text>
    <view class="notice">
      <text class="notice-title">{{ action ? '功能建设中' : '入口暂不可用' }}</text>
      <text class="description">{{ action ? action.description : '请返回首页，重新选择你想了解的办事工具。' }}</text>
      <text v-if="action" class="availability">工具尚未开放，目前仅提供用途说明。</text>
    </view>
    <button class="home-button" hover-class="pressed" @click="returnHome">返回首页</button>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { findHomeAction } from '@/features/home/content'
import { returnHome } from '@/navigation'

const action = ref<ReturnType<typeof findHomeAction>>()

onLoad((query) => {
  action.value = findHomeAction(query?.scene)
})
</script>

<style scoped>
.intro { max-width: 600px; margin: 0 auto; padding: 36px 20px; padding-bottom: calc(36px + env(safe-area-inset-bottom)); color: #203b38; }
.eyebrow { display: block; font-size: 12px; color: #627570; }
.title { display: block; margin-top: 12px; font-size: 26px; font-weight: 700; }
.notice { margin-top: 24px; padding: 24px; border-radius: 16px; background: #eaf2ee; }
.notice-title { display: block; font-size: 18px; font-weight: 600; }
.description { display: block; margin-top: 12px; font-size: 15px; line-height: 1.8; }
.availability { display: block; margin-top: 20px; font-size: 13px; line-height: 1.6; color: #627570; }
.home-button { margin-top: 24px; border-radius: 12px; font-size: 15px; line-height: 48px; color: #fff; background: #286456; }
.home-button::after { border: 0; }
.home-button:focus-visible { outline: 2px solid #26766a; outline-offset: 3px; }
.pressed { opacity: .7; }
</style>
