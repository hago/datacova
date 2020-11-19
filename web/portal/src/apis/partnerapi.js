const axios = require('axios')

class PartnerApiHelper {
  constructor (host) {
    this.urlprefix = host === undefined ? '' : host
  }

  async loadPartners () {
    let rsp = await axios({
      method: 'GET',
      url: `${this.urlprefix}/api/partner/list`,
      withCredentials: true
    })
    return rsp
  }

  async loadPartner (id) {
    console.log('loadPartner')
    let rsp = await axios({
      method: 'GET',
      url: `${this.urlprefix}/api/partner/${id}`,
      withCredentials: true
    })
    return rsp
  }

  async getIdKey () {
    console.log('getIdKey')
    let rsp = await axios({
      method: 'GET',
      url: `${this.urlprefix}/api/partner/identity`,
      withCredentials: true
    })
    return rsp
  }

  async savePartner (partner) {
    let rsp = await axios({
      method: 'PUT',
      url: `${this.urlprefix}/api/partner/${partner.id}`,
      data: partner,
      withCredentials: true
    })
    return rsp
  }

  async togglePartner (id) {
    let rsp = await axios({
      method: 'DELETE',
      url: `${this.urlprefix}/api/partner/${id}`,
      withCredentials: true
    })
    return rsp
  }
}

export default PartnerApiHelper
