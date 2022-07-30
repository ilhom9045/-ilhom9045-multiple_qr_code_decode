package tj.qr.scanner.analyze

import android.graphics.Bitmap
import tj.qr.scanner.callback.QRCodeFoundListener
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.qrcode.QRCodeMultiReader
import java.util.*


class DecodeQrCodeImpl(private val listener: QRCodeFoundListener?) : DecodeQrCode {

    private val multiQRCodeReader = QRCodeMultiReader()
    private val hints = Hashtable<DecodeHintType, Any>()

    private val decodeFormats = Vector<BarcodeFormat>().apply {
        add(BarcodeFormat.QR_CODE)
    }

    init {
        hints[DecodeHintType.POSSIBLE_FORMATS] = decodeFormats
    }

    override fun decode(byteArray: ByteArray, width: Int, height: Int) {
        val source = PlanarYUVLuminanceSource(
            byteArray, width, height, 0, 0, width, height, false
        )
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        try {
            val result: Array<Result> = multiQRCodeReader.decodeMultiple(binaryBitmap, hints)
            if (result.isNotEmpty()) {
                if (result.size == 1) {
                    listener?.onQRCodeFound(result[0])
                } else {
                    listener?.onManyQRCodeFound(result)
                }
            }
        } catch (e: FormatException) {
            listener?.qrCodeNotFound()
        } catch (e: ChecksumException) {
            listener?.qrCodeNotFound()
        } catch (e: NotFoundException) {
            listener?.qrCodeNotFound()
        } finally {
            listener?.qrCodeNotFound()
        }
    }

    override fun decode(bitmap: Bitmap) {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val source = RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        try {
            val result: Array<Result> = multiQRCodeReader.decodeMultiple(binaryBitmap, hints)
            if (result.isNotEmpty()) {
                if (result.size == 1) {
                    listener?.onQRCodeFound(result[0])
                } else {
                    listener?.onManyQRCodeFound(result)
                }
            } else {
                listener?.qrCodeNotFound()
            }
        } catch (e: FormatException) {
            e.printStackTrace()
            listener?.qrCodeNotFound()
        } catch (e: ChecksumException) {
            e.printStackTrace()
            listener?.qrCodeNotFound()
        } catch (e: NotFoundException) {
            e.printStackTrace()
            listener?.qrCodeNotFound()
        }
    }
}