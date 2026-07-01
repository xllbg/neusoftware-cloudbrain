import { describe, it, expect, beforeEach } from 'vitest'
import {
  getDoctorToken, setDoctorToken, removeDoctorToken,
  getDoctorUser, setDoctorUser,
  isLoggedIn, isDoctor, isPatient,
  getToken, setToken, removeToken,
  getUser, setUser,
  getPatientToken, setPatientToken, removePatientToken,
  getPatientUser, setPatientUser,
} from '@/utils/auth'

describe('auth utils - doctor', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  describe('setDoctorToken / getDoctorToken', () => {
    it('should set and get doctor token', () => {
      setDoctorToken('doctor-token-123')
      expect(getDoctorToken()).toBe('doctor-token-123')
    })

    it('should return null when no doctor token', () => {
      expect(getDoctorToken()).toBeNull()
    })

    it('should overwrite existing token', () => {
      setDoctorToken('old-token')
      setDoctorToken('new-token')
      expect(getDoctorToken()).toBe('new-token')
    })
  })

  describe('setDoctorUser / getDoctorUser', () => {
    it('should set and get doctor user info', () => {
      const user = { userId: 1, name: '张医生', role: 'DOCTOR', department: '心内科' }
      setDoctorUser(user)
      expect(getDoctorUser()).toEqual(user)
    })

    it('should return null when no doctor user', () => {
      expect(getDoctorUser()).toBeNull()
    })

    it('should return null for invalid JSON in storage', () => {
      localStorage.setItem('cloud-brain-doctor-user', 'invalid-json')
      expect(getDoctorUser()).toBeNull()
    })
  })

  describe('removeDoctorToken', () => {
    it('should remove doctor token and user info', () => {
      setDoctorToken('token-to-remove')
      setDoctorUser({ name: 'test' })
      removeDoctorToken()
      expect(getDoctorToken()).toBeNull()
      expect(getDoctorUser()).toBeNull()
    })

    it('should not throw when nothing to remove', () => {
      expect(() => removeDoctorToken()).not.toThrow()
    })
  })

  describe('isDoctor', () => {
    it('should return true for doctor user', () => {
      setDoctorUser({ userId: 1, name: '张医生', role: 'DOCTOR' })
      expect(isDoctor()).toBe(true)
    })

    it('should return false for non-doctor user', () => {
      setDoctorUser({ userId: 2, name: '患者', role: 'PATIENT' })
      expect(isDoctor()).toBe(false)
    })

    it('should return false when no user', () => {
      expect(isDoctor()).toBe(false)
    })
  })

  describe('isPatient', () => {
    it('should return true for patient user', () => {
      setPatientUser({ userId: 1, name: '患者', role: 'PATIENT' })
      expect(isPatient()).toBe(true)
    })

    it('should return false for non-patient user', () => {
      setPatientUser({ userId: 2, name: '医生', role: 'DOCTOR' })
      expect(isPatient()).toBe(false)
    })

    it('should return false when no user', () => {
      expect(isPatient()).toBe(false)
    })
  })

  describe('isLoggedIn', () => {
    it('should return true when doctor token exists', () => {
      setDoctorToken('doctor-token')
      expect(isLoggedIn()).toBe(true)
    })

    it('should return true when patient token exists', () => {
      setPatientToken('patient-token')
      expect(isLoggedIn()).toBe(true)
    })

    it('should return false when no token', () => {
      expect(isLoggedIn()).toBe(false)
    })

    it('should return true when both tokens exist', () => {
      setDoctorToken('doctor-token')
      setPatientToken('patient-token')
      expect(isLoggedIn()).toBe(true)
    })
  })
})

describe('auth utils - general', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  describe('setToken / getToken', () => {
    it('should set and get general token', () => {
      setToken('general-token')
      expect(getToken()).toBe('general-token')
    })

    it('should return null when no token', () => {
      expect(getToken()).toBeNull()
    })
  })

  describe('setUser / getUser', () => {
    it('should set and get general user info', () => {
      const user = { id: 1, name: 'test' }
      setUser(user)
      expect(getUser()).toEqual(user)
    })

    it('should return null when no user', () => {
      expect(getUser()).toBeNull()
    })

    it('should return null for invalid JSON', () => {
      localStorage.setItem('cloud-brain-user', 'bad-json')
      expect(getUser()).toBeNull()
    })
  })

  describe('removeToken', () => {
    it('should remove general token and user', () => {
      setToken('token')
      setUser({ name: 'test' })
      removeToken()
      expect(getToken()).toBeNull()
      expect(getUser()).toBeNull()
    })
  })
})

describe('auth utils - patient', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  describe('setPatientToken / getPatientToken', () => {
    it('should set and get patient token', () => {
      setPatientToken('patient-token-123')
      expect(getPatientToken()).toBe('patient-token-123')
    })

    it('should return null when no patient token', () => {
      expect(getPatientToken()).toBeNull()
    })
  })

  describe('setPatientUser / getPatientUser', () => {
    it('should set and get patient user info', () => {
      const user = { userId: 1, name: '患者甲', role: 'PATIENT' }
      setPatientUser(user)
      expect(getPatientUser()).toEqual(user)
    })

    it('should return null when no patient user', () => {
      expect(getPatientUser()).toBeNull()
    })

    it('should return null for invalid JSON in storage', () => {
      localStorage.setItem('cloud-brain-patient-user', 'invalid')
      expect(getPatientUser()).toBeNull()
    })
  })

  describe('removePatientToken', () => {
    it('should remove patient token and user info', () => {
      setPatientToken('token-to-remove')
      setPatientUser({ name: 'test' })
      removePatientToken()
      expect(getPatientToken()).toBeNull()
      expect(getPatientUser()).toBeNull()
    })
  })
})
