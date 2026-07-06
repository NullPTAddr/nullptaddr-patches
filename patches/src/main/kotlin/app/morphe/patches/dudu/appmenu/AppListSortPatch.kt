package app.morphe.patches.dudu.appmenu

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.dudu.shared.Constants
import app.morphe.patches.dudu.signature.spoofSignaturePatch
import app.morphe.util.getReference
import com.android.tools.smali.dexlib2.iface.reference.Reference

private val appListSortingFingerprint = Fingerprint(
    parameters = listOf("Ljava/lang/String;", "Ljava/lang/String;"),
    strings = listOf("com.dudu", "com.wow"),
    returnType = "I",
    custom = { method, classDef ->
        method.parameters.size == 2 && classDef.superclass == "Lcom/dudu/autoui/manage/ContextEx;"
    }
)

private val appListSortingFingerprint2 = Fingerprint(
    strings = listOf("com.dudu.setting", "com.dudu.action.restart_auto"),
    returnType = "I",
    custom = { method, classDef ->
        method.parameters.size == 2 && classDef.superclass == "Lcom/dudu/autoui/manage/ContextEx;"
    }
)

@Suppress("unused")
val sortingAppMenuPatch = bytecodePatch(
    name = "Sort App",
    description = "Sort app menu by app name"
) {
    compatibleWith(Constants.compatibility)
    dependsOn(spoofSignaturePatch)
    execute {
        appListSortingFingerprint.method.apply {
            instructions.forEachIndexed { index, builderInstruction ->
                if (builderInstruction.getReference<Reference>()
                        .toString() == "Ljava/lang/String;->compareTo(Ljava/lang/String;)I"
                ) {
                    replaceInstruction(
                        index, """
                        invoke-virtual {p1, p2}, Ljava/lang/String;->compareTo(Ljava/lang/String;)I
                    """.trimIndent()
                    )
                }
            }
        }

        val size = appListSortingFingerprint2.method.instructions.size
        appListSortingFingerprint2.method.apply {
            replaceInstruction(size - 5, "nop")
            replaceInstruction(size - 4, "nop")
            val params = parameters[0].toString()
            val fieldName = getAllClassesWithStrings().find { it.type == params }
                ?.fields?.firstOrNull { it.type == "Ljava/lang/CharSequence;" }
                ?.name.orEmpty()
            addInstructions(
                size - 3, """
                iget-object p1, p1, $params->$fieldName:Ljava/lang/CharSequence;
                invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
                move-result-object p1
                
                iget-object p2, p2, $params->$fieldName:Ljava/lang/CharSequence;
                invoke-virtual {p2}, Ljava/lang/Object;->toString()Ljava/lang/String;
                move-result-object p2
            """.trimIndent()
            )
        }
    }
}