/*
 *  Copyright (c) 2021.
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.surveyor.rule

/**
 * The class defines a boundary of number.
 *
 * @property value  the boundary value
 * @property inclusive  whether ths boundary value should be considered when comparing
 *
 * @author Chaojun Sun
 */
data class NumberBoundary(
    val value: Double,
    val inclusive: Boolean
) {
    /**
     * Compare operation, whether the input is less than / less than or equals boundary.
     *
     * @param v input value
     * @return  true if the input is less than / less than or equals boundary, otherwise false
     */
    fun lt(v: Double): Boolean {
        return if (inclusive) value <= v else value < v
    }

    /**
     * Compare operation, whether the input is greater than / greater than or equals boundary.
     *
     * @param v input value
     * @return  true if the input is greater than / greater than or equals boundary, otherwise false
     */
    fun gt(v: Double): Boolean {
        return if (inclusive) value >= v else value > v
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NumberBoundary

        if (value != other.value) return false
        if (inclusive != other.inclusive) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + inclusive.hashCode()
        return result
    }

    override fun toString(): String {
        return "NumberBoundary(value=$value, inclusive=$inclusive)"
    }


}
