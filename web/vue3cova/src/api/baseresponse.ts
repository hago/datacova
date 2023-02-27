import { stringifyFailResponseBody } from "./failresponse"

export interface BaseResponse {
    code: number
}

export async function fromFetchResponse<Type>(response: Response): Promise<Type> {
    let s = await response.text()
    if (response.status === 200) {
        let t = JSON.parse(s) as Type
        return Promise.resolve(t)
    } else {
        throw new Error(stringifyFailResponseBody(s))
    }
}