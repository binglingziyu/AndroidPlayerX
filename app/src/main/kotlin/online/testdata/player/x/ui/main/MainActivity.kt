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
            val resizeBitmap = resizeBitmap(bitmap, 640)
            smartLogDebug { "resize: ${System.currentTimeMillis() - startMS}MS" }
            startMS = System.currentTimeMillis()
            binding.dnfImage.setImageBitmap(resizeBitmap)
            // 转换为输入层(1, 640, 640, 3)结构的float数组
            //val inputArr: Array<Array<Array<FloatArray>>> = bitmapToFloatArray(resizeBitmap)
            smartLogDebug { "bitmap: ${bitmap.width},${bitmap.height}" }
            val inputByteBuffer = convertBitmapToByteBuffer(resizeBitmap)
            smartLogDebug { "bitmapToFloatArray: ${System.currentTimeMillis() - startMS}MS" }
            startMS = System.currentTimeMillis()
            // 构建一个空的输出结构
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

            // 取出(1, 6, 8400)中的(6, 8400)
            val matrix_2d = outArray[0]
            // (6, 8400)变为(8400, 6)
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

    fun resizeBitmap(source: Bitmap, maxSize: Int): Bitmap {
        val outWidth: Int
        val outHeight: Int
        val inWidth = source.width
        val inHeight = source.height
        if (inWidth > inHeight) {
            outWidth = maxSize
            outHeight = inHeight * maxSize / inWidth
        } else {
            outHeight = maxSize
            outWidth = inWidth * maxSize / inHeight
        }
        val resizedBitmap = Bitmap.createScaledBitmap(source, outWidth, outHeight, false)
        val outputImage = Bitmap.createBitmap(maxSize, maxSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputImage)
        canvas.drawColor(Color.WHITE)
        val left = (maxSize - outWidth) / 2
        val top = (maxSize - outHeight) / 2
        canvas.drawBitmap(resizedBitmap, left.toFloat(), top.toFloat(), null)
        return outputImage
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val FLOAT_TYPE_SIZE = 4
        val PIXEL_SIZE = 3
        val modelInputSize = FLOAT_TYPE_SIZE * bitmap.width * bitmap.height * PIXEL_SIZE
        val byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixelValue in pixels) {
            val r = (pixelValue shr 16 and 0xFF)
            val g = (pixelValue shr 8 and 0xFF)
            val b = (pixelValue and 0xFF)

            // Convert RGB to grayscale and normalize pixel value to [0..1].
//            val normalizedPixelValue = (r + g + b) / 3.0f / 255.0f
//            byteBuffer.putFloat(normalizedPixelValue)
            byteBuffer.putFloat(r / 255.0f)
            byteBuffer.putFloat(g / 255.0f)
            byteBuffer.putFloat(b / 255.0f)
        }

        return byteBuffer
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