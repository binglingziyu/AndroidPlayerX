package online.testdata.player.common.initializer

import android.content.Context
import androidx.startup.Initializer

class PlayerCommonInitializer : Initializer<Unit> {

    override fun create(context: Context) {

    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}