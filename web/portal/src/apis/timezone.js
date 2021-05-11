import getUrlPrefix from '../utils.js'
const axios = require('axios')

async function loadtimezones (host) {
  let k = `timezones`
  return new Promise(
    function (resolve, reject) {
      let x = sessionStorage.getItem(k)
      if (x != null) {
        resolve(JSON.parse(x))
      } else {
        let urlprefix = host === undefined ? getUrlPrefix() : host
        axios({
          method: 'GET',
          url: `${urlprefix}/api/default/timezones`,
          withCredentials: true
        }).then(rsp => {
          // sessionStorage.setItem(k, JSON.stringify(rsp.data.data))
          resolve(rsp.data.data)
        }).catch(err => reject(err))
      }
    }
  )
}

function getLocalTimeOffset () {
  return -new Date().getTimezoneOffset() * 60
}

export {loadtimezones, getLocalTimeOffset}
