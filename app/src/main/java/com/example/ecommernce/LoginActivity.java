package com.example.ecommernce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecommernce.Model.Users;
import com.example.ecommernce.Prevalent.Prevalent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {

    private EditText inputPhoneNumber, inputPassword;
    private Button loginButton;
    private ProgressDialog loadingBar;
    private String parentDBname="Users";
    private CheckBox chkBoxRemeberme;
    private TextView adminLink, notAdminLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton=(Button)findViewById(R.id.log_btn);
        inputPassword=(EditText) findViewById(R.id.login_password_input);
        inputPhoneNumber=(EditText) findViewById(R.id.login_phone_number_input);
        loadingBar=new ProgressDialog(this);
        chkBoxRemeberme=(CheckBox)findViewById(R.id.remember_me_chkb);
        //use to store password,names in android studio
        Paper.init(this);

        adminLink=(TextView)findViewById(R.id.admin_panel_link);
        notAdminLink=(TextView)findViewById(R.id.not_admin_panel_link);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        adminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               loginButton.setText("Login Admin");
               adminLink.setVisibility(View.INVISIBLE);
               notAdminLink.setVisibility(View.VISIBLE);
               parentDBname="Admins";
            }
        });

        notAdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.setText("Login");
                adminLink.setVisibility(View.VISIBLE);
                notAdminLink.setVisibility(View.INVISIBLE);
                parentDBname="Users";

            }
        });
    }

    private void loginUser() {
        String password=inputPassword.getText().toString().trim();
        String phoneNumber=inputPhoneNumber.getText().toString().trim();

         if (TextUtils.isEmpty(password)){
            inputPassword.setError("Field Required..");
            return;
        }
        else if (TextUtils.isEmpty(phoneNumber)){
            inputPhoneNumber.setError("Field Required..");
            return;
        }
        else {
             loadingBar.setTitle("Logging in Account");
             loadingBar.setMessage("Please wait, while we are checking the credentials.");
             loadingBar.setCanceledOnTouchOutside(false);
             loadingBar.show();

             allowAccessToAccount(phoneNumber,password);
         }

    }

    private void allowAccessToAccount(final String phoneNumber, final String password) {

        if (chkBoxRemeberme.isChecked()){
            //storing the phoneNumber and password
            Paper.book().write(Prevalent.userPhoneKey,phoneNumber);
            Paper.book().write(Prevalent.UserPasswordKey,password);
        }

        final DatabaseReference rootRef;
        rootRef= FirebaseDatabase.getInstance().getReference();

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(parentDBname).child(phoneNumber).exists()){
                 // getting a data out from a database
                    Users userdata=dataSnapshot.child(parentDBname).child(phoneNumber).getValue(Users.class);
                    if (userdata.getPhone().equals(phoneNumber) )
                    {
                        if (userdata.getPassword().equals(password)){

                            //check for admin
                            if (parentDBname.equals("Admins")){
                                Toast.makeText(LoginActivity.this,"Welcome Admin,you are logged in Successfully",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                Intent intent=new Intent(LoginActivity.this,AdminCategoryActivity.class);
                                startActivity(intent);
                            }
                            else if (parentDBname.equals("Users")){
                                Toast.makeText(LoginActivity.this,"logged in Successfully",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                Intent intent=new Intent(LoginActivity.this,HomeActivity.class);
                                Prevalent.currentOnlineUser=userdata;
                                startActivity(intent);

                            }
                        }
                        else {
                            loadingBar.dismiss();
                            Toast.makeText(LoginActivity.this,"passWord is inCorect",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else {
                    Toast.makeText(LoginActivity.this,"Account wit this "+phoneNumber+" do not exist",Toast.LENGTH_SHORT).show();
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
