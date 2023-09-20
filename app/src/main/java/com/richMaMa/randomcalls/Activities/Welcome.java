package com.richMaMa.randomcalls.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.richMaMa.randomcalls.R;

public class Welcome extends AppCompatActivity {

    Button buttonGetStarted;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            Log.e("tilak", "already signned in");
            Intent intent = new Intent(Welcome.this,MainActivity.class);
            startActivity(intent);
            finish();
        }

        buttonGetStarted = findViewById(R.id.button_getstarted);
        buttonGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Welcome.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }
}