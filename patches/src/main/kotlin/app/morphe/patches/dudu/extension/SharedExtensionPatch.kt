package app.morphe.patches.dudu.extension

import app.morphe.patcher.Fingerprint
import app.morphe.patches.all.misc.extension.ExtensionHook
import app.morphe.patches.all.misc.extension.sharedExtensionPatch


internal val applicationInitHook = ExtensionHook(
    Fingerprint(
        custom = { method, classDef ->
            val superClass = classDef.superclass
            if (superClass != null && superClass.equals("Landroid/app/Application;", true)) {
                if (method.name == "onCreate") {
                    return@Fingerprint true
                }
            }
            false
        }
    )
)

val sharedExtensionPatch = sharedExtensionPatch(listOf("dudu"), applicationInitHook)