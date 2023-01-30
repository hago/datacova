export default interface Identity {
    id: string | null,
    name: string | null,
    token: string | null
}

export const emptyIdentity: Identity = {
    name: null,
    id: null,
    token: null
}