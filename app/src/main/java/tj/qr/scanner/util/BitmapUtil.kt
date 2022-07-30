package tj.qr.scanner.util

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.graphics.drawable.toBitmap
import tj.qr.scanner.analyze.BaseAnalyser
import java.io.ByteArrayOutputStream

object BitmapUtil {
    fun getBitmap(data: ByteArray, metadata: BaseAnalyser.FrameMetadata): Bitmap {

        val image = YuvImage(
            data, ImageFormat.NV21, metadata.width, metadata.height, null
        )
        val stream = ByteArrayOutputStream()
        image.compressToJpeg(
            Rect(0, 0, metadata.width, metadata.height),
            80,
            stream
        )
        val bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())
        stream.close()
        return rotateBitmap(bmp, metadata.rotation, flipX = false, flipY = false)
    }

    private fun rotateBitmap(
        bitmap: Bitmap, rotationDegrees: Int, flipX: Boolean, flipY: Boolean
    ): Bitmap {
        val matrix = Matrix()

        // Rotate the image back to straight.
        matrix.postRotate(rotationDegrees.toFloat())

        // Mirror the image along the X or Y axis.
        matrix.postScale(if (flipX) -1.0f else 1.0f, if (flipY) -1.0f else 1.0f)
        val rotatedBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        // Recycle the old bitmap if it has changed.
        if (rotatedBitmap != bitmap) {
            bitmap.recycle()
        }
        return rotatedBitmap
    }
}

fun Bitmap.resize(newWidth: Int, newHeight: Int): Bitmap? {
    val scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
    val ratioX = newWidth / width.toFloat()
    val ratioY = newHeight / height.toFloat()
    val middleX = newWidth / 2.0f
    val middleY = newHeight / 2.0f
    val scaleMatrix = Matrix()
    scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
    val canvas = Canvas(scaledBitmap)
    canvas.setMatrix(scaleMatrix)
    canvas.drawBitmap(this, middleX - width / 2, middleY - height / 2, Paint(Paint.FILTER_BITMAP_FLAG))
    return scaledBitmap
}

fun Bitmap.changeBitmapColor(@ColorInt color: Int): Bitmap {
    val resultBitmap = copy(config, true)
    val paint = Paint()
    val filter: ColorFilter = LightingColorFilter(color, 1)
    paint.colorFilter = filter
    val canvas = Canvas(resultBitmap)
    canvas.drawBitmap(resultBitmap, 0f, 0f, paint)
    return resultBitmap
}

fun Drawable.toBitmapOrNull(
    @Px width: Int = intrinsicWidth,
    @Px height: Int = intrinsicHeight,
    config: Bitmap.Config? = null
): Bitmap? {
    if (this is BitmapDrawable && bitmap == null) {
        return null
    }
    return toBitmap(width, height, config)
}