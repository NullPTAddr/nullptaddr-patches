package app.morphe.patches.dudu.shared

import app.morphe.patcher.patch.Compatibility

internal object Constants {
    const val PACKAGE_NAME = "com.dudu.autoui"
    val compatibility = Compatibility(
        name = "Dudu Launcher",
        packageName = PACKAGE_NAME
    )
}