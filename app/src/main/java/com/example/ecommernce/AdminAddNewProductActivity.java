package com.example.ecommernce;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AdminAddNewProductActivity extends AppCompatActivity {

    private String catagoryName, description, pName, price, saveCurrentDate, saveCurrentTime;
    private Button addNewProductButton;
    private EditText inputProductName, inputProductPrice, inputProductDescription;
    private ImageView inputProductImage;
    public static final int GALLARY_PICK = 123;
    public static final int PERMISSION_CODE = 1;
    private Uri imageUri;
    private String productRandomKey, downLoadImageUrl;
    private StorageReference productImagesRef;
    private DatabaseReference productRef;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_new_product);

        catagoryName = getIntent().getExtras().get("category").toString();
        productImagesRef = FirebaseStorage.getInstance().getReference().child("Product Images");
        productRef = FirebaseDatabase.getInstance().getReference().child("Product");
        loadingBar = new ProgressDialog(this);

        addNewProductButton = (Button) findViewById(R.id.add_new_product);
        inputProductImage = (ImageView) findViewById(R.id.select_product_image);
        inputProductName = (EditText) findViewById(R.id.product_name);
        inputProductPrice = (EditText) findViewById(R.id.product_price);
        inputProductDescription = (EditText) findViewById(R.id.product_description);

        inputProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //openGallary();
                //check for runtime permission
                checkPermission();
            }
        });

        addNewProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateProductdata();
            }
        });
    }

    private void ValidateProductdata() {
        description = inputProductDescription.getText().toString().trim();
        pName = inputProductName.getText().toString().trim();
        price = inputProductPrice.getText().toString().trim();

        if (imageUri == null) {
            Toast.makeText(this, "Product Image is Mandatory", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            inputProductDescription.setError("Field Required...");
            return;
        } else if (TextUtils.isEmpty(pName)) {
            inputProductName.setError("Field Required...");
            return;
        } else if (TextUtils.isEmpty(price)) {
            inputProductPrice.setError("Field Required...");
            return;
        } else {
            //store Image to firebase database/storageFirebase
            storeProductInformation();
        }

    }

    private void storeProductInformation() {
        //pushing the image to the firebase database/storage
        loadingBar.setTitle("Adding new product");
        loadingBar.setMessage("Please wait, while we are checking the credentials.");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        productRandomKey = saveCurrentDate + saveCurrentTime;
        //link generated to store image inside firebase storage
        final StorageReference filePath = productImagesRef.child(imageUri.getLastPathSegment() + productRandomKey + "jpg");

        final UploadTask uploadTask = filePath.putFile(imageUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = e.toString();
                Toast.makeText(AdminAddNewProductActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(AdminAddNewProductActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                //get the link of image store it in firebase database
                Task<Uri> uriTaskImageLink = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        downLoadImageUrl = filePath.getDownloadUrl().toString();
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            downLoadImageUrl = task.getResult().toString();
                            Toast.makeText(AdminAddNewProductActivity.this, "got the Product image save to Database Successfully..", Toast.LENGTH_SHORT).show();
                            //store all the information of the new product to the database
                            saveProductInfoToDatabase();

                        }
                    }
                });
            }
        });


    }

    private void saveProductInfoToDatabase() {
        // adding product object to firebase Database
        HashMap<String, Object> productMap = new HashMap<>();
        productMap.put("pId", productRandomKey);
        productMap.put("date", saveCurrentDate);
        productMap.put("time", saveCurrentTime);
        productMap.put("description", description);
        productMap.put("image", downLoadImageUrl);
        productMap.put("category", catagoryName);
        productMap.put("price", price);
        productMap.put("pname", pName);

        productRef.child(productRandomKey).updateChildren(productMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            Intent intent = new Intent(AdminAddNewProductActivity.this, AdminCategoryActivity.class);
                            startActivity(intent);

                            loadingBar.dismiss();
                            Toast.makeText(AdminAddNewProductActivity.this, "product is added successfully..", Toast.LENGTH_SHORT).show();
                        } else {

                            loadingBar.dismiss();
                            String message = task.getException().toString();
                            Toast.makeText(AdminAddNewProductActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void openGallary() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"pick an image"),GALLARY_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLARY_PICK && resultCode == RESULT_OK && data != null) {
            //setting image view
            imageUri = data.getData();
            inputProductImage.setImageURI(imageUri);
        }
    }

    public void checkPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                //permission not granted, request it.
                String[] permission ={Manifest.permission.READ_EXTERNAL_STORAGE};
                //show popup for runtime permission
                requestPermissions(permission, PERMISSION_CODE);
            }
            else {
                //permission already granted
                openGallary();
            }
        }
        else {
            openGallary();
        }
    }


     //handle result of runtime permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_CODE:{
                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    //permission was granted
                    openGallary();
                }
                else {
                    //permission was denied
                    Toast.makeText(this, "permission denied...!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}