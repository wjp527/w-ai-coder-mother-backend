<template>
  <div class="home-page">
    <div class="task-form">
      <input
        v-model="newTask"
        @keyup.enter="addTask"
        placeholder="输入新任务..."
        class="task-input"
      />
      <button @click="addTask" class="add-btn">添加</button>
    </div>
    
    <div class="task-list">
      <div
        v-for="task in tasks"
        :key="task.id"
        :class="['task-item', { completed: task.completed }]"
      >
        <input
          type="checkbox"
          :checked="task.completed"
          @change="toggleTask(task.id)"
          class="task-checkbox"
        />
        <span class="task-text">{{ task.text }}</span>
        <button @click="removeTask(task.id)" class="delete-btn">删除</button>
      </div>
      
      <div v-if="tasks.length === 0" class="empty-state">
        <p>暂无任务，开始添加你的第一个任务吧！</p>
      </div>
    </div>
    
    <div class="stats">
      <p>总计: {{ tasks.length }} 个任务 | 已完成: {{ completedCount }} 个</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const newTask = ref('')
const tasks = ref([
  { id: 1, text: '学习 Vue 3', completed: true },
  { id: 2, text: '完成项目开发', completed: false },
  { id: 3, text: '阅读技术文档', completed: false }
])

const completedCount = computed(() => {
  return tasks.value.filter(task => task.completed).length
})

function addTask() {
  if (newTask.value.trim()) {
    tasks.value.push({
      id: Date.now(),
      text: newTask.value.trim(),
      completed: false
    })
    newTask.value = ''
  }
}

function toggleTask(id) {
  const task = tasks.value.find(t => t.id === id)
  if (task) {
    task.completed = !task.completed
  }
}

function removeTask(id) {
  tasks.value = tasks.value.filter(t => t.id !== id)
}
</script>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.task-form {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
}

.task-input {
  flex: 1;
  padding: 0.75rem 1rem;
  border: 2px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
  outline: none;
  transition: border-color 0.3s;
}

.task-input:focus {
  border-color: #667eea;
}

.add-btn {
  padding: 0.75rem 1.5rem;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.3s;
}

.add-btn:hover {
  background: #5a6fd8;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.task-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  transition: transform 0.2s;
}

.task-item:hover {
  transform: translateY(-1px);
}

.task-item.completed {
  opacity: 0.7;
}

.task-checkbox {
  width: 20px;
  height: 20px;
  cursor: pointer;
}

.task-text {
  flex: 1;
  font-size: 1rem;
}

.task-item.completed .task-text {
  text-decoration: line-through;
  color: #888;
}

.delete-btn {
  padding: 0.5rem 1rem;
  background: #ff4757;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background 0.3s;
}

.delete-btn:hover {
  background: #ff3742;
}

.empty-state {
  text-align: center;
  padding: 2rem;
  color: #888;
}

.stats {
  text-align: center;
  color: #666;
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .task-form {
    flex-direction: column;
  }
  
  .task-item {
    padding: 0.75rem;
  }
  
  .task-text {
    font-size: 0.9rem;
  }
}
</style>