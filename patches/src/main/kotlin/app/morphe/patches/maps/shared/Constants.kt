package app.morphe.patches.maps.shared

import app.morphe.patcher.patch.Compatibility

internal object Constants {
    const val PACKAGE_NAME = "com.google.android.apps.maps"
    val compatibility = Compatibility(
        packageName = PACKAGE_NAME
    )
}