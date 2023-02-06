export interface BasicLoginInfo {
    userId: string,
    password: string,
    captcha: string
}

export class UserAuth {
    constructor() {
        //
    }

    async login(params: BasicLoginInfo) {
        const myHeaders = new Headers();
        myHeaders.append("Content-Type", "application/json");
        return await fetch('/api/auth/login', {
            method: 'POST',
            headers: myHeaders
        })
    }
}

const loginHelper: UserAuth = new UserAuth()
export default loginHelper