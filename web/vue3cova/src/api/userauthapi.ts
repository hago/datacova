import type { User, Permission, BasicLoginInfo } from "@/entities/user"
import type { BaseResponse } from "./baseresponse"
import { stringifyFailResponseBody } from "./failresponse"

export interface LoginResponse extends BaseResponse {
    data: {
        user: User,
        token: string,
        permission: {
            userInfo: User,
            permissions: Permission[]
        }
    }
}

export class UserAuthApi {
    constructor() {
        //
    }

    async login(params: BasicLoginInfo): Promise<LoginResponse> {
        const myHeaders = new Headers();
        //myHeaders.append("Content-Type", "application/form-data");
        var data = new URLSearchParams()
        data.append('userId', params.userId)
        data.append('password', params.password)
        data.append('captcha', params.captcha)
        let p = await fetch('/api/auth/login?provider=0', {
            method: 'POST',
            headers: myHeaders,
            body: data,
            cache: 'no-cache'
        })
        let s = await p.text()
        if (p.status === 200) {
            let loginrsp: LoginResponse = JSON.parse(s)
            return Promise.resolve(loginrsp)
        } else {
            throw new Error(stringifyFailResponseBody(s))
        }
    }
}

const loginHelper: UserAuthApi = new UserAuthApi()
export default loginHelper
