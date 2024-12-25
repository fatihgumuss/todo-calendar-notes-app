package com.example.thenotesapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PdfProcessor(private val context: Context) {
    suspend fun convertPdfToImages(uri: Uri): List<Bitmap> = withContext(Dispatchers.IO) {
        val images = mutableListOf<Bitmap>()

        // Create a temporary file to store the PDF
        val tempFile = File(context.cacheDir, "temp_pdf.pdf")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }

        // Open the PDF file
        val fileDescriptor = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdfRenderer = PdfRenderer(fileDescriptor)

        // Convert each page to bitmap
        for (pageIndex in 0 until pdfRenderer.pageCount) {
            pdfRenderer.openPage(pageIndex).use { page ->
                val bitmap = Bitmap.createBitmap(
                    page.width,
                    page.height,
                    Bitmap.Config.ARGB_8888
                )
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                images.add(bitmap)
            }
        }

        pdfRenderer.close()
        fileDescriptor.close()
        tempFile.delete()

        Log.d("PdfProcessor", "Converting PDF to images...")
        Log.d("PdfProcessor", "Created ${images.size} images")
        Log.d("PdfProcessor", "Image dimensions: ${images.firstOrNull()?.width}x${images.firstOrNull()?.height}")

        return@withContext images
    }
} 