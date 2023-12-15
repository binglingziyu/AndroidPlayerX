package online.testdata.player.common.logger

import online.testdata.player.common.logger.smartLogDebug
import online.testdata.player.common.logger.smartLogError
import online.testdata.player.common.logger.smartLogInfo
import online.testdata.player.common.logger.smartLogVerbose
import online.testdata.player.common.logger.smartLogWarn

class SmartLog constructor(val tag: String? = null) {

    public inline fun v(message: () -> String) {
        smartLogVerbose(tag, null, message)
    }

    public inline fun d(message: () -> String) {
        smartLogDebug(tag, null, message)
    }

    public inline fun i(message: () -> String) {
        smartLogInfo(tag, null, message)
    }

    public inline fun w(message: () -> String) {
        smartLogWarn(tag, null, message)
    }

    public inline fun e(throwable: Throwable? = null, message: () -> String) {
        smartLogError(tag, throwable, message)
    }


}