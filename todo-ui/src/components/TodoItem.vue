<script setup lang="ts">
import { Star, StarFilled, Plus, Delete } from '@element-plus/icons-vue'

interface TodoItem {
  id: number
  title: string
  content: string
  status: number
  isStarred: number
  createTime: string
  updateTime: string
  parentId?: number
  children?: TodoItem[]
}

const props = defineProps<{
  item: TodoItem
}>()

const emit = defineEmits<{
  toggleStar: [item: TodoItem]
  toggleStatus: [item: TodoItem]
  delete: [item: TodoItem]
  addSubtask: [parentId: number]
}>()

// 是否有子任务
const hasChildren = () => (props.item.children?.length ?? 0) > 0
</script>

<template>
  <div class="todo-item-wrap">
    <!-- 任务项主体 -->
    <div class="todo-item">
      <!-- 星标 -->
      <el-icon
        class="todo-item__star"
        @click.stop="emit('toggleStar', item)"
      >
        <StarFilled v-if="item.isStarred === 1" class="star--active" />
        <Star v-else class="star--inactive" />
      </el-icon>

      <!-- 标题 -->
      <span
        class="todo-item__title"
        :class="{ 'todo-item__title--done': item.status === 1 }"
      >
        {{ item.title }}
      </span>

      <!-- 状态标签 -->
      <el-tag
        :type="item.status === 0 ? 'info' : 'success'"
        size="small"
        class="todo-item__status"
        @click.stop="emit('toggleStatus', item)"
      >
        {{ item.status === 0 ? '未完成' : '已完成' }}
      </el-tag>

      <!-- 操作按钮组 -->
      <div class="todo-item__actions">
        <el-button
          v-if="!item.parentId"
          size="small"
          :icon="Plus"
          circle
          title="添加子任务"
          @click.stop="emit('addSubtask', item.id)"
        />
        <el-button
          type="danger"
          size="small"
          :icon="Delete"
          circle
          plain
          title="删除"
          @click.stop="emit('delete', item)"
        />
      </div>
    </div>

    <!-- 子任务容器：margin-left: 36px + 淡灰色左边框 -->
    <div v-if="hasChildren()" class="todo-children">
      <TodoItem
        v-for="child in item.children"
        :key="child.id"
        :item="child"
        @toggle-star="emit('toggleStar', $event)"
        @toggle-status="emit('toggleStatus', $event)"
        @delete="emit('delete', $event)"
        @add-subtask="emit('addSubtask', $event)"
      />
    </div>
  </div>
</template>

<style scoped>
.todo-item-wrap {
  width: 100%;
}

/* 任务项主体：Flexbox 行布局 */
.todo-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  transition: background 0.15s ease;
}

.todo-item:hover {
  background: #f5f7fa;
}

/* 星标图标 */
.todo-item__star {
  cursor: pointer;
  font-size: 18px;
  flex-shrink: 0;
  transition: transform 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.todo-item__star:hover {
  transform: scale(1.25);
}

.star--active {
  color: #f7ba2a;
}

.star--inactive {
  color: #dcdfe6;
}

/* 标题 */
.todo-item__title {
  flex: 1;
  font-size: 15px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.todo-item__title--done {
  text-decoration: line-through;
  color: #b0b3bb;
}

/* 状态标签 */
.todo-item__status {
  cursor: pointer;
  flex-shrink: 0;
  transition: transform 0.15s ease;
}

.todo-item__status:hover {
  transform: scale(1.08);
}

/* 操作按钮组 */
.todo-item__actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

/* 子任务容器：margin-left: 36px + 淡灰色左边框 */
.todo-children {
  margin-left: 36px;
  border-left: 2px solid #e4e7ed;
  padding-left: 16px;
}
</style>
