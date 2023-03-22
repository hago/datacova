import { getTimeZone } from "@/entities/timezones"
import type { RuleConfig } from "../taskverify"

export const TimeAnchor = {
    Now: 'Now',
    BeginOfToday: 'Begin of Today',
    EndOfToday: 'End of Today',
    BeginOfThisMonth: 'Begin of This Month',
    EndOfThisMonth: 'End of This Month',
    BeginOfThisWeek: 'Begin of This Week',
    EndOfThisWeek: 'End of This Week',
    BeginOfThisQuarter: 'Begin of This Quarter',
    EndOfThisQuarter: 'End of This Quarter',
    BeginOfThisYear: 'Begin of This Year',
    EndOfThisYear: 'End of This Year'
} as {
    [key: string]: string
}

export interface TimeDiff {
    year: number
    month: number
    day: number
    hour: number
    minute: number
    second: number
    isLaterThan: boolean
}

export const newTimeDiff = (
    y?: number, m?: number, d?: number, h?: number, min?: number, sec?: number, isLater?: boolean
): TimeDiff => {
    let n = new Date()
    return {
        year: y === undefined ? n.getFullYear() : y,
        month: m === undefined ? n.getMonth() : m,
        day: d === undefined ? n.getDay() : d,
        hour: h === undefined ? n.getHours() : h,
        minute: min === undefined ? n.getMinutes() : min,
        second: sec === undefined ? n.getSeconds() : sec,
        isLaterThan: isLater === undefined ? true : isLater
    }
}

export interface RelativeTimeBoundary {
    anchor: typeof TimeAnchor[keyof typeof TimeAnchor]
    diff: TimeDiff
    inclusive: boolean
    timeZoneName: string
}

export const newRelativeTimeBoundary = (): RelativeTimeBoundary => ({
    anchor: TimeAnchor.Now,
    diff: newTimeDiff(),
    inclusive: true,
    timeZoneName: getTimeZone(new Date())
})

export interface RelativeTimeRangeRuleConfig extends RuleConfig {
    lowerBoundary: RelativeTimeBoundary | null
    upperBoundary: RelativeTimeBoundary | null
}

export const fromRuleConfig = (ruleConfig: RuleConfig): RelativeTimeRangeRuleConfig => {
    let ret = ruleConfig as RelativeTimeRangeRuleConfig
    if (ret.lowerBoundary === undefined) {
        ret.lowerBoundary = null
    }
    if (ret.upperBoundary === undefined) {
        ret.upperBoundary = null
    }
    return ret
}
