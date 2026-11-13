/*!
 * Material Design 3 (Material You) — Tailwind CSS v3 runtime override.
 *
 * Generated for jsj2434 by the Material Design style-pack migration.
 * Consumed by vendor/tailwindcss-3.4.16.js (the Play CDN runtime):
 *   window.tailwind.config is read on first render, so this file MUST load
 *   AFTER the runtime script and BEFORE any class is emitted.
 *
 * Design contract (see material-design-style-pack/material-design-SKILL.md):
 *   - 8dp spacing grid            → spacing scale unchanged (4dp steps, 8dp multiples used in markup)
 *   - elevation physics           → boxShadow.dp1 … dp8 two-layer shadows
 *     hover: dp2 → dp8  (0 1px 3px rgba(0,0,0,.12),0 1px 2px rgba(0,0,0,.24))
 *                       (0 14px 28px rgba(0,0,0,.25),0 10px 10px rgba(0,0,0,.22))
 *   - deceleration curve          → transitionTimingFunction.material = cubic-bezier(0.4, 0, 0.2, 1)
 *   - Roboto                      → fontFamily.sans / .material
 *   - bold non-pastel palette     → primary #6200ee, primary-variant #3700b3,
 *                                   secondary #03dac6, secondary-variant #018786,
 *                                   surface #ffffff, background #fafafa, error #b00020
 */
window.tailwind = window.tailwind || {};

window.tailwind.config = {
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#6200ee',
          variant: '#3700b3',
          container: '#ede7f6',
          on: '#ffffff',
        },
        secondary: {
          DEFAULT: '#03dac6',
          variant: '#018786',
          container: '#d0f5f1',
          on: '#000000',
        },
        surface: {
          DEFAULT: '#ffffff',
          muted: '#f5f5f5',
          dim: '#fafafa',
        },
        'on-surface': {
          DEFAULT: '#212121',
          medium: '#757575',
          weak: '#9e9e9e',
        },
        outline: '#e0e0e0',
        'outline-strong': '#bdbdbd',
        material: {
          error: '#b00020',
          'error-container': '#fde7e9',
        },
      },
      fontFamily: {
        sans: ['Roboto', 'Microsoft YaHei', 'Noto Sans SC', 'PingFang SC', 'system-ui', 'sans-serif'],
        material: ['Roboto', 'Microsoft YaHei', 'Noto Sans SC', 'PingFang SC', 'system-ui', 'sans-serif'],
        mono: ['Roboto Mono', 'Consolas', 'monospace'],
      },
      borderRadius: {
        material: '8px',
        card: '12px',
      },
      boxShadow: {
        dp1: '0 1px 3px rgba(0,0,0,0.12), 0 1px 2px rgba(0,0,0,0.24)',
        dp2: '0 1px 3px rgba(0,0,0,0.12), 0 1px 2px rgba(0,0,0,0.24)',
        dp3: '0 3px 6px rgba(0,0,0,0.16), 0 3px 6px rgba(0,0,0,0.23)',
        dp4: '0 4px 8px rgba(0,0,0,0.16), 0 3px 6px rgba(0,0,0,0.23)',
        dp6: '0 6px 12px rgba(0,0,0,0.20), 0 6px 8px rgba(0,0,0,0.22)',
        dp8: '0 14px 28px rgba(0,0,0,0.25), 0 10px 10px rgba(0,0,0,0.22)',
        dp12: '0 20px 40px rgba(0,0,0,0.25), 0 14px 14px rgba(0,0,0,0.22)',
      },
      transitionTimingFunction: {
        material: 'cubic-bezier(0.4,0,0.2,1)',
        'material-in': 'cubic-bezier(0.4,0,1,1)',
        'material-out': 'cubic-bezier(0,0,0.2,1)',
      },
      transitionDuration: {
        material: '250ms',
      },
      keyframes: {
        'md-fade-in': {
          from: { opacity: '0', transform: 'translateY(8px)' },
          to: { opacity: '1', transform: 'translateY(0)' },
        },
        'md-ripple': {
          from: { transform: 'scale(0)', opacity: '0.45' },
          to: { transform: 'scale(2.6)', opacity: '0' },
        },
      },
      animation: {
        'md-fade-in': 'md-fade-in 250ms cubic-bezier(0.4,0,0.2,1) both',
        'md-ripple': 'md-ripple 500ms cubic-bezier(0.4,0,0.2,1) forwards',
      },
    },
  },
};
