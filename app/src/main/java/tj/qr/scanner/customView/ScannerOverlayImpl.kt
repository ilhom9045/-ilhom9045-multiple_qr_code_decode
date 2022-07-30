package tj.qr.scanner.customView

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.Size
import android.view.View
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import tj.qr.scanner.callback.QrCenterChangedListener
import tj.qr.scanner.R
import tj.qr.scanner.callback.ScannerOverlay
import tj.qr.scanner.util.*
import kotlin.math.min

@SuppressLint("ObsoleteSdkInt")
@UiThread
class ScannerOverlayImpl @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), ScannerOverlay,
    QrCenterChangedListener {

    private val transparentPaint: Paint by lazy {
        Paint().apply {
            setLayerType(LAYER_TYPE_SOFTWARE, null)
            isAntiAlias = true
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
    }

    private var rectf: List<RectF>? = null

    private var color = Color.WHITE

    private var bitmap: Bitmap? = null

    init {
        setWillNotDraw(false)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            setLayerType(LAYER_TYPE_HARDWARE, null)
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(ContextCompat.getColor(context, R.color.qr_custom_view_background))
        if (bitmap == null) {
            createBitmap()
        }
        val rectF: List<RectF> = rectf ?: ArrayList<RectF>().apply { add(scanRect) }
        rectF.forEach {
            val width = it.width()
            val height = it.height()
            val left = it.left
            val top = it.top
            val pdHeight = pxToDp(height)
            val radius = dpToPx(pdHeight * 0.1195f).toFloat()
            canvas.drawRoundRect(it, radius, radius, transparentPaint)

            bitmap?.let { bm ->
                val newResizedBitmap =
                    bm.resize(width.toInt() + 10, height.toInt() + 10)
                newResizedBitmap?.let { it1 ->
                    canvas.drawBitmap(
                        it1.changeBitmapColor(color),
                        left - 5,
                        top - 5,
                        null
                    )
                }
            }
        }
    }

    private fun createBitmap() {
        val drawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_qr_stroke_corner)
        bitmap = drawable?.toBitmapOrNull(config = Bitmap.Config.ARGB_8888)
    }

    override val size: Size
        get() = Size(width, height)

    override val scanRect: RectF
        get() = if (context.isPortrait()) {
            val size = min(width * 0.72f, MAX_WIDTH_PORTRAIT)
            val l = (width - size) / 2
            val r = width - l
            val t = height * 0.25f
            val b = t + size
            RectF(l, t, r, b)
        } else {
            val size = min(width * 0.25f, MAX_WIDTH_LANDSCAPE)
            val l = width * 0.05f
            val r = l + size
            val t = height * 0.05f
            val b = t + size
            RectF(l, t, r, b)
        }

    @UiThread
    override fun onCenterChanged(rectFs: List<RectF>) {
        val scanRect = scanRect
        val newRectFs = ArrayList<RectF>(rectFs.size)
        rectFs.forEach { item ->
            scanRect.let {
                val newRect =
                    RectF(
                        it.left + item.left,
                        it.top + item.top,
                        it.right - item.right,
                        it.bottom - item.bottom
                    )
                if (newRect.height() > 0 && newRect.width() > 0) {
                    val tenPercent = newRect.width() * 0.1
                    val min = newRect.width() - tenPercent
                    val max = newRect.width() + tenPercent
                    if (newRect.height() in min..max) newRectFs.add(newRect)
                }
            }
        }
        if (newRectFs.isNotEmpty()) {
            rectf = newRectFs
            invalidate()
        }
    }

    private companion object {
        const val MAX_WIDTH_PORTRAIT = 1200f
        const val MAX_WIDTH_LANDSCAPE = 1600f
    }
}
