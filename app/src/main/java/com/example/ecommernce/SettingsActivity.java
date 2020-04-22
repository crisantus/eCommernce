package com.example.ecommernce;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecommernce.Prevalent.Prevalent;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView profileImageView;
    private EditText fullNameEditText, userPhoneEditText, addressEditText;
    private TextView profileChangeTextBtn, closeTextBtn, saveTextButton;

    private Uri imageUri;
    private String myUri="";
    private StorageTask uploadtask;
    private StorageReference mStorageReference;
    private String checker="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        profileImageView=(CircleImageView) findViewById(R.id.setting_profile_image);
        fullNameEditText=(EditText)findViewById(R.id.settings_full_name);
        userPhoneEditText=(EditText)findViewById(R.id.settings_phone_number);
        addressEditText=(EditText)findViewById(R.id.settings_address);
        profileChangeTextBtn=(TextView)findViewById(R.id.profile_image_change_btn);
        closeTextBtn=(TextView)findViewById(R.id.close_setting);
        saveTextButton=(TextView)findViewById(R.id.update_account_setting_btn);

        //this methos is use to update thr
        userInforDisplay(profileImageView,fullNameEditText,userPhoneEditText,addressEditText);

        mStorageReference= FirebaseStorage.getInstance().getReference().child("Profile pictures");


        closeTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //updateBtn
        saveTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checker.equals("clicked")){
                    userInforSaved();
                }
                else {
                    //this method execute only when the user change information apart from userImage
                    updateOnUserInfor();
                }
            }
        });

        profileChangeTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checker="clicked";
                // start cropping activity for pre-acquired image saved on the device,image selected from gallery
                CropImage.activity(imageUri)
                        .setAspectRatio(1,1)
                        .start(SettingsActivity.this);
            }
        });
    }

    private void updateOnUserInfor() {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("Users");

        HashMap<String,Object> userMap=new HashMap<>();
        userMap.put("name",fullNameEditText.getText().toString());
        userMap.put("address",addressEditText.getText().toString());
        userMap.put("phoneOrder",userPhoneEditText.getText().toString());
        ref.child(Prevalent.currentOnlineUser.getPhone()).updateChildren(userMap);

        Intent intent=new Intent(SettingsActivity.this,MainActivity.class);
        Toast.makeText(SettingsActivity.this, "profile info Update successfully.", Toast.LENGTH_SHORT).show();
        startActivity(intent);
        finish();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode==RESULT_OK && data!=null){

            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            imageUri=result.getUri();
            profileImageView.setImageURI(imageUri);
        }
        else{
            Toast.makeText(this, "Error try again", Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(SettingsActivity.this,SettingsActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void userInforSaved() {
        String fullname=fullNameEditText.getText().toString().trim();
        String phone=userPhoneEditText.getText().toString().trim();
        String address=addressEditText.getText().toString().trim();

        if (TextUtils.isEmpty(fullname)){
            fullNameEditText.setError("Field Required...");
            return;
        }
        else if (TextUtils.isEmpty(phone)) {
            userPhoneEditText.setError("Field Required...");
            return;
        }
        else if (TextUtils.isEmpty(address)) {
            addressEditText.setError("Field Required...");
            return;
        }else if(checker.equals("clicked")){
            //means that image has been selected
           uploadTask();
        }
    }

    private void uploadTask() {

        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Update Profile");
        progressDialog.setMessage("Please wait,while we are updating your account information");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        if (imageUri!=null){
            final StorageReference fileRef=mStorageReference
                    .child(Prevalent.currentOnlineUser.getPhone() + "jpg");
            uploadtask=fileRef.putFile(imageUri);
            //get the result of the upload task
            Task<Uri> uriTaskImageLink = uploadtask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    myUri = fileRef.getDownloadUrl().toString();
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                       // Uri downloadUri=task.getResult();
                        myUri = task.getResult().toString();
                        //myUri=downloadUri.toString();

                        //store it to the database
                        DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("Users");

                        HashMap<String,Object> userMap=new HashMap<>();
                        userMap.put("name",fullNameEditText.getText().toString());
                        userMap.put("address",addressEditText.getText().toString());
                        userMap.put("phoneOrder",userPhoneEditText.getText().toString());
                        userMap.put("image",myUri);
                        ref.child(Prevalent.currentOnlineUser.getPhone()).updateChildren(userMap);

                        progressDialog.dismiss();

                        Intent intent=new Intent(SettingsActivity.this,HomeActivity.class);
                        Toast.makeText(SettingsActivity.this, "profile info Update successfully.", Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                        finish();
                    }

                    else{
                        progressDialog.dismiss();
                        Toast.makeText(SettingsActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else {
            Toast.makeText(this, "Image not selected", Toast.LENGTH_SHORT).show();
        }

    }


    private void userInforDisplay(final CircleImageView profileImageView, final EditText fullNameEditText, final EditText userPhoneEditText, final EditText addressEditText) {
        DatabaseReference userRef= FirebaseDatabase.getInstance().getReference().child("Users").child(Prevalent.currentOnlineUser.getPhone());

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    if (dataSnapshot.child("image").exists()){

                        String image=dataSnapshot.child("image").getValue().toString();
                        String name=dataSnapshot.child("name").getValue().toString();
                        String phone=dataSnapshot.child("phone").getValue().toString();
                        String address=dataSnapshot.child("address").getValue().toString();

                        Picasso.get().load(image).into(profileImageView);
                        fullNameEditText.setText(name);
                        userPhoneEditText.setText(phone);
                        addressEditText.setText(address);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
