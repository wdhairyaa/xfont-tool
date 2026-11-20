package com.xfont.app.manager

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.LruCache
import androidx.compose.ui.text.font.FontFamily
import androidx.documentfile.provider.DocumentFile
import com.xfont.app.model.FontItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class FontFileManager(private val context: Context) {

    private val typefaceCache = LruCache<String, Typeface>(1024)
    private val fontFamilyCache = LruCache<String, FontFamily>(1024)
    private val dirCache = java.util.concurrent.ConcurrentHashMap<String, List<FontItem>>()

    fun getCachedFontFamily(key: String): FontFamily? {
        return fontFamilyCache.get(key)
    }

    suspend fun loadFontFamilyAsync(item: FontItem): FontFamily? = withContext(Dispatchers.IO) {
        if (item.isDirectory) return@withContext null
        val key = item.file?.absolutePath ?: item.uri.toString()
        val cached = fontFamilyCache.get(key)
        if (cached != null) return@withContext cached

        val diskFile = item.file ?: getCachedFontFile(item.uri, item.name)
        if (diskFile != null && diskFile.exists() && diskFile.length() > 0) {
            try {
                val tf = Typeface.createFromFile(diskFile)
                if (tf != null) {
                    val ff = FontFamily(tf)
                    typefaceCache.put(key, tf)
                    fontFamilyCache.put(key, ff)
                    return@withContext ff
                }
            } catch (_: Exception) {}
        }
        return@withContext null
    }

    suspend fun prewarmFonts(items: List<FontItem>) = withContext(Dispatchers.IO) {
        for (item in items) {
            if (!item.isDirectory) {
                loadFontFamilyAsync(item)
            }
        }
    }

    fun invalidateCache(dir: File? = null) {
        if (dir != null) {
            dirCache.remove(dir.absolutePath)
        } else {
            dirCache.clear()
        }
    }

    /**
     * Primary font storage directory: Android/media/com.xfont.app/fonts
     * Fully accessible to users and external file managers without Scoped Storage restrictions.
     */
    @Suppress("DEPRECATION")
    fun getRootFontsDirectory(): File {
        val mediaDirs = context.externalMediaDirs
        val baseDir = if (mediaDirs.isNotEmpty() && mediaDirs[0] != null) {
            mediaDirs[0]
        } else {
            File(Environment.getExternalStorageDirectory(), "Android/media/${context.packageName}")
        }
        val fontsDir = File(baseDir, "fonts")
        if (!fontsDir.exists()) {
            fontsDir.mkdirs()
        }
        return fontsDir
    }

    fun getExportedApksDirectory(): File {
        val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
        val apksDir = File(baseDir, "Exported_APKs")
        if (!apksDir.exists()) {
            apksDir.mkdirs()
        }
        return apksDir
    }

    /**
     * Lists files synchronously with permanent in-memory caching.
     */
    fun listFilesInDirSync(dir: File, forceRefresh: Boolean = false): List<FontItem> {
        if (!forceRefresh) {
            dirCache[dir.absolutePath]?.let { return it }
        }
        if (!dir.exists()) dir.mkdirs()
        val items = mutableListOf<FontItem>()
        val files = dir.listFiles() ?: return emptyList()

        for (file in files) {
            val name = file.name
            if (name.startsWith(".")) continue

            if (file.isDirectory) {
                items.add(
                    FontItem(
                        uri = Uri.fromFile(file),
                        name = name,
                        isDirectory = true,
                        sizeBytes = 0L,
                        lastModified = file.lastModified(),
                        file = file
                    )
                )
            } else {
                val lower = name.lowercase()
                if (lower.endsWith(".ttf") || lower.endsWith(".otf")) {
                    items.add(
                        FontItem(
                            uri = Uri.fromFile(file),
                            name = name,
                            isDirectory = false,
                            sizeBytes = file.length(),
                            lastModified = file.lastModified(),
                            file = file
                        )
                    )
                }
            }
        }

        items.sortWith(
            compareBy<FontItem> { !it.isDirectory }
                .thenBy { it.name.lowercase() }
        )
        dirCache[dir.absolutePath] = items
        return items
    }

    /**
     * Lists files directly from filesystem with instant response and permanent in-memory caching.
     */
    suspend fun listFilesInDir(dir: File, forceRefresh: Boolean = false): List<FontItem> = withContext(Dispatchers.IO) {
        listFilesInDirSync(dir, forceRefresh)
    }

    /**
     * Recursive search across all subdirectories inside the root fonts directory.
     * Computes relative subfolder path for each result.
     */
    suspend fun searchAllFonts(query: String): List<FontItem> = withContext(Dispatchers.IO) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return@withContext emptyList()

        val root = getRootFontsDirectory()
        val results = mutableListOf<FontItem>()

        root.walkTopDown().forEach { file ->
            if (file.name.startsWith(".")) return@forEach
            val name = file.name
            val lower = name.lowercase()

            val isFont = file.isFile && (lower.endsWith(".ttf") || lower.endsWith(".otf"))
            val isDir = file.isDirectory && file != root

            if ((isFont || isDir) && lower.contains(q)) {
                val relParent = file.parentFile?.relativeToOrNull(root)?.path ?: ""
                val breadcrumbPath = if (relParent.isEmpty()) {
                    "Fonts"
                } else {
                    "Fonts › " + relParent.replace(File.separator, " › ")
                }

                results.add(
                    FontItem(
                        uri = Uri.fromFile(file),
                        name = name,
                        isDirectory = isDir,
                        sizeBytes = if (isFont) file.length() else 0L,
                        lastModified = file.lastModified(),
                        file = file,
                        subfolderPath = breadcrumbPath
                    )
                )
            }
        }

        results.sortWith(
            compareBy<FontItem> { !it.isDirectory }
                .thenBy { it.name.lowercase() }
        )
        return@withContext results
    }

    /**
     * Imports a picked font file (Uri) into the target directory on disk.
     */
    suspend fun addFontFromUri(sourceUri: Uri, targetDir: File): Boolean = withContext(Dispatchers.IO) {
        try {
            var fileName = "font_${System.currentTimeMillis()}.ttf"
            context.contentResolver.query(sourceUri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    val name = cursor.getString(nameIndex)
                    if (!name.isNullOrBlank()) fileName = name
                }
            }

            var targetFile = File(targetDir, fileName)
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (targetFile.exists() && targetFile.length() >= 4) {
                // Inspect sfnt magic bytes to guarantee proper .otf / .ttf extension
                val header = ByteArray(4)
                targetFile.inputStream().use { it.read(header) }
                val isOtf = header[0] == 'O'.code.toByte() && header[1] == 'T'.code.toByte() &&
                            header[2] == 'T'.code.toByte() && header[3] == 'O'.code.toByte()
                val lower = targetFile.name.lowercase()
                if (isOtf && !lower.endsWith(".otf")) {
                    val correctedName = targetFile.nameWithoutExtension + ".otf"
                    val correctedFile = File(targetDir, correctedName)
                    if (targetFile.renameTo(correctedFile)) {
                        targetFile = correctedFile
                    }
                } else if (!isOtf && !lower.endsWith(".ttf") && !lower.endsWith(".otf")) {
                    val correctedName = targetFile.nameWithoutExtension + ".ttf"
                    val correctedFile = File(targetDir, correctedName)
                    if (targetFile.renameTo(correctedFile)) {
                        targetFile = correctedFile
                    }
                }
            }
            val success = targetFile.exists() && targetFile.length() > 0
            if (success) invalidateCache(targetDir)
            return@withContext success
        } catch (_: Exception) {
            return@withContext false
        }
    }

    /**
     * Imports a folder and its font files recursively from an SAF OpenDocumentTree Uri.
     */
    suspend fun addFolderFromTreeUri(treeUri: Uri, targetDir: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val documentFile = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext false
            val folderName = documentFile.name ?: "Imported_Folder"
            val newDir = File(targetDir, folderName).apply { mkdirs() }
            copyDocumentFolderToDisk(documentFile, newDir)
            invalidateCache(targetDir)
            return@withContext true
        } catch (_: Exception) {
            return@withContext false
        }
    }

    private fun copyDocumentFolderToDisk(docDir: DocumentFile, diskDir: File) {
        docDir.listFiles().forEach { child ->
            val childName = child.name ?: return@forEach
            if (child.isDirectory) {
                val subDir = File(diskDir, childName).apply { mkdirs() }
                copyDocumentFolderToDisk(child, subDir)
            } else {
                val lower = childName.lowercase()
                if (lower.endsWith(".ttf") || lower.endsWith(".otf")) {
                    val targetFile = File(diskDir, childName)
                    try {
                        context.contentResolver.openInputStream(child.uri)?.use { input ->
                            FileOutputStream(targetFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }

    suspend fun createFolder(parentDir: File, folderName: String): Boolean = withContext(Dispatchers.IO) {
        val newDir = File(parentDir, folderName)
        val created = newDir.mkdirs() || newDir.exists()
        if (created) invalidateCache(parentDir)
        return@withContext created
    }

    suspend fun deleteItem(item: FontItem): Boolean = withContext(Dispatchers.IO) {
        val file = item.file ?: return@withContext false
        val parent = file.parentFile
        val deleted = file.deleteRecursively()
        val key = item.uri.toString()
        typefaceCache.remove(key)
        fontFamilyCache.remove(key)
        if (deleted && parent != null) invalidateCache(parent)
        return@withContext deleted
    }

    suspend fun renameItem(item: FontItem, newName: String): Boolean = withContext(Dispatchers.IO) {
        val file = item.file ?: return@withContext false
        val parent = file.parentFile
        val newFile = File(parent, newName)
        val renamed = file.renameTo(newFile)
        if (renamed) {
            val oldKey = item.uri.toString()
            typefaceCache.remove(oldKey)
            fontFamilyCache.remove(oldKey)
            if (parent != null) invalidateCache(parent)
        }
        return@withContext renamed
    }

    suspend fun copyOrMove(items: List<FontItem>, targetDir: File, isCut: Boolean): Boolean = withContext(Dispatchers.IO) {
        var allSuccess = true
        for (item in items) {
            val src = item.file ?: continue
            val dest = File(targetDir, src.name)
            try {
                if (src.isDirectory) {
                    src.copyRecursively(dest, overwrite = true)
                } else {
                    src.copyTo(dest, overwrite = true)
                }
                if (isCut) {
                    src.deleteRecursively()
                }
            } catch (_: Exception) {
                allSuccess = false
            }
        }
        invalidateCache()
        return@withContext allSuccess
    }

    suspend fun getOrLoadFontFamilyAsync(item: FontItem): FontFamily? {
        return loadFontFamilyAsync(item)
    }

    suspend fun getOrLoadTypefaceAsync(uri: Uri, fileName: String): Typeface? = withContext(Dispatchers.IO) {
        val cacheKey = uri.toString()
        val cached = typefaceCache.get(cacheKey)
        if (cached != null) return@withContext cached

        val diskFile = getCachedFontFile(uri, fileName) ?: return@withContext null
        return@withContext try {
            val tf = Typeface.createFromFile(diskFile)
            if (tf != null) typefaceCache.put(cacheKey, tf)
            tf
        } catch (_: Exception) {
            null
        }
    }

    fun getCachedFontFile(uri: Uri, fileName: String): File? {
        if (uri.scheme == "file") {
            val path = uri.path
            if (path != null) {
                val f = File(path)
                if (f.exists()) return f
            }
        }

        val tempFile = File(context.cacheDir, "preview_${fileName.hashCode()}.tmp")
        if (tempFile.exists() && tempFile.length() > 0) {
            return tempFile
        }
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (tempFile.exists() && tempFile.length() > 0) tempFile else null
        } catch (_: Exception) {
            null
        }
    }

    fun readFontBytes(uri: Uri): ByteArray? {
        if (uri.scheme == "file") {
            val path = uri.path
            if (path != null) {
                val f = File(path)
                if (f.exists()) return f.readBytes()
            }
        }
        return try {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (_: Exception) {
            null
        }
    }
}
