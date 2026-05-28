package org.fossify.gallery.dialogs

import android.app.Activity
import androidx.exifinterface.media.ExifInterface
import org.fossify.commons.dialogs.PropertiesDialog
import org.fossify.commons.extensions.isImageFast
import org.fossify.commons.extensions.isRawFast

class MediaPropertiesDialog(
    activity: Activity,
    path: String,
    countHiddenItems: Boolean = false
) {
    init {
        val dialog = PropertiesDialog(activity, path, countHiddenItems)
        addGpsLocation(dialog, path)
    }

    private fun addGpsLocation(dialog: PropertiesDialog, path: String) {
        if (!path.isImageFast() && !path.isRawFast()) {
            return
        }

        val gpsLocation = getGpsLocation(path)
        if (gpsLocation.isEmpty()) {
            return
        }

        runCatching {
            val addProperty = dialog.javaClass.superclass.getDeclaredMethod(
                "addProperty",
                Int::class.javaPrimitiveType,
                String::class.java,
                Int::class.javaPrimitiveType
            )
            addProperty.isAccessible = true
            addProperty.invoke(dialog, org.fossify.commons.R.string.gps_coordinates, gpsLocation, 0)
        }
    }

    private fun getGpsLocation(path: String): String {
        val exif = try {
            ExifInterface(path)
        } catch (_: Exception) {
            return ""
        }

        val latLon = FloatArray(2)
        if (!exif.getLatLong(latLon)) {
            return ""
        }

        val location = StringBuilder("${latLon[0]}, ${latLon[1]}")
        val altitude = exif.getAltitude(0.0)
        if (altitude != 0.0) {
            location.append(", ${altitude}m")
        }

        return location.toString()
    }
}
