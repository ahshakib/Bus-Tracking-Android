package com.example.trackcoubus_student;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class SelectionPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection_page);

        Button mBusNo1 = findViewById(R.id.dbutton1);
        mBusNo1.setOnClickListener(view -> {
            Intent intent = new Intent(SelectionPageActivity.this, StudentsMapsActivity.class);
            startActivity(intent);
            finish();
        });
        Button mBusNo2 = findViewById(R.id.dbutton2);
        mBusNo2.setOnClickListener(view -> {
            Intent intent = new Intent(SelectionPageActivity.this, StudentsMapsActivity2.class);
            startActivity(intent);
            finish();
        });
        Button mBusNo3 = findViewById(R.id.dbutton3);
        mBusNo3.setOnClickListener(view -> {
            Intent intent = new Intent(SelectionPageActivity.this, StudentsMapsActivity3.class);
            startActivity(intent);
            finish();
        });
        Button mLogout = (Button) findViewById(R.id.slogout);
        mLogout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(SelectionPageActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}