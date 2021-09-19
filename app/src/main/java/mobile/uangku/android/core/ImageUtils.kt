package mobile.uangku.android.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.*

object ImageUtils {

    fun bitmapToFile(context: Context, temp: String, realBitmap: Bitmap, maxWidth: Float = 1600F): File? {
        val bitmap = resize(realBitmap, maxWidth)
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, bytes)
        val file = File(context.getExternalFilesDir(null), temp)
        val fileOutputStream: FileOutputStream

        try {
            file.createNewFile()
            fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(bytes.toByteArray())
            fileOutputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return null
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        return file
    }

    fun resize(realImage: Bitmap, maxWidth: Float): Bitmap {
        // Resize photo to max width 800 pixels (default)
        val multiplier = Math.min(maxWidth / realImage.width.toFloat(), maxWidth / realImage.height.toFloat())
        val width = Math.round(multiplier * realImage.width)
        val height = Math.round(multiplier * realImage.height)

        return Bitmap.createScaledBitmap(realImage, width, height, true)
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun bitmapDecode(input: InputStream?): Bitmap? {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.RGB_565
        // get dimension bitmap width, and height
        options.inJustDecodeBounds = true
        options.inSampleSize = calculateInSampleSize(options, options.outWidth, options.outHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeStream(input, null, options)
    }
}
