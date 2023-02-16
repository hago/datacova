export default interface BaseResponseHandler {
    fail: (status: number, reason: string, data?: any) => any
}
