package app.morphe.patches.maps.mapstyle

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.all.misc.extension.sharedExtensionPatch
import app.morphe.patches.maps.shared.Constants

private val mapStyleFingerprint = Fingerprint(
    strings = listOf("SATELLITE_HYBRID", "ROADMAP", "NAVIGATION")
)

private const val EXTENSION_CLASS = "Lapp/morphe/extension/maps/patches/MapStylePatches;"

@Suppress("unused")
val mapStylePatch = bytecodePatch(
    name = "Maps automotive more POIs",
    description = "Make maps automotive always show POIs"
) {
    compatibleWith(Constants.compatibility)
    dependsOn(
        sharedExtensionPatch(
            listOf("maps"),
        )
    )

    execute {
        val className = mapStyleFingerprint.classDef.type
        val method = mapStyleFingerprint.classDef.methods.firstOrNull {
            it.parameterTypes.singleOrNull() == "Z"
        } ?: return@execute // throw PatchException("Not found method to patch")

        method.addInstructions(
            0, """
            invoke-static {p0}, $EXTENSION_CLASS->replaceMapStyle(Ljava/lang/Enum;)Ljava/lang/Enum;
            move-result-object p0
            check-cast p0, $className
            """.trimIndent()
        )
    }
}