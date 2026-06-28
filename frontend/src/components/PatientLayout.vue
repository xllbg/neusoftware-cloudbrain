<template>
  <div class="mobile-layout">
    <!-- 顶部导航栏 -->
    <div class="mobile-topbar" v-if="!isGuestPage">
      <div class="topbar-left" @click="handleBack">
        <el-icon :size="20"><ArrowLeft /></el-icon>
      </div>
      <div class="topbar-title">{{ title }}</div>
      <div class="topbar-right" @click="handleLogout">
        <el-icon :size="20"><SwitchButton /></el-icon>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="mobile-main" :class="{ 'no-tabbar': hideTabbar, 'no-topbar': isGuestPage }">
      <router-view />
    </div>

    <!-- 底部导航栏 -->
    <div class="mobile-tabbar" v-if="!hideTabbar">
      <div v-for="tab in tabs" :key="tab.path" class="tab-item" :class="{ active: currentRoute === tab.path }" @click="router.push(tab.path)">
        <el-icon :size="22"><component :is="tab.icon" /></el-icon>
        <span>{{ tab.label }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue"
import { useRouter, useRoute } from "vue-router"
import { ElMessageBox, ElMessage } from "element-plus"
import { ArrowLeft, SwitchButton, Search, Document, Clock, User } from "@element-plus/icons-vue"
import { useUserStore } from "@/stores/user"

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const title = computed(() => (route.meta.title as string) || "智慧云脑")
const hideTabbar = computed(() => !!route.meta.hideTabbar)
const isGuestPage = computed(() => !!route.meta.guest)
const currentRoute = computed(() => route.path)

const tabs = [
  { path: "/patient/triage", label: "智能分诊", icon: Search },
  { path: "/patient/registration", label: "挂号", icon: Document },
  { path: "/patient/my-registrations", label: "我的", icon: Clock },
  { path: "/patient/profile", label: "个人", icon: User },
]

function handleBack() {
  if (window.history.length > 1) { router.back() }
  else { router.push("/") }
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm("确定退出登录？", "提示", { type: "warning" })
    userStore.logout()
    ElMessage.success("已退出")
    router.push("/")
  } catch { }
}
</script>

<style scoped>
.mobile-layout {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
  max-width: 480px;    /* 手机最大宽度 */
  margin: 0 auto;      /* 居中显示 */
  box-shadow: 0 0 20px rgba(0,0,0,0.05); /* 手机边框效果 */
}
.mobile-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  z-index: 100;
}
.topbar-left, .topbar-right { width: 40px; cursor: pointer; color: #5f6368; }
.topbar-title { font-size: 17px; font-weight: 600; color: #202124; }
.mobile-main {
  flex: 1;
  padding: 16px;
  padding-bottom: 80px;
}
.mobile-main.no-tabbar { padding-bottom: 16px; }
.mobile-main.no-topbar { padding: 0; }
.mobile-tabbar {
  position: fixed;
  bottom: 0; left: 50%; transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  height: 64px;
  background: #fff;
  border-top: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: space-around;
  z-index: 100;
  padding-bottom: env(safe-area-inset-bottom, 0);
}
.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  cursor: pointer;
  color: #9aa0a6;
  font-size: 11px;
  padding: 4px 12px;
  transition: color 0.2s;
}
.tab-item.active { color: #1a73e8; }
.tab-item.active .el-icon { color: #1a73e8; }
</style>

<!-- 全局手机端覆盖样式 — 作用于患者页面内部 -->
<style>
/* 只有在小屏幕或者通过 PatientLayout 展示时生效 */
.mobile-main .page-container {
  max-width: 100% !important;
  padding: 0 !important;
}
.mobile-main .page-title {
  font-size: 20px !important;
}
.mobile-main .page-desc {
  font-size: 13px !important;
  margin-bottom: 16px !important;
}
.mobile-main .page-card,
.mobile-main .el-card {
  border-radius: 10px !important;
  border: 1px solid #eee !important;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04) !important;
}
.mobile-main .page-card {
  padding: 16px !important;
}
/* 表格手机化 */
.mobile-main .el-table {
  font-size: 12px !important;
  border-radius: 8px !important;
}
.mobile-main .el-table th.el-table__cell {
  padding: 8px 6px !important;
  font-size: 12px !important;
}
.mobile-main .el-table .el-table__cell {
  padding: 8px 6px !important;
}
/* 按钮/输入框全宽 */
.mobile-main .action-bar .el-button {
  width: 100% !important;
  justify-content: center;
}
.mobile-main .el-button--large {
  height: 44px !important;
  font-size: 15px !important;
}
.mobile-main .el-form-item:last-child {
  margin-bottom: 0 !important;
}
/* 描述列表手机化 */
.mobile-main .el-descriptions__title {
  font-size: 15px !important;
}
.mobile-main .el-descriptions--border .el-descriptions__cell {
  padding: 8px 12px !important;
}
/* 步骤条 */
.mobile-main .el-step__title {
  font-size: 12px !important;
}
/* 标签 */
.mobile-main .el-tag {
  margin: 2px !important;
}
/* AI结果卡片 */
.mobile-main .ai-result {
  padding: 12px !important;
  margin: 12px 0 !important;
}
/* 让el-row在小屏幕上不溢出 */
.mobile-main .el-row {
  margin-left: 0 !important;
  margin-right: 0 !important;
}
.mobile-main .el-col {
  padding-left: 0 !important;
  padding-right: 0 !important;
}
</style>
