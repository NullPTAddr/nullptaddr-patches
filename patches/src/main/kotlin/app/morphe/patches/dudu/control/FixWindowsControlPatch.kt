package app.morphe.patches.dudu.control

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.dudu.shared.Constants
import app.morphe.patches.dudu.signature.spoofSignaturePatch
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference


private val keywords = listOf(
    "BODYWORK_LF_WINDOW_CTRL_SET",
    "BODYWORK_LR_WINDOW_CTRL_SET",
    "BODYWORK_RF_WINDOW_CTRL_SET",
    "BODYWORK_RR_WINDOW_CTRL_SET"
)
private val windowsControlFingerprint = Fingerprint(
    parameters = listOf("[I", "[I"),
    returnType = "Z",
    custom = { method, _ ->
        val fields = method.instructions
            .filterIsInstance<ReferenceInstruction>()
            .mapNotNull { it.reference as? FieldReference }
            .map { it.name }
            .toSet()

        keywords.all(fields::contains)
    }
)

@Suppress("unused")
val fixWindowsControlPatch = bytecodePatch(
    name = "Fix Windows Control",
    description = "Fix windows position control"
) {
    compatibleWith(Constants.compatibility)
    dependsOn(spoofSignaturePatch)
    execute {
        windowsControlFingerprint.method.addInstructions(
            0,
            """
                nop
            """.trimIndent()
        )
    }
}