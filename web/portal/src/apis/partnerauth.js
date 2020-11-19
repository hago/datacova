const sha256 = require('js-sha256')

class PartnerAuth {
  constructor (partnerId, partnerKey) {
    this.pId = partnerId
    this.pKey = partnerKey
  }

  createHeader (userId) {
    let ts = Date.now()
    let sig = sha256(`${this.pId}:${ts}:${this.pKey}`)
    // console.log(`id: ${this.pId}`)
    // console.log(`timestamp: ${ts}`)
    // console.log(`key: ${this.pKey}`)
    // console.log(`sig: ${sig}`)
    return {
      'CoDiVa-Partner-ID': this.pId,
      'CoDiVa-Auth-Timestamp': ts,
      'CoDiVa-Auth-Signature': sig,
      'CoDiVa-User-ID': userId
    }
  }
}

export default PartnerAuth
