<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getTodoList, addTodo, updateTodo, deleteTodo, getStats, clearCompletedTodo } from './api/todo'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Star, StarFilled, Search } from '@element-plus/icons-vue'

interface TodoItem {
  id: number
  title: string
  content: string
  status: number
  isStarred: number
  createTime: string
  updateTime: string
}

const todoList = ref<TodoItem[]>([])
const loading = ref(false)
const newTitle = ref('')
const newContent = ref('')
const keyword = ref('')

// 统计数据
const stats = ref({ total: 0, completed: 0, pending: 0 })
const statsPercentage = computed(() => {
  if (stats.value.total === 0) return 0
  return Math.round((stats.value.completed / stats.value.total) * 100)
})
const fetchStats = async () => {
  try {
    const res = await getStats()
    stats.value = res.data.data || { total: 0, completed: 0, pending: 0 }
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

// 获取列表（支持搜索关键字）
const fetchList = async (searchKeyword?: string) => {
  loading.value = true
  try {
    const res = await getTodoList(searchKeyword || undefined)
    todoList.value = res.data.data || []
  } catch (error) {
    console.error('获取列表失败', error)
  } finally {
    loading.value = false
  }
}

// 清空已完成任务
const handleClearCompleted = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有已完成的任务吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    })
    loading.value = true
    const res = await clearCompletedTodo()
    if (res.data.code === 200) {
      ElMessage.success({ message: '已清空所有已完成任务', duration: 2000 })
      await fetchList()
      await fetchStats()
    }
  } catch (error: any) {
    if (error?.toString().includes('cancel')) return
    ElMessage.error('清空失败')
  } finally {
    loading.value = false
  }
}

// 回车搜索
const handleSearch = () => {
  fetchList(keyword.value.trim())
}

// 清空搜索
const handleClear = () => {
  keyword.value = ''
  fetchList()
}

// 新增任务
const handleAdd = async () => {
  const title = newTitle.value.trim()
  if (!title) {
    ElMessage.warning('请输入任务标题')
    return
  }
  try {
    const res = await addTodo({ title, content: newContent.value.trim() || undefined })
    if (res.data.code === 200) {
      ElMessage.success({ message: '添加成功', duration: 2000 })
      newTitle.value = ''
      newContent.value = ''
      await fetchList()
      await fetchStats()
    }
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const handleToggleStatus = async (item: TodoItem) => {
  const newStatus = item.status === 0 ? 1 : 0
  try {
    const res = await updateTodo(item.id, { status: newStatus })
    if (res.data.code === 200) {
      ElMessage.success({ message: newStatus === 1 ? '标记为已完成' : '标记为未完成', duration: 2000 })
      item.status = newStatus
      await fetchList()
      await fetchStats()
    }
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

//星标切换
const handleToggleStar = async (item: TodoItem) => {
  const newStarred = item.isStarred === 1 ? 0 : 1
  try {
    // 调用你现有的 update 接口
    const res = await updateTodo(item.id, { isStarred: newStarred })
    if (res.data.code === 200) {
      item.isStarred = newStarred
      // 星标改变后，由于排序权重变化，需要重新获取列表以触发“跳跃”效果
      await fetchList(keyword.value)
    }
  } catch (error) {
    ElMessage.error('设置星标失败')
  }
}

// 删除任务
const handleDelete = async (item: TodoItem) => {
  try {
    await ElMessageBox.confirm(`确定要删除「${item.title}」吗？`, '确认删除', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    })
    const res = await deleteTodo(item.id)
    if (res.data.code === 200) {
      ElMessage.success({ message: '删除成功', duration: 2000 })
      await fetchList()
      await fetchStats()
    }
  } catch (error: any) {
    // 取消弹窗时不报错
    if (error?.toString().includes('cancel')) return
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  fetchList()
  fetchStats()
})
</script>

<template>
  <div class="app-wrapper">
    <el-card class="main-card" shadow="always">
      <template #header>
        <h1 class="card-title">Todo List</h1>
      </template>

      <!-- 统计区域 -->
      <div class="stats-section">
        <div class="stats-numbers">
          <el-statistic title="总计" :value="stats.total" />
          <el-statistic title="已完成" :value="stats.completed">
            <template #suffix>
              <transition name="fade">
              <el-button
                v-if="stats.completed > 0"
                type="danger"
                size="small"
                plain
                :loading="loading"
                style="margin-left: 6px"
                @click="handleClearCompleted"
              >
                清空
              </el-button>
              </transition>
            </template>
          </el-statistic>
          <el-statistic title="待办" :value="stats.pending" />
        </div>
        <el-progress
          :percentage="statsPercentage"
          :stroke-width="24"
          :text-inside="true"
          status="success"
        />
      </div>

      <!-- 操作栏：新增 + 搜索 -->
      <div class="toolbar">
        <el-form :inline="true" class="add-form" @submit.prevent="handleAdd">
          <el-form-item>
            <el-input
              v-model="newTitle"
              placeholder="输入任务标题"
              :prefix-icon = 'Search'
              clearable
              style="width: 260px"
              @keyup.enter="handleAdd"
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="newContent"
              placeholder="输入内容描述"
              :prefix-icon = 'Search'
              clearable
              style="width: 260px"
              @keyup.enter="handleAdd"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="small" @click="handleAdd">添加</el-button>
          </el-form-item>
        </el-form>

        <el-input
          v-model="keyword"
          placeholder="搜索任务..."
          :prefix-icon="Search"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
          @clear="handleClear"
        />
      </div>

      <!-- 任务列表 -->
      <el-table :data="todoList" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="标题">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 8px;">
              <!-- 这里的点击事件使用了 .stop 阻止冒泡，防止触发表格行点击（如果你以后加的话） -->
              <el-icon
                  class="star-icon"
                  @click.stop="handleToggleStar(row)"
              >
                <!-- 这里引用了引入的组件，报错会随之消失 -->
                <StarFilled v-if="row.isStarred === 1" style="color: #f7ba2a" />
                <Star v-else style="color: #c0c4cc" />
              </el-icon>

              <span :class="{ 'task-completed': row.status === 1 }">
        {{ row.title }}
      </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 0 ? 'info' : 'success'"
              style="cursor: pointer"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 0 ? '未完成' : '已完成' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>

        <!-- 空状态 -->
        <template #empty>
          <el-empty description="暂无任务，快去添加吧！" />
        </template>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.app-wrapper {
  padding: 40px 20px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.main-card {
  max-width: 960px;
  margin: 0 auto;
}

.card-title {
  margin: 0;
  text-align: center;
  font-size: 22px;
}

.toolbar {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.toolbar .el-form-item {
  margin-bottom: 0;
  margin-right: 12px;
}
.add-form {
  display: flex;
  align-items: center;
}

.task-completed {
  text-decoration: line-through;
  color: #a0a0a0;
}

.stats-section {
  margin-bottom: 20px;
  padding: 16px 20px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.stats-numbers {
  display: flex;
  justify-content: space-around;
  margin-bottom: 12px;
}

/* 进入和离开的过程动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

/* 进入前和离开后的状态 */
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateX(5px); /* 稍微带一点位移，更显动感 */
}

.star-icon {
  cursor: pointer;
  font-size: 18px;
  /* 增加一个简单的缩放动画 */
  transition: transform 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.star-icon:hover {
  transform: scale(1.2);
}
</style>