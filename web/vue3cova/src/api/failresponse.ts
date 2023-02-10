export default interface FailResponse {
    code: number,
    error: {
        message: string,
        data: any
    }
}
