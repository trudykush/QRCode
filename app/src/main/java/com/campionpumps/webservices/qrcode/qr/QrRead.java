package com.campionpumps.webservices.qrcode.qr;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by Kushal.Mishra on 27/03/2018.
 */

public class QrRead extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    String TAG="QRREADER";

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        permissionForCamera();

        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                        // Set the scanner view as the content view
    }

    private void permissionForCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 110);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 110);
        } else {
            mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
            mScannerView.stopCameraPreview();   //stopPreview
            mScannerView.startCamera();         // Start camera on resume
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.v(TAG, rawResult.getText()); // Prints scan results
        Log.v(TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)

        String qrCodeText = rawResult.getText();
        if (qrCodeText.contains("market")) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
                    ("market://details?id=com.campionpumps.caretaker")));
        } else if (qrCodeText.contains("com.campionpumps.caretaker")) {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.campionpumps.caretaker");
            if (launchIntent != null) {
                startActivity(launchIntent);//null pointer check in case package name was not found
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
                        ("market://details?id=com.campionpumps.caretaker")));
            }
        }  else if (qrCodeText.contains("https://myscadacloud.com")) {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.campionpumps.caretaker");
            if (launchIntent != null) {
                startActivity(launchIntent);//null pointer check in case package name was not found
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
                        ("market://details?id=com.campionpumps.caretaker")));
            }
        }


        // call the alert dialog
        Alert(rawResult);

    }


    public void Alert(Result rawResult){
        AlertDialog.Builder builder = new AlertDialog.Builder(QrRead.this);
        builder.setTitle("Qr scan result");
        builder.setMessage("Result :"+rawResult.getText()+"\nType :"+rawResult.getBarcodeFormat().toString())
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // back to previous activity
                        finish();

                    }
                })
                .setNegativeButton("Scan Again", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
// If you would like to resume scanning, call this method below:
                        mScannerView.resumeCameraPreview(QrRead.this);
                    }
                });
        // Create the AlertDialog object and return it
        builder.create().show();
    }
}
