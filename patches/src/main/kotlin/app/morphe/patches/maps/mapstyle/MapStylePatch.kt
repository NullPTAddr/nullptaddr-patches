package app.morphe.patches.maps.mapstyle

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.all.misc.extension.sharedExtensionPatch
import app.morphe.patches.maps.shared.Constants
import app.morphe.util.indexOfFirstLiteralInstructionOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.WideLiteralInstruction

private val mapAutomotiveStyleFingerprint = Fingerprint(
    strings = listOf("SATELLITE_HYBRID", "ROADMAP", "NAVIGATION")
)

private val mapStyleFingerprint = Fingerprint(
    strings = listOf("CAR_CLUSTER", "CAR_LIMITED", "CAR_MAIN", "PHONE_MAIN", "UNSPECIFIED")
)

private val carModeFlagsFingerprint = Fingerprint(
    strings = listOf("shouldHideMyMapsOverlays", "useCarNavStyles", "mapMode")
)

private const val EXTENSION_CLASS = "Lapp/morphe/extension/maps/patches/MapStylePatches;"

@Suppress("unused")
val mapAutomotiveStylePatch = bytecodePatch(
    name = "Maps more POIs",
    description = "Make maps always show POIs even in navigation mode"
) {
    compatibleWith(Constants.compatibility)
    dependsOn(
        sharedExtensionPatch(
            listOf("maps"),
        )
    )

    execute {
        mapAutomotiveStyleFingerprint.matchOrNull()?.classDef?.methods?.firstOrNull {
            it.parameterTypes.singleOrNull() == "Z"
        }?.apply {
            val className = mapAutomotiveStyleFingerprint.classDef.type

            addInstructions(
                0,
                """
                    invoke-static {p0}, $EXTENSION_CLASS->replaceMapStyle(Ljava/lang/Enum;)Ljava/lang/Enum;
                    move-result-object p0
                    check-cast p0, $className
                """.trimIndent()
            )
        }

        carModeFlagsFingerprint.matchOrNull()?.classDef?.methods?.firstOrNull {
            AccessFlags.CONSTRUCTOR.isSet(it.accessFlags) &&
                    AccessFlags.PUBLIC.isSet(it.accessFlags) &&
                    !AccessFlags.STATIC.isSet(it.accessFlags) &&
                    it.parameters.isNotEmpty()
        }?.apply {
            val useCarNavStylesFlagIndex = indexOfFirstLiteralInstructionOrThrow(0x02000000)
            val useCarNavStylesFlagRegister =
                getInstruction<OneRegisterInstruction>(useCarNavStylesFlagIndex).registerA

            val shouldHideMyMapsOverlaysFlagIndex =
                indexOfFirstLiteralInstructionOrThrow(0x00020000)
            val shouldHideMyMapsOverlaysFlagRegister =
                getInstruction<OneRegisterInstruction>(shouldHideMyMapsOverlaysFlagIndex).registerA

            replaceInstruction(
                useCarNavStylesFlagIndex,
                """
                    const/high16 v$useCarNavStylesFlagRegister, 0xF000000
                """.trimIndent()
            )
            replaceInstruction(
                shouldHideMyMapsOverlaysFlagIndex,
                """
                    const/high16 v$shouldHideMyMapsOverlaysFlagRegister, 0xF0000
                """.trimIndent()
            )
        }

        mapStyleFingerprint.matchOrNull()?.classDef?.methods?.firstOrNull { method ->
            if (method.returnType != "I" && method.parameters.size != 1) return@firstOrNull false

            val literals = method.instructions
                .mapNotNull { ins ->
                    (ins as? WideLiteralInstruction)?.wideLiteral
                }
            literals.isNotEmpty() &&
                    literals.all { it == 3L || it == 4L } &&
                    literals.containsAll(listOf(3L, 4L))
        }?.apply {
            val mapDriveStyleIndex = indexOfFirstLiteralInstructionOrThrow(4L)

            replaceInstruction(
                mapDriveStyleIndex,
                """
                    const/4 p0, 0x3
                """.trimIndent()
            )
        }
    }
}