<template>
  <div class="app-loading" v-if="visible">
    <el-icon class="loading-icon" :size="32" :style="{ color: color }">
      <Loading />
    </el-icon>
    <p class="loading-text">{{ text }}</p>
  </div>
</template>

<script setup lang="ts">
import { Loading } from "@element-plus/icons-vue"

defineProps({
  visible: { type: Boolean, default: false },
  text: { type: String, default: "加载中..." },
  color: { type: String, default: "#409eff" },
})
</script>

<style scoped>
.app-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}
.loading-icon {
  animation: rotating 1.5s linear infinite;
}
.loading-text {
  margin-top: 12px;
  color: #909399;
  font-size: 14px;
}
@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
