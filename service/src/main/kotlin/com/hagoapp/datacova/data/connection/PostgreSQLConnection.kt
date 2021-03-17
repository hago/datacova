package com.hagoapp.datacova.data.connection

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.data.IDatabaseConnection
import com.hagoapp.datacova.entity.connection.ConnectionConfig
import com.hagoapp.datacova.entity.connection.PostgreSQLConfig
import java.sql.DriverManager
import java.sql.JDBCType
import java.util.Properties
import kotlin.Exception
import java.sql.Connection

class PostgreSQLConnection : IDatabaseConnection {

    companion object {
        private const val PGSQL_DRIVER_CLASS_NAME = "org.postgresql.Driver"

        init {
            Class.forName(PGSQL_DRIVER_CLASS_NAME)
        }
    }

    private val logger = CoVaLogger.getLogger()

    override fun canConnect(conf: ConnectionConfig): Pair<Boolean, String> {
        try {
            getConnection(conf).use {
                return Pair(true, "")
            }
        } catch (ex: Exception) {
            return Pair(false, ex.message ?: ex.toString())
        }
    }

    override fun getAvailableTables(conf: ConnectionConfig): Map<String, List<Table>> {
        try {
            val ret = mutableMapOf<String, MutableList<Table>>()
            getConnection(conf).use { con ->
                val sql = """
                    select schemaname, tablename, tableowner from pg_tables 
                    where schemaname<>'pg_catalog' and schemaname<>'information_schema'
                    order by schemaname, tablename
                    """
                con.prepareStatement(sql).use { st ->
                    st.executeQuery().use { rs ->
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
            }
        } catch (ex: Exception) {
            logger.error("fetch table list error: $ex")
            // println(ex)
            return mapOf()
        }
    }

    override fun listDatabases(conf: ConnectionConfig): List<String> {
        if (conf !is PostgreSQLConfig) {
            throw CoVaException("Not a configuration for PostgreSQL")
        }
        conf.dbName = "postgres"
        try {
            val ret = mutableListOf<String>()
            getConnection(conf).use { con ->
                con.prepareStatement("select datname from pg_database where datistemplate = false and datname != 'postgres'")
                    .use { st ->
                        st.executeQuery().use { rs ->
                            while (rs.next()) {
                                ret.add(rs.getString("datname"))
                            }
                            return ret
                        }
                    }
            }
        } catch (ex: Exception) {
            logger.error("fetch database list error: $ex")
            // println(ex)
            return listOf()
        }
    }

    override fun getConnection(conf: ConnectionConfig): Connection {
        if (conf !is PostgreSQLConfig) {
            throw CoVaException("Not a configuration for PostgreSQL")
        }
        if (conf.dbName.isNullOrBlank()) {
            conf.dbName = "postgres"
        }
        if (listOf(conf.host, conf.userName, conf.password).any { it == null }) {
            throw CoVaException("Configuration is incomplete")
        }
        val conStr = "jdbc:postgresql://${conf.host}:${conf.port}/${conf.dbName}"
        val props = Properties()
        props.putAll(mapOf("user" to conf.userName, "password" to conf.password))
        return DriverManager.getConnection(conStr, props)
    }

    override fun clearTable(conf: ConnectionConfig, tableName: String): Pair<Boolean, String?> {
        try {
            getConnection(conf).use { con ->
                con.prepareStatement("truncate table $tableName").use { st ->
                    return Pair(st.execute(), null)
                }
            }
        } catch (ex: Exception) {
            return Pair(false, ex.message)
        }
    }

    override fun dropTable(conf: ConnectionConfig, tableName: String): Pair<Boolean, String?> {
        try {
            getConnection(conf).use { con ->
                con.prepareStatement("drop table if exists $tableName").use { st ->
                    return Pair(st.execute(), null)
                }
            }
        } catch (ex: Exception) {
            return Pair(false, ex.message)
        }
    }

    override fun getWrapperCharacter(): Pair<String, String> {
        return Pair("\"", "\"")
    }

    override fun escapeNameString(name: String): String {
        return name.replace("\"", "\"\"")
    }

    override fun createTable(conf: ConnectionConfig, table: Table, columnDefinition: Map<String, JDBCType>) {
        val tableFullName = getFullTableName(table.schema, table.table)
        getConnection(conf).use { con ->
            val wrapper = getWrapperCharacter()
            val defStr = columnDefinition.map { (col, type) ->
                "${wrapper.first}${escapeNameString(col)}${wrapper.second} ${convertJDBCTypeToDBNativeType(type)}"
            }.joinToString(", ")
            val sql = "create table $tableFullName ($defStr)"
            con.prepareStatement(sql).use { it.execute() }
        }
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

    override fun getExistingTableDefinition(conf: ConnectionConfig, table: Table): Map<String, JDBCType> {
        getConnection(conf).use { con ->
            val sql = """select
            a.attname, format_type(a.atttypid, a.atttypmod) as typename
            from pg_attribute as a
            inner join pg_class as c on c.oid = a.attrelid
            inner join pg_namespace as n on n.oid = c.relnamespace
            where a.attnum > 0 and not a.attisdropped and c.relkind= 'r' and c.relname = ? and n.nspname = ?"""
            con.prepareStatement(sql).use { stmt ->
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

    override fun isCaseSensitive(conf: ConnectionConfig): Boolean {
        return true
    }

    override fun isTableExists(conf: ConnectionConfig, table: Table): Boolean {
        getConnection(conf).use { con ->
            con.prepareStatement("select schemaname, tablename, tableowner from pg_tables where tablename = ? and schemaname = ? ")
                .use { stmt ->
                    val schema = if (table.schema.isBlank()) getDefaultSchema() else table.schema
                    stmt.setString(1, table.table)
                    stmt.setString(2, schema)
                    stmt.executeQuery().use { rs ->
                        return rs.next()
                    }
                }
        }
    }

    override fun getDefaultSchema(): String {
        return "public";
    }

}