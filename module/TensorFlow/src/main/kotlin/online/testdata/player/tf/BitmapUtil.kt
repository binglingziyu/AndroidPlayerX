package online.testdata.player.tf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 *
 * [Bitmap的一些操作](https://www.cnblogs.com/GnagWang/archive/2011/06/28/2091980.html)
 */
object BitmapUtil {

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


    /**
     * Bitmap to ByteBuffer
     * https://github.com/tensorflow/examples/blob/01f9529732557913d188992b63efc44cd73d3452/lite/codelabs/digit_classifier/android/finish/app/src/main/java/org/tensorflow/lite/codelabs/digitclassifier/DigitClassifier.kt#L138C20-L138C20
     */
    public fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val FLOAT_TYPE_SIZE = 4
        val PIXEL_SIZE = 3 // R G B
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