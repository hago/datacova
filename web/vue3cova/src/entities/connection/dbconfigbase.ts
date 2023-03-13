export interface BaseDbConfig {
    dbType: string,
    username: string,
    password: string,
    databaseName: string
}

export const newDbConfig = (): BaseDbConfig => ({
    dbType: '',
    username: '',
    password: '',
    databaseName: ''
})
