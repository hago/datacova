import type { User, Permission, BasicLoginInfo } from "@/entities/user"
import type BaseResponseHandler from "./basehandler"
import type FailResponse from "./failresponse"

export interface LoginResponse {
    code: number,
    data: {
        user: User,
        token: string,
        permission: {
            userInfo: User,
            permissions: Permission[]
        }
    }
}

export interface LoginResponseHanlder extends BaseResponseHandler {
    success: (user: LoginResponse) => any
}

export class UserAuthApi {
    constructor() {
        //
    }

    async login(params: BasicLoginInfo, handler?: LoginResponseHanlder) {
        const myHeaders = new Headers();
        //myHeaders.append("Content-Type", "application/form-data");
        var data = new URLSearchParams()
        data.append('userId', params.userId)
        data.append('password', params.password)
        data.append('captcha', params.captcha)
        let p = fetch('/api/auth/login?provider=0', {
            method: 'POST',
            headers: myHeaders,
            body: data,
            cache: 'no-cache'
        })
        if (handler !== undefined) {
            p.then(rsp => {
                rsp.text().then(s => {
                    if (rsp.status === 200) {
                        handler.success(JSON.parse(s))
                    } else {
                        let rsperr = JSON.parse(s) as FailResponse
                        handler.fail(rsperr.code, rsperr.error.message, rsperr.error)
                    }
                })
            }).catch(err => {
                console.log("login fail: ", err)
                handler.fail(-1, "fetch error", err)
            })
        } else {
            return p
        }
    }
}

const loginHelper: UserAuthApi = new UserAuthApi()
export default loginHelper
