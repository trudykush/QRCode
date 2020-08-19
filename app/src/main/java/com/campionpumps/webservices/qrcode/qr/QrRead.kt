package com.campionpumps.webservices.qrcode.qr

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView

/**
 * Created by Kushal.Mishra on 27/03/2018.
 */
class QrRead : Activity(), ZXingScannerView.ResultHandler {
    private var mScannerView: ZXingScannerView? = null
    var TAG = "QRREADER"
    public override fun onCreate(state: Bundle) {
        super.onCreate(state)
        permissionForCamera()
        mScannerView = ZXingScannerView(this) // Programmatically initialize the scanner view
        setContentView(mScannerView) // Set the scanner view as the content view
    }

    private fun permissionForCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 110)
        }
    }

    public override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 110)
        } else {
            mScannerView!!.setResultHandler(this) // Register ourselves as a handler for scan results.
            mScannerView!!.stopCameraPreview() //stopPreview
            mScannerView!!.startCamera() // Start camera on resume
        }
    }

    public override fun onPause() {
        super.onPause()
        mScannerView!!.stopCamera() // Stop camera on pause
    }

    override fun handleResult(rawResult: Result) {
        // Do something with the result here
        Log.v(TAG, rawResult.text) // Prints scan results
        Log.v(TAG, rawResult.barcodeFormat.toString()) // Prints the scan format (qrcode, pdf417 etc.)
        val qrCodeText = rawResult.text
        if (qrCodeText.contains("market")) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.campionpumps.caretaker")))
        } else if (qrCodeText.contains("com.campionpumps.caretaker")) {
            val launchIntent = packageManager.getLaunchIntentForPackage("com.campionpumps.caretaker")
            if (launchIntent != null) {
                startActivity(launchIntent) //null pointer check in case package name was not found
            } else {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.campionpumps.caretaker")))
            }
        } else if (qrCodeText.contains("https://myscadacloud.com")) {
            val launchIntent = packageManager.getLaunchIntentForPackage("com.campionpumps.caretaker")
            if (launchIntent != null) {
                startActivity(launchIntent) //null pointer check in case package name was not found
            } else {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.campionpumps.caretaker")))
            }
        }

        // call the alert dialog
        alert(rawResult)
    }

    private fun alert(rawResult: Result) {
        val builder = AlertDialog.Builder(this@QrRead)
        builder.setTitle("Qr scan result")
        builder.setMessage("""
    Result :${rawResult.text}
    Type :${rawResult.barcodeFormat}
    """.trimIndent())
                .setPositiveButton("Ok") { dialog, id -> // back to previous activity
                    finish()
                }
                .setNegativeButton("Scan Again") { dialog, id -> // User cancelled the dialog
// If you would like to resume scanning, call this method below:
                    mScannerView!!.resumeCameraPreview(this@QrRead)
                }
        // Create the AlertDialog object and return it
        builder.create().show()
    }
}