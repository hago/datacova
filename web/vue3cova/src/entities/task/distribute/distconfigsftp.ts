import type { DistributeConfiguraton } from "../taskdist";

export default interface DistSFtpConfiguration extends DistributeConfiguraton {
    host: string
    port: number
    login: string
    password: string | undefined
    remotePath: string
    remoteName: string | undefined
    authType: "PASSWORD" | "PRIVATE_KEY"
    privateKeyFile: string | undefined
    passPhrase: string | undefined
}

export const newDistSFtpConfiguration = (conf: DistributeConfiguraton): DistSFtpConfiguration => {
    let ret = conf as DistSFtpConfiguration
    if (ret.host === undefined) {
        ret.host = ""
    }
    if (ret.port === undefined) {
        ret.port = 22
    }
    if (ret.login === undefined) {
        ret.login = ""
    }
    if (ret.password === undefined) {
        ret.password = ""
    }
    if (ret.remotePath === undefined) {
        ret.remotePath = "~"
    }
    if (ret.authType === undefined) {
        ret.authType = "PASSWORD"
    }
    return ret
}
