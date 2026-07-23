<script setup lang="ts">
defineOptions({ name: 'AppBookCard' })

withDefaults(defineProps<{
  title?: string
  author?: string
  rating?: string
  available?: string
  coverIcon?: string
}>(), {
  title: 'Book Title',
  author: 'Author Name',
  rating: '★ 4.5',
  available: '3/5',
  coverIcon: '📖',
})

const emit = defineEmits<{
  click: []
}>()
</script>

<template>
  <div class="app-book-card" @click="emit('click')">
    <div class="app-book-card__cover">
      <span class="app-book-card__cover-icon">{{ coverIcon }}</span>
    </div>
    <div class="app-book-card__info">
      <h3 class="app-book-card__title">{{ title }}</h3>
      <p class="app-book-card__author">{{ author }}</p>
      <div class="app-book-card__meta">
        <span class="app-book-card__tag app-book-card__tag--rating">{{ rating }}</span>
        <span class="app-book-card__tag app-book-card__tag--avail">{{ available }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.app-book-card {
  width: 200px;
  background: var(--bg-primary, #FFFFFF);
  border-radius: var(--card-radius, 16px);
  border: 1px solid var(--border, #E5E7EB);
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
}
.app-book-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  transform: translateY(-2px);
}

.app-book-card__cover {
  height: 180px;
  background: var(--accent-light, #E8F4FD);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px 14px 0 0;
}

.app-book-card__cover-icon {
  font-size: 40px;
  line-height: 1;
}

.app-book-card__info {
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.app-book-card__title {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary, #1A1A1A);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-book-card__author {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 12px;
  color: var(--text-secondary, #666666);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-book-card__meta {
  display: flex;
  gap: 8px;
  margin-top: 4px;
}

.app-book-card__tag {
  padding: 3px 8px;
  border-radius: 999px;
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 11px;
  font-weight: 500;
  color: var(--text-inverse, #FFFFFF);
  line-height: 1.3;
}

.app-book-card__tag--rating {
  background: var(--warning, #FBBF24);
}

.app-book-card__tag--avail {
  background: var(--success, #34D399);
}
</style>
