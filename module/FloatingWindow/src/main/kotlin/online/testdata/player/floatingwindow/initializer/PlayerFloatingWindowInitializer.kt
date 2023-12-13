package online.testdata.player.floatingwindow.initializer

import android.content.Context
import androidx.startup.Initializer

class PlayerFloatingWindowInitializer : Initializer<Unit> {

    override fun create(context: Context) {

    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}