import type { DrawerPlacement } from "naive-ui/"
import { ref, type Ref } from "vue"

export interface GlobalDrawerConfig {
    whetherShow: boolean
    title: string
    message: string
    className: string
    position: DrawerPlacement,
    milliSeconds: number
    timer: (cfg: GlobalDrawerConfig) => void
}

export const buildErrorDrawerConfig = (message: string, pos: DrawerPlacement, title?: string
    , milliSeconds: number = 5, timer?: (cfg: GlobalDrawerConfig) => void): GlobalDrawerConfig => {
    return buildDrawerConfig(title === undefined ? "Error" : title, message, "error", pos, milliSeconds, timer)
}

export const buildSuccessDrawerConfig = (message: string, pos: DrawerPlacement, title?: string
    , milliSeconds: number = 5, timer?: (cfg: GlobalDrawerConfig) => void): GlobalDrawerConfig => {
    return buildDrawerConfig(title === undefined ? "Succeeded" : title, message, "success", pos, milliSeconds, timer)
}

export const buildDrawerConfig = (title: string, message: string, className: string, pos: DrawerPlacement
    , milliSeconds: number = 5, timer?: (cfg: GlobalDrawerConfig) => void): GlobalDrawerConfig => {
    return {
        whetherShow: true,
        title: title,
        message: message,
        className: className,
        position: pos,
        milliSeconds: milliSeconds,
        timer: timer !== undefined ? timer : (cfg: GlobalDrawerConfig) => {
            cfg.whetherShow = false
        }
    }
}

export const defaultDrawerConfig: GlobalDrawerConfig = {
    whetherShow: false,
    title: "",
    message: "",
    className: "",
    position: 'top',
    milliSeconds: 5,
    timer: () => { }
}
