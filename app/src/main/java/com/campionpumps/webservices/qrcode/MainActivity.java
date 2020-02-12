package com.campionpumps.webservices.qrcode; /**
 * Created by Kushal.Mishra on 22/02/2018.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.campionpumps.webservices.qrcode.ble.BluetoothLE;
import com.campionpumps.webservices.qrcode.qr.QrGenerate;
import com.campionpumps.webservices.qrcode.qr.QrRead;

import services.AlarmNotifications;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //   FacebookSdk.setApplicationId("com.campionpumps.webservices.qrcode");
        setContentView(R.layout.activity_main);
/*
        Settings.sdkInitialize(this);

        LikeView likeView = (LikeView) findViewById(R.id.like_view);
        likeView.setObjectIdAndType(
                "https://www.facebook.com/FacebookDevelopers",
                LikeView.ObjectType.PAGE);*/

    }

    public void readQr(View view) {
        Intent intent = new Intent(MainActivity.this, QrRead.class);
        startActivity(intent);
    }

    public void generateQr(View view) {
        Intent intent = new Intent(MainActivity.this, QrGenerate.class);
        startActivity(intent);
    }

    public void bluetoothMenu(View view) {
        Intent intent = new Intent(MainActivity.this, BluetoothLE.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        startService(new Intent(MainActivity.this, AlarmNotifications.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.content_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_signout) {
            goToLoginPage();
        }

        return super.onOptionsItemSelected(item);
    }

    private void goToLoginPage() {
        Intent goToLoginPage = new Intent(MainActivity.this, Login.class);

        Context context = MainActivity.this.getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLogged", false);
        editor.commit();

        startActivity(goToLoginPage);
    }

    @Override
    public void onBackPressed() {
        exitApplication();   //To exit application with back button press
    }

    public void exitApplication() {
        Intent exitIntent = new Intent(Intent.ACTION_MAIN);
        exitIntent.addCategory(Intent.CATEGORY_HOME);
        exitIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(exitIntent);
    }
}