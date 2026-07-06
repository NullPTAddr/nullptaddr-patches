package app.morphe.patches.dudu.navbar

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.dudu.Constans
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction31i

private val stateBarStillShowOnKeybaordFingerprint = Fingerprint (
    parameters = listOf("Landroid/content/Context;"),
    returnType = "V",
    strings = listOf("navBarHeight:")
)
private val statusBarStillShowOnKeybaordFingerprint = Fingerprint (
    parameters = listOf("Lcom/dudu/autoui/service/DuduAccessibilityService;"),
    returnType = "V",
    strings = listOf("winparams.height:")
)

@Suppress("unused")
val stateBarStillShowOnKeybaordPatch = bytecodePatch(
    "fix Navbar"
) {
    compatibleWith(Constans.compatibility)
    execute {
        statusBarStillShowOnKeybaordFingerprint.method.apply {
            instructions.forEachIndexed { index, builderInstruction ->
                if (builderInstruction !is Instruction31i) return@forEachIndexed
                if (builderInstruction.narrowLiteral == 0x20528) {
                    val register = getInstruction<OneRegisterInstruction>(index).registerA
                    replaceInstruction(
                        index, """
                            const v$register, 0x10528
                        """.trimIndent()
                    )
                    return@apply
                }
            }
        }
        stateBarStillShowOnKeybaordFingerprint.method.apply {
            instructions.forEachIndexed { index, builderInstruction ->
                if (builderInstruction !is Instruction31i) return@forEachIndexed
                if (builderInstruction.narrowLiteral == 0x20528) {
                    val register = getInstruction<OneRegisterInstruction>(index).registerA
                    replaceInstruction(
                        index, """
                            const v$register, 0x10528
                        """.trimIndent()
                    )
                    return@apply
                }
            }
        }
    }
}