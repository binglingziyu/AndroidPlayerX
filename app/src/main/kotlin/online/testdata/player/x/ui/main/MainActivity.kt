package online.testdata.player.x.ui.main

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.hjq.window.EasyWindow
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
import online.testdata.player.x.R
import online.testdata.player.x.databinding.ActivityMainBinding
import org.tensorflow.lite.Interpreter
import java.util.Arrays


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val interpreter: Interpreter? by lazy { DetectTool.getInterpreter(this) }
    private val easyWindow by lazy { EasyWindow.with(application) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()

        easyWindow
            .setContentView(R.layout.floating_window)
            .setAnimStyle(-1)
            .setImageDrawable(R.id.floating_window_icon, R.mipmap.ic_launcher)
            .setWindowFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_FULLSCREEN)
            .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN)
            .show()
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
        var working = false
        binding.buttonScreenShare.setOnClickListener {
            startScreenShare { bitmap ->
                if(working) {
                    smartLogDebug { "ScreenShare-Skip" }
                    return@startScreenShare
                }
                working = true
                var startMS = System.currentTimeMillis()

                val resizeBitmap = BitmapUtil.resizeBitmap(bitmap, 640)
                runOnUiThread {
                    //binding.dnfImage.setImageBitmap(bitmap)
                }
                val inputByteBuffer = BitmapUtil.convertBitmapToByteBuffer(resizeBitmap)
                // 构建一个空的输出结构
                val outArray = Array(1) {
                    Array(7) {
                        FloatArray(8400)
                    }
                }
                // 运行解释器，input_arr是输入，它会将结果写到outArray中
                interpreter?.run(inputByteBuffer, outArray)
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
                val nonMaxBoxes = NonMaxSuppression.nonMaxSuppression(boxes, maxScores, non_max)
                smartLogDebug { "ScreenShare: ${System.currentTimeMillis() - startMS}MS" }
                smartLogDebug { "ScreenShare: ${boxes.size}, scores: ${maxScores.size}, nonMaxBoxes: ${nonMaxBoxes.size}" }

                easyWindow.findViewById<DetectBoxView>(R.id.box_view).boxes = adjustToRealBox(nonMaxBoxes)
                working = false
            }
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
//            smartLogDebug { "bitmap: ${bitmap.width},${bitmap.height}" }
//            val inputByteBuffer = BitmapUtil.convertBitmapToByteBuffer(resizeBitmap)
//            smartLogDebug { "bitmapToFloatArray: ${System.currentTimeMillis() - startMS}MS" }
//            startMS = System.currentTimeMillis()
//            // 构建一个空的输出结构
//            //
//            val outArray = Array(1) {
//                Array(7) {
//                    FloatArray(8400)
//                }
//            }
//            // 运行解释器，input_arr是输入，它会将结果写到outArray中
//            interpreter?.run(inputByteBuffer, outArray)
//            smartLogDebug { "tflite.interpreter: ${System.currentTimeMillis() - startMS}MS" }
//            startMS = System.currentTimeMillis()
//            smartLogDebug { "outArray: $outArray" }
//
//            // 取出(1, 7, 8400)中的(7, 8400)
//            val matrix_2d = outArray[0]
//            // (7, 8400)变为(8400, 7)
//            val outputMatrix = Array(8400) {
//                FloatArray(
//                    7
//                )
//            }
//            for (i in 0..8399) {
//                for (j in 0..6) {
//                    outputMatrix[i][j] = matrix_2d[j][i]
//                }
//            }
//            smartLogDebug { "for-for: ${System.currentTimeMillis() - startMS}MS" }
//            startMS = System.currentTimeMillis()
//            val threshold = 0.6f // 类别准确率筛选
//
//            val non_max = 0.8f // nms非极大值抑制
//
//            val boxes = ArrayList<FloatArray>()
//            val maxScores: ArrayList<Float> = ArrayList()
//            for (detection in outputMatrix) {
//                // 6位数中的后两位是两类的置信度
//                val score = Arrays.copyOfRange(detection, 4, 7)
//                var maxValue = score[0]
//                var maxIndex = 0f
//                for (i in 1 until score.size) {
//                    if (score[i] > maxValue) { // 找出最大的一项
//                        maxValue = score[i]
//                        maxIndex = i.toFloat()
//                    }
//                }
//                if (maxValue >= threshold) { // 如果置信度超过60%则记录
//                    detection[4] = maxIndex
//                    detection[5] = maxValue
//                    boxes.add(detection) // 筛选后的框
//                    maxScores.add(maxValue) // 筛选后的准确率
//                }
//            }
//            smartLogDebug { "box-filter: ${System.currentTimeMillis() - startMS}MS" }
//            startMS = System.currentTimeMillis()
//            val nonMaxBoxes = NonMaxSuppression.nonMaxSuppression(boxes, maxScores, non_max)
//            smartLogDebug { "nonMaxBoxes: ${System.currentTimeMillis() - startMS}MS" }
//            startMS = System.currentTimeMillis()
//            smartLogDebug { "boxes: ${boxes.size}, scores: ${maxScores.size}, nonMaxBoxes: ${nonMaxBoxes.size}" }
//            binding.boxView.boxes = nonMaxBoxes
        }

        binding.buttonFloatingWindow.setOnClickListener {
            easyWindow
                .setContentView(R.layout.floating_window)
                .setAnimStyle(-1)
                .setImageDrawable(R.id.floating_window_icon, R.mipmap.ic_launcher)
                .setWindowFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_FULLSCREEN)
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN)
                .show()
        }
    }

    private fun adjustToRealBox(list: List<FloatArray>): List<FloatArray> {
        val resources = this.resources
        val dm = resources.displayMetrics
        val screenWidth = dm.widthPixels
        val screenHeight = dm.heightPixels
        val orientation = if(screenWidth < screenHeight) {
            0 // 竖屏
        } else {
            1 // 横屏
        }
        return list.map { item ->
            if(orientation == 0) {
                val ratio = screenHeight / 640.0
                val ratioWidth = screenWidth / ratio
                val difRatio = 640.0 / ratioWidth
                val singleWidth = (640 - ratioWidth) / 2

                val mutableList = item.toMutableList()
                // 中心点x, 中心点y, 宽度w, 高度h,
                mutableList[0] = ((640 * item[0] - singleWidth)/ratioWidth).toFloat()
                mutableList[2] = (mutableList[2] * difRatio).toFloat()
                mutableList.toFloatArray()
            } else {
                val ratio = screenWidth / 640.0
                val ratioHeight = screenHeight / ratio
                val difRatio = 640.0 / ratioHeight
                val singleHeight = (640 - ratioHeight) / 2

                val mutableList = item.toMutableList()
                // 中心点x, 中心点y, 宽度w, 高度h,
                mutableList[1] = ((640 * item[1] - singleHeight)/ratioHeight).toFloat()
                mutableList[3] = (mutableList[3] * difRatio).toFloat()
                mutableList.toFloatArray()
            }
        }
    }

}