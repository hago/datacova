import type { BaseDbConfig } from "./dbconfigbase";

export interface MsSqlConfig extends BaseDbConfig {
    host: string
    port: number
}
