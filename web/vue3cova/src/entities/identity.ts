export default class Identity {
    id: string | null = null;
    name: string | null = null;
    token: string | null = null;

    isValidIdentity() {
        console.log(`isValidIdentity ${this.id}`)
        return (this.id !== undefined) && (this.id != null)
    }
}

export function newIdentity(id: string | null, name: string | null, token: string | null): Identity {
    let i = new Identity()
    i.id = id
    i.name = name
    i.token = token
    return i
}

export function anonymousIdentity(): Identity {
    let i = new Identity()
    i.name = "anonymous"
    return i
}

export const emptyIdentity: Identity = new Identity()