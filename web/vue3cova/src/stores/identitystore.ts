import { defineStore } from 'pinia'
import Identity from '@/entities/identity'
import { emptyIdentity } from '@/entities/identity'

type LogonStatus = {
    current: Identity,
    impersonated: Identity,
    loginTime: number,
    lastActiveTime: number
}

export function currentIdentity(status: LogonStatus): Identity {
    return (status.impersonated != emptyIdentity) ? status.impersonated : status.current
}

export function currentActualIdentity(status: LogonStatus): Identity {
    return status.current
}

export function isImpersonating(status: LogonStatus): boolean {
    return status.current.id != status.impersonated.id
}

export const identityStore = defineStore('identity', {
    state: (): LogonStatus => ({
        current: new Identity(),
        impersonated: new Identity(),
        loginTime: 0,
        lastActiveTime: 0
    }),
    actions: {
        impersonate(imperonatee: Identity) {
            this.impersonated = imperonatee
        },
        soptImpersonate() {
            this.impersonated = emptyIdentity
        },
        logout() {
            this.current = emptyIdentity
            this.loginTime = 0,
                this.lastActiveTime = 0
        },
        login(loginUser: Identity) {
            this.current = loginUser
            let t = new Date().getTime()
            this.loginTime = t
            this.lastActiveTime = t
        },
        keepActive() {
            this.lastActiveTime = new Date().getTime()
        }
    },
    persist: {
        storage: sessionStorage
    }
})
