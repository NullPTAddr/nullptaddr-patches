package app.morphe.patches.dudu.weatherinfo


import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.dudu.shared.Constants
import app.morphe.patches.dudu.signature.spoofSignaturePatch
import app.morphe.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private val urlGeneratorFingerprint = Fingerprint(
    strings = listOf(
        "%3A",
        ":",
        "%2F",
        "/",
        "%3F",
        "?",
        "%3D",
        "=",
        "%26",
        "&",
        "url: ",
        "cookie",
        "token"
    )
)

private val urlGeneratorFingerprint2 = Fingerprint(
    strings = listOf(
        "key: ", " value: ", "cookie", "token", "url: ", "param: "
    )
)


private val weatherPullingFingerprint = Fingerprint(
    strings = listOf(
        "ZDATA_LAST_WEATHER_AD_CODE",
        "ZDATA_LAST_WEATHER_DISTRICT",
        "ZDATA_LAST_WEATHER_CITY"
    )
)

private val moduleDrivingServiceFingerprint = Fingerprint(
    custom = { method, classDef ->
        classDef.endsWith("service/ModuleDrivingService;")
    }
)

@Suppress("unused")
val fixWeatherInfoPatch = bytecodePatch(
    name = "Fix Weather",
    description = "Fix weather info"
) {
    compatibleWith(Constants.compatibility)
    dependsOn(spoofSignaturePatch)

    execute {
        val replaceUrlInstruction = """
                const-string v0, "https://app.dudu-lucky.com/api/app/weather/info"
                invoke-virtual {p0, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                move-result v0
                if-eqz v0, :do-original
                const-string p0, "https://abroad-app-ea1.dudu-auto.com/api/app/weather/info"
                :do-original
            """.trimIndent()

        urlGeneratorFingerprint.method.addInstructions(
            0,
            replaceUrlInstruction
        )

        urlGeneratorFingerprint2.method.addInstructions(
            0,
            replaceUrlInstruction
        )

        val matcheds = mutableMapOf<String, Int>()

        weatherPullingFingerprint.classDef.methods.forEach { method ->
            method.instructions.forEach { instruction ->
                val ref = instruction.getReference<MethodReference>() ?: return@forEach

                if (
                    instruction.opcode == Opcode.INVOKE_STATIC &&
                    ref.returnType == "Z" &&
                    ref.parameterTypes.isEmpty()
                ) {
                    val key = "${ref.definingClass}->${ref.name}"
                    matcheds[key] = matcheds.getOrDefault(key, 0) + 1
                }
            }
        }

        val targetMethodKey = matcheds
            .maxByOrNull { it.value }
            ?.key
            ?: return@execute

        weatherPullingFingerprint.classDef.methods.forEach { method ->
            method.instructions.forEachIndexed { index, instruction ->
                val ref = instruction.getReference<MethodReference>() ?: return@forEachIndexed
                val key = "${ref.definingClass}->${ref.name}"

                if (
                    instruction.opcode == Opcode.INVOKE_STATIC &&
                    ref.returnType == "Z" &&
                    ref.parameterTypes.isEmpty() &&
                    key == targetMethodKey
                ) {
                    val register = method
                        .getInstruction<OneRegisterInstruction>(index + 1)
                        .registerA

                    method.addInstruction(
                        index + 2,
                        "const v$register, 0x0"
                    )
                }
            }
        }

        moduleDrivingServiceFingerprint.classDef.methods.forEach { method ->
            method.instructions.forEachIndexed { index, instruction ->
                val ref = instruction.getReference<MethodReference>() ?: return@forEachIndexed
                if (
                    instruction.opcode == Opcode.INVOKE_STATIC &&
                    ref.returnType == "Z" &&
                    ref.parameterTypes.isEmpty()
                ) {
                    val register = method
                        .getInstruction<OneRegisterInstruction>(index + 1)
                        .registerA

                    method.addInstruction(
                        index + 2,
                        "const v$register, 0x0"
                    )
                }
            }
        }
    }
}