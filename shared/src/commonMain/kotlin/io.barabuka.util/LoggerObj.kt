package io.barabuka.util

import co.touchlab.kermit.Logger

class LoggerObj(private val tag: String, private val isEnabled: Boolean) {
    fun w(msg: () -> String) {
        if (isEnabled) {
            Logger.w { "[$tag] ${msg()}" }
        }
    }

    fun d(msg: () -> String) {
        if (isEnabled) {
            Logger.d { "[$tag] ${msg()}" }
        }
    }

    fun e(th: Throwable?, msg: () -> String) {
        if (isEnabled) {
            Logger.e(th) { "[$tag] ${msg()}" }
        }
    }
}
