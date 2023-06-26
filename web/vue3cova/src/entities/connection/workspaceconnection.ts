import type { MariaDBConfig } from "@/entities/connection/mariadbconfig"
import type { MsSqlConfig } from "@/entities/connection/mssqlconfig"
import type { PostgreSqlConfig } from "@/entities/connection/pgconfig"
import { newDbConfig, type BaseDbConfig } from "./dbconfigbase"

export interface WorkspaceConnection {
    id: number
    name: string
    description: string
    workspaceId: number
    addBy: number
    addTime: number
    modifyBy: number
    modifyTime: number
    configuration: BaseDbConfig
}

export function dbConfigStringify(configuration: BaseDbConfig): string {
    switch (configuration.dbType.toLowerCase()) {
        case "PostgreSQL".toLowerCase(): {
            let config = configuration as PostgreSqlConfig
            return `${config.dbType} - ${config.host}:${config.port}/${config.databaseName}`
        }
        case "MariaDB".toLowerCase(): {
            let config = configuration as MariaDBConfig
            return `${config.dbType} - ${config.host}:${config.port}/${config.databaseName}`
        }
        case "Microsoft SQL Sever".toLowerCase(): {
            let config = configuration as MsSqlConfig
            return `${config.dbType} - ${config.host}:${config.port}/${config.databaseName}`
        }
        default:
            console.log(`not recognized type ${configuration.dbType}`)
            throw new Error(`unknown db config: ${JSON.stringify(configuration)}`)
    }
}

export const newWorkspaceConnection = (wkid: number): WorkspaceConnection => ({
    id: -1,
    name: 'new connection',
    description: '',
    workspaceId: wkid,
    addBy: -1,
    addTime: new Date().getTime(),
    modifyBy: -1,
    modifyTime: new Date().getTime(),
    configuration: newDbConfig()
})
