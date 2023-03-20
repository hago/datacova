import type { RuleConfig } from "../taskverify"

export interface JythonRuleConfig extends RuleConfig {
    snippet: string
    allowImports: string[]
}

export const fromRuleConfig = (ruleConfig: RuleConfig): JythonRuleConfig => {
    let ret = ruleConfig as JythonRuleConfig
    if (ret.snippet === undefined) {
        ret.snippet = ""
    }
    if (ret.allowImports === undefined) {
        ret.allowImports = []
    }
    return ret
}
