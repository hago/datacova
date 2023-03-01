import type Identity from "@/entities/identity"
import type DistFtpConfiguration from "@/entities/task/dist/distconfigftp"
import { fromFetchResponse, type BaseResponse } from "./baseresponse"
import { addTokenHeader } from "./credential"

export interface FtpVerificationResponse extends BaseResponse {
    data: {
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
}

const verificationApi: VerificationApi = new VerificationApi()
export default verificationApi
