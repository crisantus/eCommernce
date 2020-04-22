package com.example.ecommernce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private Button createAccountButton;
    private EditText inputName, inputPassword, inputPhoneNumer;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        createAccountButton=(Button)findViewById(R.id.register_btn);
        inputName=(EditText) findViewById(R.id.register_username_input);
        inputPassword=(EditText) findViewById(R.id.register_password_input);
        inputPhoneNumer=(EditText) findViewById(R.id.register_phone_number_input);
        loadingBar=new ProgressDialog(this);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createAccount();
            }

        });
    }

    private void createAccount() {
        String name=inputName.getText().toString().trim();
        String password=inputPassword.getText().toString().trim();
        String phoneNumber=inputPhoneNumer.getText().toString().trim();

        if (TextUtils.isEmpty(name)){
            inputName.setError("Field Required..");
            return;
        }
        else if (TextUtils.isEmpty(password)){
            inputPassword.setError("Field Required..");
            return;
        }
        else if (TextUtils.isEmpty(phoneNumber)){
            inputPhoneNumer.setError("Field Required..");
            return;
        }
        else {
            loadingBar.setTitle("Create Account");
            loadingBar.setMessage("Please wait, while we are checking the credentials.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            validatePhoneNumer(name,phoneNumber,password);
        }
    }

    private void validatePhoneNumer(final String name, final String phoneNumber, final String password) {

        final DatabaseReference rootRef;
        rootRef= FirebaseDatabase.getInstance().getReference();

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!(dataSnapshot.child("users").child(phoneNumber).exists())){
                 // using HasMap to insert an object into the firebase
                    HashMap<String,Object> userdataMap=new HashMap<>();
                    userdataMap.put("phone",phoneNumber);
                    userdataMap.put("password",password);
                    userdataMap.put("name",name);
                    rootRef.child("Users").child(phoneNumber).updateChildren(userdataMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(RegisterActivity.this,"Congratulations,your account created successfully",Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                        Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                                        startActivity(intent);
                                    }
                                    else {
                                        loadingBar.dismiss();
                                        Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                else {
                    Toast.makeText(RegisterActivity.this,"This "+phoneNumber+"already exist",Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                    Toast.makeText(RegisterActivity.this,"please try using another phone number",Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                    startActivity(intent);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
