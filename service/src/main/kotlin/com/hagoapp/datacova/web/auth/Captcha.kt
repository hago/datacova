/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.web.auth

import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.util.web.CaptchaUtils
import com.hagoapp.datacova.web.MethodName
import com.hagoapp.datacova.web.WebInterface
import io.netty.handler.codec.http.HttpResponseStatus

class Captcha : WebInterface {

    private val logger = CoVaLogger.getLogger()

    override fun getPath(): String {
        return "/api/auth/captcha"
    }

    override fun requestHandlers(): MutableMap<String, WebInterface.Handler> {
        return mutableMapOf(
            MethodName.GET to WebInterface.Handler { context ->
                val captchaInfo = CaptchaUtils.createCaptcha(context, CaptchaUtils.CaptchaConfig(200, 100, 6))
                logger.debug("create captcha ${captchaInfo.first}")
                ResponseHelper.sendResponse(
                    context,
                    HttpResponseStatus.OK,
                    mutableMapOf("Content-Type" to "image/png"),
                    captchaInfo.second
                )
            }
        )
    }
}