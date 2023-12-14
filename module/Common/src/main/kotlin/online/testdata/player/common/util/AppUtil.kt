package online.testdata.player.common.util

import android.app.ActivityManager
import android.content.Context

object AppUtil {

    fun Context.isServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningServices = @Suppress("DEPRECATION")  activityManager.getRunningServices(Integer.MAX_VALUE)
        for (service in runningServices) {
            val componentName = service.service
            if (serviceClass.name == componentName.className) {
                return true
            }
        }
        return false
    }

}