package com.campionpumps.webservices.qrcode

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.campionpumps.webservices.qrcode.ble.BluetoothLE
import com.campionpumps.webservices.qrcode.qr.QrGenerate
import com.campionpumps.webservices.qrcode.qr.QrRead
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import services.AlarmNotifications
import java.io.IOException

/**
 * Created by Kushal.Mishra on 22/02/2018.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //   FacebookSdk.setApplicationId("com.campionpumps.webservices.qrcode");
        setContentView(R.layout.activity_main)
        /*
        Settings.sdkInitialize(this);

        LikeView likeView = (LikeView) findViewById(R.id.like_view);
        likeView.setObjectIdAndType(
                "https://www.facebook.com/FacebookDevelopers",
                LikeView.ObjectType.PAGE);*/
        sendPushNotification()
    }

    private fun sendPushNotification() {
        val fm = FirebaseMessaging.getInstance()
        fm.send(RemoteMessage.Builder(SENDER_ID + "@fcm.googleapis.com")
                .setMessageId(Integer.toString(1))
                .addData("Title", "Hello World")
                .addData("Message", "SAY_HELLO")
                .build())
    }

    fun readQr() {
        val db = FirebaseFirestore.getInstance()
        db.collection("UserDetails")
                .get()
                .addOnCompleteListener { task: Task<QuerySnapshot> ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            Log.d(TAG, document.id + " => " + document.data)
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.exception)
                    }
                }
        val intent = Intent(this@MainActivity, QrRead::class.java)
        startActivity(intent)
    }

    fun generateQr() {
        val intent = Intent(this@MainActivity, QrGenerate::class.java)
        startActivity(intent)
    }

    fun bluetoothMenu(view: View?) {
        val intent = Intent(this@MainActivity, BluetoothLE::class.java)
        startActivity(intent)
    }

    override fun onPause() {
        super.onPause()
        startService(Intent(this@MainActivity, AlarmNotifications::class.java))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.content_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_signout) {
            goToLoginPage()
        } else if (id == R.id.push_notification) {
            jsonArray.put(refreshedToken)
            sendMessage(jsonArray, "Hello", "How r u", "Http:\\google.com", "My Name is Kush").execute()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun goToLoginPage() {
        val goToLoginPage = Intent(this@MainActivity, Login::class.java)
        val context = this@MainActivity.applicationContext
        val sharedPreferences = context.getSharedPreferences("LoginDetails", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLogged", false)
        editor.apply()

        val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signOut()

        startActivity(goToLoginPage)
    }

    override fun onBackPressed() {
        exitApplication() //To exit application with back button press
    }

    private fun exitApplication() {
        val exitIntent = Intent(Intent.ACTION_MAIN)
        exitIntent.addCategory(Intent.CATEGORY_HOME)
        exitIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(exitIntent)
    }

    private var mClient = OkHttpClient()

    //add your user refresh tokens who are logged in with firebase.
    var refreshedToken = ""
    var jsonArray = JSONArray()
    @Throws(IOException::class)
    fun postToFCM(bodyString: String?): String {
        val FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send"
        //        final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/v1/projects/qrcode-29aa2/messages";
        val JSON = MediaType.parse("application/json; charset=utf-8")
        val body = RequestBody.create(JSON, bodyString)
        val request = Request.Builder()
                .url(FCM_MESSAGE_URL)
                .post(body)
                .addHeader("Authorization", "key=" + "")
                .build()
        val response = mClient.newCall(request).execute()
        return response.body()!!.string()
    }

    private inner class sendMessage(var recipients: JSONArray, var title: String, var body: String, var icon: String, var message: String) : AsyncTask<String?, String?, String?>() {

        override fun onPostExecute(result: String?) {
            try {
                val resultJson = JSONObject(result)
                val success: Int
                val failure: Int
                success = resultJson.getInt("success")
                failure = resultJson.getInt("failure")
            Toast.makeText(this@MainActivity, "Message Success: " + success + "Message Failed: " + failure, Toast.LENGTH_LONG).show()
            } catch (e: JSONException) {
                e.printStackTrace()
            Toast.makeText(this@MainActivity, "Message Failed, Unknown error occurred.", Toast.LENGTH_LONG).show()
            }
        }

        override fun doInBackground(vararg p0: String?): String? {
            try {
                val root = JSONObject()
                val notification = JSONObject()
                notification.put("body", body)
                notification.put("title", title)
                notification.put("icon", icon)
                val data = JSONObject()
                data.put("Title", title)
                data.put("Message", message)
                root.put("notification", notification)
                root.put("data", data)
                root.put("topic", "Kush")
                //                root.put("registration_ids", recipients);
                val result = postToFCM(root.toString())
                Log.d("Main Activity", "Result: $result")
                return result
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return null
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.name
        private const val SENDER_ID = "635890155276"
    }
}