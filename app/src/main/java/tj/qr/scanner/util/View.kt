package tj.qr.scanner.util

import android.content.Context
import android.content.res.Configuration

fun Context.isPortrait() : Boolean {
   val orientation = this.resources.configuration.orientation
   return orientation == Configuration.ORIENTATION_PORTRAIT
}
