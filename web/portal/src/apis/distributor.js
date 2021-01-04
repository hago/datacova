import getUrlPrefix from '../utils.js'
const axios = require('axios')

class DistributorApiHelper {
  constructor (host) {
    this.urlprefix = host === undefined ? getUrlPrefix() : host
  }

  async verifyFtp (ftpConfig) {
    let rsp = await axios({
      method: 'POST',
      url: `${this.urlprefix}/api/distribute/verify/ftp`,
      data: ftpConfig,
      withCredentials: true
    })
    return rsp
  }

  async verifySFtp (sFtpConfig) {
    let rsp = await axios({
      method: 'POST',
      url: `${this.urlprefix}/api/distribute/verify/sftp`,
      data: sFtpConfig,
      withCredentials: true
    })
    return rsp
  }
}

export default DistributorApiHelper
