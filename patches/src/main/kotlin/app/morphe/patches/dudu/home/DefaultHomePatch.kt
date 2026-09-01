package app.morphe.patches.dudu.home

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.dudu.shared.Constants
import app.morphe.patches.dudu.signature.spoofSignaturePatch

val applicationInit = Fingerprint(
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

val setDefaultHomePatch = bytecodePatch(
    name = "setDefaultHome",
    description = "Set default home",
) {
    compatibleWith(Constants.compatibility)
    dependsOn(spoofSignaturePatch)

    execute {
        applicationInit.methodOrNull?.apply {
            addInstruction(
                0,
                """
                    invoke-static {}, Lapp/morphe/extension/dudu/patches/home/DefaultHome;->setDefaultHome()V
                """.trimIndent()
            )
        }
    }
}
