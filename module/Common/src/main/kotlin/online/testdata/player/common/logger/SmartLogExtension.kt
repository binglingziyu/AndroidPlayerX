package online.testdata.player.common.logger

import io.getstream.log.Priority
import io.getstream.log.StreamLog
import io.getstream.log.taggedLogger

enum class SmartLogPriority { VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT,}

inline fun Any.smartLog(
    priority: SmartLogPriority = SmartLogPriority.DEBUG,
    tag: String? = null,
    throwable: Throwable? = null,
    message: () -> String,
) {
    taggedLogger()
    val tagOrCaller = tag ?: outerClassSimpleTagName()
    StreamLog.log(priority.toStreamLogPriority(), tagOrCaller, throwable, message)
}

inline fun Any.smartLogVerbose(
    tag: String? = null,
    throwable: Throwable? = null,
    message: () -> String,
) {
    smartLog(SmartLogPriority.VERBOSE, tag, throwable, message)
}

inline fun Any.smartLogDebug(
    tag: String? = null,
    throwable: Throwable? = null,
    message: () -> String,
) {
    smartLog(SmartLogPriority.DEBUG, tag, throwable, message)
}

inline fun Any.smartLogInfo(
    tag: String? = null,
    throwable: Throwable? = null,
    message: () -> String,
) {
    smartLog(SmartLogPriority.INFO, tag, throwable, message)
}

inline fun Any.smartLogWarn(
    tag: String? = null,
    throwable: Throwable? = null,
    message: () -> String,
) {
    smartLog(SmartLogPriority.WARN, tag, throwable, message)
}

inline fun Any.smartLogError(
    tag: String? = null,
    throwable: Throwable? = null,
    message: () -> String,
) {
    smartLog(SmartLogPriority.ERROR, tag, throwable, message)
}

inline fun Any.smartLogAssert(
    tag: String? = null,
    throwable: Throwable? = null,
    message: () -> String,
) {
    smartLog(SmartLogPriority.ASSERT, tag, throwable, message)
}

fun SmartLogPriority.toStreamLogPriority(): Priority {
    return when (this) {
        SmartLogPriority.VERBOSE -> Priority.VERBOSE
        SmartLogPriority.DEBUG -> Priority.DEBUG
        SmartLogPriority.INFO -> Priority.INFO
        SmartLogPriority.WARN -> Priority.WARN
        SmartLogPriority.ERROR -> Priority.ERROR
        SmartLogPriority.ASSERT -> Priority.ASSERT
    }
}

public fun Any.smartTaggedLogger(
    tag: String? = null,
): Lazy<SmartLog> {
    val tagOrCaller = tag ?: outerClassSimpleTagName()
    return lazy { SmartLog(tagOrCaller) }
}

@PublishedApi
internal fun Any.outerClassSimpleTagName(): String {
    val javaClass = this::class.java
    val fullClassName = javaClass.name
    val outerClassName = fullClassName.substringBefore('$')
    val simplerOuterClassName = outerClassName.substringAfterLast('.')
    return if (simplerOuterClassName.isEmpty()) {
        fullClassName
    } else {
        simplerOuterClassName.removeSuffix("Kt")
    }
}
