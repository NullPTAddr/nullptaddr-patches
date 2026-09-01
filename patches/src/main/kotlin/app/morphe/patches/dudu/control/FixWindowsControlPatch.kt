package app.morphe.patches.dudu.control

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.dudu.shared.Constants
import app.morphe.patches.dudu.signature.spoofSignaturePatch
import com.android.tools.smali.dexlib2.iface.instruction.formats.ArrayPayload


private val windowsControlFingerprint = Fingerprint(
    strings = listOf("BydOperation:", "SDATA_BYD_ALLOW_OPEN_WIN")
)

@Suppress("unused")
val fixWindowsControlPatch = bytecodePatch(
    name = "Fix Windows Control",
    description = "Fix windows position control"
) {
    compatibleWith(Constants.compatibility)
    dependsOn(spoofSignaturePatch)
    execute {
        windowsControlFingerprint.classDef.methods.apply {
            forEach method@{ mutableMethod ->
                mutableMethod.instructions.forEachIndexed { index, builderInstruction ->
                    when (builderInstruction) {
                        is ArrayPayload -> {
                            val arraySet = builderInstruction.arrayElements.toSet()
                            if (arraySet == setOf(1, 4)) {
                                mutableMethod.replaceInstruction(
                                    index,
                                    """
                                        .array-data 4
                                                0x1
                                                0x2
                                        .end array-data
                                    """.trimIndent()
                                )
                            } else if (arraySet == setOf(2, 3)) {
                                mutableMethod.replaceInstruction(
                                    index,
                                    """
                                        .array-data 4
                                                0x3
                                                0x4
                                        .end array-data
                                    """.trimIndent()
                                )
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}