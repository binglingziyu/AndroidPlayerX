package online.testdata.player.x.initializer

import android.content.Context
import androidx.startup.Initializer
import online.testdata.player.accessibility.initializer.PlayerAccessibilityInitializer
import online.testdata.player.common.initializer.PlayerCommonInitializer
import online.testdata.player.floatingwindow.initializer.PlayerFloatingWindowInitializer
import online.testdata.player.screenshare.initializer.PlayerScreenShareInitializer

class PlayerXAppInitializer : Initializer<Unit> {

    override fun create(context: Context) {

    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(
            PlayerCommonInitializer::class.java,
            PlayerAccessibilityInitializer::class.java,
            PlayerFloatingWindowInitializer::class.java,
            PlayerScreenShareInitializer::class.java,
        )
    }
}