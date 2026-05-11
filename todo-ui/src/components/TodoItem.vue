<script setup lang="ts">
import { Star, StarFilled, Plus, Delete, RefreshRight } from '@element-plus/icons-vue'
import draggable from 'vuedraggable'

interface TodoItem {
  id: number
  title: string
  content: string
  status: number
  isStarred: number
  sortOrder: number
  createTime: string
  updateTime: string
  parentId?: number
  children?: TodoItem[]
}

const props = defineProps<{
  item: TodoItem
  trashMode: boolean
}>()

const emit = defineEmits<{
  toggleStar: [item: TodoItem]
  toggleStatus: [item: TodoItem]
  delete: [item: TodoItem]
  addSubtask: [parentId: number]
  reorderChildren: [parentId: number, ids: number[]]
  restore: [item: TodoItem]
  permanentDelete: [item: TodoItem]
}>()

const hasChildren = () => (props.item.children?.length ?? 0) > 0

// 子任务拖拽结束：收集当前子任务 ID 顺序并向上冒泡
const onChildDragEnd = () => {
  const ids = props.item.children?.map(c => c.id) ?? []
  if (ids.length > 0) {
    emit('reorderChildren', props.item.id, ids)
  }
}

// 阻止子任务手柄的 mousedown 事件冒泡到父 draggable
const onDragHandleMouseDown = (e: MouseEvent) => {
  if (props.item.parentId != null) {
    e.stopPropagation()
  }
}
</script>

<template>
  <div class="todo-item-wrap">
    <!-- 任务项主体 -->
    <div class="todo-item" :class="{ 'todo-item--trash': trashMode }">
      <!-- 拖拽手柄（回收站模式隐藏） -->
      <el-icon
        v-if="!trashMode"
        :class="[
          item.parentId != null ? 'drag-handle-child' : 'drag-handle',
          'todo-item__drag'
        ]"
        @mousedown="onDragHandleMouseDown"
      >
        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="9" cy="5" r="1.5" /><circle cx="15" cy="5" r="1.5" />
          <circle cx="9" cy="12" r="1.5" /><circle cx="15" cy="12" r="1.5" />
          <circle cx="9" cy="19" r="1.5" /><circle cx="15" cy="19" r="1.5" />
        </svg>
      </el-icon>

      <!-- 星标（回收站模式隐藏） -->
      <el-icon
        v-if="!trashMode"
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

      <!-- 状态标签（回收站模式隐藏） -->
      <el-tag
        v-if="!trashMode"
        :type="item.status === 0 ? 'info' : 'success'"
        size="small"
        class="todo-item__status"
        @click.stop="emit('toggleStatus', item)"
      >
        {{ item.status === 0 ? '未完成' : '已完成' }}
      </el-tag>

      <!-- 操作按钮组 -->
      <div class="todo-item__actions">
        <!-- 正常模式：添加子任务 + 删除 -->
        <template v-if="!trashMode">
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
        </template>

        <!-- 回收站模式：还原 + 彻底删除 -->
        <template v-if="trashMode">
          <el-button
            type="primary"
            size="small"
            :icon="RefreshRight"
            circle
            plain
            title="还原"
            @click.stop="emit('restore', item)"
          />
          <el-button
            type="danger"
            size="small"
            :icon="Delete"
            circle
            title="彻底删除"
            @click.stop="emit('permanentDelete', item)"
          />
        </template>
      </div>
    </div>

    <!-- 子任务容器：回收站模式不可拖拽 -->
    <div v-if="hasChildren()" class="todo-children" :class="{ 'todo-children--trash': trashMode }">
      <draggable
        v-if="!trashMode"
        v-model="props.item.children"
        item-key="id"
        handle=".drag-handle-child"
        ghost-class="drag-ghost"
        @end="onChildDragEnd"
      >
        <template #item="{ element: child }">
          <TodoItem
            :key="child.id"
            :item="child"
            :trash-mode="trashMode"
            @toggle-star="emit('toggleStar', $event)"
            @toggle-status="emit('toggleStatus', $event)"
            @delete="emit('delete', $event)"
            @add-subtask="emit('addSubtask', $event)"
            @reorder-children="(parentId: number, ids: number[]) => emit('reorderChildren', parentId, ids)"
            @restore="emit('restore', $event)"
            @permanent-delete="emit('permanentDelete', $event)"
          />
        </template>
      </draggable>

      <!-- 回收站模式：静态子任务列表 -->
      <template v-if="trashMode">
        <TodoItem
          v-for="child in props.item.children"
          :key="child.id"
          :item="child"
          :trash-mode="true"
          @restore="emit('restore', $event)"
          @permanent-delete="emit('permanentDelete', $event)"
        />
      </template>
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

/* 拖拽手柄 */
.todo-item__drag {
  cursor: grab;
  color: #c0c4cc;
  flex-shrink: 0;
  transition: color 0.15s ease;
}

.todo-item__drag:active {
  cursor: grabbing;
  color: #409eff;
}

.todo-item:hover .todo-item__drag {
  color: #909399;
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

/* 子任务容器 */
.todo-children {
  margin-left: 36px;
  border-left: 2px solid #e4e7ed;
  padding-left: 16px;
}

/* 拖拽占位样式（星标金色主题，与 App.vue 保持一致） */
.drag-ghost {
  opacity: 0.55;
  background: linear-gradient(135deg, #fef7e8, #fdf0d5);
  border: 2px dashed #e6a23c;
  border-radius: 8px;
  box-shadow: 0 0 12px rgba(230, 162, 60, 0.25), inset 0 0 8px rgba(230, 162, 60, 0.08);
}
</style>
