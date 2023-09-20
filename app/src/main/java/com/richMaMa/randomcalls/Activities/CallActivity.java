package com.richMaMa.randomcalls.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.richMaMa.randomcalls.R;
import com.richMaMa.randomcalls.databinding.ActivityCallBinding;
import com.richMaMa.randomcalls.models.InterfaceJavaScript;
import com.richMaMa.randomcalls.models.User;

import java.util.UUID;

public class CallActivity extends AppCompatActivity {
    String TAG = "CallActivity";
    ActivityCallBinding binding;
    String uniqueId = "";
    FirebaseAuth mAuth;
    String username = "";
    String matchedUsername = "";
    boolean isePeerConnected = false;

    DatabaseReference firebaseRef;
    boolean isAudio = true;
    boolean isVideo = true;
    String createdBy = "";
    boolean pageExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        firebaseRef = FirebaseDatabase.getInstance().getReference().child("users");
        username = getIntent().getStringExtra("username");
        String incoming = getIntent().getStringExtra("incoming");
        createdBy = getIntent().getStringExtra("createdBy");
//        matchedUsername = "";
//
//        if (incoming.equalsIgnoreCase(matchedUsername)) {
//            matchedUsername = incoming;
//        }

        matchedUsername = incoming;

        setUpWebView();
        binding.buttonMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAudio = !isAudio;
                callJavaScriptFunc("javascript:toggleAudio(\"" + isAudio + "\")");
                if (isAudio) {
                    binding.buttonMic.setImageResource(R.drawable.btn_unmute_normal);
                } else {
                    binding.buttonMic.setImageResource(R.drawable.btn_mute_normal);
                }
            }
        });

        binding.buttonVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isVideo = !isVideo;
                callJavaScriptFunc("javascript:toggleVideo(\"" + isVideo + "\")");
                if (isVideo) {
                    binding.buttonVideo.setImageResource(R.drawable.btn_video_normal);
                } else {
                    binding.buttonVideo.setImageResource(R.drawable.btn_video_muted);
                }
            }
        });

        binding.buttonHangUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    void setUpWebView() {
        binding.webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }
        });

        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        binding.webView.addJavascriptInterface(new InterfaceJavaScript(this), "Android");

        loadVideoCall();

    }

    public void loadVideoCall() {
        Log.e(TAG, "loadvideocall");
        String filePath = "file:android_asset/call.html";
        binding.webView.loadUrl(filePath);

        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                initializePeer();
            }
        });
    }

    void initializePeer() {
        uniqueId = getUniqueId();

        callJavaScriptFunc("javascript:init(\"" + uniqueId + "\")");
        //Log.e(TAG, "initializePeer");
        if (createdBy.equalsIgnoreCase(username)) {
            if(pageExit)
                return;
            firebaseRef.child(username).child("connId").setValue(uniqueId);
            firebaseRef.child(username).child("isAvailable").setValue(true);
            binding.loadingAnimationGroup.setVisibility(View.GONE);
            binding.controls.setVisibility(View.VISIBLE);

            FirebaseDatabase.getInstance().getReference()
                    .child("profiles")
                    .child(matchedUsername)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            Glide.with(CallActivity.this).load(user.getProfile())
                                    .into(binding.profilePicture);
                            binding.profileUsername.setText(user.getName());
                            binding.city.setText(user.getCity());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            //Log.e(TAG, "created by us");
        } else {
            //Log.e(TAG, "not created by us");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    matchedUsername = createdBy;
                    FirebaseDatabase.getInstance().getReference()
                            .child("profiles")
                            .child(matchedUsername)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    User user = snapshot.getValue(User.class);
                                    Glide.with(CallActivity.this).load(user.getProfile())
                                            .into(binding.profilePicture);
                                    binding.profileUsername.setText(user.getName());
                                    binding.city.setText(user.getCity());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                    //Log.e(TAG, "matched username = " + matchedUsername);
                    FirebaseDatabase.getInstance().getReference()
                            .child("users")
                            .child(matchedUsername)
                            .child("connId")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    //Log.e(TAG, "onDataChange with" + snapshot.getValue());
                                    if (snapshot.getValue() != null) {
                                        sendCallRequest();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    //Log.e(TAG, "onCancelled");
                                }
                            });
                }
            }, 2000);

        }
    }

    public void onPeerConnected() {
        isePeerConnected = true;
    }

    void sendCallRequest() {
        //Log.e(TAG, "sendCallRequest");
        if (!isePeerConnected) {
            Toast.makeText(CallActivity.this, "You are not Connected, Please check your internet", Toast.LENGTH_SHORT).show();
            return;
        }
        listenConnId();

    }

    void listenConnId() {
        firebaseRef.child(matchedUsername).child("connId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null) {
                    return;
                }
                binding.loadingAnimationGroup.setVisibility(View.GONE);
                binding.controls.setVisibility(View.VISIBLE);
                String connId = snapshot.getValue(String.class);
                //Log.e(TAG, "javascript:startCall");
                callJavaScriptFunc("javascript:startCall(\"" + connId + "\")");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void callJavaScriptFunc(String fun) {
        binding.webView.post(new Runnable() {
            @Override
            public void run() {
                binding.webView.evaluateJavascript(fun, null);
            }
        });
    }

    String getUniqueId() {
        return UUID.randomUUID().toString();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        pageExit = true;
        firebaseRef.child(createdBy).setValue(null);
        finish();
    }
}