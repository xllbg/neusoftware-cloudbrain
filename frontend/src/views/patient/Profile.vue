<template>
  <div class="profile-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>个人中心</span>
          <el-button type="primary" size="small" @click="toggleEdit">{{ isEditing ? '取消编辑' : '编辑资料' }}</el-button>
        </div>
      </template>
      <div v-if="userStore.userInfo" class="profile-info">
        <div class="avatar-section">
          <el-avatar :size="80" :icon="UserFilled" />
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">{{ userStore.userInfo.username }}</el-descriptions-item>
          <el-descriptions-item label="姓名">
            <template v-if="isEditing">
              <el-input v-model="editForm.name" size="small" />
            </template>
            <template v-else>{{ userStore.userInfo.name }}</template>
          </el-descriptions-item>
          <el-descriptions-item label="性别">
            <template v-if="isEditing">
              <el-select v-model="editForm.gender" size="small" style="width: 100%;">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </template>
            <template v-else>{{ userStore.userInfo.gender }}</template>
          </el-descriptions-item>
          <el-descriptions-item label="年龄">
            <template v-if="isEditing">
              <el-input-number v-model="editForm.age" :min="1" :max="150" size="small" style="width: 100%;" />
            </template>
            <template v-else>{{ userStore.userInfo.age }}</template>
          </el-descriptions-item>
          <el-descriptions-item label="手机号">
            <template v-if="isEditing">
              <el-input v-model="editForm.phone" size="small" />
            </template>
            <template v-else>{{ userStore.userInfo.phone }}</template>
          </el-descriptions-item>
          <el-descriptions-item label="身份证号">
            <template v-if="isEditing">
              <el-input v-model="editForm.idCard" size="small" placeholder="选填" />
            </template>
            <template v-else>{{ userStore.userInfo.idCard || '未填写' }}</template>
          </el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">
            <template v-if="isEditing">
              <el-input v-model="editForm.address" size="small" placeholder="选填" />
            </template>
            <template v-else>{{ userStore.userInfo.address || '未填写' }}</template>
          </el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag type="success">患者</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <div class="action-bar" v-if="isEditing">
          <el-button type="primary" size="large" :loading="saving" @click="handleSave">保存修改</el-button>
        </div>
        <div class="logout-section">
          <el-button type="danger" size="large" @click="handleLogout">退出登录</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue"
import { UserFilled } from "@element-plus/icons-vue"
import { useUserStore } from "@/stores/user"
import { useRouter } from "vue-router"
import { ElMessage } from "element-plus"

const userStore = useUserStore()
const router = useRouter()

const isEditing = ref(false)
const saving = ref(false)

const editForm = reactive({
  name: "",
  gender: "",
  age: 0,
  phone: "",
  idCard: "",
  address: "",
})

function toggleEdit() {
  if (isEditing.value) {
    // 取消编辑，恢复原值
    isEditing.value = false
  } else {
    // 开始编辑，填充当前值
    const info = userStore.userInfo
    if (info) {
      editForm.name = info.name || ""
      editForm.gender = info.gender || ""
      editForm.age = info.age || 0
      editForm.phone = info.phone || ""
      editForm.idCard = info.idCard || ""
      editForm.address = info.address || ""
    }
    isEditing.value = true
  }
}

async function handleSave() {
  // 更新用户信息到 store
  if (userStore.userInfo) {
    userStore.userInfo.name = editForm.name
    userStore.userInfo.gender = editForm.gender
    userStore.userInfo.age = editForm.age
    userStore.userInfo.phone = editForm.phone
    userStore.userInfo.idCard = editForm.idCard
    userStore.userInfo.address = editForm.address
  }
  isEditing.value = false
  ElMessage.success("修改成功")
}

function handleLogout() {
  userStore.logout()
  router.push("/patient/login")
}
</script>

<style scoped>
.profile-page { max-width: 700px; margin: 40px auto; padding: 0 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.avatar-section { text-align: center; margin-bottom: 24px; }
.action-bar { margin-top: 24px; }
.logout-section { margin-top: 24px; }
.logout-section .el-button { width: 100%; }
</style>
