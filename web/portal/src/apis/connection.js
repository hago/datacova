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

  async verifyConnection (type, conf) {
    let rsp = await axios({
      method: 'POST',
      url: `${this.urlprefix}/api/connection/verify/${type}`,
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
}

export default ConnectionApiHelper
