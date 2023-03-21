import type { RuleConfig } from "../taskverify"

export interface TimeBoundary {
    timeStamp: number,
    inclusive: boolean
}

export const newTimeBoundary = (t?: number, include?: boolean): TimeBoundary => ({
    timeStamp: t === undefined ? new Date().getTime() : t,
    inclusive: include === undefined ? false : include
})

export interface TimeRangeRuleConfig extends RuleConfig {
    lowerBoundary: TimeBoundary | null,
    upperBoundary: TimeBoundary | null
}

export const fromRuleConfig = (ruleConfig: RuleConfig): TimeRangeRuleConfig => {
    let ret = ruleConfig as TimeRangeRuleConfig
    if (ret.lowerBoundary === undefined) {
        ret.lowerBoundary = null
    }
    if (ret.upperBoundary === null) {
        ret.upperBoundary = null
    }
    return ret
}
