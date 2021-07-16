import SiteApiHelper from './siteapi.js'

class SiteSetting {
  constructor () {
    if (!SiteSetting.instance) {
      SiteSetting.instance = this
    }
    return SiteSetting.instance
  }

  async getSettings () {
    let settingStr = sessionStorage.getItem('sitesetting')
    return new Promise(
      function (resolve, reject) {
        if (settingStr !== null) {
          console.log('site setting info found')
          resolve(JSON.parse(settingStr))
        } else {
          (new SiteApiHelper(process.env.SERVICE_BASE_URL)).loadsettings().then(rsp => {
            settingStr = JSON.stringify(rsp.data.settings)
            sessionStorage.setItem('sitesetting', settingStr)
            resolve(rsp.data.settings)
          }).catch(err => {
            reject(err)
          })
        }
      }
    )
  }
}

export default SiteSetting
