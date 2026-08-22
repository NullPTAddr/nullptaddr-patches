package app.morphe.patches.all.misc.microg

import app.morphe.patcher.PatcherConfig
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patches.all.misc.extension.sharedExtensionPatch
import app.morphe.util.adoptChild
import com.android.apksig.apk.ApkUtils
import com.android.apksig.util.DataSources
import org.w3c.dom.Element
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Base64

private const val V2_BLOCK_ID = 0x7109871a
private const val V3_BLOCK_ID = 0xf05368c0.toInt()

@Suppress("unused")
val microGSupportPatch = resourcePatch(
    name = "MicroG Support",
    description = "Making support MicroG",
    default = false
) {
    dependsOn(sharedExtensionPatch())

    execute {
        val signature = getPackageSignature(this)

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

private fun getPackageSignature(resourcePatchContext: ResourcePatchContext): String {
    try {
        val config = resourcePatchContext.javaClass.getDeclaredField("config").let {
            it.isAccessible = true
            it.get(resourcePatchContext) as PatcherConfig
        }

        val apkFile = config.javaClass.getDeclaredField("apkFile").let {
            it.isAccessible = true
            it.get(config) as File
        }

        val rawCertBytes =
            getCertificate(apkFile) ?: throw Exception("got null certificate")
        return Base64.getMimeEncoder().encodeToString(rawCertBytes).replace("\r", "")
    } catch (e: Throwable) {
        throw PatchException("Failed to get signature $e")
    }
}

fun getCertificate(apkFile: File): ByteArray? {
    return try {
        RandomAccessFile(apkFile, "r").use { raf ->
            val dataSource = DataSources.asDataSource(raf)
            val inputZipSections = ApkUtils.findZipSections(dataSource)
            val signingBlockInfo = ApkUtils.findApkSigningBlock(dataSource, inputZipSections)
            val contents = signingBlockInfo.contents // DataSource
            val size = contents.size()

            val pairsBuf = contents.getByteBuffer(8, (size - 8 - 24).toInt())
            pairsBuf.order(ByteOrder.LITTLE_ENDIAN)

            var v2Value: ByteBuffer? = null
            var v3Value: ByteBuffer? = null

            while (pairsBuf.remaining() >= 12) {
                val entryLen = pairsBuf.long
                if (entryLen < 4 || entryLen > pairsBuf.remaining()) {
                    break
                }
                val id = pairsBuf.int
                val valueLen = (entryLen - 4).toInt()

                when (id) {
                    V3_BLOCK_ID -> {
                        val value = ByteArray(valueLen)
                        pairsBuf.get(value)
                        v3Value = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN)
                    }
                    V2_BLOCK_ID -> {
                        val value = ByteArray(valueLen)
                        pairsBuf.get(value)
                        v2Value = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN)
                    }
                    else -> pairsBuf.position(pairsBuf.position() + valueLen)
                }
            }

            val chosen = v3Value ?: v2Value
            chosen?.let { extractFirstCert(it) }
        }
    } catch (e: Exception) {
        e.printStackTrace(); null
    }
}

private fun extractFirstCert(v2OrV3Block: ByteBuffer): ByteArray? {
    val signers = readSlice(v2OrV3Block) ?: return null
    val signer = readSlice(signers) ?: return null
    val signedData = readSlice(signer) ?: return null

    val digestsLen = readLen(signedData) ?: return null
    signedData.position(signedData.position() + digestsLen)

    val certs = readSlice(signedData) ?: return null
    val certLen = readLen(certs) ?: return null
    if (certLen > certs.remaining()) return null

    val cert = ByteArray(certLen)
    certs.get(cert)
    return cert
}

private fun readLen(buf: ByteBuffer): Int? {
    if (buf.remaining() < 4) return null
    val len = buf.int
    return if (len in 0..buf.remaining()) len else null
}

private fun readSlice(buf: ByteBuffer): ByteBuffer? {
    val len = readLen(buf) ?: return null
    val slice = buf.slice().order(ByteOrder.LITTLE_ENDIAN).limit(len) as ByteBuffer
    buf.position(buf.position() + len)
    return slice
}