import getUrlPrefix from '../utils.js'
const querystring = require('querystring')
const axios = require('axios')

class UserApiHelper {
  constructor (host) {
    this.urlprefix = host === undefined ? getUrlPrefix() : host
  }

  async checkLogin () {
    let rsp = await axios({
      method: 'GET',
      url: `${this.urlprefix}/api/auth/check`,
      withCredentials: true
    })
    return rsp
  }

  async login (user, pwd, otp) {
    let data = {
      userId: user,
      password: pwd,
      external: false,
      otp: otp
    }
    // console.log(data)
    let rsp = await axios({
      method: 'POST',
      url: `${this.urlprefix}/api/auth/login`,
      data: querystring.stringify(data),
      withCredentials: true
    })
    return rsp
  }

  async loginExternal (user, pwd, captcha) {
    let data = {
      userId: user,
      password: pwd,
      captcha: captcha,
      external: true
    }
    let rsp = await axios({
      method: 'POST',
      url: `${this.urlprefix}/api/auth/login`,
      data: querystring.stringify(data),
      withCredentials: true
    })
    return rsp
  }

  async checkNetwork () {
    let rsp = await axios({
      method: 'GET',
      url: `${this.urlprefix}/api/auth/networktype`
    })
    return rsp
  }

  async logout () {
    let rsp = await axios({
      method: 'GET',
      url: `${this.urlprefix}/api/auth/logout`
    })
    return rsp
  }

  async searchInternalUser (word) {
    let rsp = await axios({
      method: 'POST',
      url: `${this.urlprefix}/api/user/internal/search`,
      data: word
    })
    return rsp
  }

  async searchExternalUser (word) {
    let rsp = await axios({
      method: 'POST',
      url: `${this.urlprefix}/api/user/external/search`,
      data: word
    })
    return rsp
  }
}

export default UserApiHelper
