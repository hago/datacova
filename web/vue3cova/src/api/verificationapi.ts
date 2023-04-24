import type Identity from "@/entities/identity"
import type DistFtpConfiguration from "@/entities/task/distribute/distconfigftp"
import type DistSFtpConfiguration from "@/entities/task/distribute/distconfigsftp"
import type { EvalField } from "@/entities/task/verify/evalfield"
import type { RegexRuleConfig } from "@/entities/task/verify/regexruleconfig"
import { fromFetchResponse, type BaseResponse } from "./baseresponse"
import { addJsonRequestHeader, addTokenHeader } from "./credential"

export interface FtpVerificationResponse extends BaseResponse {
    data: {
        pwd: string
    }
}

export interface SFtpVerificationResponse extends BaseResponse {
    data: {
        keyStored?: string
        pwd: string
    }
}

export interface PythonEvalResponse extends BaseResponse {
    data: {
        row: {
            [key: string]: string
        },
        result: any
    }
}

export interface JsEvalResponse extends BaseResponse {
    data: {
        row: {
            [key: string]: string
        },
        result: any,
        paramCount: number
    }
}

export class VerificationApi {
    constructor() {
        //
    }

    async verifyFtp(user: Identity, config: DistFtpConfiguration): Promise<FtpVerificationResponse> {
        let headers = addTokenHeader(user)
        headers.append("content-type", "application/json")
        let p = await fetch("/api/distribute/verify/ftp", {
            headers: headers,
            method: "POST",
            body: JSON.stringify(config)
        })
        return fromFetchResponse(p)
    }

    async verifySFtp(user: Identity, config: DistSFtpConfiguration): Promise<SFtpVerificationResponse> {
        let headers = addTokenHeader(user)
        headers.append("content-type", "application/json")
        let p = await fetch("/api/distribute/verify/sftp", {
            headers: headers,
            method: "POST",
            body: JSON.stringify(config)
        })
        return fromFetchResponse(p)
    }

    async verifySFtpWithKey(user: Identity, sFtpConfig: DistSFtpConfiguration, file: File): Promise<SFtpVerificationResponse> {
        let form = new FormData()
        form.append('file', file, file.name)
        form.append('config', JSON.stringify(sFtpConfig))
        let headers = addTokenHeader(user)
        let p = await fetch("/api/distribute/verify/sftp/keyfile", {
            headers: headers,
            method: "POST",
            body: form
        })
        return fromFetchResponse(p)
    }

    async verifyRegexRule(user: Identity, regexConfig: {
        regexConfig: RegexRuleConfig
        text: string
    }): Promise<BaseResponse> {
        let headers = addJsonRequestHeader(addTokenHeader(user))
        let p = await fetch("/api/rule/regex/evaluate", {
            headers: headers,
            method: "POST",
            body: JSON.stringify(regexConfig)
        })
        return fromFetchResponse(p)
    }

    async verifyPythonScript(user: Identity, config: {
        code: string
        fieldValues: {
            [key: string]: EvalField
        }
    }): Promise<PythonEvalResponse> {
        let headers = addJsonRequestHeader(addTokenHeader(user))
        let p = await fetch("/api/python/evaluate", {
            headers: headers,
            method: "POST",
            body: JSON.stringify(config)
        })
        return fromFetchResponse(p)
    }

    async verifyJsScript(user: Identity, config: {
        code: string
        fieldValues: {
            [key: string]: EvalField
        }
    }): Promise<JsEvalResponse> {
        let headers = addJsonRequestHeader(addTokenHeader(user))
        let p = await fetch("/api/js/evaluate", {
            headers: headers,
            method: "POST",
            body: JSON.stringify(config)
        })
        return fromFetchResponse(p)
    }
}

const verificationApi: VerificationApi = new VerificationApi()
export default verificationApi
