import getUrlPrefix from '../utils.js'

class WSConnection {
  constructor (host) {
    this.urlprefix = host === undefined ? getUrlPrefix() : host
    this.urlprefix = this.urlprefix.replace('https://', 'wss://').replace('http://', 'ws://')
  }

  open () {
    // console.log(`${this.urlprefix}/api/event`)
    let ws = new WebSocket(`${this.urlprefix}/api/event`)
    ws.onmessage = function (event) {
      console.log(`websocket message: ${event}`)
    }
    ws.onerror = function (event) {
      console.log(event)
      console.log(`websocket error: ${JSON.stringify(event)}`)
    }
  }
}

export default WSConnection
