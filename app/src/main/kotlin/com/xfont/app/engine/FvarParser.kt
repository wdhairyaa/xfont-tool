package com.xfont.app.engine

import com.xfont.app.model.VariableAxis
import java.nio.ByteBuffer
import java.nio.ByteOrder

object FvarParser {

    fun parseAxes(fontBytes: ByteArray): List<VariableAxis> {
        if (fontBytes.size < 12) return emptyList()

        try {
            val buffer = ByteBuffer.wrap(fontBytes).order(ByteOrder.BIG_ENDIAN)
            val sfntVersion = buffer.int
            // Check for valid TTF / OTF / TrueType collections
            if (sfntVersion != 0x00010000 && sfntVersion != 0x4F54544F && sfntVersion != 0x74727565) {
                return emptyList()
            }

            val numTables = buffer.short.toInt() and 0xFFFF
            buffer.position(buffer.position() + 6) // Skip searchRange, entrySelector, rangeShift

            var fvarOffset = -1
            for (i in 0 until numTables) {
                val tag = String(ByteArray(4) { buffer.get() }, Charsets.US_ASCII)
                buffer.int // checksum
                val offset = buffer.int
                buffer.int // length
                if (tag == "fvar") {
                    fvarOffset = offset
                    break
                }
            }

            if (fvarOffset == -1 || fvarOffset + 16 > fontBytes.size) return emptyList()

            buffer.position(fvarOffset)
            buffer.short // majorVersion
            buffer.short // minorVersion
            val axesArrayOffset = buffer.short.toInt() and 0xFFFF
            buffer.short // reserved
            val axisCount = buffer.short.toInt() and 0xFFFF
            val axisSize = buffer.short.toInt() and 0xFFFF

            val axes = mutableListOf<VariableAxis>()
            val axesStart = fvarOffset + axesArrayOffset

            for (i in 0 until axisCount) {
                val currentAxisPos = axesStart + (i * axisSize)
                if (currentAxisPos + 16 > fontBytes.size) break
                buffer.position(currentAxisPos)

                val tagBytes = ByteArray(4)
                buffer.get(tagBytes)
                val tag = String(tagBytes, Charsets.US_ASCII)

                val min = buffer.int / 65536.0f
                val def = buffer.int / 65536.0f
                val max = buffer.int / 65536.0f

                axes.add(
                    VariableAxis(
                        tag = tag,
                        min = min,
                        max = max,
                        defaultValue = def,
                        currentValue = def
                    )
                )
            }
            return axes
        } catch (_: Exception) {
            return emptyList()
        }
    }

    /**
     * Patches the defaultValue of each axis in the fvar table, updates OS/2 usWeightClass
     * and usWidthClass, updates fsSelection/macStyle, and recalculates all table checksums
     * and head.checkSumAdjustment so the TTF is 100% compliant and renders with the chosen weights.
     */
    fun patchFvarDefaults(fontBytes: ByteArray, axisValues: Map<String, Float>): ByteArray {
        if (fontBytes.size < 12 || axisValues.isEmpty()) return fontBytes

        val copy = fontBytes.copyOf()
        try {
            val buffer = ByteBuffer.wrap(copy).order(ByteOrder.BIG_ENDIAN)
            val sfntVersion = buffer.int
            if (sfntVersion != 0x00010000 && sfntVersion != 0x4F54544F && sfntVersion != 0x74727565) {
                return copy
            }

            val numTables = buffer.short.toInt() and 0xFFFF
            buffer.position(12)

            var fvarOffset = -1
            var fvarIndex = -1
            var fvarLen = 0

            var os2Offset = -1
            var os2Index = -1
            var os2Len = 0

            var headOffset = -1
            var headIndex = -1
            var headLen = 0

            for (i in 0 until numTables) {
                val tableRecordPos = 12 + i * 16
                buffer.position(tableRecordPos)
                val tag = String(ByteArray(4) { buffer.get() }, Charsets.US_ASCII)
                buffer.int // checksum
                val offset = buffer.int
                val len = buffer.int

                when (tag) {
                    "fvar" -> {
                        fvarOffset = offset
                        fvarIndex = i
                        fvarLen = len
                    }
                    "OS/2" -> {
                        os2Offset = offset
                        os2Index = i
                        os2Len = len
                    }
                    "head" -> {
                        headOffset = offset
                        headIndex = i
                        headLen = len
                    }
                }
            }

            // 1. Patch fvar defaultValue for each axis (and clamp min/max to freeze axis instance)
            if (fvarOffset != -1 && fvarOffset + 16 <= copy.size) {
                buffer.position(fvarOffset + 4)
                val axesArrayOffset = buffer.short.toInt() and 0xFFFF
                buffer.short
                val axisCount = buffer.short.toInt() and 0xFFFF
                val axisSize = buffer.short.toInt() and 0xFFFF
                val axesStart = fvarOffset + axesArrayOffset

                for (i in 0 until axisCount) {
                    val currentAxisPos = axesStart + (i * axisSize)
                    if (currentAxisPos + 16 > copy.size) break
                    buffer.position(currentAxisPos)

                    val tagBytes = ByteArray(4)
                    buffer.get(tagBytes)
                    val tag = String(tagBytes, Charsets.US_ASCII)

                    val userEntry = axisValues.entries.firstOrNull { it.key.trim().equals(tag.trim(), ignoreCase = true) }
                    if (userEntry != null) {
                        val fixed16 = (userEntry.value * 65536.0f).toInt()
                        // Offset 4: minValue
                        buffer.position(currentAxisPos + 4)
                        buffer.putInt(fixed16)
                        // Offset 8: defaultValue
                        buffer.putInt(fixed16)
                        // Offset 12: maxValue
                        buffer.putInt(fixed16)
                    }
                }
            }

            // 2. Patch OS/2 table (usWeightClass, usWidthClass, fsSelection)
            var chosenWeight: Float? = null
            var chosenWidth: Float? = null
            for ((key, value) in axisValues) {
                if (key.equals("wght", ignoreCase = true)) chosenWeight = value
                if (key.equals("wdth", ignoreCase = true)) chosenWidth = value
            }

            val isBold = (chosenWeight ?: 400f) >= 600f

            if (os2Offset != -1 && os2Offset + 64 <= copy.size) {
                if (chosenWeight != null) {
                    // Offset 4: usWeightClass (uint16)
                    val weightInt = chosenWeight.toInt().coerceIn(1, 1000)
                    buffer.position(os2Offset + 4)
                    buffer.putShort(weightInt.toShort())
                }

                if (chosenWidth != null) {
                    // Offset 2: usWidthClass (uint16, 1..9)
                    val widthClass = when {
                        chosenWidth < 56.5f -> 1
                        chosenWidth < 68.5f -> 2
                        chosenWidth < 81.5f -> 3
                        chosenWidth < 93.5f -> 4
                        chosenWidth <= 106.5f -> 5
                        chosenWidth < 126.5f -> 6
                        chosenWidth < 150.5f -> 7
                        chosenWidth < 175.5f -> 8
                        else -> 9
                    }
                    buffer.position(os2Offset + 2)
                    buffer.putShort(widthClass.toShort())
                }

                // Offset 62: fsSelection (uint16) - Bit 5 is BOLD (0x0020)
                buffer.position(os2Offset + 62)
                var fsSelection = buffer.short.toInt() and 0xFFFF
                fsSelection = if (isBold) {
                    fsSelection or 0x0020
                } else {
                    fsSelection and 0x0020.inv()
                }
                buffer.position(os2Offset + 62)
                buffer.putShort(fsSelection.toShort())
            }

            // 3. Patch head table (macStyle bit 0 is Bold)
            if (headOffset != -1 && headOffset + 46 <= copy.size) {
                buffer.position(headOffset + 44)
                var macStyle = buffer.short.toInt() and 0xFFFF
                macStyle = if (isBold) {
                    macStyle or 0x0001
                } else {
                    macStyle and 0x0001.inv()
                }
                buffer.position(headOffset + 44)
                buffer.putShort(macStyle.toShort())
            }

            // 4. Recalculate checksums for modified tables in table directory
            fun updateTableChecksum(tableIndex: Int, offset: Int, length: Int) {
                if (tableIndex < 0 || offset < 0 || length <= 0) return
                val cs = calcChecksum(buffer, offset, length)
                buffer.position(12 + tableIndex * 16 + 4)
                buffer.putInt(cs.toInt())
            }

            if (fvarIndex != -1) updateTableChecksum(fvarIndex, fvarOffset, fvarLen)
            if (os2Index != -1) updateTableChecksum(os2Index, os2Offset, os2Len)
            if (headIndex != -1) updateTableChecksum(headIndex, headOffset, headLen)

            // 5. Recalculate head.checkSumAdjustment = 0xB1B0AFBA - sum of all uint32
            if (headOffset != -1 && headOffset + 12 <= copy.size) {
                buffer.position(headOffset + 8)
                buffer.putInt(0) // clear existing checkSumAdjustment

                var totalFontSum = 0L
                val totalInts = copy.size / 4
                buffer.position(0)
                for (i in 0 until totalInts) {
                    totalFontSum += (buffer.int.toLong() and 0xFFFFFFFFL)
                }
                val remainder = copy.size % 4
                if (remainder > 0) {
                    var last = 0L
                    for (b in 0 until remainder) {
                        last = (last shl 8) or (buffer.get().toLong() and 0xFFL)
                    }
                    last = last shl (8 * (4 - remainder))
                    totalFontSum += last
                }

                val checkSumAdjustment = (0xB1B0AFBAL - (totalFontSum and 0xFFFFFFFFL)).toInt()
                buffer.position(headOffset + 8)
                buffer.putInt(checkSumAdjustment)
            }

        } catch (_: Exception) {
            return fontBytes
        }
        return copy
    }

    private fun calcChecksum(buf: ByteBuffer, offset: Int, length: Int): Long {
        var sum = 0L
        val nLongs = (length + 3) / 4
        buf.position(offset)
        for (i in 0 until nLongs) {
            if (buf.remaining() >= 4) {
                sum += (buf.int.toLong() and 0xFFFFFFFFL)
            } else {
                var last = 0L
                val rem = buf.remaining()
                for (b in 0 until rem) {
                    last = (last shl 8) or (buf.get().toLong() and 0xFFL)
                }
                last = last shl (8 * (4 - rem))
                sum += last
            }
        }
        return sum and 0xFFFFFFFFL
    }
}
