import type { TaskAction } from "./task";

export interface RuleConfig {

}

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
