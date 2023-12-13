package online.testdata.player.screenshare.initializer

import android.content.Context
import androidx.startup.Initializer

class PlayerScreenShareInitializer : Initializer<Unit> {

    override fun create(context: Context) {

    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}