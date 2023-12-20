package online.testdata.player.x.ui.main

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
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
import online.testdata.player.tf.BitmapUtil
import online.testdata.player.tf.DetectTool
import online.testdata.player.tf.NonMaxSuppression
import online.testdata.player.x.databinding.ActivityMainBinding
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Arrays


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
            var startMS = System.currentTimeMillis()
            val inputStream = assets.open("8.jpg")
            val bitmap = BitmapFactory.decodeStream(inputStream)
            smartLogDebug { "decode: ${System.currentTimeMillis() - startMS}MS" }
            startMS = System.currentTimeMillis()
            val resizeBitmap = BitmapUtil.resizeBitmap(bitmap, 640)
            smartLogDebug { "resize: ${System.currentTimeMillis() - startMS}MS" }
            startMS = System.currentTimeMillis()
            binding.dnfImage.setImageBitmap(resizeBitmap)
            // 转换为输入层(1, 640, 640, 3)结构的float数组
            smartLogDebug { "bitmap: ${bitmap.width},${bitmap.height}" }
            val inputByteBuffer = BitmapUtil.convertBitmapToByteBuffer(resizeBitmap)
            smartLogDebug { "bitmapToFloatArray: ${System.currentTimeMillis() - startMS}MS" }
            startMS = System.currentTimeMillis()
            // 构建一个空的输出结构
            //
            val outArray = Array(1) {
                Array(7) {
                    FloatArray(8400)
                }
            }
            // 运行解释器，input_arr是输入，它会将结果写到outArray中
            interpreter?.run(inputByteBuffer, outArray)
            smartLogDebug { "tflite.interpreter: ${System.currentTimeMillis() - startMS}MS" }
            startMS = System.currentTimeMillis()
            smartLogDebug { "outArray: $outArray" }

            // 取出(1, 7, 8400)中的(7, 8400)
            val matrix_2d = outArray[0]
            // (7, 8400)变为(8400, 7)
            val outputMatrix = Array(8400) {
                FloatArray(
                    7
                )
            }
            for (i in 0..8399) {
                for (j in 0..6) {
                    outputMatrix[i][j] = matrix_2d[j][i]
                }
            }
            smartLogDebug { "for-for: ${System.currentTimeMillis() - startMS}MS" }
            startMS = System.currentTimeMillis()
            val threshold = 0.6f // 类别准确率筛选

            val non_max = 0.8f // nms非极大值抑制

            val boxes = ArrayList<FloatArray>()
            val maxScores: ArrayList<Float> = ArrayList()
            for (detection in outputMatrix) {
                // 6位数中的后两位是两类的置信度
                val score = Arrays.copyOfRange(detection, 4, 7)
                var maxValue = score[0]
                var maxIndex = 0f
                for (i in 1 until score.size) {
                    if (score[i] > maxValue) { // 找出最大的一项
                        maxValue = score[i]
                        maxIndex = i.toFloat()
                    }
                }
                if (maxValue >= threshold) { // 如果置信度超过60%则记录
                    detection[4] = maxIndex
                    detection[5] = maxValue
                    boxes.add(detection) // 筛选后的框
                    maxScores.add(maxValue) // 筛选后的准确率
                }
            }
            smartLogDebug { "box-filter: ${System.currentTimeMillis() - startMS}MS" }
            startMS = System.currentTimeMillis()
            val nonMaxBoxes = NonMaxSuppression.nonMaxSuppression(boxes, maxScores, non_max)
            smartLogDebug { "nonMaxBoxes: ${System.currentTimeMillis() - startMS}MS" }
            startMS = System.currentTimeMillis()
            smartLogDebug { "boxes: ${boxes.size}, scores: ${maxScores.size}, nonMaxBoxes: ${nonMaxBoxes.size}" }
        }
    }

}