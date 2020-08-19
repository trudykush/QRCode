package com.campionpumps.webservices.qrcode

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.campionpumps.webservices.qrcode.Login
import com.campionpumps.webservices.qrcode.MainActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*

class Login : AppCompatActivity() {
    var username: EditText? = null
    var password: EditText? = null
    var loginBtn: Button? = null
    private var firebaseAuth: FirebaseAuth? = null
    private var firebaseDatabase: FirebaseDatabase? = null
    private var myDatabaseRef: DatabaseReference? = null
    private var firebaseFirestore: FirebaseFirestore? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        username = et_username
        password = et_password
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        myDatabaseRef = firebaseDatabase!!.getReference("UserDetails")
        //myDatabaseRef.setValue("Kush!");

        // Access a Cloud Firestore instance from your Activity
        firebaseFirestore = FirebaseFirestore.getInstance()
        val registerUser = findViewById<Button>(R.id.btn_register)
        registerUser.setOnClickListener { view: View? ->
            val userNameTxt = username?.text.toString().trim { it <= ' ' }
            val passwordTxt = password?.text.toString().trim { it <= ' ' }
            firebaseAuth!!.createUserWithEmailAndPassword(userNameTxt, passwordTxt)
                    .addOnFailureListener(this@Login) { e: Exception? -> val x = 1 }
                    .addOnCompleteListener(this@Login) { task: Task<AuthResult?> ->
                        Log.d(TAG, "onCreate: Task $task")
                        if (!task.isSuccessful) {
                            Toast.makeText(applicationContext,
                                    "SignUp unsuccessful: " + task.exception!!.message,
                                    Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(applicationContext, "SignUp Successful: ",
                                    Toast.LENGTH_LONG).show()
                        }
                    }
        }

        if (firebaseAuth?.currentUser!= null) {
            FirebaseMessaging.getInstance().subscribeToTopic("Kush")
                    .addOnCompleteListener { task: Task<Void?> ->
                        var msg = "Subscribed"
                        if (!task.isSuccessful) {
                            msg = "Failed to Subscribed"
                        }
                        Log.d(TAG, "onComplete: $msg")
                        Toast.makeText(this@Login, "" + msg, Toast.LENGTH_SHORT).show()
                    }
            Log.d(TAG, "Before Bundle")
            val extras = intent.extras
            if (extras != null && extras.containsKey("test")) {
                Log.d(TAG, "onCreate: The value of FROM FCM is: " + extras.getString("test"))
            }
            goToMainActivity()
        }

        /*if (false) {
            FirebaseMessaging.getInstance().subscribeToTopic("Kush")
                    .addOnCompleteListener { task: Task<Void?> ->
                        var msg = "Subscribed"
                        if (!task.isSuccessful) {
                            msg = "Failed to Subscribed"
                        }
                        Log.d(TAG, "onComplete: $msg")
                        Toast.makeText(this@Login, "" + msg, Toast.LENGTH_SHORT).show()
                    }
            Log.d(TAG, "Before Bundle")
            val extras = intent.extras
            if (extras != null && extras.containsKey("test")) {
                Log.d(TAG, "onCreate: The value of FROM FCM is: " + extras.getString("test"))
            }
            goToMainActivity()
        }*/
        loginBtn = findViewById(R.id.btn_Login)
        loginBtn?.setOnClickListener {
            val userNameTxt = username?.text.toString().trim { it <= ' ' }
            val passwordTxt = password?.text.toString().trim { it <= ' ' }
            firebaseAuth!!.signInWithEmailAndPassword(userNameTxt, passwordTxt)
                    .addOnCompleteListener(this@Login) { task: Task<AuthResult?> ->
                        if (!task.isSuccessful) {
                            Toast.makeText(applicationContext, "Invalid Credentials",
                                    Toast.LENGTH_LONG).show()
                        } else {
                            val context = this@Login.applicationContext
                            val sharedPreferences = context.getSharedPreferences("LoginDetails", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("isLogged", true)
                            editor.apply()

                            /*myDatabaseRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String value = snapshot.getValue(String.class);
                                    Log.d(TAG, "onDataChange: " + value);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    // Failed to read value
                                    Log.w(TAG, "Failed to read value.", error.toException());
                                }
                            });*/
                            val userDetails: MutableMap<String, Any> = HashMap()
                            userDetails["FirstName"] = "SuperMan"
                            userDetails["Password"] = "superPassword"
                            userDetails["Power"] = "Super-Fly"
                            userDetails["Born"] = "Krypton"

                            // Add a new document with a generated ID
                            /* firebaseFirestore.collection("UserDetails")
                                    .add(userDetails)
                                    .addOnSuccessListener(documentReference -> {
                                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w(TAG, "Error adding document", e);
                                    });*/
                            goToMainActivity()
                        }
                    }
        }
    }

    private fun goToMainActivity() {
        val goToMainActivity = Intent(this@Login, MainActivity::class.java)
        startActivity(goToMainActivity)
    }

    private val isLogged: Boolean
        get() {
            val context = this@Login.applicationContext
            val sharedPreferences = context.getSharedPreferences("LoginDetails", MODE_PRIVATE)
            return sharedPreferences.getBoolean("isLogged", false)
        }

    companion object {
        private val TAG = Login::class.java.name
        private const val SENDER_ID = "635890155276"
    }
}