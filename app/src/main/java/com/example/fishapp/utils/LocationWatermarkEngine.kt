package com.example.fishapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.location.Geocoder
import android.location.Location // ADDED: Direct import to resolve the reference conflict
import android.net.Uri
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume

object LocationWatermarkEngine {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? = suspendCancellableCoroutine { continuation ->
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    continuation.resume(location)
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        } catch (e: Exception) {
            continuation.resume(null)
        }
    }

    fun getReadableAddress(context: Context, latitude: Double, longitude: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val city = address.locality ?: address.subAdminArea ?: ""
                val state = address.adminArea ?: ""
                val country = address.countryName ?: ""
                listOf(city, state, country).filter { it.isNotEmpty() }.joinToString(", ")
            } else {
                "Unknown Location"
            }
        } catch (e: Exception) {
            "GPS Location Bound"
        }
    }

    fun addGpsWatermark(
        context: Context,
        sourceUri: Uri,
        lat: Double,
        lng: Double,
        addressText: String
    ): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null
            inputStream?.close()

            val workingBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(workingBitmap)

            val width = workingBitmap.width
            val height = workingBitmap.height

            val bannerHeight = (height * 0.18f).toInt()
            val padding = (width * 0.04f)
            val textSizeMain = (bannerHeight * 0.16f)
            val textSizeSub = (bannerHeight * 0.11f)

            val bannerPaint = Paint().apply {
                color = Color.BLACK
                alpha = 180
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, (height - bannerHeight).toFloat(), width.toFloat(), height.toFloat(), bannerPaint)

            val textPaintMain = Paint().apply {
                color = Color.WHITE
                textSize = textSizeMain
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val textPaintSub = Paint().apply {
                color = Color.WHITE
                alpha = 215
                textSize = textSizeSub
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            val timestamp = SimpleDateFormat("dd/MM/yy hh:mm a 'GMT'Z", Locale.US).format(Date())
            var currentY = (height - bannerHeight) + padding + textSizeMain

            canvas.drawText(addressText, padding, currentY, textPaintMain)

            currentY += textSizeSub + (bannerHeight * 0.06f)
            canvas.drawText("Lat ${String.format("%.6f", lat)}°  |  Long ${String.format("%.6f", lng)}°", padding, currentY, textPaintSub)

            currentY += textSizeSub + (bannerHeight * 0.06f)
            canvas.drawText(timestamp, padding, currentY, textPaintSub)

            val watermarkedFile = File(context.cacheDir, "AquaSense_GPS_${System.currentTimeMillis()}.jpg")
            FileOutputStream(watermarkedFile).use { out ->
                workingBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            originalBitmap.recycle()
            workingBitmap.recycle()

            Uri.fromFile(watermarkedFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}