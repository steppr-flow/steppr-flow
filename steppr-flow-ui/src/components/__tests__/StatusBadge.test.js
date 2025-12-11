import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import StatusBadge from '../StatusBadge.vue'

describe('StatusBadge', () => {
  describe('rendering', () => {
    it('renders with required status prop', () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 'COMPLETED' }
      })

      expect(wrapper.text()).toBe('Completed')
    })

    it('shows dot by default', () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 'PENDING' }
      })

      expect(wrapper.find('.rounded-full.w-1\\.5').exists()).toBe(true)
    })

    it('hides dot when showDot is false', () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 'PENDING', showDot: false }
      })

      expect(wrapper.find('.w-1\\.5.h-1\\.5').exists()).toBe(false)
    })
  })

  describe('status labels', () => {
    const statuses = [
      { status: 'PENDING', label: 'Pending' },
      { status: 'IN_PROGRESS', label: 'In Progress' },
      { status: 'COMPLETED', label: 'Completed' },
      { status: 'FAILED', label: 'Failed' },
      { status: 'RETRY_PENDING', label: 'Retry Pending' },
      { status: 'CANCELLED', label: 'Cancelled' },
      { status: 'PASSED', label: 'Passed' }
    ]

    statuses.forEach(({ status, label }) => {
      it(`displays "${label}" for ${status} status`, () => {
        const wrapper = mount(StatusBadge, {
          props: { status }
        })

        expect(wrapper.text()).toBe(label)
      })
    })
  })

  describe('styling', () => {
    it('applies success styling for COMPLETED', () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 'COMPLETED' }
      })

      expect(wrapper.classes()).toContain('bg-emerald-50')
      expect(wrapper.classes()).toContain('text-emerald-700')
    })

    it('applies danger styling for FAILED', () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 'FAILED' }
      })

      expect(wrapper.classes()).toContain('bg-red-50')
      expect(wrapper.classes()).toContain('text-red-700')
    })

    it('applies warning styling for PENDING', () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 'PENDING' }
      })

      expect(wrapper.classes()).toContain('bg-amber-50')
      expect(wrapper.classes()).toContain('text-amber-700')
    })

    it('applies pulse animation for IN_PROGRESS dot', () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 'IN_PROGRESS' }
      })

      const dot = wrapper.find('.w-1\\.5.h-1\\.5')
      expect(dot.classes()).toContain('animate-pulse')
    })

    it('uses default styling for unknown status', () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 'UNKNOWN_STATUS' }
      })

      // Falls back to PENDING styling
      expect(wrapper.text()).toBe('Pending')
    })
  })
})
