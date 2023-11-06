package com.hagoapp.datacova.surveyor.surveyor

import com.hagoapp.datacova.surveyor.RuleConfig
import com.hagoapp.datacova.surveyor.rule.TimeRangeRuleConfig
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

class TimeRangeRuleSurveyor : Surveyor {

    private lateinit var config: TimeRangeRuleConfig
    private val logger = LoggerFactory.getLogger(TimeRangeRuleSurveyor::class.java)

    override fun getSupportedConfigType(): String {
        return TimeRangeRuleConfig.TIME_RANGE_CONFIG
    }

    override fun acceptConfiguration(ruleConfig: RuleConfig): Surveyor {
        if (ruleConfig !is TimeRangeRuleConfig) {
            throw UnsupportedOperationException("A TimeRangeRuleConfig is expected while ${config::class.java} is given")
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
            config.lowerBoundary?.timeStamp,
            config.upperBoundary?.timeStamp
        )
        return largerThanLowerOrNull(v) && lessThanUpperOrNull(v)
    }

    private fun largerThanLowerOrNull(value: Long): Boolean {
        return when {
            config.lowerBoundary == null -> true
            config.lowerBoundary!!.timeStamp == null -> true
            config.lowerBoundary!!.inclusive -> value >= config.lowerBoundary!!.timeStamp!!
            else -> value > config.lowerBoundary!!.timeStamp!!
        }
    }

    private fun lessThanUpperOrNull(value: Long): Boolean {
        return when {
            config.upperBoundary == null -> true
            config.upperBoundary!!.timeStamp == null -> true
            config.upperBoundary!!.inclusive -> value <= config.upperBoundary!!.timeStamp!!
            else -> value < config.upperBoundary!!.timeStamp!!
        }
    }
}
