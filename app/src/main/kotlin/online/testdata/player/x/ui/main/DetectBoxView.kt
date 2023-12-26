package online.testdata.player.x.ui.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class DetectBoxView : View {

    constructor(context: Context): super(context)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs, 0)

    var boxes = listOf<FloatArray>()
        set(value) {
            field = value
            invalidate()
        }

    private var boxPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        boxPaint.color = Color.RED
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = 4F
    }

    override fun onDraw(canvas: Canvas) {
        boxes.forEach { box ->
            //中心点x, 中心点y, 宽度w, 高度h,
            canvas.drawRect(boxToRect(box), boxPaint)
        }
    }

    private fun boxToRect(array: FloatArray): RectF {
        val centerX = array[0]
        val centerY = array[1]
        val boxWidth = array[2]
        val boxHeight = array[3]
        return RectF(
            width * (centerX - boxWidth / 2),
            height * (centerY - boxHeight / 2),
            width * (centerX + boxWidth / 2),
            height * (centerY + boxHeight / 2),
        )
    }

}