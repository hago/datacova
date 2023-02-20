import { assert } from "@vue/compiler-core"

type EventHandler = (...args: any[]) => any

class EventBus {
    private eventsMap = new Map<string, EventHandler[]>()

    register(event: string, handler: EventHandler) {
        if (!this.eventsMap.has(event)) {
            this.eventsMap.set(event, [handler])
        } else {
            let handlers = this.eventsMap.get(event) as EventHandler[]
            if (handlers.indexOf(handler) < 0) {
                handlers.concat(handler)
            }
        }
    }

    unregister(event: string, handler: EventHandler) {
        if (!this.eventsMap.has(event)) {
            return
        }
        let handlers = this.eventsMap.get(event) as EventHandler[]
        let i = handlers.findIndex(h => h == handler)
        if (i >= 0) {
            handlers.splice(i, 1)
        }
    }

    send(event: string, ...args: any[]) {
        let handlers = this.eventsMap.get(event)
        if (handlers === undefined) {
            return
        }
        console.log(this.eventsMap)
        let corrupted = false
        for (let h of handlers) {
            if (!corrupted && ((h === undefined) || (h === null))) {
                corrupted = true
            } else {
                h.call(null, ...args)
            }
        }
        if (corrupted) {
            handlers = handlers.filter(h => h !== undefined && h !== null)
        }
    }
}

export const eventBus = new EventBus()
