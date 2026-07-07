package app.morphe.patches.dudu.misc.accessibility

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.dudu.shared.Constants
import app.morphe.patches.dudu.signature.spoofSignaturePatch
import app.morphe.util.indexOfFirstLiteralInstruction

private val restartServiceFingerprint = Fingerprint(
    strings = listOf(":com.dudu.autoui/com.dudu.autoui.service.DuduAccessibilityService")
)

private const val EXTENSION_CLASS =
    "Lapp/morphe/extension/dudu/patches/accessibility/AutoRestartPatch;"

@Suppress("unused")
val fixAccessibilityServicePatch = bytecodePatch(
    name = "Fix Accessibility Service",
    description = "Auto restart accessibility service else restart app"
) {
    compatibleWith(Constants.compatibility)
    dependsOn(spoofSignaturePatch)

    execute {
        restartServiceFingerprint.classDef.methods.forEach { method ->
            if (method.indexOfFirstLiteralInstruction(2000L) != -1) {
                method.addInstructions(
                    0,
                    """
                    invoke-static {}, $EXTENSION_CLASS->restartService()Z
                    move-result v0
                    if-eqz v0, :do-original-restart
                    return-void
                    :do-original-restart
                """.trimIndent()
                )
            } else if (method.parameters.isEmpty()) {
                method.addInstructions(
                    0,
                    """
                    invoke-static {}, $EXTENSION_CLASS->stopService()Z
                    move-result v0
                    if-eqz v0, :do-original-stop
                    return-void
                    :do-original-stop
                """.trimIndent()
                )
            }
        }
    }
}