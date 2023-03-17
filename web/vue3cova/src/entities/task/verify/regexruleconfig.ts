import type { RuleConfig } from "../taskverify";

export interface RegexRuleConfig extends RuleConfig {
    pattern: string,
    caseSensitive: boolean
}

export const fromRuleConfig = (ruleConfig: RuleConfig): RegexRuleConfig => {
    let r = ruleConfig as RegexRuleConfig
    if (r.pattern === undefined) {
        r.pattern = ""
    }
    if (r.caseSensitive === undefined) {
        r.caseSensitive = false
    }
    return r
}
