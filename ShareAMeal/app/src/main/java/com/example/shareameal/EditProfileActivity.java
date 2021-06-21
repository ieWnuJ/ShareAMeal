package com.example.shareameal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

public class EditProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private AppCompatButton chooseImageBtn, uploadImageBtn;
    private ImageView profilePicImg;
    private Uri imageUri;
    private StorageReference mStorageRef;

    // Purpose of these boolean values is to ensure user update the profile after he/she has
    // confirmed on the image choice for the profile picture
    private boolean isImageUploaded, isProfileUpdated;

    private AppCompatButton backBtn, updateProfileInfoBtn;
    private EditText usernameEdt, addressEdt, restaurantEdt;
    private String userGroup, imageUrl, oldImageUrl;
    private TextInputLayout restaurantWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // If user is not logged in, direct user to login page. Else, direct to donor homepage.
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(EditProfileActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();

        backBtn = findViewById(R.id.backBtn);
        updateProfileInfoBtn = findViewById(R.id.updateProfileInfoBtn);
        usernameEdt = findViewById(R.id.usernameEdt);
        addressEdt = findViewById(R.id.addressEdt);
        restaurantEdt = findViewById(R.id.restaurantEdt);
        restaurantWrapper = findViewById(R.id.restaurantWrapper);

        // "Update Profile Info" button is clickable only after any of the fields are updated
        usernameEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateProfileInfoBtn.setClickable(true);
                updateProfileInfoBtn.setEnabled(true);
                updateProfileInfoBtn.setBackground(getDrawable(R.drawable.button2));
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        addressEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateProfileInfoBtn.setClickable(true);
                updateProfileInfoBtn.setEnabled(true);
                updateProfileInfoBtn.setBackground(getDrawable(R.drawable.button2));
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        restaurantEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateProfileInfoBtn.setClickable(true);
                updateProfileInfoBtn.setEnabled(true);
                updateProfileInfoBtn.setBackground(getDrawable(R.drawable.button2));
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Profile picture feature: Initialising of widgets and storage
        chooseImageBtn = findViewById(R.id.chooseImageBtn);
        uploadImageBtn = findViewById(R.id.uploadImageBtn);
        uploadImageBtn.setClickable(false);
        uploadImageBtn.setEnabled(false);
        uploadImageBtn.setBackground(getDrawable(R.drawable.disabledbutton));
        profilePicImg = findViewById(R.id.profilePicImg);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("profilePicUploads");
        mStorageRef = storageReference.child(userId);

        // Obtaining the user info from the database and printing them onto the edit fields
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                usernameEdt.setText(user.getName());
                addressEdt.setText(user.getAddress());
                userGroup = user.getUserGroup();
                oldImageUrl = user.getImageUrl();

                // If user is recipient, the "Name of food service" field will be uneditable
                if (user.getUserGroup().equals("recipient")) {
                    restaurantEdt.setEnabled(false);
                    restaurantEdt.setClickable(false);
                    restaurantEdt.setBackground(getDrawable(R.drawable.disablededittext));
                    restaurantWrapper.setHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                } else {
                    if (!TextUtils.isEmpty(user.getRestaurant())) {
                        restaurantEdt.setText(user.getRestaurant());
                    }
                }

                // If no profile pic previously, set default profile pic
                if (oldImageUrl == null) {
                    profilePicImg.setImageResource(R.drawable.profile128px);
                } else {
                    if (oldImageUrl.equals("null")) {
                        profilePicImg.setImageResource(R.drawable.profile128px);
                    } else {
                        Picasso.get().load(oldImageUrl).into(profilePicImg);
                    }
                }

                updateProfileInfoBtn.setClickable(false);
                updateProfileInfoBtn.setEnabled(false);
                updateProfileInfoBtn.setBackground(getDrawable(R.drawable.disabledbutton));
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });

        // Implementing the "Choose Image" and "Confirm Image Choice" buttons
        chooseImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        uploadImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Deleting the old photo from the cloud storage
                if (oldImageUrl != null) {
                    if (!oldImageUrl.equals("null")) {
                        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(oldImageUrl);
                        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {}
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {}
                        });
                    }
                }
                uploadImage();
                updateProfileInfoBtn.setClickable(true);
                updateProfileInfoBtn.setEnabled(true);
                updateProfileInfoBtn.setBackground(getDrawable(R.drawable.button2));
                isImageUploaded = true;
                isProfileUpdated = false;
                uploadImageBtn.setClickable(false);
                uploadImageBtn.setEnabled(false);
                uploadImageBtn.setBackground(getDrawable(R.drawable.disabledbutton));
            }
        });


        // Updating the database of the new user information
        updateProfileInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newUsername = usernameEdt.getText().toString();
                String newAddress = addressEdt.getText().toString();
                String newRestaurant = restaurantEdt.getText().toString();

                if (TextUtils.isEmpty(newUsername)) {
                    Toast.makeText(EditProfileActivity.this, "Must provide username", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(newAddress)) {
                    Toast.makeText(EditProfileActivity.this, "Must provide address", Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseReference databaseReference1 = databaseReference.child(userId);
                    User user = new User();
                    user.setName(newUsername);
                    user.setAddress(newAddress);
                    user.setUserId(userId);
                    user.setUserGroup(userGroup);
                    user.setRestaurant(newRestaurant);

                    if (imageUrl == null) {
                        user.setImageUrl(oldImageUrl);
                    } else {
                        user.setImageUrl(imageUrl);
                    }

                    databaseReference1.setValue(user);
                    databaseReference1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Toast.makeText(EditProfileActivity.this, "Profile information successfully updated", Toast.LENGTH_SHORT).show();
                            updateProfileInfoBtn.setClickable(false);
                            updateProfileInfoBtn.setEnabled(false);
                            updateProfileInfoBtn.setBackground(getDrawable(R.drawable.disabledbutton));
                            isProfileUpdated = true;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(EditProfileActivity.this, "Profile information failed to update", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isImageUploaded) {
                    if (!isProfileUpdated) {
                        Toast.makeText(EditProfileActivity.this, "Please click on \"Update Profile Information\" down below", Toast.LENGTH_SHORT).show();
                    } else {
                        if (userGroup.equals("donor")) {
                            Intent intent = new Intent(EditProfileActivity.this, DonorUserPageActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Intent intent = new Intent(EditProfileActivity.this, RecipientUserPageActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                } else {
                    if (userGroup.equals("donor")) {
                        Intent intent = new Intent(EditProfileActivity.this, DonorUserPageActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(EditProfileActivity.this, RecipientUserPageActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
        uploadImageBtn.setClickable(true);
        uploadImageBtn.setEnabled(true);
        uploadImageBtn.setBackground(getDrawable(R.drawable.button));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(profilePicImg);
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadImage() {
        if (imageUri != null) {
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            fileReference.putFile(imageUri).continueWithTask(
                    new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return fileReference.getDownloadUrl();
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                imageUrl = downloadUri.toString();
                                Toast.makeText(EditProfileActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                            }
                            else { Toast.makeText(EditProfileActivity.this, "Upload failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(EditProfileActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }
}