const axios = require('axios')

class JobApiHelper {
  constructor (host = process.env.SERVICE_BASE_URL) {
    this.urlprefix = host === undefined ? '' : host
    this.partnerHeader = null
  }

  setPartnerHeader (header = null) {
    this.partnerHeader = header
    return this
  }

  async loadJobList (jobType = 'all', start = 0, size = 10) {
    let rsp = await axios({
      method: 'GET',
      url: `${this.urlprefix}/api/job/list/${jobType}/${start}/${size}`
    }).then(rsp => {
      if (rsp.code === 200) {
        for (var job of rsp.data.data.jobs) {
          console.log(job.id)
        }
      }
      return rsp
    }).then(rsp => rsp)
    return rsp
  }

  async getJob (id) {
    let rsp = await axios({
      method: 'GET',
      url: `${this.urlprefix}/api/job/${id}`
    }).then(rsp => rsp)
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

  async getAvailableTables (type, conf) {
    let rsp = await axios({
      method: 'POST',
      url: `${this.urlprefix}/api/connection/tables/${type}`,
      data: conf
    })
    return rsp
  }

  async saveJob (job, file) {
    let fd = new FormData()
    fd.append('file', new Blob([file], { type: file.type }), file.name)
    fd.append('job', JSON.stringify(job))
    let rsp = await axios({
      method: 'POST',
      url: `${this.urlprefix}/api/job/add`,
      data: fd,
      headers: this.partnerHeader === null ? { 'Content-Type': 'multipart/form-data' }
        : Object.assign({ 'Content-Type': 'multipart/form-data' }, this.partnerHeader)
    })
    return rsp
  }

  async parseFile (file, enc) {
    let fd = new FormData()
    fd.append('file', new Blob([file], { type: file.type }), file.name)
    fd.append('encoding', enc)
    let rsp = await axios({
      method: 'POST',
      url: `${this.urlprefix}/api/job/file/parse`,
      data: fd,
      headers: this.partnerHeader === null ? { 'Content-Type': 'multipart/form-data' }
        : Object.assign({ 'Content-Type': 'multipart/form-data' }, this.partnerHeader)
    })
    return rsp
  }
}

export default JobApiHelper
