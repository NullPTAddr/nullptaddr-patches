package app.morphe.patches.dudu.music

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.dudu.shared.Constants
import app.morphe.patches.dudu.extension.sharedExtensionPatch
import app.morphe.patches.dudu.signature.spoofSignaturePatch

private val musicTitleFingerprint = Fingerprint(
    custom = { method, classDef ->
        classDef.equals("Lcom/dudu/autoui/service/musicInfo/GetMusicInfoService\$b;")
    },
    parameters = listOf("Landroid/media/MediaMetadata;"),
    strings = listOf("android.media.metadata.TITLE")
)

private val musicLyricLoadingFingerprint = Fingerprint(
    custom = { method, classDef ->
        classDef.equals("Lcom/dudu/autoui/manage/music/LrcUtil;")
    },
    strings = listOf("SDATA_MUSIC_LRC_LOCAL_PATH")
)

private const val EXTENSION_MUSIC_CLASS = "Lapp/morphe/extension/dudu/pathes/music/MusicName;"
private const val EXTENSION_LYRIC_FINDER_CLASS = "Lapp/morphe/extension/dudu/pathes/music/LyricFinder;"


@Suppress("unused")
val musicInfoPatch = bytecodePatch(
    "Music Lyric",
    "search music lyric and auto download to selected folder"
) {
    compatibleWith(Constants.compatibility)
    dependsOn(spoofSignaturePatch)
    execute {
        musicTitleFingerprint.method.addInstructions(
            0, """
            invoke-static {p1}, $EXTENSION_MUSIC_CLASS->musicInfoRename(Landroid/media/MediaMetadata;)Landroid/media/MediaMetadata;
            move-result-object p1
        """.trimIndent()
        )

        musicLyricLoadingFingerprint.method.addInstructions(
            0, """
            invoke-static {}, $EXTENSION_LYRIC_FINDER_CLASS->waitForLyric()V
    """.trimIndent()
        )
    }
}