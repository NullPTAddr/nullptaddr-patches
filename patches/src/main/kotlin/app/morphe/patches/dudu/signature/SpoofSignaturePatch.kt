package app.morphe.patches.dudu.signature

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.dudu.shared.Constants
import app.morphe.patches.dudu.extension.sharedExtensionPatch

private val applicationFingerprint = Fingerprint(
    custom = { method, classDef ->
        val superClass = classDef.superclass
        if (superClass != null && superClass.equals("Landroid/app/Application;", true)) {
            if (method.name.equals("<init>", true)) {
                return@Fingerprint true
            }
        }
        false
    }
)

private const val EXTENSION_CLASS = "Lapp/morphe/extension/dudu/patches/spoof/SpoofSignature;"

@Suppress("unused")
internal val spoofSignaturePatch = bytecodePatch(
    name = "Spoof Signature",
    description = "Spoof signature"
) {
    compatibleWith(Constants.compatibility)
    dependsOn(sharedExtensionPatch)
    execute {
        applicationFingerprint.method.addInstructions(
            0,
            "invoke-static {}, $EXTENSION_CLASS->killPM()V"
        )
    }
}