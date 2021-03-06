package com.example.shareameal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

public class DonorUserPageActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private TextView recordsTxt, editProfileTxt, changePasswordTxt, logoutTxt, donatedFoodsQtyTxt, numberOfReportsTxt;
    private TextView userNameTxt;
    private ImageView userProfilePic;
    private int numberOfReports;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_user_page);

        // Check if user is logged in
        // If user is not logged in, direct user to login page
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(DonorUserPageActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        // Setting name to the registered name of the account
        userNameTxt = findViewById(R.id.userNameTxt);
        userProfilePic = findViewById(R.id.userProfilePic);
        numberOfReportsTxt = findViewById(R.id.numberOfReportTxt);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userid = user.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference
                .child(userid)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User user = snapshot.getValue(User.class);
                                userNameTxt.setText(user.getName());
                                numberOfReports = user.getNumberOfReports();

                                String imageUrl = user.getImageUrl();
                                if (imageUrl == null) {
                                    userProfilePic.setImageResource(R.drawable.profile128px);
                                } else {
                                    if (imageUrl.equals("null")) {
                                        userProfilePic.setImageResource(R.drawable.profile128px);
                                    } else {
                                        Picasso.get().load(imageUrl).into(userProfilePic);
                                    }
                                }

                                if (numberOfReports == 0) {
                                    numberOfReportsTxt.setVisibility(View.GONE);
                                } else {
                                    numberOfReportsTxt.setText("Number of Received Reports: " + numberOfReports);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });

        // Highlighting the right icon in the bottom navigation bar
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.profile);

        // Setting the quantity of donated food items of the donor
        donatedFoodsQtyTxt = findViewById(R.id.donatedFoodsQtyTxt);
        FirebaseDatabase.getInstance().getReference("Users").child(userid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                User currUser = task.getResult().getValue(User.class);
                int numberOfFoodsDonatedAllTime = currUser.getNumberOfPoints();
                int numberOfFoodsDonatedThisWeek = currUser.getNumberOfWeeklyPoints();
                donatedFoodsQtyTxt.setText("All-time: " + String.valueOf(numberOfFoodsDonatedAllTime) + ", This Week: "
                        + String.valueOf(numberOfFoodsDonatedThisWeek));
            }
        });

        // Adding reactions to the different settings
        recordsTxt = findViewById(R.id.recordsTxt);
        editProfileTxt = findViewById(R.id.editProfileTxt);
        changePasswordTxt = findViewById(R.id.changePasswordTxt);
        logoutTxt = findViewById(R.id.logoutTxt);
        recordsTxt.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(DonorUserPageActivity.this, DonorsRecords.class);
                        startActivity(intent);
                        finish();
                    }
                });
        editProfileTxt.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(DonorUserPageActivity.this, EditProfileActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
        changePasswordTxt.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(DonorUserPageActivity.this, ChangePasswordActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
        logoutTxt.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(DonorUserPageActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });

        bottomNav.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        int curr = item.getItemId();
                        if (curr == R.id.food) {
                            if (numberOfReports < 3) {
                                Intent intent = new Intent(DonorUserPageActivity.this, DonateFoodActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(DonorUserPageActivity.this, "You are not allowed access to this page", Toast.LENGTH_SHORT).show();
                            }
                        } else if (curr == R.id.schedule) {
                            if (numberOfReports < 3) {
                                Intent intent = new Intent(DonorUserPageActivity.this, DonorsScheduleActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(DonorUserPageActivity.this, "You are not allowed access to this page", Toast.LENGTH_SHORT).show();
                            }
                        } else if (curr == R.id.home) {
                            Intent intent = new Intent(DonorUserPageActivity.this, DonorHomepageActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        return true;
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(DonorUserPageActivity.this, DonorHomepageActivity.class);
        startActivity(intent);
        finish();
    }
}
