package com.campionpumps.webservices.qrcode.qr

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.campionpumps.webservices.qrcode.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.android.synthetic.main.activity_qr_generate.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Kushal.Mishra on 27/03/2018.
 */
class QrGenerate : AppCompatActivity() {
    var shareQrCodeIV: ImageView? = null
    var qrInputET: EditText? = null
    var emailET: EditText? = null
    private var mMergedImage: Bitmap? = null
    var userFormNumberForQrCode: String? = null
    var userEmailToSendQrCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_generate)
        qrInputET = qrInput as EditText
        userFormNumberForQrCode = qrInputET?.text.toString()
        shareQrCodeIV = shareQrCode
        shareQrCodeIV?.setOnClickListener {
            val generatedQrCodeBitmap = mMergedImage //null if nothing
            if (userFormNumberForQrCode != "" || generatedQrCodeBitmap != null) {
                userEmailToSendQrCode = emailET?.text.toString()
                userFormNumberForQrCode = qrInputET?.text.toString()
                if (isValidEmail(userEmailToSendQrCode!!)) {
                    emailImage(generatedQrCodeBitmap, userFormNumberForQrCode!!, userEmailToSendQrCode!!)
                } else {
                    Toast.makeText(applicationContext, "Invalid Email ID", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(applicationContext, "No Qr-Code Generated", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isValidEmail(userEmailToSendQrCode: String): Boolean {
        val regex = Regex("^[\\w-_.+]*[\\w-_.]@([\\w]+\\.)+[\\w]+[\\w]$")
        return userEmailToSendQrCode.matches(regex)
    }

    private fun emailImage(generatedQrCodeBitmap: Bitmap?, userFormNumberForQrCode: String, userEmailToSendQrCode: String) { // Test
        var userEmailToSendQrCode = userEmailToSendQrCode
        if (generatedQrCodeBitmap != null) {
            if (userEmailToSendQrCode == "") {
                userEmailToSendQrCode = "kushal@campion.ie"
            }
            val emailQrCode = Intent(Intent.ACTION_SEND)
            emailQrCode.putExtra(Intent.EXTRA_EMAIL, arrayOf(userEmailToSendQrCode))
            emailQrCode.putExtra(Intent.EXTRA_SUBJECT, "Qr-Code for $userFormNumberForQrCode")
            emailQrCode.putExtra(Intent.EXTRA_TEXT, "Please find attached QR-Code for" +
                    " Form ID " + userFormNumberForQrCode)
            emailQrCode.type = "image/*"
            val myDir = File(Environment.getExternalStorageDirectory().toString() + "/qr_code")
            myDir.mkdirs()
            val dateFormat: DateFormat = SimpleDateFormat("yyyy_MM_dd_H_mm_SS", Locale.getDefault())
            val currentDate = Date()
            val dateForFileName = dateFormat.format(currentDate)
            val pathToMyAttachedFile = dateForFileName + "__" + userFormNumberForQrCode + "_qr_.png"
            val file = File(myDir, pathToMyAttachedFile)
            try {
                val fileCreated = file.createNewFile()
                if (fileCreated) {
                    // Write bitmap to that file
                    val fileOutputStream = FileOutputStream(file)
                    generatedQrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                    fileOutputStream.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val uri = FileProvider.getUriForFile(this@QrGenerate, this@QrGenerate.applicationContext.packageName
                    + ".provider", file)
            emailQrCode.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            emailQrCode.putExtra(Intent.EXTRA_STREAM, uri)
            startActivity(Intent.createChooser(emailQrCode, "Sending Qr-Code..."))
        }
    }

    // This method is called onClick to generate QRCode
    fun qrGenerator(v: View?) {
        try {
            //setting size of qr code
            val manager = getSystemService(WINDOW_SERVICE) as WindowManager
            val display = manager.defaultDisplay
            val point = Point()
            display.getSize(point)
            val width = point.x
            val height = point.y
            val smallestDimension = Math.min(width, height)
            val qrInput = findViewById<View>(R.id.qrInput) as EditText
            val qrCodeData = qrInput.text.toString()
            //setting parameters for qr code
            val charset = "UTF-8" // or "ISO-8859-1"
            val hintMap: MutableMap<EncodeHintType, ErrorCorrectionLevel> = EnumMap(com.google.zxing.EncodeHintType::class.java)
            hintMap[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.L
            createQRCode(qrCodeData, charset, hintMap, smallestDimension, smallestDimension)
        } catch (ex: Exception) {
            Log.e("QrGenerate", ex.message)
        }
    }

    private fun createQRCode(qrCodeData: String, charset: String?, hintMap: Map<*, *>?, qrCodeheight: Int, qrCodewidth: Int) {
        try {
            //generating qr code in bitMatrix type
            val matrix = MultiFormatWriter().encode(String(qrCodeData.toByteArray(charset(charset!!))), BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight, null)
            // BitMatrix matrix = new MultiFormatWriter().encode(new String(qrCodeData.getBytes(charset), charset), BarcodeFormat.CODE_128, qrCodewidth, qrCodeheight, hintMap);
            // converting bitMatrix to bitmap
            val width = matrix.width
            val height = matrix.height
            val pixels = IntArray(width * height)
            // All are 0, or black, by default
            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (matrix[x, y]) Color.BLACK else Color.WHITE
                }
            }
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            //setting bitmap to image view
            val myImage = findViewById<View>(R.id.imageView1) as ImageView
            val overlay = BitmapFactory.decodeResource(resources, R.drawable.logo_new)
            mMergedImage = mergeBitmaps(overlay, bitmap)
            myImage.setImageBitmap(mMergedImage)
            //myImage.setImageBitmap(bitmap);
//            myImage.setImageBitmap(bitmap);
        } catch (er: Exception) {
            Log.e("QrGenerate", er.message)
        }
    }

    private fun mergeBitmaps(overlay: Bitmap, bitmap: Bitmap): Bitmap {
        val height = bitmap.height
        val width = bitmap.width
        val combined = Bitmap.createBitmap(width, height, bitmap.config)
        val canvas = Canvas(combined)
        val canvasWidth = canvas.width
        val canvasHeight = canvas.height
        canvas.drawBitmap(bitmap, Matrix(), null)
        val centreX = (canvasWidth - overlay.width) / 2
        val centreY = (canvasHeight - overlay.height) / 2
        canvas.drawBitmap(overlay, centreX.toFloat(), centreY.toFloat(), null)
        return combined
    }
}
