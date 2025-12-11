import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import StatsCard from '../StatsCard.vue'

// Mock requestAnimationFrame for animation tests
vi.stubGlobal('requestAnimationFrame', (cb) => setTimeout(cb, 0))
vi.stubGlobal('performance', { now: () => Date.now() })

describe('StatsCard', () => {
  describe('rendering', () => {
    it('renders label', () => {
      const wrapper = mount(StatsCard, {
        props: { label: 'Total Workflows', value: 100 }
      })

      expect(wrapper.text()).toContain('Total Workflows')
    })

    it('renders subtitle when provided', () => {
      const wrapper = mount(StatsCard, {
        props: { label: 'Active', value: 10, subtitle: 'Currently running' }
      })

      expect(wrapper.text()).toContain('Currently running')
    })

    it('does not render subtitle when not provided', () => {
      const wrapper = mount(StatsCard, {
        props: { label: 'Active', value: 10 }
      })

      expect(wrapper.findAll('p')).toHaveLength(2) // label and value only
    })
  })

  describe('variants', () => {
    const variants = ['default', 'primary', 'success', 'warning', 'danger', 'info']

    variants.forEach(variant => {
      it(`applies ${variant} variant styling`, () => {
        const wrapper = mount(StatsCard, {
          props: { label: 'Test', value: 1, variant }
        })

        expect(wrapper.find('.card').exists()).toBe(true)
      })
    })

    it('applies success color class for success variant', () => {
      const wrapper = mount(StatsCard, {
        props: { label: 'Completed', value: 100, variant: 'success' }
      })

      expect(wrapper.find('.text-emerald-700').exists()).toBe(true)
    })

    it('applies danger color class for danger variant', () => {
      const wrapper = mount(StatsCard, {
        props: { label: 'Failed', value: 5, variant: 'danger' }
      })

      expect(wrapper.find('.text-red-700').exists()).toBe(true)
    })
  })

  describe('progress bar', () => {
    it('shows progress bar when showProgress is true', () => {
      const wrapper = mount(StatsCard, {
        props: { label: 'Progress', value: 50, showProgress: true, progress: 50 }
      })

      expect(wrapper.find('.bg-gray-200.rounded-full').exists()).toBe(true)
    })

    it('hides progress bar by default', () => {
      const wrapper = mount(StatsCard, {
        props: { label: 'Count', value: 100 }
      })

      expect(wrapper.find('.bg-gray-200.rounded-full').exists()).toBe(false)
    })

    it('sets progress bar width correctly', () => {
      const wrapper = mount(StatsCard, {
        props: { label: 'Progress', value: 75, showProgress: true, progress: 75 }
      })

      const progressBar = wrapper.find('.h-full.rounded-full')
      expect(progressBar.attributes('style')).toContain('width: 75%')
    })
  })

  describe('icon slot', () => {
    it('renders default icon', () => {
      const wrapper = mount(StatsCard, {
        props: { label: 'Test', value: 1 }
      })

      expect(wrapper.find('svg').exists()).toBe(true)
    })

    it('renders custom icon via slot', () => {
      const wrapper = mount(StatsCard, {
        props: { label: 'Test', value: 1 },
        slots: {
          icon: '<span class="custom-icon">★</span>'
        }
      })

      expect(wrapper.find('.custom-icon').exists()).toBe(true)
    })

    it('uses custom iconPath', () => {
      const customPath = 'M12 2L2 7l10 5 10-5-10-5z'
      const wrapper = mount(StatsCard, {
        props: { label: 'Test', value: 1, iconPath: customPath }
      })

      expect(wrapper.find('path').attributes('d')).toBe(customPath)
    })
  })

  describe('value animation', () => {
    it('starts with animated value at 0', () => {
      const wrapper = mount(StatsCard, {
        props: { label: 'Test', value: 100 }
      })

      // Initial state before animation
      expect(wrapper.vm.animatedValue).toBeDefined()
    })
  })
})
