package online.testdata.player.common.initializer

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import online.testdata.player.common.logger.smartLog
import io.getstream.log.Priority
import io.getstream.log.StreamLog
import io.getstream.log.android.AndroidStreamLogger

class PlayerXCommonInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        AndroidStreamLogger.installOnDebuggableApp(context as Application)
        StreamLog.setValidator { priority, _ -> priority.level >= Priority.VERBOSE.level }
        smartLog(tag = "Initializer") { "PlayerXCommon 模块初始化完成" }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}