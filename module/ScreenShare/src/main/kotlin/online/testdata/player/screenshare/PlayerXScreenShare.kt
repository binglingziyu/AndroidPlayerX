package online.testdata.player.screenshare

import android.R.attr
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import online.testdata.player.common.logger.smartLogDebug
import org.loka.screensharekit.EncodeBuilder
import org.loka.screensharekit.ScreenShareKit
import org.loka.screensharekit.callback.RGBACallBack
import java.nio.ByteBuffer


typealias ScreenShareBitmapCallback = (Bitmap) -> Unit

object PlayerXScreenShare {

    fun AppCompatActivity.startScreenShare(bitmapCallback: ScreenShareBitmapCallback) {
        // 获取 RGBA 数据
        ScreenShareKit.init(this)
            .config(screenDataType = EncodeBuilder.SCREEN_DATA_TYPE.RGBA)
            .onRGBA(object :
            RGBACallBack {
            override fun onRGBA(
                rgba: ByteArray,
                width: Int,
                height: Int,
                stride: Int,
                rotation: Int,
                rotationChanged: Boolean
            ) {
                val rowStride = stride * 4
                // 屏幕截图数据
                smartLogDebug("PlayerXScreenShare") { "屏幕截图数据: $width,$height,$stride,$rotation" }
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                if(stride == width) {
                    bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(rgba))
                } else {
                    // If rowStride is greater than width, copy row by row
                    //val pixelStride: Int = 4
                    //val bitmapRowStride: Int = bitmap.rowBytes

                    val bytesPerPixel: Int = 4 // 4 bytes per pixel (RGBA_8888)

                    val buffer = ByteBuffer.wrap(rgba)

                    val totalBuffer = ByteArray(width * bytesPerPixel * height)

                    for (row in 0 until height) {
                        buffer.position(row * rowStride)
                        buffer.get(totalBuffer, row * width * bytesPerPixel, width * bytesPerPixel)
                    }
                    bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(totalBuffer))
                }
                bitmapCallback(bitmap)
            }

        }).onStart {
                // 用户同意采集，开始采集数据
                smartLogDebug("PlayerXScreenShare") { " 用户同意采集，开始采集数据" }
            }.start()
    }

}