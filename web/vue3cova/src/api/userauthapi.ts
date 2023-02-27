import type { User, Permission, BasicLoginInfo } from "@/entities/user"
import { fromFetchResponse, type BaseResponse } from "./baseresponse"

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
        return fromFetchResponse(p)
    }
}

const loginHelper: UserAuthApi = new UserAuthApi()
export default loginHelper
