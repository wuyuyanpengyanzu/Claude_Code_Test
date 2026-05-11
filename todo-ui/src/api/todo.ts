import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例，配置 baseURL 为 /api（通过 Vite 代理转发到后端）
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

/**
 * 响应拦截器
 *
 * 全局处理后端返回的 Result 对象：
 *  - 当 code !== 200 时，将错误信息转为 Promise.reject
 *  - 这样 App.vue 的 catch(error) 中就能直接拿到错误消息
 *  - 所有 API 函数无需单独处理业务状态码
 */
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 后端返回的 code 不是 200，视为业务逻辑错误
    if (res.code !== 200) {
      ElMessage.error({ message: res.message || '请求失败', duration: 2000 })
      return Promise.reject(new Error(res.message))
    }
    return response
  },
  (error) => {
    // 网络错误或 HTTP 状态码异常（如 404、500）
    ElMessage.error({ message: error.message || '网络错误', duration: 2000 })
    return Promise.reject(error)
  }
)

/**
 * 获取所有 Todo 列表（支持按标题搜索）
 */
export function getTodoList(title?: string) {
  const params: Record<string, string> = {}
  if (title) {
    params.title = title
  }
  return request.get('/todos', { params })
}

/**
 * 新增 Todo（支持 parentId 创建子任务）
 */
export function addTodo(todo: { title: string; content?: string; status?: number; parentId?: number }) {
  return request.post('/todos', todo)
}

/**
 * 更新 Todo
 */
export function updateTodo(id: number, todo: { status?: number; title?: string; content?: string; isStarred?: number }) {
  return request.put(`/todos/${id}`, todo)
}

/**
 * 删除 Todo
 */
export function deleteTodo(id: number) {
  return request.delete(`/todos/${id}`)
}

/**
 * 获取统计数据
 */
export function getStats() {
  return request.get('/todos/stats')
}

/**
 * 级联删除任务及其子任务
 */
export function deleteTodoCascade(id: number) {
  return request.delete(`/todos/${id}/cascade`)
}

/**
 * 拖拽排序：按传入的 ID 顺序更新后端 sort_order
 */
export function reorderTodo(ids: number[]) {
  return request.put('/todos/reorder', ids)
}

/**
 * 清空所有已完成任务
 */
export function clearCompletedTodo() {
  return request.delete('/todos/completed')
}

/**
 * 获取回收站任务列表
 */
export function getTrashList() {
  return request.get('/todos/trash')
}

/**
 * 还原任务
 */
export function restoreTodo(id: number) {
  return request.put(`/todos/${id}/restore`)
}

/**
 * 彻底删除任务（物理删除）
 */
export function permanentDeleteTodo(id: number) {
  return request.delete(`/todos/${id}/permanent`)
}
