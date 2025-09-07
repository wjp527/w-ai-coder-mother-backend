<template>
  <div class="app">
    <h1>任务记录</h1>
    <div class="input-area">
      <input v-model="newTask" @keyup.enter="addTask" placeholder="输入任务...">
      <button @click="addTask">添加</button>
    </div>
    <div class="tasks">
      <div v-for="task in tasks" :key="task.id" class="task">
        <input type="checkbox" v-model="task.completed">
        <span :class="{ done: task.completed }">{{ task.text }}</span>
        <button @click="removeTask(task.id)">删除</button>
      </div>
    </div>
    <div class="stats">总计: {{ tasks.length }} | 完成: {{ completedCount }}</div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const newTask = ref('')
const tasks = ref([
  { id: 1, text: '学习 Vue 3', completed: true },
  { id: 2, text: '完成项目', completed: false }
])

const completedCount = computed(() => tasks.value.filter(t => t.completed).length)

function addTask() {
  if (newTask.value.trim()) {
    tasks.value.push({ id: Date.now(), text: newTask.value.trim(), completed: false })
    newTask.value = ''
  }
}

function removeTask(id) {
  tasks.value = tasks.value.filter(t => t.id !== id)
}
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: sans-serif; background: #f0f2f5; padding: 20px; }
.app { max-width: 500px; margin: 0 auto; background: white; padding: 20px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
h1 { text-align: center; margin-bottom: 20px; color: #333; }
.input-area { display: flex; gap: 10px; margin-bottom: 20px; }
input[type="text"] { flex: 1; padding: 10px; border: 1px solid #ddd; border-radius: 5px; }
button { padding: 10px 15px; background: #4CAF50; color: white; border: none; border-radius: 5px; cursor: pointer; }
button:hover { background: #45a049; }
.tasks { margin-bottom: 20px; }
.task { display: flex; align-items: center; gap: 10px; padding: 10px; border-bottom: 1px solid #eee; }
.task:last-child { border-bottom: none; }
.done { text-decoration: line-through; color: #888; }
.stats { text-align: center; color: #666; font-size: 14px; }
</style>