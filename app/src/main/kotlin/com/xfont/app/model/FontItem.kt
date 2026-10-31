package com.xfont.app.model

import android.graphics.Typeface
import android.net.Uri
import java.io.File

data class FontItem(
    val uri: Uri,
    val name: String,
    val isDirectory: Boolean,
    val sizeBytes: Long = 0L,
    val lastModified: Long = 0L,
    val typeface: Typeface? = null,
    val isVariable: Boolean = false,
    val axes: List<VariableAxis> = emptyList(),
    val file: File? = null,
    val subfolderPath: String = ""
)

data class BreadcrumbItem(
    val title: String,
    val uri: Uri,
    val file: File? = null
)

enum class ClipboardOp {
    COPY, CUT
}

data class ClipboardState(
    val op: ClipboardOp,
    val items: List<FontItem>
)
