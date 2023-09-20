package com.richMaMa.randomcalls.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.richMaMa.randomcalls.R;
import com.richMaMa.randomcalls.databinding.ActivityConnectingBinding;
import com.richMaMa.randomcalls.databinding.ActivityMainBinding;

import java.util.HashMap;

public class ConnectingActivity extends AppCompatActivity {
    final String TAG = "ConnectingActivity";
    ActivityConnectingBinding binding;
    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    boolean isOkey = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConnectingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        String profile = getIntent().getStringExtra("profile");
        Glide.with(this)
                .load(profile)
                .into(binding.imageviewProfile);

        String userame = mAuth.getUid();

        mDatabase.getReference().child(getString(R.string.child_users))
                .orderByChild(getString(R.string.child_status))
                .equalTo(0).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.e(TAG, "onDataChange child = " + snapshot.getChildrenCount());
                        if (snapshot.getChildrenCount() > 0) {
                            //room available
                            isOkey = true;
                            Log.e(TAG, "room available");
                            for (DataSnapshot childSnap : snapshot.getChildren()) {
                                mDatabase.getReference()
                                        .child(getString(R.string.child_users))
                                        .child(childSnap.getKey())
                                        .child(getString(R.string.child_incoming))
                                        .setValue(userame);
                                mDatabase.getReference()
                                        .child(getString(R.string.child_users))
                                        .child(childSnap.getKey())
                                        .child(getString(R.string.child_status))
                                        .setValue(1);
                                Intent intent = new Intent(ConnectingActivity.this, CallActivity.class);
                                String incoming = childSnap.child(getString(R.string.child_incoming)).getValue(String.class);
                                String createdBy = childSnap.child(getString(R.string.child_createdBy)).getValue(String.class);
                                boolean isAvailable = childSnap.child(getString(R.string.child_isAvailable)).getValue(boolean.class);
                                intent.putExtra("username", userame);
                                intent.putExtra("incoming", incoming);
                                intent.putExtra("createdBy", createdBy);
                                intent.putExtra("isAvailable", isAvailable);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            //room not available, create new room
                            Log.e(TAG, "room not available");
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("incoming", userame);
                            map.put("createdBy", userame);
                            map.put("isAvailable", true);
                            map.put("status", 0);

                            mDatabase.getReference()
                                    .child(getString(R.string.child_users))
                                    .child(userame)
                                    .setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            mDatabase.getReference()
                                                    .child(getString(R.string.child_users))
                                                    .child(userame).addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if (snapshot.child(getString(R.string.child_status)).exists()) {
                                                                if (snapshot.child(getString(R.string.child_status)).getValue(Integer.class) == 1) {
                                                                    if (isOkey)
                                                                        return;
                                                                    isOkey = true;
                                                                    Intent intent = new Intent(ConnectingActivity.this, CallActivity.class);
                                                                    String incoming = snapshot.child(getString(R.string.child_incoming)).getValue(String.class);
                                                                    String createdBy = snapshot.child(getString(R.string.child_createdBy)).getValue(String.class);
                                                                    boolean isAvailable = snapshot.child(getString(R.string.child_isAvailable)).getValue(boolean.class);
                                                                    intent.putExtra("username", userame);
                                                                    intent.putExtra("incoming", incoming);
                                                                    intent.putExtra("createdBy", createdBy);
                                                                    intent.putExtra("isAvailable", isAvailable);
                                                                    startActivity(intent);
                                                                    finish();
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }
}