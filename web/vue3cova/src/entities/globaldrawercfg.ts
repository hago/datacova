import type { DrawerPlacement } from "naive-ui/"

export interface GlobalDrawerConfig {
    whetherShow: boolean
    title: string
    message: string
    color: string
    position: DrawerPlacement,
    milliSeconds: number
    timer: (cfg: GlobalDrawerConfig) => void
}

export const buildErrorDrawerConfig = (message: string, pos: DrawerPlacement, title?: string
    , milliSeconds: number = 5000, timer?: (cfg: GlobalDrawerConfig) => void): GlobalDrawerConfig => {
    return buildDrawerConfig(title === undefined ? "Error" : title, message, "red", pos, milliSeconds, timer)
}

export const buildSuccessDrawerConfig = (message: string, pos: DrawerPlacement, title?: string
    , milliSeconds: number = 5000, timer?: (cfg: GlobalDrawerConfig) => void): GlobalDrawerConfig => {
    return buildDrawerConfig(title === undefined ? "Succeeded" : title, message, "green", pos, milliSeconds, timer)
}

export const buildDrawerConfig = (title: string, message: string, color: string, pos: DrawerPlacement
    , milliSeconds: number = 5, timer?: (cfg: GlobalDrawerConfig) => void): GlobalDrawerConfig => {
    return {
        whetherShow: true,
        title: title,
        message: message,
        color: color,
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
    color: "red",
    position: 'top',
    milliSeconds: 5000,
    timer: () => { }
}
