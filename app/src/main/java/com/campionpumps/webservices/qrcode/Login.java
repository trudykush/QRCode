package com.campionpumps.webservices.qrcode;

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

public class Login extends AppCompatActivity {

    EditText username, password;
    Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.et_username);
        password = findViewById(R.id.et_password);

        if (isLogged()) {
            goToMainActivity();
        }

        loginBtn = findViewById(R.id.btn_Login);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String userNameTxt = username.getText().toString().trim();
                String passwordTxt = password.getText().toString().trim();

                if (userNameTxt.equalsIgnoreCase(Constants.defaultUserName) &&
                        passwordTxt.equalsIgnoreCase(Constants.defaultPassword)) {

                    Context context = Login.this.getApplicationContext();
                    SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isLogged", true);
                    editor.commit();

                    goToMainActivity();
                } else {
                    Toast.makeText(getApplicationContext(), "Invalid Credentials",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
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
