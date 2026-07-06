package app.morphe.patches.dudu.pip

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.dudu.shared.Constants
import app.morphe.patches.dudu.signature.spoofSignaturePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.ArrayPayload
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21s

private val pipDipLevelFingerprint = Fingerprint(
    parameters = listOf("Ljava/lang/Integer;"),
    strings = listOf("SDATA_PIP_DPI_LEVEL")
)

@Suppress("unused")
val setLowestPipAsHighestPatch = bytecodePatch(
    "Lowest Pip as Highest",
    description = "Replace the lowest pip dpi level with the highest"
) {
    compatibleWith(Constants.compatibility)
    dependsOn(spoofSignaturePatch)
    execute {
        pipDipLevelFingerprint.classDef.methods.apply {
            forEach method@{ mutableMethod ->
                mutableMethod.instructions.forEachIndexed { index, builderInstruction ->
                    when (builderInstruction) {
                        is Instruction21s -> {
                            if (builderInstruction.narrowLiteral == 0x90) {
                                val register = mutableMethod.getInstruction<OneRegisterInstruction>(index).registerA
                                mutableMethod.replaceInstruction(
                                    index, """
                                    const/16 v$register, 0x140
                                """.trimIndent()
                                )
                            }
                        }

                        is ArrayPayload -> {
                            mutableMethod.replaceInstruction(
                                index, """
                                .array-data 4
                                        0x140
                                        0xb0
                                        0xd0
                                        0xf0
                                    .end array-data
                            """.trimIndent()
                            )
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}