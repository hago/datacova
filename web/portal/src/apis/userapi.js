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

  async loginRegular (user, pwd, captcha) {
    let data = {
      userId: user,
      password: pwd,
      captcha: captcha
    }
    let rsp = await axios({
      method: 'POST',
      url: `${this.urlprefix}/api/auth/login`,
      data: querystring.stringify(data),
      withCredentials: true
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

  async searchUser (word) {
    let rsp = await axios({
      method: 'POST',
      url: `${this.urlprefix}/api/user/search`,
      data: word
    })
    return rsp
  }

  async batchGetUser (idList) {
    let deduplicated = idList.filter((e, i, arr) => arr.indexOf(e) === i)
    let rsp = await axios({
      method: 'POST',
      url: `${this.urlprefix}/api/user/batch`,
      data: deduplicated
    })
    return rsp
  }

  async checkRegisterUserId (type, userid) {
    let rsp = await axios({
      method: 'GET',
      url: `${this.urlprefix}/api/register/check/${type}/userid/${userid}`
    })
    return rsp
  }

  async checkRegisterEmail (type, email) {
    let encoded = encodeURIComponent(email)
    let rsp = await axios({
      method: 'GET',
      url: `${this.urlprefix}/api/register/check/${type}/email/${encoded}`
    })
    return rsp
  }

  async checkRegisterMobile (type, mobile) {
    let encoded = encodeURIComponent(mobile)
    let rsp = await axios({
      method: 'GET',
      url: `${this.urlprefix}/api/register/check/${type}/mobile/${encoded}`
    })
    return rsp
  }

  async register (user, captcha) {
    let rsp = await axios({
      method: 'PUT',
      url: `${this.urlprefix}/api/register/${captcha}`,
      data: user
    })
    return rsp
  }

  async activate (code) {
    let rsp = await axios({
      method: 'GET',
      url: `${this.urlprefix}/api/user/activate/${code}`
    })
    return rsp
  }
}

export default UserApiHelper
