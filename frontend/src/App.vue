<script setup lang="ts">
import { onLaunch, onShow, onHide } from "@dcloudio/uni-app";
onLaunch(() => {
  console.log("App Launch");
});
onShow(() => {
  console.log("App Show");
});
onHide(() => {
  console.log("App Hide");
});
</script>
<style>
page {
  background: #f7f9f6;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif;
  line-height: 1.5;
}
/* #ifdef H5 */
/* Keep desktop navigation within the same reading area as the pages. */
@media (min-width: 760px) {
  .uni-tabbar-bottom .uni-tabbar {
    padding-left: calc((100% - 760px) / 2);
    padding-right: calc((100% - 760px) / 2);
  }
}
/* #endif */
</style>
