import getUrlPrefix from '../utils.js'
const axios = require('axios')

class ValidationTemplateApi {
  constructor (host) {
    this.urlprefix = host === undefined ? getUrlPrefix() : host
  }

  async saveRuleTemplate (workspaceid, template) {
    let rsp = await axios({
      method: 'POST',
      url: `${this.urlprefix}/api/workspaces/${workspaceid}/rules`,
      data: template,
      withCredentials: true
    })
    return rsp
  }
}

function defaultValidatorConfig (type) {
  let x
  switch (type) {
    case 1:
      x = {pattern: '', ignoreCase: false, dotAll: true}
      break
    case 2:
      x = {lowerBounder: null, upperBound: null}
      break
    case 3:
      x = {options: [''], ignoreCase: false, allowEmpty: false}
      break
    case 4:
      x = {lowerBounder: null, upperBound: null}
      break
    case 5:
      x = {snippet: '', fields: []}
      break
    case 6:
      x = {lowerBounder: null, upperBound: null}
      break
    default:
      throw new Error(`invalid validator type ${type}`)
  }
  return x
}

export default ValidationTemplateApi
export {defaultValidatorConfig}
