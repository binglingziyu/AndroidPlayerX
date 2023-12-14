package online.testdata.player.screenshare

import androidx.appcompat.app.AppCompatActivity
import com.smart.myapp.logger.smartLogDebug
import org.loka.screensharekit.EncodeBuilder
import org.loka.screensharekit.ScreenShareKit
import org.loka.screensharekit.callback.RGBACallBack

object PlayerXScreenShare {

    fun AppCompatActivity.startScreenShare() {
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
                // 屏幕截图数据
                smartLogDebug("PlayerXScreenShare") { "屏幕截图数据: $width,$height,$stride" }
            }

        }).onStart({
            // 用户同意采集，开始采集数据
            smartLogDebug("PlayerXScreenShare") { " 用户同意采集，开始采集数据" }
        }).start()
    }

}