package app.morphe.patches.dudu.music

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.dudu.Constans
import app.morphe.patches.dudu.extension.sharedExtensionPatch
import kotlin.collections.listOf

private val musicTitleFingerprint = Fingerprint (
    custom = { method, classDef ->
        classDef.equals("Lcom/dudu/autoui/service/musicInfo/GetMusicInfoService\$b;")
    },
    parameters = listOf("Landroid/media/MediaMetadata;"),
    strings = listOf("android.media.metadata.TITLE")
)

private val musicLyricLoadingFingerprint = Fingerprint (
    custom = { method, classDef ->
        classDef.equals("Lcom/dudu/autoui/manage/music/LrcUtil;")
    },
    strings = listOf("SDATA_MUSIC_LRC_LOCAL_PATH")
)


@Suppress("unused")
val musicInfoPatch = bytecodePatch(
    "Music Lyric",
    "search music lyric and auto download to selected folder"
) {
    compatibleWith(Constans.compatibility)
    dependsOn(sharedExtensionPatch)
    execute {
        musicTitleFingerprint.method.addInstructions(
            0, """
            invoke-static {p1}, Lapp/revanced/extension/dudulauncher/music/MusicName;->musicInfoRename(Landroid/media/MediaMetadata;)Landroid/media/MediaMetadata;
            move-result-object p1
        """.trimIndent()
        )

        musicLyricLoadingFingerprint.method.addInstructions(
            0, """
            invoke-static {}, Lapp/revanced/extension/dudulauncher/music/LyricFinder;->waitForLyric()V
    """.trimIndent()
        )
    }
}