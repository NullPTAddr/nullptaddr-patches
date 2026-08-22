package app.morphe.patches.dudu.home

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.dudu.shared.Constants
import app.morphe.patches.dudu.signature.spoofSignaturePatch

val bootReceiverFingerprint = Fingerprint(
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "Landroid/content/Intent;"),
    name = "onReceive",
    definingClass = "Lcom/dudu/autoui/receiver/BootReceiver;"
)


val setDefaultHomePatch = bytecodePatch(
    name = "setDefaultHome",
    description = "Set default home",
) {
    compatibleWith(Constants.compatibility)
    dependsOn(spoofSignaturePatch)

    execute {
        bootReceiverFingerprint.methodOrNull?.apply {
            addInstruction(
                0,
                """
                    invoke-static {}, Lapp/morphe/extension/dudu/patches/home/DefaultHome;->setDefaultHome()V
                """.trimIndent()
            )
        }
    }
}
