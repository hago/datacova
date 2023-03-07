export default interface FailResponse {
    code: number,
    error: {
        message: string,
        data: any
    }
}

export function stringifyFailResponse(rsp: FailResponse): string {
    return `error: ${rsp.error.message}; data: ${rsp.error.data.toString()}`
}

export function stringifyFailResponseBody(body: string): string {
    let rsp = JSON.parse(body) as FailResponse
    return `error: ${rsp.error?.message}; data: ${JSON.stringify(rsp.error.data)}`
}
