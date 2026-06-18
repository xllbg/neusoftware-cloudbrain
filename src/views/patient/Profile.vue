<template>
  <div class="profile-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>个人中心</span>
          <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </template>
      <div v-if="userStore.userInfo" class="profile-info">
        <div class="avatar-section">
          <el-avatar :size="80" :icon="UserFilled" />
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">{{ userStore.userInfo.username }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ userStore.userInfo.name }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ userStore.userInfo.gender }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ userStore.userInfo.age }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ userStore.userInfo.phone }}</el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag type="success">患者</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { UserFilled } from "@element-plus/icons-vue"
import { useUserStore } from "@/stores/user"
import { useRouter } from "vue-router"

const userStore = useUserStore()
const router = useRouter()

function handleLogout() {
  userStore.logout()
  router.push("/patient/login")
}
</script>

<style scoped>
.profile-page { max-width: 700px; margin: 40px auto; padding: 0 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.avatar-section { text-align: center; margin-bottom: 24px; }
</style>
