package com.hagoapp.datacova.data

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.data.pgsql.PostgreSqlConnection
import java.io.Closeable
import java.sql.Connection
import java.sql.JDBCType

abstract class DatabaseConnection(connectionConfig: ConnectionConfig) : Closeable {
    companion object {
        fun getDatabaseConnection(conf: ConnectionConfig): DatabaseConnection {
            return when (conf.dbType) {
                ConnectionType.pgsql -> PostgreSqlConnection(conf as PgSqlConfig)
                else -> throw CoVaException("Unsupported database type ${conf.dbType}")
            }
        }
    }

    protected open val config = connectionConfig

    protected lateinit var connection: Connection

    abstract fun getAvailableTables(): Map<String, List<Table>>

    abstract fun listDatabases(): List<String>

    abstract fun clearTable(tableName: String): Pair<Boolean, String?>
    fun clearTable(table: Table): Pair<Boolean, String?> {
        return clearTable(getFullTableName(table))
    }

    abstract fun dropTable(tableName: String): Pair<Boolean, String?>
    fun dropTable(table: Table): Pair<Boolean, String?> {
        return dropTable(getFullTableName(table))
    }

    fun normalizeName(name: String): String {
        return if (isNormalizedName(name)) name else {
            val wrapper = getWrapperCharacter()
            "${wrapper.first}${escapeNameString(name)}${wrapper.second}"
        }
    }

    fun isNormalizedName(name: String): Boolean {
        val w = getWrapperCharacter()
        return name.trim().startsWith(w.first) && name.trim().endsWith(w.second)
    }

    abstract fun escapeNameString(name: String): String

    abstract fun getWrapperCharacter(): Pair<String, String>

    fun getFullTableName(schema: String, tableName: String): String {
        return if (schema.isBlank()) normalizeName(tableName)
        else "${normalizeName(schema)}.${normalizeName(tableName)}"
    }

    fun getFullTableName(table: Table): String {
        return getFullTableName(table.schema, table.table)
    }

    data class Table(
        val schema: String = "",
        val table: String
    )

    abstract fun isTableExists(table: Table): Boolean

    abstract fun createTable(table: Table, columnDefinition: Map<String, JDBCType>)

    abstract fun convertJDBCTypeToDBNativeType(aType: JDBCType): String

    abstract fun getExistingTableDefinition(table: Table): Map<String, JDBCType>

    abstract fun mapDBTypeToJDBCType(typeName: String): JDBCType

    abstract fun isCaseSensitive(): Boolean

    abstract fun getDefaultSchema(): String

    fun getInsertBatchAmount(): Long {
        return 1000
    }

    /**
     * this method is for those database / JDBC drivers that doesn't support certain kind
     * of data types, e.g. timestamp in hive.
     * It should convert a value with specific unsupported type to new value in supported
     * type, to allow database inserting functions to create a compatible JDBC statement.
     */
    fun getTypedDataConverters(): Map<JDBCType, (Any) -> Pair<JDBCType, Any?>> {
        return mapOf()
    }
}