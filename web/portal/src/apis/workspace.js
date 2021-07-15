import getUrlPrefix from '../utils.js'
const axios = require('axios')

class WorkspaceApiHelper {
  constructor (host) {
    this.urlprefix = host === undefined ? getUrlPrefix() : host
  }

  async getMyWorkspaces () {
    let rsp = await axios({
      method: 'GET',
      url: `${this.urlprefix}/api/workspaces/mine`,
      withCredentials: true
    })
    return rsp
  }

  async getWorkspace (id) {
    let rsp = await axios({
      method: 'GET',
      url: `${this.urlprefix}/api/workspace/${id}`,
      withCredentials: true
    })
    return rsp
  }

  async newWorkspace (workspace) {
    let rsp = await axios({
      method: 'PUT',
      url: `${this.urlprefix}/api/workspace/add`,
      data: workspace,
      withCredentials: true
    })
    return rsp
  }

  async updateWorkspace (workspace) {
    let rsp = await axios({
      method: 'PUT',
      url: `${this.urlprefix}/api/workspace/update`,
      data: workspace,
      withCredentials: true
    })
    return rsp
  }

  async getConnections (workspaceId) {
    let rsp = await axios({
      method: 'GET',
      url: `${this.urlprefix}/api/workspace/${workspaceId}/connections`,
      withCredentials: true
    })
    return rsp
  }

  async getConnection (workspaceId, connectionId) {
    let rsp = await axios({
      method: 'GET',
      url: `${this.urlprefix}/api/workspace/${workspaceId}/connection/${connectionId}`,
      withCredentials: true
    })
    return rsp
  }

  async saveConn4Workspace (workspaceId, conf) {
    let url = conf.id === undefined ? `${this.urlprefix}/api/workspace/${workspaceId}/connection/add`
      : `${this.urlprefix}/api/workspace/${workspaceId}/connection/update`
    // console.log(url)
    let rsp = await axios({
      method: 'PUT',
      url: url,
      data: conf,
      withCredentials: true
    })
    return rsp
  }

  async deleteConn4Workspace (workspaceId, connectionId) {
    let rsp = await axios({
      method: 'DELETE',
      url: `${this.urlprefix}/api/workspace/${workspaceId}/connection/${connectionId}/delete`,
      withCredentials: true
    })
    return rsp
  }

  async removeMember (workspaceId, userId, type) {
    let rsp = await axios({
      method: 'DELETE',
      url: `${this.urlprefix}/api/workspace/${workspaceId}/member/${type}/remove/${userId}`,
      withCredentials: true
    })
    return rsp
  }

  async addMember (workspaceId, type, users) {
    let rsp = await axios({
      method: 'PUT',
      url: `${this.urlprefix}/api/workspace/${workspaceId}/member/${type}/add`,
      data: users,
      withCredentials: true
    })
    return rsp
  }

  async getTasks (workspaceId) {
    let rsp = await axios({
      method: 'GET',
      url: `${this.urlprefix}/api/workspace/${workspaceId}/tasks`,
      withCredentials: true
    })
    return rsp
  }

  async getTask (workspaceId, taskId) {
    let rsp = await axios({
      method: 'GET',
      url: `${this.urlprefix}/api/workspace/${workspaceId}/task/${taskId}`,
      withCredentials: true
    })
    return rsp
  }

  async updateTask (task) {
    let rsp = await axios({
      method: 'PUT',
      url: `${this.urlprefix}/api/workspace/${task.workspaceId}/task/update`,
      data: task,
      withCredentials: true
    })
    return rsp
  }

  async deleteTask (workspaceId, id) {
    let rsp = await axios({
      method: 'DELETE',
      url: `${this.urlprefix}/api/workspace/${workspaceId}/task/${id}`,
      withCredentials: true
    })
    return rsp
  }

  async runTask (workspaceId, taskId, file, extra) {
    let form = new FormData()
    form.append('file', file, file.name)
    if (extra !== undefined) {
      form.append('extra', JSON.stringify(extra))
    }
    let rsp = await axios({
      method: 'POST',
      url: `${this.urlprefix}/api/workspace/${workspaceId}/task/${taskId}/run`,
      data: form
    })
    return rsp
  }

  async parseExcel (file) {
    let form = new FormData()
    form.append('file', file, file.name)
    let rsp = await axios({
      method: 'POST',
      url: `${this.urlprefix}/api/excel/parse`,
      data: form
    })
    return rsp
  }

  async loadTaskExecutions (workspaceId, start = 0, size = 10) {
    let rsp = await axios({
      method: 'GET',
      url: `${this.urlprefix}/api/workspace/${workspaceId}/executions/${start}/${size}`
    })
    return rsp
  }

  async loadTaskExecution (id) {
    let rsp = await axios({
      method: 'GET',
      url: `${this.urlprefix}/api/execution/${id}`
    })
    return rsp
  }

  async previewFile (file, extra, start = 0, size = 20) {
    let form = new FormData()
    form.append('file', file, file.name)
    form.append('extra', JSON.stringify(extra))
    form.append('start', start)
    form.append('size', size)
    let rsp = await axios({
      method: 'POST',
      url: `${this.urlprefix}/api/file/preview`,
      data: form
    })
    return rsp
  }

  async getRules (workspaceId, start = 0, size = 20) {
    let rsp = await axios({
      method: 'GET',
      url: `${this.urlprefix}/api/workspace/${workspaceId}/rules/${start}/${size}`
    })
    return rsp
  }
}

export default WorkspaceApiHelper
