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
}

export default UserApiHelper
