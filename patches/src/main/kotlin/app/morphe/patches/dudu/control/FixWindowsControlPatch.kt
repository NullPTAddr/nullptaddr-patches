package app.morphe.patches.dudu.control

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.dudu.Constans


private val keywords = listOf(
    "BODYWORK_LF_WINDOW_CTRL_SET",
    "BODYWORK_RF_WINDOW_CTRL_SET",
    "BODYWORK_LR_WINDOW_CTRL_SET",
    "BODYWORK_RR_WINDOW_CTRL_SET"
)
private val windowsControlFingerprint = Fingerprint (
    parameters = listOf("[I", "[I"),
    returnType = "Z",
    custom = { method, classDef ->
        method.instructions.any { ins ->
            keywords.all { key -> ins.toString().contains(key) }
        }
    }
)

@Suppress("unused")
val fixWindowsControlPatch = bytecodePatch(
    name = "Fix Windows Control"
) {
    compatibleWith(Constans.compatibility)
    execute {
        windowsControlFingerprint.method.addInstructions(
            0,
            """
                
            """.trimIndent()
        )
    }
}