package tj.qr.scanner.callback

import android.graphics.RectF
import android.util.Size

interface ScannerOverlay {

    val size: Size

    val scanRect: RectF

}