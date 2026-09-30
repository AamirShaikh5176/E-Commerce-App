package com.example.e_commerceapp.utils

import android.content.Context
import android.util.Log
import coil.Coil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/**
 * Information model holding measured storage and cache sizes.
 */
data class StorageCacheInfo(
    val imageCacheBytes: Long = 0L,
    val temporaryDataBytes: Long = 0L,
    val totalCacheBytes: Long = 0L,
    val imageCacheFormatted: String = "0 B",
    val temporaryDataFormatted: String = "0 B",
    val totalCacheFormatted: String = "0 B"
)

/**
 * Manages measurable application temporary and image cache data.
 *
 * CRITICAL DATA PROTECTION GUARANTEE:
 * This manager strictly operates on [Context.getCacheDir], [Context.getExternalCacheDir],
 * and Coil's image disk/memory cache.
 *
 * It NEVER modifies or deletes:
 * - Room databases (Cart database, local tables)
 * - SharedPreferences (Addresses, user tokens, notification preferences)
 * - Firebase data or offline caches
 * - User authentication state (FirebaseAuth, active session)
 * - Wishlist or Order collections
 */
@OptIn(coil.annotation.ExperimentalCoilApi::class)
object StorageCacheManager {

    private const val TAG = "StorageCacheManager"

    /**
     * Measures actual image cache and temporary data sizes on disk.
     */
    suspend fun getCacheInfo(context: Context): StorageCacheInfo = withContext(Dispatchers.IO) {
        val imageBytes = getImageCacheSize(context)
        val tempBytes = getTemporaryDataSize(context)
        val totalBytes = imageBytes + tempBytes

        StorageCacheInfo(
            imageCacheBytes = imageBytes,
            temporaryDataBytes = tempBytes,
            totalCacheBytes = totalBytes,
            imageCacheFormatted = formatSize(imageBytes),
            temporaryDataFormatted = formatSize(tempBytes),
            totalCacheFormatted = formatSize(totalBytes)
        )
    }

    /**
     * Calculates the size of Coil's disk cache.
     */
    private fun getImageCacheSize(context: Context): Long {
        var size = 0L
        try {
            val coilLoader = Coil.imageLoader(context)
            val coilDiskCache = coilLoader.diskCache
            if (coilDiskCache != null) {
                size = coilDiskCache.size
            }
            val coilDir = coilDiskCache?.directory?.let { File(it.toString()) }
            if (coilDir != null && coilDir.exists()) {
                val dirSize = getDirectorySize(coilDir)
                if (dirSize > size) {
                    size = dirSize
                }
            } else {
                val defaultImageCacheDir = File(context.cacheDir, "image_cache")
                if (defaultImageCacheDir.exists()) {
                    val dirSize = getDirectorySize(defaultImageCacheDir)
                    if (dirSize > size) {
                        size = dirSize
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to inspect Coil disk cache size", e)
        }
        return size
    }

    /**
     * Calculates non-image temporary cache size in cacheDir and externalCacheDir.
     */
    private fun getTemporaryDataSize(context: Context): Long {
        var tempSize = 0L
        try {
            val coilDir = try {
                Coil.imageLoader(context).diskCache?.directory?.let { File(it.toString()) }
            } catch (_: Exception) {
                null
            }

            context.cacheDir.listFiles()?.forEach { file ->
                if (coilDir != null && file.absolutePath == coilDir.absolutePath) {
                    return@forEach
                }
                if (file.name == "image_cache") {
                    return@forEach
                }
                tempSize += if (file.isDirectory) getDirectorySize(file) else file.length()
            }

            context.externalCacheDir?.listFiles()?.forEach { file ->
                tempSize += if (file.isDirectory) getDirectorySize(file) else file.length()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to calculate temporary cache size", e)
        }
        return tempSize
    }

    /**
     * Recursively measures the byte size of a file or directory.
     */
    private fun getDirectorySize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var size = 0L
        val files = dir.listFiles() ?: return 0L
        for (file in files) {
            size += if (file.isDirectory) {
                getDirectorySize(file)
            } else {
                file.length()
            }
        }
        return size
    }

    /**
     * Safely clears ONLY image and temporary cache directories.
     */
    suspend fun clearCache(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Clear Coil memory and disk caches via existing Coil architecture
            try {
                val coilLoader = Coil.imageLoader(context)
                coilLoader.diskCache?.clear()
                coilLoader.memoryCache?.clear()
            } catch (e: Exception) {
                Log.w(TAG, "Error clearing Coil cache", e)
            }

            // 2. Clear context.cacheDir files (temporary files only)
            context.cacheDir.listFiles()?.forEach { file ->
                try {
                    file.deleteRecursively()
                } catch (e: Exception) {
                    Log.w(TAG, "Error deleting cache file ${file.name}", e)
                }
            }

            // 3. Clear context.externalCacheDir files (external temporary files only)
            context.externalCacheDir?.listFiles()?.forEach { file ->
                try {
                    file.deleteRecursively()
                } catch (e: Exception) {
                    Log.w(TAG, "Error deleting external cache file ${file.name}", e)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during cache clearing", e)
            Result.failure(e)
        }
    }

    /**
     * Formats bytes into human-readable B, KB, MB, or GB.
     */
    fun formatSize(bytes: Long): String {
        if (bytes <= 0L) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        var size = bytes.toDouble()
        var unitIndex = 0
        while (size >= 1024.0 && unitIndex < units.size - 1) {
            size /= 1024.0
            unitIndex++
        }
        return String.format(Locale.US, "%.1f %s", size, units[unitIndex])
    }
}
