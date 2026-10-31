package com.xfont.app.engine

import android.content.Context
import android.util.Log
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import org.json.JSONObject
import java.io.File

object ChaquopyFontInstancer {
    private const val TAG = "ChaquopyFontInstancer"

    fun ensureInitialized(context: Context) {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context.applicationContext))
        }
    }

    /**
     * Instantiates a variable font at specified axis coordinates into a pure static font.
     * Uses official fontTools.varLib.instancer embedded via Chaquopy Python.
     * Writes temporary font files to cacheDir and automatically removes them when done.
     */
    fun instantiate(
        context: Context,
        rawFontBytes: ByteArray,
        axisValues: Map<String, Float>
    ): ByteArray {
        if (axisValues.isEmpty()) return rawFontBytes

        val cacheDir = context.cacheDir
        val timestamp = System.currentTimeMillis()
        val tempInput = File(cacheDir, "input_var_${timestamp}.ttf")
        val tempOutput = File(cacheDir, "frozen_${timestamp}.ttf")

        return try {
            tempInput.writeBytes(rawFontBytes)

            ensureInitialized(context)
            val py = Python.getInstance()
            val module = py.getModule("font_instancer")

            val jsonObject = JSONObject()
            for ((key, value) in axisValues) {
                jsonObject.put(key, value.toDouble())
            }

            Log.d(TAG, "Freezing font axes via fontTools: $jsonObject")
            module.callAttr("freeze_font", tempInput.absolutePath, tempOutput.absolutePath, jsonObject.toString())

            if (tempOutput.exists() && tempOutput.length() > 0) {
                val outputBytes = tempOutput.readBytes()
                Log.d(TAG, "Successfully instantiated static font: ${outputBytes.size} bytes (source: ${rawFontBytes.size} bytes)")
                outputBytes
            } else {
                Log.w(TAG, "Frozen static font file empty or missing, falling back to original bytes")
                rawFontBytes
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error freezing variable font coordinates via fontTools", e)
            rawFontBytes
        } finally {
            if (tempInput.exists()) tempInput.delete()
            if (tempOutput.exists()) tempOutput.delete()
        }
    }
}
