import type { TaskAction } from "./task";

export interface RuleConfig {
    nullable: boolean
    configType: string
}

export const newRuleConfig = (): RuleConfig => ({ nullable: true, configType: '' })

export interface VerifyConfiguration {
    fields: string[]
    // nullable: boolean
    ignoreFieldCase: boolean
    fieldCountLimit: number
    ruleConfig: RuleConfig
}

export interface TaskActionVerify extends TaskAction {
    configurations: VerifyConfiguration[]
}

export const newTaskActionVerify = (action: TaskAction): TaskActionVerify => {
    let act = action as TaskActionVerify
    if (act.configurations === undefined) {
        act.configurations = [{
            fields: [""],
            // nullable: true,
            ignoreFieldCase: true,
            fieldCountLimit: 1,
            ruleConfig: newRuleConfig()
        }]
    }
    return act
}
const VERIFY_TYPE_REGEX = "com.hagoapp.regex"
const VERIFY_TYPE_PYTHON_SCRIPT = "com.hagoapp.embed.jython"
const VERIFY_TYPE_NUMBER_RANGE = "com.hagoapp.number.range"
const VERIFY_TYPE_OPTIONS = "com.hagoapp.options"
const VERIFY_TYPE_TIME_RANGE = "com.hagoapp.time.range"
const VERIFY_TYPE_RELATIVE_TIME_RANGE = "com.hagoapp.relative.time.range"

export const VerificationTypes = [
    { label: "Python Script", value: VERIFY_TYPE_PYTHON_SCRIPT },
    { label: "Number Range", value: VERIFY_TYPE_NUMBER_RANGE },
    { label: "Options", value: VERIFY_TYPE_OPTIONS },
    { label: "Regular Expression Matching", value: VERIFY_TYPE_REGEX },
    { label: "Time Range", value: VERIFY_TYPE_TIME_RANGE },
    { label: "Relative Time Range", value: VERIFY_TYPE_RELATIVE_TIME_RANGE },
]

export const isRegexRule = (ruleConfig: RuleConfig): boolean => {
    return ruleConfig.configType === VERIFY_TYPE_REGEX
}
export const isPythonRule = (ruleConfig: RuleConfig): boolean => {
    return ruleConfig.configType === VERIFY_TYPE_PYTHON_SCRIPT
}
export const isOptionsRule = (ruleConfig: RuleConfig): boolean => {
    return ruleConfig.configType === VERIFY_TYPE_OPTIONS
}
export const isNumberRangeRule = (ruleConfig: RuleConfig): boolean => {
    return ruleConfig.configType === VERIFY_TYPE_NUMBER_RANGE
}
export const isTimeRangeRule = (ruleConfig: RuleConfig): boolean => {
    return ruleConfig.configType === VERIFY_TYPE_TIME_RANGE
}
export const isRelativeTimeRangeRule = (ruleConfig: RuleConfig): boolean => {
    return ruleConfig.configType === VERIFY_TYPE_RELATIVE_TIME_RANGE
}
