export interface BasicLoginInfo {
    userId: string,
    password: string,
    captcha: string
}

export interface User {
    userId: string,
    provider: string,
    name: string,
    description: string | null,
    email: string | null,
    mobile: string | null,
    status: string
}

export interface Permission {
    id: number,
    name: string,
    description: string | null,
    parentId: number,
    parent: Permission | null
}

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

export interface LoginFailResponse {
    code: number,
    error: {
        message: string,
        data: any
    }
}

export interface LoginResponseHanlder {
    success: (user: LoginResponse) => any
    fail: (status: number, reason: string, data?: any) => any
}

export class UserAuth {
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
                        let rsperr = JSON.parse(s) as LoginFailResponse
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

const loginHelper: UserAuth = new UserAuth()
export default loginHelper