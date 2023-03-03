import type { DistributeConfiguraton } from "../taskdist";

export default interface DistFtpConfiguration extends DistributeConfiguraton {
    host: string
    port: number
    login: string
    password: string
    remotePath: string
    remoteName: string | null
    passive: boolean
    binaryTransport: boolean
}

export const newDistFtpConfiguration = (orig: DistributeConfiguraton): DistFtpConfiguration => {
    let ret = orig as DistFtpConfiguration
    if (ret.host === undefined) {
        ret.host = ""
    }
    if (ret.port === undefined) {
        ret.port = 21
    }
    if (ret.login === undefined) {
        ret.login = "anonymous"
    }
    if (ret.password === undefined) {
        ret.password = ""
    }
    if (ret.remotePath === undefined) {
        ret.remotePath = "/"
    }
    if (ret.remoteName === undefined) {
        ret.remoteName = null
    }
    if (ret.passive === undefined) {
        ret.passive = false
    }
    if (ret.binaryTransport === undefined) {
        ret.binaryTransport = true
    }
    return ret
}