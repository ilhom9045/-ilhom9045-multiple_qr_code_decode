package tj.qr.scanner.callback

import android.graphics.RectF

interface QrCenterChangedListener {

    fun onCenterChanged(rectFs: List<RectF>)

}