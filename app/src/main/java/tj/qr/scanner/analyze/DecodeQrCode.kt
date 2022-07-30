package tj.qr.scanner.analyze

import android.graphics.Bitmap

interface DecodeQrCode {

    fun decode(byteArray: ByteArray, width: Int, height: Int)

    fun decode(bitmap: Bitmap)
}