package com.atlast.utils

import io.ktor.util.logging.Logger

object LogExt {
    private var log: Logger? = null
    fun init(logger: Logger) {
        log = logger
    }

    fun d(msg: String) {
        log?.debug(msg)
    }

    fun i(msg: String) {
        log?.info(msg)
    }

    fun w(msg: String) {
        log?.warn(msg)
    }

    fun e(msg: String) {
        log?.error(msg)
    }


}