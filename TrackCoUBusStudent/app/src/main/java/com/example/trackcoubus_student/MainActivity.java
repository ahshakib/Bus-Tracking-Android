package com.example.trackcoubus_student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private EditText mEmail, mPassword;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int STORAGE_PERMISSION_CODE = 1;
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, STORAGE_PERMISSION_CODE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, STORAGE_PERMISSION_CODE);
        }

        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        firebaseAuthListener = firebaseAuth -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user != null){
                Intent intent = new Intent(MainActivity.this, SelectionPageActivity.class);
                startActivity(intent);
                finish();
            }
        };

        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);

        Button mLogin = (Button) findViewById(R.id.login);
        Button mRegistration = (Button) findViewById(R.id.registration);

        mRegistration.setOnClickListener(v -> {
            final String email = mEmail.getText().toString();
            final String password = mPassword.getText().toString();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(MainActivity.this, task -> {
                if(!task.isSuccessful()){
                    Toast.makeText(MainActivity.this, "Registration Error", Toast.LENGTH_SHORT).show();
                }
                else{
                    String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                    DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Students").child(user_id);
                    current_user_db.setValue(true);
                }
            });
        });

        mLogin.setOnClickListener(view -> {
            final String email = mEmail.getText().toString();
            final String password = mPassword.getText().toString();
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(MainActivity.this, task -> {
                if(!task.isSuccessful()){
                    Toast.makeText(MainActivity.this, "Login Error", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
}