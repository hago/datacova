export default class Identity {
    id: string | null = null;
    name: string | null = null;
    token: string | null = null;

    isValidIdentity() {
        return (this.id !== undefined) && (this.id != null)
    }
}

export function newIdentity(id: string, name: string, token: string): Identity {
    let i = new Identity()
    i.id = id
    i.name = name
    i.token = token
    return i
}

export const emptyIdentity: Identity = new Identity()
