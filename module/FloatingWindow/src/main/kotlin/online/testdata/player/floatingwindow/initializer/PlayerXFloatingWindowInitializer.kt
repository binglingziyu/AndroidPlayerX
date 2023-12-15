package online.testdata.player.floatingwindow.initializer

import android.content.Context
import androidx.startup.Initializer
import online.testdata.player.common.logger.smartLog

class PlayerXFloatingWindowInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        smartLog(tag = "Initializer") { "PlayerXFloatingWindow 模块初始化完成" }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}