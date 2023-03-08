import type Identity from "@/entities/identity"
import type DistFtpConfiguration from "@/entities/task/distribute/distconfigftp"
import type DistSFtpConfiguration from "@/entities/task/distribute/distconfigsftp"
import { fromFetchResponse, type BaseResponse } from "./baseresponse"
import { addTokenHeader } from "./credential"

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
}

const verificationApi: VerificationApi = new VerificationApi()
export default verificationApi
