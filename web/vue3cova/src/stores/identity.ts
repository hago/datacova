import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type Identity from '@/entities/identity'
import { emptyIdentity } from '@/entities/identity'

type LogonStatus = {
    current: Identity,
    pseudo: Identity,
    loginTime: number,
    lastActiveTime: number
}

export const identityStore = defineStore('identity', {
    state: (): LogonStatus => ({
        current: emptyIdentity,
        pseudo: emptyIdentity,
        loginTime: 0,
        lastActiveTime: 0
    }),
    actions: {
        impersonate(imperonatee: Identity) {
            this.pseudo = imperonatee
        },
        soptImpersonate() {
            this.pseudo = emptyIdentity
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
    }
})
