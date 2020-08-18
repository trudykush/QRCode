package com.campionpumps.webservices.qrcode.qr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.campionpumps.webservices.qrcode.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Kushal.Mishra on 27/03/2018.
 */

public class QrGenerate extends AppCompatActivity {

    ImageView shareQrCodeIV;
    EditText qrInputET, emailET;
    private Bitmap mMergedImage;
    String userFormNumberForQrCode;
    String userEmailToSendQrCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_generate);

        emailET = findViewById(R.id.emailET);

        qrInputET = findViewById(R.id.qrInput);
        userFormNumberForQrCode = qrInputET.getText().toString();

        shareQrCodeIV = findViewById(R.id.shareQrCode);
        shareQrCodeIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap generatedQrCodeBitmap = mMergedImage; //null if nothing
                if (!userFormNumberForQrCode.equals("") || generatedQrCodeBitmap != null) {
                    userEmailToSendQrCode = emailET.getText().toString();
                    userFormNumberForQrCode = qrInputET.getText().toString();

                    if (isValidEmail(userEmailToSendQrCode)) {
                        emailImage(generatedQrCodeBitmap, userFormNumberForQrCode, userEmailToSendQrCode);
                    } else {
                        Toast.makeText(getApplicationContext(), "Invalid Email ID", Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "No Qr-Code Generated", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean isValidEmail(String userEmailToSendQrCode) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return userEmailToSendQrCode.matches(regex);
    }

    private void emailImage(Bitmap generatedQrCodeBitmap, String userFormNumberForQrCode, String userEmailToSendQrCode) { // Test
        if (generatedQrCodeBitmap != null) {
            if(userEmailToSendQrCode.equals("")) {
                userEmailToSendQrCode = "kushal@campion.ie";
            }

            Intent emailQrCode = new Intent(Intent.ACTION_SEND);
            emailQrCode.putExtra(Intent.EXTRA_EMAIL, new String[]{userEmailToSendQrCode});
            emailQrCode.putExtra(Intent.EXTRA_SUBJECT, "Qr-Code for " + userFormNumberForQrCode);
            emailQrCode.putExtra(Intent.EXTRA_TEXT, "Please find attached QR-Code for" +
                    " Form ID " + userFormNumberForQrCode);
            emailQrCode.setType("image/*");

            File myDir = new File(Environment.getExternalStorageDirectory() + "/qr_code");
            myDir.mkdirs();

            DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_H_mm_SS", Locale.getDefault());
            Date currentDate = new Date();
            String dateForFileName = dateFormat.format(currentDate);
            String pathToMyAttachedFile = dateForFileName + "__" + userFormNumberForQrCode + "_qr_.png";

            File file = new File(myDir, pathToMyAttachedFile);

            try {
                boolean fileCreated = file.createNewFile();
                if (fileCreated) {
                    // Write bitmap to that file
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    generatedQrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Uri uri = FileProvider.getUriForFile(QrGenerate.this, QrGenerate.this.getApplicationContext().getPackageName()
                    + ".provider", file);

            emailQrCode.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            emailQrCode.putExtra(Intent.EXTRA_STREAM, uri);

            startActivity(Intent.createChooser(emailQrCode, "Sending Qr-Code..."));
        }
    }


    // This method is called onClick to generate QRCode
    public void qrGenerator(View v){
        try {
            //setting size of qr code
            WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            int width = point.x;
            int height = point.y;
            int smallestDimension = Math.min(width, height);

            EditText qrInput = (EditText) findViewById(R.id.qrInput);
            String qrCodeData = qrInput.getText().toString();
            //setting parameters for qr code
            String charset = "UTF-8"; // or "ISO-8859-1"
            Map<EncodeHintType, ErrorCorrectionLevel> hintMap =new HashMap<EncodeHintType, ErrorCorrectionLevel>();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            createQRCode(qrCodeData, charset, hintMap, smallestDimension, smallestDimension);

        } catch (Exception ex) {
            Log.e("QrGenerate",ex.getMessage());
        }
    }

    public  void createQRCode(String qrCodeData, String charset, Map hintMap, int qrCodeheight, int qrCodewidth){

        try {
            //generating qr code in bitMatrix type
            BitMatrix matrix = new MultiFormatWriter().encode(new String(qrCodeData.getBytes(charset), charset), BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight, hintMap);
//            BitMatrix matrix = new MultiFormatWriter().encode(new String(qrCodeData.getBytes(charset), charset), BarcodeFormat.CODE_128, qrCodewidth, qrCodeheight, hintMap);
            //converting bitMatrix to bitmap
            int width = matrix.getWidth();
            int height = matrix.getHeight();
            int[] pixels = new int[width * height];
            // All are 0, or black, by default
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = matrix.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            //setting bitmap to image view
            ImageView myImage = (ImageView) findViewById(R.id.imageView1);

            Bitmap overlay = BitmapFactory.decodeResource(getResources(), R.drawable.logo_new);
            mMergedImage = mergeBitmaps(overlay,bitmap);
            myImage.setImageBitmap(mMergedImage);
            //myImage.setImageBitmap(bitmap);
//            myImage.setImageBitmap(bitmap);
        }catch (Exception er){
            Log.e("QrGenerate",er.getMessage());
        }
    }

    public Bitmap mergeBitmaps(Bitmap overlay, Bitmap bitmap) {

        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        Bitmap combined = Bitmap.createBitmap(width, height, bitmap.getConfig());
        Canvas canvas = new Canvas(combined);
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        canvas.drawBitmap(bitmap, new Matrix(), null);

        int centreX = (canvasWidth  - overlay.getWidth()) /2;
        int centreY = (canvasHeight - overlay.getHeight()) /2;
        canvas.drawBitmap(overlay, centreX, centreY, null);

        return combined;
    }

}
