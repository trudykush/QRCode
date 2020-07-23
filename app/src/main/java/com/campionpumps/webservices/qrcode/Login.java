package com.campionpumps.webservices.qrcode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    private static final String TAG = Login.class.getName();
    private static final String SENDER_ID = "635890155276";

    EditText username, password;
    Button loginBtn;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference myDatabaseRef;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.et_username);
        password = findViewById(R.id.et_password);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        myDatabaseRef = firebaseDatabase.getReference("UserDetails");
        //myDatabaseRef.setValue("Kush!");

        // Access a Cloud Firestore instance from your Activity
        firebaseFirestore = FirebaseFirestore.getInstance();

        Button registerUser = findViewById(R.id.btn_register);
        registerUser.setOnClickListener(view -> {

            String userNameTxt = username.getText().toString().trim();
            String passwordTxt = password.getText().toString().trim();

            firebaseAuth.createUserWithEmailAndPassword(userNameTxt, passwordTxt)
                    .addOnFailureListener(Login.this, e -> {
                        int x = 1;
                    })
                    .addOnCompleteListener(Login.this, task -> {
                        Log.d(TAG, "onCreate: Task " + task.toString());
                        if (!task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(),
                                    "SignUp unsuccessful: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "SignUp Successful: ",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });


        if (false) {

            FirebaseMessaging.getInstance().subscribeToTopic("Kush")
                    .addOnCompleteListener(task -> {
                        String msg = "Subscribed";
                        if (!task.isSuccessful()) {
                            msg = "Failed to Subscribed";
                        }
                        Log.d(TAG, "onComplete: "+ msg);
                        Toast.makeText(Login.this, "" + msg, Toast.LENGTH_SHORT).show();
                    });

            Log.d(TAG, "Before Bundle" );

            Bundle extras = getIntent().getExtras();
            if(extras != null && extras.containsKey("test")) {
                Log.d(TAG, "onCreate: The value of FROM FCM is: " + extras.getString("test"));
            }

            goToMainActivity();
        }

        loginBtn = findViewById(R.id.btn_Login);

        loginBtn.setOnClickListener(view -> {

            String userNameTxt = username.getText().toString().trim();
            String passwordTxt = password.getText().toString().trim();

            firebaseAuth.signInWithEmailAndPassword(userNameTxt, passwordTxt)
                    .addOnCompleteListener(Login.this, task -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Invalid Credentials",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Context context = Login.this.getApplicationContext();
                            SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("isLogged", true);
                            editor.apply();

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

                            Map<String, Object> userDetails = new HashMap<>();
                            userDetails.put("FirstName", "SuperMan");
                            userDetails.put("Password", "superPassword");
                            userDetails.put("Power", "Super-Fly");
                            userDetails.put("Born", "Krypton");

                            // Add a new document with a generated ID
                            /* firebaseFirestore.collection("UserDetails")
                                    .add(userDetails)
                                    .addOnSuccessListener(documentReference -> {
                                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w(TAG, "Error adding document", e);
                                    });*/

                            goToMainActivity();
                        }
                    });
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        //FirebaseUser currentUser = firebaseAuth.getCurrentUser();
    }

    private void goToMainActivity() {
        Intent goToMainActivity = new Intent(Login.this, MainActivity.class);
        startActivity(goToMainActivity);
    }

    private boolean isLogged() {
        Context context = Login.this.getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", MODE_PRIVATE);
        boolean isLogged = sharedPreferences.getBoolean("isLogged", false);

        return isLogged;
    }
}
