<template>
  <view class="profile-page">
    <text class="eyebrow">港易通 · 反馈</text>
    <text class="title">告诉我们你的建议</text>
    <text class="notice">功能建设中，表单仅展示布局，暂不能输入或提交。请勿填写证件、电话等个人信息。</text>
    <view class="panel">
      <text class="field-label">反馈类型</text>
      <input disabled placeholder="产品反馈（暂未开放）" aria-label="反馈类型" />
      <text class="field-label">问题说明（未来必填）</text>
      <textarea disabled placeholder="未来可在这里说明问题或建议" aria-label="问题说明" />
      <text class="field-label">参考来源（未来选填）</text>
      <input disabled placeholder="未来可填写相关来源链接" aria-label="参考来源" />
    </view>
    <button class="nav-button" disabled>功能建设中</button>
    <button class="nav-button" @click="returnProfile">返回我的</button>
    <button class="nav-button" @click="returnHome">返回首页</button>
  </view>
</template>
<script setup lang="ts">
import { returnProfile, returnHome } from '@/navigation'
</script>
<style scoped src="./profile.css"></style>
