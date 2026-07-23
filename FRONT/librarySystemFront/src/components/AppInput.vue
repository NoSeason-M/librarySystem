<script setup lang="ts">
defineOptions({ name: 'AppInput' })

const props = withDefaults(defineProps<{
  modelValue: string
  label?: string
  placeholder?: string
  type?: 'text' | 'password' | 'email' | 'tel'
  status?: 'default' | 'focus' | 'error' | 'success'
  helperText?: string
  errorText?: string
  disabled?: boolean
  maxlength?: number
}>(), {
  type: 'text',
  status: 'default',
  disabled: false,
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  focus: [e: FocusEvent]
  blur: [e: FocusEvent]
}>()

function onInput(e: Event) {
  const target = e.target as HTMLInputElement
  emit('update:modelValue', target.value)
}
</script>

<template>
  <div :class="['app-input', `app-input--${status}`, { 'app-input--disabled': disabled }]">
    <label v-if="label" class="app-input__label">{{ label }}</label>
    <div class="app-input__box">
      <input
        :value="modelValue"
        :type="type"
        :placeholder="placeholder"
        :disabled="disabled"
        :maxlength="maxlength"
        @input="onInput"
        @focus="$emit('focus', $event)"
        @blur="$emit('blur', $event)"
      />
    </div>
    <span v-if="helperText && status === 'focus'" class="app-input__helper">{{ helperText }}</span>
    <span v-if="errorText && status === 'error'" class="app-input__error">{{ errorText }}</span>
  </div>
</template>

<style scoped>
.app-input {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 100%;
}

.app-input__label {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  font-weight: 500;
  transition: color 0.2s;
}

/* Label colors per status */
.app-input--default .app-input__label { color: var(--text-primary, #1A1A1A); }
.app-input--focus .app-input__label  { color: var(--accent, #4A9FD8); }
.app-input--error .app-input__label  { color: var(--danger, #F87171); }
.app-input--success .app-input__label { color: var(--success, #34D399); }

.app-input__box {
  width: 100%;
  padding: 12px 16px;
  border-radius: var(--input-radius, 12px);
  transition: background 0.2s, border-color 0.2s, border-width 0.2s;
}

/* Box per status */
.app-input--default .app-input__box {
  background: var(--bg-secondary, #F7F8FA);
  border: 1.5px solid var(--border, #E5E7EB);
}
.app-input--focus .app-input__box {
  background: var(--bg-primary, #FFFFFF);
  border: 2px solid var(--accent, #4A9FD8);
}
.app-input--error .app-input__box {
  background: var(--bg-primary, #FFFFFF);
  border: 2px solid var(--danger, #F87171);
}
.app-input--success .app-input__box {
  background: var(--bg-primary, #FFFFFF);
  border: 2px solid var(--success, #34D399);
}
.app-input--disabled .app-input__box {
  background: var(--bg-secondary, #F7F8FA);
  border: 1.5px solid var(--border, #E5E7EB);
  opacity: 0.5;
}

.app-input__box input {
  width: 100%;
  background: transparent;
  border: none;
  outline: none;
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 14px;
  color: var(--text-primary, #1A1A1A);
}
.app-input__box input::placeholder {
  color: var(--text-muted, #888888);
}

.app-input__helper {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 11px;
  color: var(--text-muted, #888888);
}

.app-input__error {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 11px;
  color: var(--danger, #F87171);
}
</style>
