/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.distribute;

import com.hagoapp.datacova.CoVaException;

public interface IDistributeExtra {
    void setExtraInformation() throws CoVaException;
}
