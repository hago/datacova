import getUrlPrefix from '../utils.js'
const axios = require('axios')

class EvaluateApiHelper {
  constructor (host) {
    this.urlprefix = host === undefined ? getUrlPrefix() : host
  }

  async evaluateRegex (pattern) {
    let rsp = await axios({
      url: `${this.urlprefix}/api/regex/evaluate`,
      method: 'POST',
      data: pattern
    })
    return rsp
  }

  async evaluateLua (code, fieldValues) {
    let converted = code.replace('\r', '\\r').replace('\n', '\\n')
    let rsp = await axios({
      url: `${this.urlprefix}/api/lua/evaluate`,
      method: 'POST',
      data: {
        code: converted,
        fieldValues: fieldValues
      }
    })
    return rsp
  }

  async evaluatePython (code, fieldValues) {
    let rsp = await axios({
      url: `${this.urlprefix}/api/python/evaluate`,
      method: 'POST',
      data: {
        code: code,
        fieldValues: fieldValues
      }
    })
    return rsp
  }
}

export default EvaluateApiHelper
