package com.wallet.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.wallet.MainActivity
import com.wallet.R
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

object CustomLauncherIconHelper {

    private const val TAG = "CustomLauncherIcon"
    private const val SHORTCUT_ID = "wallet_custom_launcher"
    private const val ICON_FILE = "custom_launcher_icon.png"
    private const val ADAPTIVE_ICON_SIZE = 192
    private const val MAX_DECODE_SIZE = 1024

    data class ApplyResult(
        val success: Boolean,
        val localIconUri: String? = null,
        val pinnedShortcutRequested: Boolean = false
    )

    fun apply(context: Context, imageUri: String): ApplyResult {
        val appContext = context.applicationContext
        val source = loadBitmap(appContext, imageUri) ?: return ApplyResult(success = false)

        return runCatching {
            val launcherBitmap = createAdaptiveLauncherBitmap(source)
            if (source != launcherBitmap) {
                source.recycle()
            }
            val iconFile = saveIconFile(appContext, launcherBitmap)
            launcherBitmap.recycle()
            val pinned = requestPinnedShortcut(appContext, iconFile)
            if (!pinned) {
                Log.w(TAG, "Pin shortcut not supported on this device")
            }
            ApplyResult(
                success = true,
                localIconUri = Uri.fromFile(iconFile).toString(),
                pinnedShortcutRequested = pinned
            )
        }.onFailure {
            Log.e(TAG, "Failed to apply custom launcher icon", it)
        }.getOrDefault(ApplyResult(success = false))
    }

    fun savedIconFile(context: Context): File {
        return File(context.applicationContext.filesDir, ICON_FILE)
    }

    private fun loadBitmap(context: Context, imageUri: String): Bitmap? {
        val savedFile = savedIconFile(context)
        if (imageUri.startsWith("file:") && savedFile.exists()) {
            return decodeBitmapFile(savedFile)
        }

        val fromUri = loadBitmapFromUri(context, imageUri)
        if (fromUri != null) {
            return fromUri
        }

        return if (savedFile.exists()) {
            decodeBitmapFile(savedFile)
        } else {
            null
        }
    }

    private fun loadBitmapFromUri(context: Context, imageUri: String): Bitmap? {
        return runCatching {
            val uri = Uri.parse(imageUri)
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, bounds)
            }

            val sampleSize = calculateInSampleSize(
                width = bounds.outWidth,
                height = bounds.outHeight,
                maxSize = MAX_DECODE_SIZE
            )
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }
        }.getOrNull()
    }

    private fun decodeBitmapFile(file: File): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        val sampleSize = calculateInSampleSize(
            width = bounds.outWidth,
            height = bounds.outHeight,
            maxSize = MAX_DECODE_SIZE
        )
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeFile(file.absolutePath, options)
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxSize: Int): Int {
        if (width <= 0 || height <= 0) return 1
        var sampleSize = 1
        while (width / sampleSize > maxSize || height / sampleSize > maxSize) {
            sampleSize *= 2
        }
        return sampleSize
    }

    private fun centerCropBitmap(source: Bitmap, size: Int): Bitmap {
        val sourceWidth = source.width
        val sourceHeight = source.height
        if (sourceWidth == size && sourceHeight == size) return source

        val scale = max(size.toFloat() / sourceWidth, size.toFloat() / sourceHeight)
        val scaledWidth = (sourceWidth * scale).roundToInt()
        val scaledHeight = (sourceHeight * scale).roundToInt()
        val scaled = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true)
        val left = (scaledWidth - size) / 2
        val top = (scaledHeight - size) / 2
        val cropped = Bitmap.createBitmap(scaled, left, top, size, size)
        if (scaled != source) {
            scaled.recycle()
        }
        return cropped
    }

    private fun createAdaptiveLauncherBitmap(source: Bitmap): Bitmap {
        val cropped = centerCropBitmap(source, ADAPTIVE_ICON_SIZE)
        val output = Bitmap.createBitmap(ADAPTIVE_ICON_SIZE, ADAPTIVE_ICON_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        canvas.drawBitmap(cropped, 0f, 0f, paint)
        if (cropped != source) {
            cropped.recycle()
        }
        return output
    }

    private fun saveIconFile(context: Context, bitmap: Bitmap): File {
        val file = File(context.filesDir, ICON_FILE)
        FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 92, stream)
        }
        return file
    }

    private fun requestPinnedShortcut(context: Context, iconFile: File): Boolean {
        val bitmap = decodeBitmapFile(iconFile) ?: return false
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val icon = runCatching {
            IconCompat.createWithAdaptiveBitmap(bitmap)
        }.getOrElse {
            IconCompat.createWithBitmap(bitmap)
        }

        val shortcut = ShortcutInfoCompat.Builder(context, SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.app_name))
            .setLongLabel(context.getString(R.string.app_name))
            .setIcon(icon)
            .setIntent(intent)
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
        bitmap.recycle()

        return if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
        } else {
            false
        }
    }
}
