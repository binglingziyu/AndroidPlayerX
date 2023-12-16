package online.testdata.player.tf.initializer

import android.content.Context
import androidx.startup.Initializer
import online.testdata.player.common.initializer.PlayerXCommonInitializer
import online.testdata.player.common.logger.smartLog

class PlayerXTensorFlowInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        smartLog(tag = "Initializer") { "PlayerXTensorFlow 模块初始化完成" }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(
            PlayerXCommonInitializer::class.java
        )
    }
}