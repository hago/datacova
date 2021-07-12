import getUrlPrefix from '../utils.js'
// const querystring = require('querystring')
const axios = require('axios')

class SiteHelper {
  constructor (host) {
    this.urlprefix = host === undefined ? getUrlPrefix() : host
  }

  async loadsettings () {
    let rsp = await axios({
      method: 'GET',
      url: `${this.urlprefix}/api/default/settings`,
      withCredentials: true
    })
    return rsp
  }
}

export default SiteHelper
