package com.campionpumps.webservices.qrcode; /**
 * Created by Kushal.Mishra on 22/02/2018.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.campionpumps.webservices.qrcode.ble.BluetoothLE;
import com.campionpumps.webservices.qrcode.qr.QrGenerate;
import com.campionpumps.webservices.qrcode.qr.QrRead;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import services.AlarmNotifications;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = MainActivity.class.getName();
    private static final String SENDER_ID = "635890155276";

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

        sendPushNotification();
    }


    private void sendPushNotification() {

        FirebaseMessaging fm = FirebaseMessaging.getInstance();
        fm.send(new RemoteMessage.Builder(SENDER_ID + "@fcm.googleapis.com")
                .setMessageId(Integer.toString(1))
                .addData("Title", "Hello World")
                .addData("Message","SAY_HELLO")
                .build());
    }

    public void readQr(View view) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("UserDetails")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.getData());
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });

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
        } else if (id == R.id.push_notification) {

            jsonArray.put(refreshedToken);
            new send0Message(jsonArray,"Hello","How r u","Http:\\google.com","My Name is Kush").execute();
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


    OkHttpClient mClient = new OkHttpClient();
    //add your user refresh tokens who are logged in with firebase.
    String refreshedToken = "";
    JSONArray jsonArray = new JSONArray();


    String postToFCM(String bodyString) throws IOException {
        final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
//        final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/v1/projects/qrcode-29aa2/messages";
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        RequestBody body = RequestBody.create(JSON, bodyString);
        Request request = new Request.Builder()
                .url(FCM_MESSAGE_URL)
                .post(body)
                .addHeader("Authorization", "key=" + "")
                .build();
        Response response = mClient.newCall(request).execute();
        return response.body().string();
    }

    private class send0Message extends AsyncTask<String, String, String>  {
        JSONArray recipients;
        String title;
        String body;
        String icon;
        String message;
        public send0Message(final JSONArray recipients, final String title, final String body, final String icon, final String message) {
            this.recipients = recipients;
            this.title = title;
            this.body = body;
            this.icon = icon;
            this.message = message;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                JSONObject root = new JSONObject();
                JSONObject notification = new JSONObject();
                notification.put("body", body);
                notification.put("title", title);
                notification.put("icon", icon);

                JSONObject data = new JSONObject();
                data.put("Title", title);
                data.put("Message", message);
                root.put("notification", notification);
                root.put("data", data);
                root.put("topic", "Kush");
//                root.put("registration_ids", recipients);

                String result = postToFCM(root.toString());
                Log.d("Main Activity", "Result: " + result);
                return result;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject resultJson = new JSONObject(result);
                int success, failure;
                success = resultJson.getInt("success");
                failure = resultJson.getInt("failure");
                Toast.makeText(MainActivity.this, "Message Success: " + success + "Message Failed: " + failure, Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Message Failed, Unknown error occurred.", Toast.LENGTH_LONG).show();
            }
        }


    }
}