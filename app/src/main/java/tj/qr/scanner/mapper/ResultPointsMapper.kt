package tj.qr.scanner.mapper

import android.graphics.RectF
import com.google.zxing.ResultPoint

class ResultPointsMapper : Mapper<Array<ResultPoint>, RectF> {

    override fun map(value: Array<ResultPoint>): RectF {
        val left = value[1].x
        val top = value[1].y
        val right = value[2].x
        val bottom = value[0].y
        return RectF(left, top, right, bottom)
    }

}