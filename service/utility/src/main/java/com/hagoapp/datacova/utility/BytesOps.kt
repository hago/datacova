/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.utility

/**
 * This class collects operations about byte array.
 *
 * @author suncjs
 * @since 0.5
 */
class BytesOps private constructor() {
    companion object {

        @JvmStatic
        fun bytesToInt(bytes: ByteArray?, littleEndian: Boolean = true): Int {
            if ((bytes == null) || (bytes.size < 4)) {
                throw UnsupportedOperationException("Int needs 4 bytes at least")
            }
            return if (littleEndian) {
                bytes[3].toInt().shl(24) +
                        bytes[2].toUByte().toInt().shl(16) +
                        bytes[1].toUByte().toInt().shl(8) +
                        bytes[0].toUByte().toInt()
            } else {
                bytes[0].toInt().shl(24) +
                        bytes[1].toUByte().toInt().shl(16) +
                        bytes[2].toUByte().toInt().shl(8) +
                        bytes[3].toUByte().toInt()
            }
        }

        @JvmStatic
        fun intToBytes(num: Int, littleEndian: Boolean = true): ByteArray {
            return if (!littleEndian) ByteArray(4) { num.shr((3 - it) * 8).and(0x000000ff).toByte() }
            else ByteArray(4) { num.shr(it * 8).and(0x000000ff).toByte() }
        }
    }
}

