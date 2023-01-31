export default class Identity {
    id: string | null = null;
    name: string | null = null;
    token: string | null = null;

    isValidIdentity(identity: Identity) {
        return (identity.id !== undefined) && (identity.id != null)
    }
}

export const emptyIdentity: Identity = new Identity()