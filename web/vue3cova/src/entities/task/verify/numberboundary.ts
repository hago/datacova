export interface NumberBoundry {
    value: number
    inclusive: boolean
}

export const newBoundary = (v?: number): NumberBoundry => ({
    value: v === undefined ? 0 : v,
    inclusive: true
})
