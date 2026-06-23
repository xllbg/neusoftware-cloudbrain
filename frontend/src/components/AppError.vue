<template>
  <div class="app-error">
    <el-result
      icon="error"
      :title="title"
      :sub-title="message"
    >
      <template #extra>
        <el-button type="primary" @click="$emit('retry')" v-if="showRetry">
          重新加载
        </el-button>
        <el-button @click="$emit('back')" v-if="showBack">
          返回上页
        </el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
defineProps({
  title: { type: String, default: "出错了" },
  message: { type: String, default: "系统繁忙，请稍后重试" },
  showRetry: { type: Boolean, default: true },
  showBack: { type: Boolean, default: true },
})

defineEmits(["retry", "back"])
</script>

<style scoped>
.app-error {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}
</style>
