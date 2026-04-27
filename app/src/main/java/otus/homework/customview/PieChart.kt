package otus.homework.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt

class PieChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    var onSliceClick: ((String) -> Unit)? = null
): View(context, attrs) {

    val Int.dp: Float
        get() = this * resources.displayMetrics.density

    val pieWith = 200.dp
    val pieHeight = 200.dp

    private var data: List<CategoryModel> = emptyList()
    private val sectors = mutableListOf<Sector>()

    fun setData(items: List<CategoryModel>) {
        data = items
        invalidate()
    }

    init{
        if (isInEditMode) {
            //setValues(listOf(1,2,3,4,5))
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        when (wMode) {
            MeasureSpec.EXACTLY -> {
                setMeasuredDimension(wSize, hSize)
            }
            MeasureSpec.AT_MOST -> {
                val newW = min(pieWith.toInt(), wSize)
                setMeasuredDimension(newW, hSize)
            }
            MeasureSpec.UNSPECIFIED -> {
                setMeasuredDimension(pieWith.toInt(), hSize)
            }
        }

        when (hMode) {
            MeasureSpec.EXACTLY -> {
                setMeasuredDimension(wSize, hSize)
            }
            MeasureSpec.AT_MOST -> {
                val newH = min(pieHeight.toInt(), wSize)
                setMeasuredDimension(wSize, newH)
            }
            MeasureSpec.UNSPECIFIED -> {
                setMeasuredDimension(wSize, pieHeight.toInt())
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    val paint = Paint().apply {
        style = Paint.Style.FILL
    }

    val rect = RectF(0f, 0f, pieWith, pieHeight)

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        sectors.clear()

        if(data.isEmpty()) return

        val total = data.sumOf { it.amount.toDouble() }.toFloat()
        var startAngle = 0f

        data.forEach {item ->
            paint.color = item.color
            val swipeAngle = (item.amount/total) * 360f

            canvas.drawArc(rect, startAngle, swipeAngle, true, paint)

            sectors.add(
                Sector(
                    category = item.category,
                    start = startAngle,
                    end = startAngle + swipeAngle
                )
            )

            startAngle += swipeAngle
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (event.action != MotionEvent.ACTION_DOWN) return true

        val dx = event.x - rect.centerX()
        val dy = event.y - rect.centerY()

        val distance = sqrt(dx * dx + dy * dy)

        if (distance > rect.width() / 2f) return false

        val angle = ((Math.toDegrees(
            atan2(dy.toDouble(), dx.toDouble())
        ) + 360) % 360).toFloat()

        sectors.forEach { sector ->
            if (angle in sector.start..sector.end) {
                onSliceClick?.invoke(sector.category)
                return true
            }
        }
        return true
    }
}