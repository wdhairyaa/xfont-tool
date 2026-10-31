package com.xfont.app.engine

import android.content.Context
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FilterOutputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object FontApkBuilder {

    private const val TEMPLATE_ASSET_PATH = "templates/font_template.apk"
    private const val FONT_TARGET_PATH = "assets/fonts/zawgyitai.ttf"
    private const val XML_TARGET_PATH = "assets/xml/zawgyitai.xml"

    private class CountingOutputStream(out: OutputStream) : FilterOutputStream(out) {
        var count: Long = 0
            private set

        override fun write(b: Int) {
            out.write(b)
            count++
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            out.write(b, off, len)
            count += len.toLong()
        }
    }

    private fun patchResourcesArsc(original: ByteArray, appLabel: String): ByteArray {
        val copy = original.copyOf()
        val fixedLen = 36
        val formatted = appLabel.take(fixedLen).padEnd(fixedLen, ' ')
        val labelBytes = formatted.toByteArray(Charsets.UTF_8)
        if (copy.size > 0x32 + fixedLen) {
            System.arraycopy(labelBytes, 0, copy, 0x32, fixedLen)
        }
        return copy
    }

    fun buildAndSignFontApk(
        context: Context,
        rawFontBytes: ByteArray,
        fontDisplayName: String,
        axisValues: Map<String, Float> = emptyMap()
    ): File {
        val outputDir = File(context.cacheDir, "generated_apks")
        if (!outputDir.exists()) outputDir.mkdirs()

        val templateTempFile = File(outputDir, "template_cache.apk")
        val baseName = fontDisplayName.substringBeforeLast(".").ifEmpty { "font" }
        val safeName = baseName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
        val unsignedApkFile = File(outputDir, "temp_unsigned.apk")
        val signedApkFile = File(outputDir, "${safeName}-xFont.apk")

        if (unsignedApkFile.exists()) unsignedApkFile.delete()
        if (signedApkFile.exists()) signedApkFile.delete()

        // 1. If axes provided, physically bake coordinates into glyph outlines and strip variable tables via official FontTools
        val fontPayload = if (axisValues.isNotEmpty()) {
            ChaquopyFontInstancer.instantiate(context, rawFontBytes, axisValues)
        } else {
            rawFontBytes
        }

        // 2. Extract template APK to temp file so ZipFile can read uncompressed headers and CRCs
        context.assets.open(TEMPLATE_ASSET_PATH).use { input ->
            templateTempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val countingOut = CountingOutputStream(BufferedOutputStream(FileOutputStream(unsignedApkFile)))
        val zipOut = ZipOutputStream(countingOut)

        ZipFile(templateTempFile).use { zipFile ->
            val entries = zipFile.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val name = entry.name

                // Skip existing signatures and target assets to replace
                if (name.startsWith("META-INF/") || name == FONT_TARGET_PATH || name == XML_TARGET_PATH) {
                    continue
                }

                var data: ByteArray = zipFile.getInputStream(entry).use { it.readBytes() }
                if (name == "resources.arsc") {
                    data = patchResourcesArsc(data, "${safeName}-xFont")
                }

                val newEntry = ZipEntry(name)
                val nameBytes = name.toByteArray(Charsets.UTF_8)

                // Preserve STORED mode and enforce 4-byte alignment on resources.arsc and drawables
                if (entry.method == ZipEntry.STORED || name == "resources.arsc") {
                    newEntry.method = ZipEntry.STORED
                    newEntry.size = data.size.toLong()
                    newEntry.compressedSize = data.size.toLong()
                    val crc = java.util.zip.CRC32()
                    crc.update(data)
                    newEntry.crc = crc.value

                    val headerOffset = countingOut.count
                    val dataOffsetWithoutPadding = headerOffset + 30 + nameBytes.size
                    val padding = ((4 - (dataOffsetWithoutPadding % 4)) % 4).toInt()
                    if (padding > 0) {
                        newEntry.extra = ByteArray(padding)
                    }
                } else {
                    newEntry.method = ZipEntry.DEFLATED
                }

                zipOut.putNextEntry(newEntry)
                zipOut.write(data)
                zipOut.closeEntry()
            }

            // Write user font payload into assets/fonts/zawgyitai.ttf
            val fontEntry = ZipEntry(FONT_TARGET_PATH)
            fontEntry.method = ZipEntry.DEFLATED
            zipOut.putNextEntry(fontEntry)
            zipOut.write(fontPayload)
            zipOut.closeEntry()

            // Write font.xml with variation settings
            val xmlEntry = ZipEntry(XML_TARGET_PATH)
            xmlEntry.method = ZipEntry.DEFLATED
            zipOut.putNextEntry(xmlEntry)

            val variationSettingStr = if (axisValues.isNotEmpty()) {
                axisValues.entries.joinToString(", ") { (tag, value) ->
                    "'$tag' ${String.format(java.util.Locale.US, "%.1f", value)}"
                }
            } else ""

            val axisTagsXml = if (axisValues.isNotEmpty()) {
                axisValues.entries.joinToString("\n                            ") { (tag, value) ->
                    """<axis tag="$tag" stylevalue="${String.format(java.util.Locale.US, "%.1f", value)}" />"""
                }
            } else ""

            val varAttr = if (variationSettingStr.isNotEmpty()) {
                """ variationSettings="$variationSettingStr" fontVariationSettings="$variationSettingStr""""
            } else ""

            val xmlContent = """
                <?xml version="1.0" encoding="utf-8"?>
                <font displayname="zawgyitai">
                    <sans>
                        <file$varAttr>
                            <filename>zawgyitai.ttf</filename>
                            <droidname>DroidSans.ttf</droidname>
                            $axisTagsXml
                        </file>
                        <file$varAttr>
                            <filename>zawgyitai.ttf</filename>
                            <droidname>DroidSans-Bold.ttf</droidname>
                            $axisTagsXml
                        </file>
                    </sans>
                </font>
            """.trimIndent().toByteArray(Charsets.UTF_8)
            zipOut.write(xmlContent)
            zipOut.closeEntry()
        }

        zipOut.finish()
        zipOut.close()

        // 3. Sign the APK using v1 + v2 + v3 schemes
        ApkSignerUtil.signApk(context, unsignedApkFile, signedApkFile)

        // 4. Save a copy to Android/media/com.xfont.app/Exported_APKs/
        try {
            val exportDir = com.xfont.app.manager.FontFileManager(context).getExportedApksDirectory()
            val exportedFile = File(exportDir, "${safeName}-xFont.apk")
            signedApkFile.copyTo(exportedFile, overwrite = true)
        } catch (_: Exception) {}

        // 5. Clean up intermediates
        if (unsignedApkFile.exists()) unsignedApkFile.delete()
        if (templateTempFile.exists()) templateTempFile.delete()

        return signedApkFile
    }
}
