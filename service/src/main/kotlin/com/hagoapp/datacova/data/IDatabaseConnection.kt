package com.hagoapp.datacova.data

import com.hagoapp.datacova.Application
import com.hagoapp.datacova.data.connection.Table
import com.hagoapp.datacova.entity.connection.ConnectionConfig
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import java.sql.Connection
import java.sql.JDBCType

interface IDatabaseConnection {

    companion object {

        private val dbConnectionMap: Map<Int, Class<out IDatabaseConnection>>

        init {
            val r = Reflections(Application::class.java.packageName, SubTypesScanner())
            dbConnectionMap = r.getSubTypesOf(ConnectionConfig::class.java).map { clz ->
                val sample = clz.getConstructor().newInstance()
                Pair(sample.dbType, sample.connectionClass)
            }.toMap()
        }

        fun getDatabaseConnection(conf: ConnectionConfig): IDatabaseConnection {
            val clz = dbConnectionMap.getValue(conf.dbType)
            return clz.getConstructor().newInstance()
        }
    }

    fun canConnect(conf: ConnectionConfig): Pair<Boolean, String>

    fun getAvailableTables(conf: ConnectionConfig): Map<String, List<Table>>

    fun listDatabases(conf: ConnectionConfig): List<String>

    //fun createConnectInfo(conf: ConnectionConfig): Pair<String, Properties>
    fun getConnection(conf: ConnectionConfig): Connection

    fun clearTable(conf: ConnectionConfig, tableName: String): Pair<Boolean, String?>
    fun clearTable(conf: ConnectionConfig, table: Table): Pair<Boolean, String?> {
        return clearTable(conf, getFullTableName(table))
    }

    fun dropTable(conf: ConnectionConfig, tableName: String): Pair<Boolean, String?>
    fun dropTable(conf: ConnectionConfig, table: Table): Pair<Boolean, String?> {
        return dropTable(conf, getFullTableName(table))
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

    fun escapeNameString(name: String): String

    fun getWrapperCharacter(): Pair<String, String>

    fun getFullTableName(schema: String, tableName: String): String {
        return if (schema.isBlank()) normalizeName(tableName)
        else "${normalizeName(schema)}.${normalizeName(tableName)}"
    }

    fun getFullTableName(table: Table): String {
        return getFullTableName(table.schema, table.table)
    }

    fun isTableExists(conf: ConnectionConfig, table: Table): Boolean

    fun createTable(conf: ConnectionConfig, table: Table, columnDefinition: Map<String, JDBCType>)

    fun convertJDBCTypeToDBNativeType(aType: JDBCType): String

    fun getExistingTableDefinition(conf: ConnectionConfig, table: Table): Map<String, JDBCType>

    fun mapDBTypeToJDBCType(typeName: String): JDBCType

    fun isCaseSensitive(conf: ConnectionConfig): Boolean

    fun getDefaultSchema(): String

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
