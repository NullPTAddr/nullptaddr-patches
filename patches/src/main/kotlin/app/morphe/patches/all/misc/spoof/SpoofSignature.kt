package app.morphe.patches.all.misc.spoof

import app.morphe.patcher.patch.Option
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import app.morphe.util.adoptChild
import com.android.apksig.apk.ApkUtils
import com.android.apksig.internal.apk.v2.V2SchemeVerifier
import com.android.apksig.util.DataSources
import com.android.apksig.util.RunnablesExecutor
import org.w3c.dom.Element
import java.io.File
import java.io.RandomAccessFile
import java.util.Base64

private lateinit var originalFilePathOption: Option<String>

@Suppress("unused")
val spoofSignature = resourcePatch(
    name = "Spoof App Signature",
    description = "Spoof the signature of the app and add GmsCore MetaData to the AndroidManifest.xml https://github.com/kangrio/AuroraStore-BYD",
    default = false
) {
    originalFilePathOption = stringOption(
        key = "filePath",
        default = "Default",
        values = mapOf("Default" to "Default"),
        title = "APK Path",
        description = "The path of the APK to spoof the signature of",
        required = true,
    ) {
        it != default
    }

    execute {
        val filePath = originalFilePathOption.value ?: run {
            throw PatchException("No file path provided")
        }
        if (filePath == originalFilePathOption.default) {
            throw PatchException("No file path provided")
            return@execute
        }
        if (!filePath.endsWith(".apk")) {
            throw PatchException("File is not an APK")
        }

        val file = File(filePath)
        if (!file.exists()) {
            throw PatchException("File does not exist")
        }

        val signature = getPackageSignature(filePath)

        document("AndroidManifest.xml").use { document ->
            val applicationNode =
                document
                    .getElementsByTagName("application")
                    .item(0) as Element

            applicationNode.setAttribute(
                "android:appComponentFactory",
                "app.morphe.extension.shared.spoof.SpoofAppComponentFactory"
            )

            applicationNode.adoptChild("meta-data") {
                setAttribute("android:name", "org.microg.gms.spoofed_certificates")
                setAttribute("android:value", signature)
            }
        }
    }
}

internal fun getPackageSignature(apkPath: String): String {
    val dataSource = DataSources.asDataSource(RandomAccessFile(apkPath, "r"))
    val zipSections = ApkUtils.findZipSections(dataSource)
    val v2 = V2SchemeVerifier.verify(
        RunnablesExecutor.SINGLE_THREADED,
        dataSource,
        zipSections,
        mapOf(2 to "APK Signature Scheme v2"),
        hashSetOf(2),
        24,
        Int.MAX_VALUE
    )
    val signatureByte = v2.signers[0].certs[0].encoded ?: run {
        throw PatchException("No signature found of APK $apkPath")
    }
//    val signatureString = signatureByte.toHexString()
    val signatureString = Base64.getMimeEncoder().encodeToString(signatureByte)
    return signatureString
}