package online.testdata.player.accessibility.util

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import online.testdata.player.common.logger.smartLogDebug

object AccessibilityActionUtils {

    fun click(accessibilityService: AccessibilityService, x: Float, y: Float) {
        smartLogDebug { "click: ($x, $y)" }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val builder = GestureDescription.Builder()
            val path = Path()
            path.moveTo(x, y)
            path.lineTo(x, y)
            builder.addStroke(GestureDescription.StrokeDescription(path, 0, 1))
            val gesture = builder.build()
            accessibilityService.dispatchGesture(
                gesture,
                object : AccessibilityService.GestureResultCallback() {
                    override fun onCancelled(gestureDescription: GestureDescription) {
                        super.onCancelled(gestureDescription)
                    }

                    override fun onCompleted(gestureDescription: GestureDescription) {
                        super.onCompleted(gestureDescription)
                    }
                },
                null
            )
        } else {
            TODO("VERSION.SDK_INT < N")
        }
    }

}
