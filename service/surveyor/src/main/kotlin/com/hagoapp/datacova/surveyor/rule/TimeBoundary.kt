package com.hagoapp.datacova.surveyor.rule

data class TimeBoundary(
    val timeStamp: Long?,
    val inclusive: Boolean = true
) {
    override fun toString(): String {
        return "TimeBoundary(timeStamp=$timeStamp, inclusive=$inclusive)"
    }
}
