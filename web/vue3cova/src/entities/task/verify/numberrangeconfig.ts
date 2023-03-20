import type { RuleConfig } from "../taskverify";
import type { NumberBoundry } from "./numberboundary";

export interface NumberRangeConfig extends RuleConfig {
    lowerBoundary: NumberBoundry | null
    upperBoundary: NumberBoundry | null
}

export const fromRuleConfig = (conf: RuleConfig): NumberRangeConfig => {
    let config = conf as NumberRangeConfig
    if (config.lowerBoundary === undefined) {
        config.lowerBoundary = null
    }
    if (config.upperBoundary === undefined) {
        config.upperBoundary = null
    }
    return config
}
