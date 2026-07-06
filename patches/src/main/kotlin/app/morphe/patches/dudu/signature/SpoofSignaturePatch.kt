package app.morphe.patches.dudu.signature

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.fingerprint
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.dudu.Constans
import app.morphe.patches.dudu.extension.sharedExtensionPatch

private val applicationFingerprint = fingerprint {
    Fingerprint(
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
}

@Suppress("unused")
val spoofSignaturePatch = bytecodePatch(
    name = "Spoof Signature"
) {
    compatibleWith(Constans.compatibility)
    dependsOn(sharedExtensionPatch)
    execute {
        applicationFingerprint.method.addInstructions(0, "invoke-static {}, Lapp/revanced/extension/dudulauncher/signature/SignaturePatch;->killPM()V")
    }
}