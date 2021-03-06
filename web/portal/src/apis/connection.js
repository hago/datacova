import getUrlPrefix from '../utils.js'
const axios = require('axios')

class ConnectionApiHelper {
  constructor (host) {
    this.urlprefix = host === undefined ? getUrlPrefix() : host
  }

  async getConnection (workspaceId, connectionId) {
    let rsp = await axios({
      method: 'GET',
      url: `${this.urlprefix}/api/workspace/${workspaceId}/connection/${connectionId}`,
      withCredentials: true
    })
    return rsp
  }

  async getAvailableTables (type, conf) {
    let rsp = await axios({
      method: 'POST',
      url: `${this.urlprefix}/api/connection/tables/${type}`,
      data: conf
    })
    return rsp
  }

  async verifyConnection (conf) {
    let rsp = await axios({
      method: 'POST',
      url: `${this.urlprefix}/api/connection/verify`,
      data: conf
    })
    return rsp
  }

  async listDatabase (type, conf) {
    let rsp = await axios({
      method: 'POST',
      url: `${this.urlprefix}/api/connection/databases/${type}`,
      data: conf
    })
    return rsp
  }

  async getConnectionTables (workspaceId, id) {
    let rsp = await axios({
      method: 'GET',
      url: `${this.urlprefix}/api/workspace/${workspaceId}/connection/${id}/tables`
    })
    return rsp
  }
}

export default ConnectionApiHelper
