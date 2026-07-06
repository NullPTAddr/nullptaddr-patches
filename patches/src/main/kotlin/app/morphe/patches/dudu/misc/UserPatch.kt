package app.morphe.patches.dudu.misc

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.dudu.Constans

private val setVipTypeFingerprint = Fingerprint(
    parameters = listOf("Ljava/lang/Integer;"),
    custom = { method, classDef ->
        classDef.type == "Lcom/dudu/autoui/user/LocalUser;" && method.name == "setVipType"
    }
)

private val setVipExpireTimeFingerprint = Fingerprint(
    parameters = listOf("Ljava/lang/Integer;"),
    custom = { method, classDef ->
        classDef.type == "Lcom/dudu/autoui/user/LocalUser;" && method.name == "setVipExpireTime"
    }
)


@Suppress("unused")
val vipPatch = bytecodePatch(
    name = "Vip Patch"
) {
    compatibleWith(Constans.compatibility)
    execute {
        setVipTypeFingerprint.method.addInstructions(
            0, """
            const p1, 0x1
            invoke-static {p1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
            move-result-object p1
        """.trimIndent()
        )

        setVipExpireTimeFingerprint.method.addInstructions(
            0, """
            const p1, 0x7fffffff
            invoke-static {p1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
            move-result-object p1
        """.trimIndent()
        )
    }
}