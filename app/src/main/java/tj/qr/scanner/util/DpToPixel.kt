package tj.qr.scanner.util

import android.content.res.Resources
import kotlin.math.roundToInt

fun dpToPx(dp: Float): Int {
    return (dp * Resources.getSystem().displayMetrics.density).roundToInt()
}

fun pxToDp(px: Float): Int {
    return (px / Resources.getSystem().displayMetrics.density).roundToInt()
}
