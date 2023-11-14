/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CoVaExceptionTest {
    @Test
    void testCoVaException() {
        Assertions.assertThrows(CoVaException.class, () -> {
            throw new CoVaException();
        });
    }

    private static final String message = "exception test";

    @Test
    void testCoVaExceptionWithMessage() {
        Assertions.assertThrows(CoVaException.class, () -> {
            var ex = new CoVaException(message);
            Assertions.assertEquals(message, ex.getMessage());
            throw ex;
        });
    }
    @Test
    void testCoVaExceptionWithMessageAndCause() {
        Assertions.assertThrows(CoVaException.class, () -> {
            var nullMessage = message + " null";
            var cause = new NullPointerException(nullMessage);
            var ex = new CoVaException(message, cause);
            Assertions.assertEquals(message, ex.getMessage());
            Assertions.assertNotNull(ex.getCause());
            Assertions.assertEquals(nullMessage, ex.getCause().getMessage());
            Assertions.assertEquals(NullPointerException.class, ex.getCause().getClass());
            throw ex;
        });
    }
}
