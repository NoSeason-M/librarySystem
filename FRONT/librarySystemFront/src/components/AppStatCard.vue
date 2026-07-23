<script setup lang="ts">
defineOptions({ name: 'AppStatCard' })

withDefaults(defineProps<{
  label?: string
  value?: string
  trend?: string
  trendArrow?: 'up' | 'down' | 'flat'
  trendColor?: 'success' | 'danger' | 'muted'
}>(), {
  label: 'Stat Label',
  value: '0',
  trend: '',
  trendArrow: 'up',
  trendColor: 'success',
})

const arrowMap: Record<string, string> = {
  up: '↑',
  down: '↓',
  flat: '→',
}
</script>

<template>
  <div class="app-stat-card">
    <p class="app-stat-card__label">{{ label }}</p>
    <p class="app-stat-card__value">{{ value }}</p>
    <div v-if="trend" :class="['app-stat-card__trend', `app-stat-card__trend--${trendColor}`]">
      <span class="app-stat-card__arrow">{{ arrowMap[trendArrow] }}</span>
      <span class="app-stat-card__text">{{ trend }}</span>
    </div>
  </div>
</template>

<style scoped>
.app-stat-card {
  width: 220px;
  padding: 20px;
  background: var(--bg-primary, #FFFFFF);
  border-radius: var(--card-radius, 16px);
  border: 1px solid var(--border, #E5E7EB);
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.app-stat-card__label {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 12px;
  color: var(--text-muted, #888888);
  margin: 0;
}

.app-stat-card__value {
  font-family: var(--font-mono, 'Geist Mono', monospace);
  font-size: 30px;
  font-weight: 700;
  color: var(--text-primary, #1A1A1A);
  margin: 0;
  line-height: 1.2;
}

.app-stat-card__trend {
  display: flex;
  gap: 6px;
  align-items: center;
  margin-top: 2px;
}

.app-stat-card__arrow {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 12px;
}

.app-stat-card__text {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 12px;
}

.app-stat-card__trend--success { color: var(--success, #34D399); }
.app-stat-card__trend--danger  { color: var(--danger, #F87171); }
.app-stat-card__trend--muted   { color: var(--text-muted, #888888); }
</style>
