import getUrlPrefix from '../utils.js'
// const querystring = require('querystring')
const axios = require('axios')

class AdminApi {
  constructor (host) {
    this.urlprefix = host === undefined ? getUrlPrefix() : host
  }

  async getwssessions () {
    let rsp = await axios({
      method: 'GET',
      url: `${this.urlprefix}/api/ws/sessions`,
      withCredentials: true
    })
    return rsp
  }
}

export default AdminApi
