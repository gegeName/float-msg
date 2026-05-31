package com.chat.float_msg

import android.util.Log

/**
 * 库内日志工具：默认关闭，开销为零（inline + lambda 形参，关闭时连字符串都不会拼）。
 *
 * 业务方在 Application 里调 `SLog.enabled = BuildConfig.DEBUG` 即可开启。
 */
object SLog {

    @JvmField
    var enabled: Boolean = false

    inline fun d(tag: String, msg: () -> String) {
        if (enabled) Log.d(tag, msg())
    }

    inline fun i(tag: String, msg: () -> String) {
        if (enabled) Log.i(tag, msg())
    }

    inline fun w(tag: String, throwable: Throwable? = null, msg: () -> String) {
        if (enabled) {
            if (throwable != null) Log.w(tag, msg(), throwable)
            else Log.w(tag, msg())
        }
    }

    inline fun e(tag: String, throwable: Throwable? = null, msg: () -> String) {
        if (enabled) {
            if (throwable != null) Log.e(tag, msg(), throwable)
            else Log.e(tag, msg())
        }
    }
}
