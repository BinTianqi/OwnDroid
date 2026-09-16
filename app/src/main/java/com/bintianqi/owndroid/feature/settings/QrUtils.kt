package com.bintianqi.owndroid.feature.settings

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter

/**
 * QR generation + decoding helpers (ZXing based).
 *
 * Raw bytes are transferred via QR byte mode using the ISO-8859-1 trick:
 * every byte maps 1:1 to a char, so `String(bytes, ISO_8859_1)` encodes exactly
 * those bytes and `result.text.toByteArray(ISO_8859_1)` restores them.
 */
object QrUtils {

    fun encodeToBitmap(data: ByteArray, sizePx: Int): Bitmap {
        val content = String(data, Charsets.ISO_8859_1)
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "ISO-8859-1",
            EncodeHintType.MARGIN to 4 // QR spec quiet zone
        )
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(sizePx * sizePx)
        for (y in 0 until sizePx) {
            for (x in 0 until sizePx) {
                pixels[y * sizePx + x] =
                    if (matrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            }
        }
        bitmap.setPixels(pixels, 0, sizePx, 0, 0, sizePx, sizePx)
        return bitmap
    }

    private val reader = MultiFormatReader().apply {
        setHints(
            mapOf(
                DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
                DecodeHintType.TRY_HARDER to true
            )
        )
    }

    /**
     * Try to decode a QR code from a CameraX YUV_420_888 ImageProxy.
     * Returns the raw byte payload or null if no code was found.
     * Does NOT close the proxy; caller owns it.
     */
    fun decodeImageProxy(image: ImageProxy): ByteArray? {
        if (image.planes.isEmpty()) return null
        val yPlane = image.planes[0]
        val buffer = yPlane.buffer
        val rowStride = yPlane.rowStride
        val pixelStride = yPlane.pixelStride
        val width = image.width
        val height = image.height

        // Compact Y plane into a contiguous byte array (stride may exceed width)
        val data = ByteArray(width * height)
        val row = ByteArray(rowStride)
        var offset = 0
        for (y in 0 until height) {
            buffer.position(y * rowStride)
            val remaining = buffer.remaining()
            if (remaining < (width - 1) * pixelStride + 1) return null
            if (pixelStride == 1) {
                buffer.get(data, offset, width)
            } else {
                buffer.get(row, 0, minOf(rowStride, remaining))
                var x = 0
                while (x < width) {
                    data[offset + x] = row[x * pixelStride]
                    x++
                }
            }
            offset += width
        }

        val source = PlanarYUVLuminanceSource(
            data, width, height, 0, 0, width, height, false
        )
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        return try {
            synchronized(reader) {
                reader.decodeWithState(bitmap)
            }.text.toByteArray(Charsets.ISO_8859_1)
        } catch (_: Exception) {
            null
        } finally {
            reader.reset()
        }
    }
}
