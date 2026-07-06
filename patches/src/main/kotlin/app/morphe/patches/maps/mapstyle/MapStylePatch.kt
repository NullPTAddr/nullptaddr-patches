package app.morphe.patches.maps.mapstyle

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.dudu.Constans

private val mapStyleFingerprint = Fingerprint(
    strings = listOf("SATELLITE_HYBRID", "ROADMAP", "NAVIGATION")
)


val mapStylePatch = bytecodePatch(name = "Maps Style", default = true) {
    compatibleWith(Constans.compatibility)
    extendWith("extensions/maps.rve")


    execute {
        val className = mapStyleFingerprint.classDef.type
        val method = mapStyleFingerprint.classDef.methods.firstOrNull {
            it.parameterTypes.singleOrNull() == "Z"
        } ?: return@execute // throw PatchException("Not found method to patch")

        method.addInstructions(
            0, """
            invoke-static {p0}, Lapp/revanced/extension/maps/patches/MapStylePatches;->replaceMapStyle(Ljava/lang/Enum;)Ljava/lang/Enum;
            move-result-object p0
            check-cast p0, $className
            """.trimIndent()
        )
    }
}