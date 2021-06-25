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

  async verifySFtpWithKey (sFtpConfig, file) {
    let form = new FormData()
    form.append('file', file, file.name)
    form.append('config', JSON.stringify(sFtpConfig))
    let rsp = await axios({
      method: 'POST',
      url: `${this.urlprefix}/api/distribute/verify/sftp/keyfile`,
      data: form,
      withCredentials: true
    })
    return rsp
  }
}

export default DistributorApiHelper
