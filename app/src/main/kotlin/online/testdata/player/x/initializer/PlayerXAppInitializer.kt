package online.testdata.player.x.initializer

import android.content.Context
import androidx.startup.Initializer
import online.testdata.player.common.logger.smartLog
import online.testdata.player.accessibility.initializer.PlayerXAccessibilityInitializer
import online.testdata.player.common.initializer.PlayerXCommonInitializer
import online.testdata.player.floatingwindow.initializer.PlayerXFloatingWindowInitializer
import online.testdata.player.screenshare.initializer.PlayerXScreenShareInitializer

class PlayerXAppInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        smartLog(tag = "Initializer") { "PlayerXApp 模块初始化完成" }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(
            PlayerXCommonInitializer::class.java,
            PlayerXAccessibilityInitializer::class.java,
            PlayerXFloatingWindowInitializer::class.java,
            PlayerXScreenShareInitializer::class.java,
        )
    }
}