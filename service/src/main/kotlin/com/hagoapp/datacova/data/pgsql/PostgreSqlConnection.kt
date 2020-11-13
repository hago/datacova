package com.hagoapp.datacova.data.pgsql

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.data.DatabaseConnection
import com.hagoapp.datacova.data.PgSqlConfig
import java.lang.Exception
import java.sql.DriverManager
import java.sql.JDBCType
import java.sql.SQLException
import java.util.*

class PostgreSqlConnection(connectionConfig: PgSqlConfig) : DatabaseConnection(connectionConfig) {

    companion object {
        private const val POSTGRE_SQL_DRIVER = "org.postgresql.Driver"

        init {
            Class.forName(POSTGRE_SQL_DRIVER)
        }
    }

    private var config: PgSqlConfig = connectionConfig

    init {
        if (config.database.isNullOrBlank()) {
            config.database = "postgres"
        }
        if (listOf(config.host, config.username, config.password).any { it == null }) {
            throw CoVaException("Configuration is incomplete")
        }
        try {
            val conStr = "jdbc:postgresql://${config.host}:${config.port}/${config.database}"
            val props = Properties()
            props.putAll(mapOf("user" to config.username, "password" to config.password))
            connection = DriverManager.getConnection(conStr, props)
        } catch (e: Exception) {
            throw CoVaException("pgsql connect failed", e)
        }
    }

    override fun getAvailableTables(): Map<String, List<Table>> {
        try {
            val sql = """
                    select schemaname, tablename, tableowner from pg_tables 
                    where schemaname<>'pg_catalog' and schemaname<>'information_schema'
                    order by schemaname, tablename
                    """
            connection.prepareStatement(sql).use { st ->
                st.executeQuery().use { rs ->
                    val ret = mutableMapOf<String, MutableList<Table>>()
                    while (rs.next()) {
                        val schema = rs.getString("schemaname")
                        val table = rs.getString("tablename")
                        if (!ret.containsKey(schema)) {
                            ret[schema] = mutableListOf()
                        }
                        ret.getValue(schema).add(Table(schema, table))
                    }
                    return ret
                }
            }
        } catch (e: SQLException) {
            throw CoVaException("list table error", e)
        }
    }

    override fun listDatabases(): List<String> {
        try {
            connection.catalog = "postgres"
            val ret = mutableListOf<String>()
            val sql = "select datname from pg_database where datistemplate = false and datname != 'postgres'"
            connection.prepareStatement(sql).use { st ->
                st.executeQuery().use { rs ->
                    while (rs.next()) {
                        ret.add(rs.getString("datname"))
                    }
                }
            }
            connection.catalog = config.database
            return ret
        } catch (e: SQLException) {
            throw CoVaException("list database error", e)
        }
    }

    override fun clearTable(tableName: String): Pair<Boolean, String?> {
        return try {
            val sql = "truncate table ?"
            connection.prepareStatement(sql).use { st ->
                st.setString(1, tableName)
                st.execute()
            }
            Pair(true, null)
        } catch (e: SQLException) {
            Pair(false, e.message)
        }
    }

    override fun dropTable(tableName: String): Pair<Boolean, String?> {
        return try {
            val sql = "drop table if exists ?"
            connection.prepareStatement(sql).use { st ->
                st.setString(1, tableName)
                st.execute()
            }
            Pair(true, null)
        } catch (e: SQLException) {
            Pair(false, e.message)
        }
    }

    override fun escapeNameString(name: String): String {
        return name.replace("\"", "\"\"")
    }

    override fun getWrapperCharacter(): Pair<String, String> {
        return Pair("\"", "\"")
    }

    override fun isTableExists(table: Table): Boolean {
        val sql = "select schemaname, tablename, tableowner from pg_tables where tablename = ? and schemaname = ?"
        connection.prepareStatement(sql).use { stmt ->
            val schema = if (table.schema.isBlank()) getDefaultSchema() else table.schema
            stmt.setString(1, table.table)
            stmt.setString(2, schema)
            stmt.executeQuery().use { rs ->
                return rs.next()
            }
        }
    }

    override fun createTable(table: Table, columnDefinition: Map<String, JDBCType>) {
        val tableFullName = getFullTableName(table.schema, table.table)
        val wrapper = getWrapperCharacter()
        val defStr = columnDefinition.map { (col, type) ->
            "${wrapper.first}${escapeNameString(col)}${wrapper.second} ${convertJDBCTypeToDBNativeType(type)}"
        }.joinToString(", ")
        val sql = "create table $tableFullName ($defStr)"
        connection.prepareStatement(sql).use { it.execute() }
    }

    override fun convertJDBCTypeToDBNativeType(aType: JDBCType): String {
        return when (aType) {
            JDBCType.BOOLEAN -> "boolean"
            JDBCType.TIMESTAMP -> "timestamp with time zone"
            JDBCType.BIGINT -> "bigint"
            JDBCType.INTEGER -> "int"
            JDBCType.DOUBLE, JDBCType.DECIMAL, JDBCType.FLOAT -> "double precision"
            else -> "text"
        }
    }

    override fun getExistingTableDefinition(table: Table): Map<String, JDBCType> {
        val sql = """select
            a.attname, format_type(a.atttypid, a.atttypmod) as typename
            from pg_attribute as a
            inner join pg_class as c on c.oid = a.attrelid
            inner join pg_namespace as n on n.oid = c.relnamespace
            where a.attnum > 0 and not a.attisdropped and c.relkind= 'r' and c.relname = ? and n.nspname = ?
            """
        connection.prepareStatement(sql).use { stmt ->
            val schema = if (table.schema.isBlank()) getDefaultSchema() else table.schema
            stmt.setString(1, table.table)
            stmt.setString(2, schema)
            stmt.executeQuery().use { rs ->
                val tblColDef = mutableMapOf<String, JDBCType>()
                while (rs.next()) {
                    tblColDef[rs.getString("attname")] = mapDBTypeToJDBCType(rs.getString("typename"))
                }
                if (tblColDef.isEmpty()) {
                    throw CoVaException(
                        "Column definition of table ${getFullTableName(schema, table.table)} not found"
                    )
                }
                return tblColDef
            }
        }
    }

    override fun mapDBTypeToJDBCType(typeName: String): JDBCType {
        return when {
            typeName.compareTo("integer") == 0 -> JDBCType.INTEGER
            typeName.compareTo("bigint") == 0 -> JDBCType.BIGINT
            typeName.compareTo("boolean") == 0 -> JDBCType.BOOLEAN
            typeName.startsWith("timestamp") -> JDBCType.TIMESTAMP
            typeName.compareTo("double precision") == 0 || typeName.compareTo("real") == 0 ||
                    typeName.startsWith("numeric") -> JDBCType.DOUBLE
            else -> JDBCType.CLOB
        }
    }

    override fun isCaseSensitive(): Boolean {
        return true
    }

    override fun getDefaultSchema(): String {
        return "public"
    }

    override fun close() {
        try {
            connection.close()
        } catch (e: Throwable) {
            //
        }
    }
}