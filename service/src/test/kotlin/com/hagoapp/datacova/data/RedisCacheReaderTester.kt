package com.hagoapp.datacova.data

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.data.RedisCacheReader.GenericLoader
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RedisCacheReaderTester {

    private val configFile: String = System.getProperty("cfg") ?: "./config.json"

    private var dataLoadCount: Int = 0

    @Test
    fun regularTest() {
        CoVaConfig.loadConfig(configFile)
        val builder = RedisCacheReader.Builder<String>()
            .shouldSkipCache(false)
            .withLoadFunction(regularLoader)
            .withDataLifeTime(5)
            .withCacheName("RegularTest")
        val reader = builder.create()
        var x = reader.readData("a", "b", "c")
        Assertions.assertNotNull(x)
        Assertions.assertTrue(x is String)
        Assertions.assertEquals(1, dataLoadCount)
        x = reader.readData("a", "b", "c")
        Assertions.assertNotNull(x)
        Assertions.assertEquals(1, dataLoadCount)
        Thread.sleep(1000 * 6)
        x = reader.readData("a", "b", "c")
        Assertions.assertNotNull(x)
        Assertions.assertEquals(2, dataLoadCount)
    }

    private val regularLoader: GenericLoader<String> = object : GenericLoader<String> {
        override fun perform(vararg params: Any?): String {
            dataLoadCount++
            return params.filterNotNull().joinToString(" + ")
        }
    }

}
