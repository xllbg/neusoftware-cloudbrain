<template>
  <div class="home-page">
    <div class="home-container">
      <div class="home-header">
        <div class="home-logo">
          <el-icon :size="36"><FirstAidKit /></el-icon>
        </div>
        <h1>智慧云脑诊疗平台</h1>
        <p class="home-subtitle">智能诊疗 · AI 辅助 · 全流程协同</p>
      </div>
      <div class="role-cards">
        <div class="role-card patient-role" @click="$router.push('/patient/login')">
          <div class="role-icon">
            <el-icon :size="48"><User /></el-icon>
          </div>
          <h2>患者入口</h2>
          <p>自助注册 · 在线挂号 · AI分诊 · 查看病历</p>
          <el-button type="primary" round size="large" class="enter-btn">进入患者端</el-button>
        </div>
        <div class="role-card doctor-role" @click="$router.push('/doctor/login')">
          <div class="role-icon">
            <el-icon :size="48"><FirstAidKit /></el-icon>
          </div>
          <h2>医生入口</h2>
          <p>患者管理 · 病历录入 · 开具处方 · 历史查询</p>
          <el-button round size="large" class="enter-btn doctor-enter-btn">进入医生端</el-button>
        </div>
      </div>
    </div>

    <!-- 右下角后台入口 -->
    <div class="admin-entry" @click="$router.push('/admin/pending-doctors')">
      <el-icon :size="18"><Setting /></el-icon>
      <span>后台管理</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { User, FirstAidKit, Setting } from "@element-plus/icons-vue"
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f0fe 0%, #f0f4f8 100%);
  padding: 20px;
  position: relative;
}
.home-container {
  max-width: 800px;
  width: 100%;
  text-align: center;
}
.home-header {
  margin-bottom: 48px;
}
.home-logo {
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #1a73e8, #0d47a1);
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  color: #fff;
  box-shadow: 0 6px 20px rgba(26, 115, 232, 0.3);
}
.home-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #202124;
  margin-bottom: 8px;
}
.home-subtitle {
  font-size: 15px;
  color: #5f6368;
}
.role-cards {
  display: flex;
  gap: 24px;
  justify-content: center;
}
.role-card {
  flex: 1;
  max-width: 340px;
  background: #fff;
  border-radius: 16px;
  padding: 40px 28px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  border: 2px solid transparent;
}
.role-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0,0,0,0.1);
}
.patient-role:hover { border-color: #1a73e8; }
.doctor-role:hover { border-color: #2c3e50; }
.role-icon {
  width: 80px; height: 80px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 20px;
}
.patient-role .role-icon { background: #e8f0fe; color: #1a73e8; }
.doctor-role .role-icon { background: #eef1f5; color: #2c3e50; }
.role-card h2 {
  font-size: 20px; font-weight: 600; color: #202124; margin-bottom: 8px;
}
.role-card p {
  font-size: 13px; color: #5f6368; margin-bottom: 24px; line-height: 1.6;
}
.enter-btn { width: 180px; font-size: 15px; }
.patient-role .enter-btn { background: #1a73e8; border-color: #1a73e8; }
.patient-role .enter-btn:hover { background: #1557b0; border-color: #1557b0; }
.doctor-enter-btn { background: #2c3e50; border-color: #2c3e50; color: #fff; }
.doctor-enter-btn:hover { background: #1a252f; border-color: #1a252f; color: #fff; }

/* 右下角后台入口 */
.admin-entry {
  position: fixed;
  bottom: 24px;
  right: 24px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: rgba(0,0,0,0.06);
  border-radius: 20px;
  font-size: 13px;
  color: #9aa0a6;
  cursor: pointer;
  transition: all 0.2s;
  z-index: 99;
}
.admin-entry:hover {
  background: rgba(0,0,0,0.1);
  color: #5f6368;
}
</style>
