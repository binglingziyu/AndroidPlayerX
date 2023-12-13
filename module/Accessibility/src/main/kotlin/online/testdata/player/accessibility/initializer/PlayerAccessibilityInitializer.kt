package online.testdata.player.accessibility.initializer

import android.content.Context
import androidx.startup.Initializer

class PlayerAccessibilityInitializer : Initializer<Unit> {

    override fun create(context: Context) {

    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}