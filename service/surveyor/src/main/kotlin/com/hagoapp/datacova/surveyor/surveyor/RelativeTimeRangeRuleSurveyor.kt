package com.hagoapp.datacova.surveyor.surveyor

import com.hagoapp.datacova.surveyor.RuleConfig
import com.hagoapp.datacova.surveyor.rule.RelativeTimeRangeConfig
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

class RelativeTimeRangeRuleSurveyor : Surveyor {

    private lateinit var config: RelativeTimeRangeConfig
    private val logger = LoggerFactory.getLogger(RelativeTimeRangeRuleSurveyor::class.java)

    override fun getSupportedConfigType(): String {
        return RelativeTimeRangeConfig.RELATIVE_TIME_RANGE_CONFIG
    }

    override fun acceptConfiguration(ruleConfig: RuleConfig): Surveyor {
        if (ruleConfig !is RelativeTimeRangeConfig) {
            throw UnsupportedOperationException("A RelativeTimeRangeRuleSurveyor is expected while ${config::class.java} is given")
        }
        config = ruleConfig
        return this
    }

    override fun process(params: MutableList<Any?>): Boolean {
        return when {
            params.isEmpty() -> false
            params[0] == null -> config.isNullable
            else -> test(params[0]!!)
        }
    }

    private fun test(value: Any): Boolean {
        val v = when (value) {
            is Long -> value
            is LocalDateTime -> value.toInstant(ZoneOffset.of(ZoneId.systemDefault().id)).toEpochMilli()
            is ZonedDateTime -> value.toInstant().toEpochMilli()
            else -> throw UnsupportedOperationException("Unsupported datetime type ${value::class.java.canonicalName}")
        }
        logger.debug(
            "test {} against {} <--> {}",
            value,
            config.lowerBoundary,
            config.upperBoundary
        )
        return largerThanLowerOrNull(v) && lessThanUpperOrNull(v)
    }

    private fun largerThanLowerOrNull(value: Long): Boolean {
        return when {
            config.lowerBoundary == null -> true
            config.lowerBoundary!!.inclusive -> value >= config.lowerBoundary!!.calculateEpochMilli()
            else -> value > config.lowerBoundary!!.calculateEpochMilli()
        }
    }

    private fun lessThanUpperOrNull(value: Long): Boolean {
        return when {
            config.upperBoundary == null -> true
            config.upperBoundary!!.inclusive -> value <= config.upperBoundary!!.calculateEpochMilli()
            else -> value < config.upperBoundary!!.calculateEpochMilli()
        }
    }
}
