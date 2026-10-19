<template><view :class="['page-container', { wide }]"><slot /></view></template>
<script setup lang="ts">
defineProps<{ wide?: boolean }>()
</script>
<style scoped>
.page-container { box-sizing: border-box; width: 100%; max-width: 760px; margin: 0 auto; padding: 28px 20px; padding-bottom: calc(32px + env(safe-area-inset-bottom)); color: #203b38; overflow-wrap: break-word; }
.wide { max-width: 880px; }
@media (max-width: 350px) { .page-container { padding-left: 14px; padding-right: 14px; } }
</style>
