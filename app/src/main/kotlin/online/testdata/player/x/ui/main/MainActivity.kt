package online.testdata.player.x.ui.main

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import online.testdata.player.accessibility.service.PlayerXAccessibilityService
import online.testdata.player.accessibility.service.PlayerXForegroundService
import online.testdata.player.accessibility.util.AccessibilitySettingUtils.isAccessibilitySettingsOn
import online.testdata.player.accessibility.util.AccessibilitySettingUtils.toAccessibilitySetting
import online.testdata.player.common.logger.smartLogDebug
import online.testdata.player.common.util.AppUtil.isServiceRunning
import online.testdata.player.screenshare.PlayerXScreenShare.startScreenShare
import online.testdata.player.tf.DetectTool
import online.testdata.player.x.databinding.ActivityMainBinding
import org.tensorflow.lite.Interpreter


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val interpreter: Interpreter? by lazy { DetectTool.getInterpreter(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    override fun onResume() {
        super.onResume()
        if(isAccessibilitySettingsOn(PlayerXAccessibilityService::class.java)) {
            binding.buttonAccessibility.text = "已开启"
        } else {
            binding.buttonAccessibility.text = "开启"
        }
        if(isServiceRunning(PlayerXForegroundService::class.java)) {
            binding.buttonForegroundService.text = "已开启"
        } else {
            binding.buttonForegroundService.text = "开启"
        }
    }

    private fun initView() {
        binding.buttonAccessibility.setOnClickListener {
            if(!isAccessibilitySettingsOn(PlayerXAccessibilityService::class.java)) {
                toAccessibilitySetting()
            }
        }
        binding.buttonForegroundService.setOnClickListener {
            if(!isServiceRunning(PlayerXForegroundService::class.java)) {
                ContextCompat.startForegroundService(this, Intent(this, PlayerXForegroundService::class.java).apply {
                    action = "START"
                })
                binding.buttonForegroundService.text = "已开启"
            }
        }
        binding.buttonScreenShare.setOnClickListener {
            startScreenShare()
        }
        binding.buttonTensorFlow.setOnClickListener {

            val inputStream = assets.open("JokerAAAA.png")
            val bitmap = BitmapFactory.decodeStream(inputStream)
            // 转换为输入层(1, 640, 640, 3)结构的float数组
            val inputArr: Array<Array<Array<FloatArray>>> = bitmapToFloatArray(bitmap)
            // 构建一个空的输出结构
            val outArray = Array(1) {
                Array(7) {
                    FloatArray(8400)
                }
            }
            // 运行解释器，input_arr是输入，它会将结果写到outArray中
            interpreter?.run(inputArr, outArray)
            smartLogDebug { "outArray: $outArray" }
        }
    }

    private fun bitmapToFloatArray(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        val height = bitmap.height
        val width = bitmap.width
        // 初始化一个float数组
        val result = Array(1) {
            Array(height) {
                Array(width) {
                    FloatArray(3)
                }
            }
        }
        for (i in 0 until height) {
            for (j in 0 until width) {
                // 获取像素值
                val pixel = bitmap.getPixel(j, i)
                // 将RGB值分离并进行标准化（假设你需要将颜色值标准化到0-1之间）
                result[0][i][j][0] = (pixel shr 16 and 0xFF) / 255.0f
                result[0][i][j][1] = (pixel shr 8 and 0xFF) / 255.0f
                result[0][i][j][2] = (pixel and 0xFF) / 255.0f
            }
        }
        return result
    }

}