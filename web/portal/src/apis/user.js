import UserApiHelper from './userapi.js'
import router from '../router'

class User {
  constructor () {
    if (!User.instance) {
      User.instance = this
    }
    return User.instance
  }

  gotoLogin (returnPath = undefined) {
    console.log(`return path: ${returnPath}`)
    console.log(`current path: ${router.currentRoute.path}`)
    router.push({name: 'Login', params: {return: (returnPath === undefined ? router.currentRoute.path : returnPath)}})
  }

  checkLogin (jump = true, onlogged = undefined, onnotlogged = undefined) {
    this.getCurrentUser().then(userobj => {
      console.log('checkLogin logged')
      this.user = userobj
      if (onlogged !== undefined) {
        onlogged(this.user)
      }
    }).catch(err => {
      console.log(`checkLogin not logged: ${err}`)
      if (this.onnotlogged !== undefined) {
        onnotlogged(err)
      }
      if (jump) {
        this.gotoLogin()
      } else {
        this.user = {user: null, isExternalUser: null, token: null}
      }
    })
  }

  async getCurrentUser () {
    let userstr = sessionStorage.getItem('user')
    return new Promise(
      function (resolve, reject) {
        if (userstr !== null) {
          console.log('user info found')
          resolve(JSON.parse(userstr))
        } else {
          (new UserApiHelper()).checkLogin().then(rsp => {
            User.instance.setLogin(rsp.data.data)
            resolve(rsp.data.data)
          }).catch(err => {
            reject(new Error(err))
          })
        }
      }
    )
  }

  setLogin (userObj) {
    if ((userObj.user === undefined) || (userObj.token === undefined) || (userObj.isExternalUser === undefined)) {
      console.log(`invalid user object: ${JSON.stringify(userObj)}`)
      return false
    }
    sessionStorage.setItem('user', JSON.stringify(userObj))
    return true
  }

  async logout () {
    return (new UserApiHelper()).logout().then(rsp => {
      sessionStorage.removeItem('user')
    })
  }
}

export default User
