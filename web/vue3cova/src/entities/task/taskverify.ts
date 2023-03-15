import type { TaskAction } from "./task";

export interface RuleConfig {
    nullable: boolean
    configType: string
}

export const newRuleConfig = (): RuleConfig => ({ nullable: true, configType: '' })

export interface VerifyConfiguration {
    fields: string[]
    nullable: boolean
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
            fields: [],
            nullable: true,
            ignoreFieldCase: true,
            fieldCountLimit: 1,
            ruleConfig: newRuleConfig()
        }]
    }
    return act
}
