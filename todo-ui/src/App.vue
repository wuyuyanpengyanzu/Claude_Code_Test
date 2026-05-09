<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getTodoList, addTodo, updateTodo, deleteTodo, deleteTodoCascade, getStats, clearCompletedTodo } from './api/todo'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Star, StarFilled, Search, Plus } from '@element-plus/icons-vue'
import TodoItem from './components/TodoItem.vue'

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
const newParentId = ref<number | undefined>(undefined)

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
    const res = await addTodo({
      title,
      content: newContent.value.trim() || undefined,
      parentId: newParentId.value,
    })
    if (res.data.code === 200) {
      ElMessage.success({
        message: newParentId.value ? '子任务添加成功' : '添加成功',
        duration: 2000,
      })
      newTitle.value = ''
      newContent.value = ''
      newParentId.value = undefined
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

// 添加子任务：设置 parentId 并聚焦标题输入框
const handleAddSubtask = (parentId: number) => {
  newParentId.value = parentId
  newTitle.value = ''
  newContent.value = ''
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 删除任务（有子任务时级联删除）
const handleDelete = async (item: TodoItem) => {
  const hasChildren = (item.children?.length ?? 0) > 0
  const msg = hasChildren
    ? `确定要删除「${item.title}」及其所有子任务吗？此操作不可恢复。`
    : `确定要删除「${item.title}」吗？`

  try {
    await ElMessageBox.confirm(msg, '确认删除', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    })
    const res = hasChildren
      ? await deleteTodoCascade(item.id)
      : await deleteTodo(item.id)
    if (res.data.code === 200) {
      ElMessage.success({ message: '删除成功', duration: 2000 })
      await fetchList()
      await fetchStats()
    }
  } catch (error: any) {
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
    <!-- 顶部标题区 -->
    <header class="app-header">
      <h1 class="app-title">Todo List</h1>
      <p class="app-subtitle" v-if="stats.total > 0">
        已完成 <strong>{{ stats.completed }}</strong> / {{ stats.total }} 项任务
      </p>
      <p class="app-subtitle" v-else>还没有任务，开始添加吧</p>
    </header>

    <!-- 统计卡片行 -->
    <div class="stats-row">
      <div class="stat-card stat-card--total">
        <div class="stat-card__accent"></div>
        <div class="stat-card__body">
          <span class="stat-card__value">{{ stats.total }}</span>
          <span class="stat-card__label">总计</span>
        </div>
      </div>
      <div class="stat-card stat-card--done">
        <div class="stat-card__accent"></div>
        <div class="stat-card__body">
          <span class="stat-card__value">{{ stats.completed }}</span>
          <span class="stat-card__label">已完成</span>
        </div>
        <transition name="fade">
          <el-button
            v-if="stats.completed > 0"
            type="danger"
            size="small"
            text
            :loading="loading"
            class="stat-card__clear-btn"
            @click="handleClearCompleted"
          >
            清空
          </el-button>
        </transition>
      </div>
      <div class="stat-card stat-card--pending">
        <div class="stat-card__accent"></div>
        <div class="stat-card__body">
          <span class="stat-card__value">{{ stats.pending }}</span>
          <span class="stat-card__label">待办</span>
        </div>
      </div>
    </div>

    <!-- 进度条 -->
    <div class="progress-wrap" v-if="stats.total > 0">
      <el-progress
        :percentage="statsPercentage"
        :stroke-width="10"
        :show-text="false"
        status="success"
      />
      <span class="progress-label">完成进度 {{ statsPercentage }}%</span>
    </div>

    <!-- 操作区 -->
    <div class="action-section">
      <div class="action-card add-card">
        <h3 class="action-card__title">
          {{ newParentId ? '添加子任务' : '添加新任务' }}
        </h3>
        <el-tag
          v-if="newParentId"
          type="warning"
          closable
          size="small"
          class="subtask-hint"
          @close="newParentId = undefined"
        >
          父任务 ID: {{ newParentId }}
        </el-tag>
        <div class="add-form">
          <el-input
            v-model="newTitle"
            placeholder="任务标题"
            clearable
            class="add-form__input"
            @keyup.enter="handleAdd"
          />
          <el-input
            v-model="newContent"
            placeholder="内容描述（可选）"
            clearable
            class="add-form__input"
            @keyup.enter="handleAdd"
          />
          <el-button type="primary" @click="handleAdd" class="add-form__btn">
            添加任务
          </el-button>
        </div>
      </div>
      <div class="action-card search-card">
        <h3 class="action-card__title">搜索</h3>
        <el-input
          v-model="keyword"
          placeholder="输入关键字回车搜索..."
          :prefix-icon="Search"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleClear"
        />
      </div>
    </div>

    <!-- 任务列表 -->
    <div class="table-section">
      <el-table
        :data="todoList"
        v-loading="loading"
        stripe
        row-key="id"
        :tree-props="{ children: 'children' }"
        class="todo-table"
      >
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="标题" min-width="200">
          <template #default="{ row }">
            <div class="task-title-cell">
              <el-icon
                class="star-icon"
                @click.stop="handleToggleStar(row)"
              >
                <StarFilled v-if="row.isStarred === 1" class="star--active" />
                <Star v-else class="star--inactive" />
              </el-icon>
              <span :class="{ 'task-completed': row.status === 1 }">
                {{ row.title }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" show-overflow-tooltip min-width="140" />
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 0 ? 'info' : 'success'"
              class="status-tag"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 0 ? '未完成' : '已完成' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="175" />
        <el-table-column label="操作" width="130" align="center">
          <template #default="{ row }">
            <el-button
              v-if="!row.parentId"
              :icon="Plus"
              size="small"
              circle
              title="添加子任务"
              @click="handleAddSubtask(row.id)"
            />
            <el-button
              type="danger"
              size="small"
              plain
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无任务，快去添加吧！" />
        </template>
      </el-table>
    </div>
  </div>
</template>

<style scoped>
/* ========== 全局背景 ========== */
.app-wrapper {
  min-height: 100vh;
  padding: 48px 24px 64px;
  background:
    radial-gradient(ellipse 80% 60% at 50% -20%, rgba(64, 158, 255, 0.06), transparent),
    radial-gradient(ellipse 60% 50% at 80% 80%, rgba(103, 194, 58, 0.04), transparent),
    #f5f7fa;
}

/* ========== 标题区 ========== */
.app-header {
  text-align: center;
  margin-bottom: 36px;
}

.app-title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #1d1e1f;
  letter-spacing: -0.5px;
}

.app-subtitle {
  margin: 8px 0 0;
  font-size: 15px;
  color: #909399;
}

.app-subtitle strong {
  color: #67c23a;
  font-weight: 600;
}

/* ========== 统计卡片行 ========== */
.stats-row {
  display: flex;
  gap: 20px;
  max-width: 960px;
  margin: 0 auto 20px;
}

.stat-card {
  flex: 1;
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 12px;
  padding: 22px 24px;
  box-shadow:
    0 1px 3px rgba(0, 0, 0, 0.04),
    0 4px 12px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  overflow: hidden;
  position: relative;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow:
    0 2px 6px rgba(0, 0, 0, 0.06),
    0 8px 20px rgba(0, 0, 0, 0.06);
}

.stat-card__accent {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  border-radius: 0 4px 4px 0;
}

.stat-card__body {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-card__value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
  letter-spacing: -1px;
}

.stat-card__label {
  font-size: 14px;
  color: #909399;
  font-weight: 500;
}

/* 总计 */
.stat-card--total .stat-card__accent {
  background: #409eff;
}
.stat-card--total .stat-card__value {
  color: #409eff;
}

/* 已完成 */
.stat-card--done .stat-card__accent {
  background: #67c23a;
}
.stat-card--done .stat-card__value {
  color: #67c23a;
}

/* 待办 */
.stat-card--pending .stat-card__accent {
  background: #e6a23c;
}
.stat-card--pending .stat-card__value {
  color: #e6a23c;
}

.stat-card__clear-btn {
  margin-left: auto;
  flex-shrink: 0;
}

/* ========== 进度条 ========== */
.progress-wrap {
  max-width: 960px;
  margin: 0 auto 28px;
  display: flex;
  align-items: center;
  gap: 14px;
}

.progress-wrap .el-progress {
  flex: 1;
}

.progress-label {
  font-size: 13px;
  color: #909399;
  white-space: nowrap;
  font-weight: 500;
}

/* ========== 操作区 ========== */
.action-section {
  max-width: 960px;
  margin: 0 auto 24px;
  display: flex;
  gap: 20px;
}

.action-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow:
    0 1px 3px rgba(0, 0, 0, 0.04),
    0 4px 12px rgba(0, 0, 0, 0.04);
}

.action-card__title {
  margin: 0 0 14px;
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.add-card {
  flex: 1;
}

.subtask-hint {
  margin-bottom: 10px;
}

.add-form {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.add-form__input {
  flex: 1;
  min-width: 180px;
}

.add-form__btn {
  flex-shrink: 0;
}

.search-card {
  width: 320px;
  flex-shrink: 0;
}

/* ========== 表格区 ========== */
.table-section {
  max-width: 960px;
  margin: 0 auto;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow:
    0 1px 3px rgba(0, 0, 0, 0.04),
    0 4px 12px rgba(0, 0, 0, 0.04);
}

.todo-table {
  border-radius: 12px;
}

/* ========== 表格行样式 ========== */
.task-title-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.task-completed {
  text-decoration: line-through;
  color: #b0b3bb;
}

.status-tag {
  cursor: pointer;
  transition: transform 0.15s ease;
}

.status-tag:hover {
  transform: scale(1.08);
}

/* ========== 星标图标 ========== */
.star-icon {
  cursor: pointer;
  font-size: 18px;
  flex-shrink: 0;
  transition: transform 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.star-icon:hover {
  transform: scale(1.25);
}

/* 用 class 替代内联 style */
.star--active {
  color: #f7ba2a;
}

.star--inactive {
  color: #dcdfe6;
}

/* ========== 过渡动画 ========== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateX(6px);
}

/* ========== 树形子行左边框 ========== */
:deep(.el-table__row--level-1) td:first-child {
  border-left: 2px solid #e4e7ed;
}

:deep(.el-table__row--level-2) td:first-child {
  border-left: 2px solid #ebeef5;
}

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .app-wrapper {
    padding: 32px 16px 48px;
  }

  .stats-row {
    flex-direction: column;
    gap: 12px;
  }

  .action-section {
    flex-direction: column;
    gap: 12px;
  }

  .search-card {
    width: 100%;
  }

  .add-form {
    flex-direction: column;
  }

  .add-form__input {
    min-width: 0;
  }

  .app-title {
    font-size: 24px;
  }
}
</style>