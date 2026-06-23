<template>
  <div class="app-empty">
    <el-empty :description="description" :image-size="120">
      <template #extra v-if="showAction">
        <el-button type="primary" @click="$emit('action')">{{ actionText }}</el-button>
      </template>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
defineProps({
  description: { type: String, default: "暂无数据" },
  showAction: { type: Boolean, default: false },
  actionText: { type: String, default: "去操作" },
})
defineEmits(["action"])
</script>

<style scoped>
.app-empty {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}
</style>
