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

export default {}
