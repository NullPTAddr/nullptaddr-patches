package app.morphe.patches.maps.shared

import app.morphe.patcher.patch.Compatibility

internal object Constants {
    const val PACKAGE_NAME = "com.google.android.apps.maps"
    val compatibility = Compatibility(
        name = "Google Maps",
        packageName = PACKAGE_NAME
    )
}