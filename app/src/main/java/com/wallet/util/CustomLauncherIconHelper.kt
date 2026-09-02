package com.wallet.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.util.Log
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.wallet.MainActivity
import com.wallet.R
import java.io.File
import java.io.FileOutputStream

object CustomLauncherIconHelper {

    private const val TAG = "CustomLauncherIcon"
    private const val SHORTCUT_ID = "wallet_custom_launcher"
    private const val ICON_FILE = "custom_launcher_icon.png"

    fun apply(context: Context, imageUri: String): Boolean {
        val appContext = context.applicationContext
        val bitmap = loadBitmap(appContext, imageUri) ?: return false
        val launcherBitmap = createLauncherBitmap(bitmap)

        return runCatching {
            saveIconFile(appContext, launcherBitmap)
            requestPinnedShortcut(appContext, launcherBitmap)
            true
        }.onFailure {
            Log.e(TAG, "Failed to apply custom launcher icon", it)
        }.getOrDefault(false)
    }

    fun savedIconFile(context: Context): File {
        return File(context.applicationContext.filesDir, ICON_FILE)
    }

    private fun loadBitmap(context: Context, imageUri: String): Bitmap? {
        return runCatching {
            context.contentResolver.openInputStream(Uri.parse(imageUri))?.use { input ->
                BitmapFactory.decodeStream(input)
            }
        }.getOrNull()
    }

    private fun createLauncherBitmap(source: Bitmap): Bitmap {
        val size = 512
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = 0xFF2E7D32.toInt()
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)

        val inset = size * 0.12f
        val rect = RectF(inset, inset, size - inset, size - inset)
        val scaled = Bitmap.createScaledBitmap(source, rect.width().toInt(), rect.height().toInt(), true)
        canvas.drawBitmap(scaled, inset, inset, paint)

        scaled.recycle()
        return output
    }

    private fun saveIconFile(context: Context, bitmap: Bitmap) {
        FileOutputStream(File(context.filesDir, ICON_FILE)).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
    }

    private fun requestPinnedShortcut(context: Context, bitmap: Bitmap): Boolean {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            return false
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val shortcut = ShortcutInfoCompat.Builder(context, SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.app_name))
            .setLongLabel(context.getString(R.string.app_name))
            .setIcon(IconCompat.createWithAdaptiveBitmap(bitmap))
            .setIntent(intent)
            .build()

        return ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
    }
}
