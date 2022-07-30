package tj.qr.scanner.analyze

import android.graphics.*
import tj.qr.scanner.callback.ScannerOverlay

class BarCodeAndQRCodeAnalyser(
    scannerOverlay: ScannerOverlay,
    private val decodeQrCode: DecodeQrCode
) : BaseAnalyser(scannerOverlay) {

    override fun onBitmapPrepared(bitmap: Bitmap) {
        decodeQrCode.decode(bitmap)
    }
}