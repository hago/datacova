import type { RuleConfig } from "../taskverify";

export interface OptionsRuleConfig extends RuleConfig {
    caseSensitive: boolean,
    options: string[]
}

export const fromRuleConfig = (conf: RuleConfig): OptionsRuleConfig => {
    let config = conf as OptionsRuleConfig
    if (config.caseSensitive === undefined) {
        config.caseSensitive = false
    }
    if (config.options === undefined) {
        config.options = ['']
    }
    return config
}
