package online.testdata.player.tf

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


object DetectTool {
    // 从Assets下加载.tflite文件
    @Throws(IOException::class)
    private fun loadModelFile(context: Context, fileName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(fileName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // 构建Interpreter，这是tflite文件的解释器
    fun getInterpreter(context: Context): Interpreter? {
        val options = Interpreter.Options()
        options.setNumThreads(4)
        var interpreter: Interpreter? = null
        interpreter = try {
            Interpreter(loadModelFile(context, "best_int8.tflite"), options)
        } catch (e: IOException) {
            throw RuntimeException("Error loading model file.", e)
        }
        return interpreter
    }

}