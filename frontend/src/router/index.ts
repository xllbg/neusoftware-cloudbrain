import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router"
import { ElMessage } from "element-plus"
import { isLoggedIn, isPatient, isDoctor } from "@/utils/auth"

// 患者端手机布局（包装所有患者页面）
const PatientLayout = () => import("@/components/PatientLayout.vue")

const routes: RouteRecordRaw[] = [
  // ===== 首页（角色选择）=====
  {
    path: "/",
    name: "Home",
    component: () => import("@/views/Home.vue"),
    meta: { title: "智慧云脑诊疗平台" },
  },
  // ===== 患者端路由（含手机布局）=====
  {
    path: "/patient",
    component: PatientLayout,
    children: [
      {
        path: "login",
        name: "PatientLogin",
        component: () => import("@/views/patient/Login.vue"),
        meta: { title: "患者登录", guest: true, hideTabbar: true },
      },
      {
        path: "register",
        name: "PatientRegister",
        component: () => import("@/views/patient/Register.vue"),
        meta: { title: "患者注册", guest: true, hideTabbar: true },
      },
      {
        path: "triage",
        name: "PatientTriage",
        component: () => import("@/views/patient/Triage.vue"),
        meta: { title: "智能分诊", requiresAuth: true, role: "PATIENT" },
      },
      {
        path: "registration",
        name: "PatientRegistration",
        component: () => import("@/views/patient/Registration.vue"),
        meta: { title: "在线挂号", requiresAuth: true, role: "PATIENT" },
      },
      {
        path: "my-registrations",
        name: "MyRegistrations",
        component: () => import("@/views/patient/MyRegistrations.vue"),
        meta: { title: "我的挂号", requiresAuth: true, role: "PATIENT" },
      },
      {
        path: "my-prescriptions",
        name: "MyPrescriptions",
        component: () => import("@/views/patient/MyPrescriptions.vue"),
        meta: { title: "我的处方", requiresAuth: true, role: "PATIENT" },
      },
      {
        path: "my-records",
        name: "MyRecords",
        component: () => import("@/views/patient/MyRecords.vue"),
        meta: { title: "电子病历", requiresAuth: true, role: "PATIENT" },
      },
      {
        path: "profile",
        name: "PatientProfile",
        component: () => import("@/views/patient/Profile.vue"),
        meta: { title: "个人中心", requiresAuth: true, role: "PATIENT" },
      },
    ],
  },
  // ===== 医生端路由（电脑端布局）=====
  {
    path: "/doctor",
    children: [
      {
        path: "login",
        name: "DoctorLogin",
        component: () => import("@/views/doctor/Login.vue"),
        meta: { title: "医生登录/注册", guest: true },
      },
      {
        path: "register",
        name: "DoctorRegister",
        component: () => import("@/views/doctor/Register.vue"),
        meta: { title: "医生注册", guest: true },
      },
      {
        path: "patients",
        name: "PatientList",
        component: () => import("@/views/doctor/PatientList.vue"),
        meta: { title: "挂号患者列表", requiresAuth: true, role: "DOCTOR" },
      },
      {
        path: "consultation/:registrationId",
        name: "Consultation",
        component: () => import("@/views/doctor/Consultation.vue"),
        meta: { title: "问诊", requiresAuth: true, role: "DOCTOR" },
      },
      {
        path: "prescription/:patientId",
        name: "DoctorPrescription",
        component: () => import("@/views/doctor/Prescription.vue"),
        meta: { title: "开具处方", requiresAuth: true, role: "DOCTOR" },
      },
      {
        path: "medical-record/:patientId",
        name: "DoctorMedicalRecord",
        component: () => import("@/views/doctor/MedicalRecord.vue"),
        meta: { title: "病历录入", requiresAuth: true, role: "DOCTOR" },
      },
      {
        path: "history",
        name: "DoctorHistory",
        component: () => import("@/views/doctor/History.vue"),
        meta: { title: "历史记录", requiresAuth: true, role: "DOCTOR" },
      },
    ],
  },
  // ===== 管理员路由 =====
  {
    path: "/admin",
    children: [
      {
        path: "pending-doctors",
        name: "PendingDoctors",
        component: () => import("@/views/admin/PendingDoctors.vue"),
        meta: { title: "待审批医生" },
      },
    ],
  },
  // ===== 错误页面 =====
  {
    path: "/:pathMatch(.*)*",
    name: "NotFound",
    component: () => import("@/views/NotFound.vue"),
    meta: { title: "404" },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  document.title = (to.meta.title as string)
    ? `${to.meta.title} - 智慧云脑诊疗平台`
    : "智慧云脑诊疗平台"

  const requiresAuth = to.meta.requiresAuth as boolean
  const role = to.meta.role as string | undefined

  if (requiresAuth) {
    if (!isLoggedIn()) {
      ElMessage.warning("请先登录")
      next("/")
      return
    }
    if (role === "PATIENT" && !isPatient()) {
      ElMessage.error("请使用患者账号登录")
      next("/")
      return
    }
    if (role === "DOCTOR" && !isDoctor()) {
      ElMessage.error("请使用医生账号登录")
      next("/")
      return
    }
  }
  next()
})

export default router
