package com.example.ecommernce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.ecommernce.Model.Users;
import com.example.ecommernce.Prevalent.Prevalent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    private Button joinNowButton, loginButton;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        joinNowButton=(Button)findViewById(R.id.main_join_now_btn);
        loginButton=(Button)findViewById(R.id.main_log_btn);
        loadingBar=new ProgressDialog(this);

        Paper.init(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        joinNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        String userPhoneKey=Paper.book().read(Prevalent.userPhoneKey);
        String userpasswordKey=Paper.book().read(Prevalent.UserPasswordKey);

        if (userPhoneKey != "" && userpasswordKey != ""){
            if (!TextUtils.isEmpty(userpasswordKey) && !TextUtils.isEmpty(userPhoneKey)){

                AllowAccess(userPhoneKey,userpasswordKey);

                loadingBar.setTitle("Already Logged in");
                loadingBar.setMessage("Please wait....");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
            }
        }
    }

    private void AllowAccess(final String userPhoneKey, final String userpasswordKey) {

        final DatabaseReference rootRef;
        rootRef= FirebaseDatabase.getInstance().getReference();

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("Users").child(userPhoneKey).exists()){
                    // getting a data out from a database
                    Users userdata=dataSnapshot.child("Users").child(userPhoneKey).getValue(Users.class);
                    if (userdata.getPhone().equals(userPhoneKey))
                    {
                        if (userdata.getPassword().equals(userpasswordKey)){
                            Toast.makeText(MainActivity.this,"please Wait,Your already Logged In",Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                            Intent intent=new Intent(MainActivity.this,HomeActivity.class);
                            Prevalent.currentOnlineUser=userdata;//auto login
                            startActivity(intent);
                            finish();
                        }
                        else {
                            loadingBar.dismiss();
                            Toast.makeText(MainActivity.this,"passWord is inCorect",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else {
                    Toast.makeText(MainActivity.this,"Account wit this "+userPhoneKey+" do not exist",Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                    //Toast.makeText(LoginActivity.this,"You need to create a new Account",Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    }
