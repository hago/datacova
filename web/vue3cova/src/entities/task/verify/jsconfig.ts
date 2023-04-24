import type { RuleConfig } from "../taskverify"

export interface JsRuleConfig extends RuleConfig {
    snippet: string
    description: string
    paramCount: number
}

export const fromRuleConfig = (ruleConfig: RuleConfig): JsRuleConfig => {
    let ret = ruleConfig as JsRuleConfig
    if (ret.snippet === undefined) {
        ret.snippet = ""
    }
    if (ret.description === undefined) {
        ret.description = ""
    }
    if (ret.paramCount === undefined) {
        ret.paramCount = 1
    }
    return ret
}
