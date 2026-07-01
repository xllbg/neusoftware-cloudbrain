import { describe, it, expect } from 'vitest'
import { formatDate, formatDateTime, getStatusTag, getStatusLabel } from '@/utils/format'

describe('formatDate', () => {
  it('should format date string correctly', () => {
    expect(formatDate('2024-01-15T10:30:00')).toBe('2024-01-15')
  })

  it('should return empty string for empty input', () => {
    expect(formatDate('')).toBe('')
  })

  it('should pad month and day with leading zero', () => {
    expect(formatDate('2024-03-05T08:00:00')).toBe('2024-03-05')
  })

  it('should handle date string without time', () => {
    expect(formatDate('2024-12-31')).toBe('2024-12-31')
  })
})

describe('formatDateTime', () => {
  it('should format datetime string correctly', () => {
    const result = formatDateTime('2024-01-15T10:30:00')
    expect(result).toContain('2024-01-15')
    expect(result).toContain('10:30')
  })

  it('should return empty string for empty input', () => {
    expect(formatDateTime('')).toBe('')
  })

  it('should pad hours and minutes with leading zero', () => {
    const result = formatDateTime('2024-03-05T08:05:00')
    expect(result).toContain('08:05')
  })
})

describe('getStatusTag', () => {
  it('should return warning for pending', () => {
    expect(getStatusTag('pending')).toBe('warning')
  })

  it('should return primary for in_progress', () => {
    expect(getStatusTag('in_progress')).toBe('primary')
  })

  it('should return success for completed', () => {
    expect(getStatusTag('completed')).toBe('success')
  })

  it('should return info for cancelled', () => {
    expect(getStatusTag('cancelled')).toBe('info')
  })

  it('should return success for approved', () => {
    expect(getStatusTag('approved')).toBe('success')
  })

  it('should return danger for rejected', () => {
    expect(getStatusTag('rejected')).toBe('danger')
  })

  it('should handle uppercase status', () => {
    expect(getStatusTag('PENDING')).toBe('warning')
    expect(getStatusTag('COMPLETED')).toBe('success')
  })

  it('should return info for unknown status', () => {
    expect(getStatusTag('unknown_status')).toBe('info')
  })

  it('should return info for empty string', () => {
    expect(getStatusTag('')).toBe('info')
  })
})

describe('getStatusLabel', () => {
  it('should return correct Chinese label for pending', () => {
    expect(getStatusLabel('pending')).toBe('待接诊')
  })

  it('should return correct Chinese label for in_progress', () => {
    expect(getStatusLabel('in_progress')).toBe('接诊中')
  })

  it('should return correct Chinese label for completed', () => {
    expect(getStatusLabel('completed')).toBe('已完成')
  })

  it('should return correct Chinese label for cancelled', () => {
    expect(getStatusLabel('cancelled')).toBe('已取消')
  })

  it('should return correct Chinese label for approved', () => {
    expect(getStatusLabel('approved')).toBe('已通过')
  })

  it('should return correct Chinese label for rejected', () => {
    expect(getStatusLabel('rejected')).toBe('已拒绝')
  })

  it('should handle uppercase status', () => {
    expect(getStatusLabel('PENDING')).toBe('待接诊')
    expect(getStatusLabel('COMPLETED')).toBe('已完成')
  })

  it('should return original status for unknown status', () => {
    expect(getStatusLabel('unknown')).toBe('unknown')
  })
})
