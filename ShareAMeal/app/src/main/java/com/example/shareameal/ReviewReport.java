package com.example.shareameal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shareameal.notifications.NotificationsSender;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

public class ReviewReport extends AppCompatActivity implements RejectReportDialogFragment.DialogListener{
    private final int MAX_ALLOWED_NUMBER_OF_REPORTS = 3;

    private ImageView foodImage, reportImage;
    private TextView foodNameTxt, foodDescriptionTxt, txtOrderQuantity, txtSchedule, reportContentTxt;
    private AppCompatButton rejectReportBtn, acceptReportBtn;

    private DatabaseReference ordersRef, reportsRef, usersRef;
    private String donorId, recipientId, slotId, reportId, foodId, foodName;
    private Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_report);

        Intent intent = getIntent();
        donorId = intent.getStringExtra("donorId");
        recipientId = intent.getStringExtra("recipientId");
        slotId = intent.getStringExtra("slotId");
        reportId = intent.getStringExtra("reportId");
        foodId = intent.getStringExtra("foodId");

        getWindow().setStatusBarColor(Color.parseColor("#F6DABA"));

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F6DABA")));
        getSupportActionBar().setTitle("Review Report");
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backarrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        foodImage = findViewById(R.id.foodImage);
        foodNameTxt = findViewById(R.id.foodNameTxt);
        foodDescriptionTxt = findViewById(R.id.foodDescriptionTxt);
        txtOrderQuantity = findViewById(R.id.txtOrderQuantity);
        txtSchedule = findViewById(R.id.txtSchedule);
        reportImage = findViewById(R.id.reportImage);
        reportContentTxt = findViewById(R.id.reportContent);

        reportsRef = FirebaseDatabase.getInstance().getReference("Reports");
        ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference foodsRef = FirebaseDatabase.getInstance().getReference("Foods");
        DatabaseReference slotsRef = FirebaseDatabase.getInstance().getReference("Slots");

        ordersRef.child("Completed").child(recipientId).child(slotId).child(foodId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                order = task.getResult().getValue(Order.class);
                if (order.getFoodImageURL() == null) {
                    foodImage.setImageResource(R.drawable.dish128);
                } else {
                    if (order.getFoodImageURL().equals("null")) {
                        foodImage.setImageResource(R.drawable.dish128);
                    } else {
                        Picasso.get().load(order.getFoodImageURL()).into(foodImage);
                    }
                }
                foodNameTxt.setText(order.getFoodName());
                String foodId = order.getFoodId();

                foodsRef.child(donorId).child(foodId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                        Food food = task.getResult().getValue(Food.class);
                        foodDescriptionTxt.setText(food.getDescription());
                        foodName = food.getName();
                    }
                });

                txtOrderQuantity.setText("Collected quantity:\n" + String.valueOf(order.getQuantity()));
                txtSchedule.setText("Scheduled for collection at:\n" + order.getDate() + ", " + order.getStartTime() + " - " + order.getEndTime());

                reportsRef.child(donorId).child(reportId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.e("Firebase", "Error getting data", task.getException());
                        } else {
                            Report report = task.getResult().getValue(Report.class);

                            if (report.getReportImageUrl() == null) {
                                reportImage.setImageResource(R.drawable.ic_baseline_insert_photo_128);
                            } else {
                                if (report.getReportImageUrl().equals("null")) {
                                    reportImage.setImageResource(R.drawable.ic_baseline_insert_photo_128);
                                } else {
                                    Picasso.get().load(report.getReportImageUrl()).into(reportImage);
                                }
                            }

                            reportContentTxt.setText("Report Description: " + report.getReportContent());
                        }
                    }
                });
            }
        });

        rejectReportBtn = findViewById(R.id.rejectReportBtn);
        acceptReportBtn = findViewById(R.id.acceptReportBtn);

        rejectReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RejectReportDialogFragment fragment = new RejectReportDialogFragment();
                fragment.show(getSupportFragmentManager(), "Reject Report Reasons Dialog Fragment");
            }
        });

        acceptReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference currReportRef = reportsRef.child(donorId).child(reportId);
                currReportRef.removeValue();

                usersRef.child(donorId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.e("Firebase", "Error getting data", task.getException());
                        } else {
                            User user = task.getResult().getValue(User.class);
                            int numOfReports = user.getNumberOfReports();
                            numOfReports += 1;
                            usersRef.child(donorId).child("numberOfReports").setValue(numOfReports);

                            // Delete user's food listings, slots, orders and reports from the database
                            if (numOfReports >= MAX_ALLOWED_NUMBER_OF_REPORTS) {
                                if (reportsRef.child(donorId) != null) {
                                    reportsRef.child(donorId).removeValue();
                                }
                                if (foodsRef.child(donorId) != null) {
                                    foodsRef.child(donorId).removeValue();
                                }
                                slotsRef.child("Completed").child(donorId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                                        DataSnapshot resultSnapshot = task.getResult();
                                        for (DataSnapshot data : resultSnapshot.getChildren()) {
                                            Slot currSlot = data.getValue(Slot.class);
                                            String slotId = currSlot.getSlotId();
                                            ordersRef.child("Completed").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                                                    DataSnapshot resultSnapshot1 = task.getResult();
                                                    for (DataSnapshot data1 : resultSnapshot1.getChildren()) {
                                                        String recipientId = data1.getKey();
                                                        if (ordersRef.child(recipientId).child(slotId) != null) {
                                                            ordersRef.child(recipientId).child(slotId).removeValue();
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                                slotsRef.child("Completed").child(donorId).removeValue();
                            }
                        }
                    }
                });

                Intent intent = new Intent(ReviewReport.this, AdminViewReportedDonors.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ReviewReport.this, AdminViewReports.class);
        intent.putExtra("donorId", donorId);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(ReviewReport.this, AdminViewReports.class);
        intent.putExtra("donorId", donorId);
        startActivity(intent);
        finish();
        return true;
    }

    @Override
    public void setReasons(String reasons) {
        usersRef.child(recipientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                User recipient = snapshot.getValue(User.class);
                usersRef.child(donorId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        User donor = snapshot.getValue(User.class);
                        String donorName;
                        if(TextUtils.isEmpty(donor.getRestaurant())) {
                            donorName = donor.getName();
                        } else {
                            donorName = donor.getRestaurant();
                        }

                        String recipientToken = recipient.getFcmToken();
                        // send notification to recipient
                        NotificationsSender sender = new NotificationsSender(recipientToken, "Report rejected", "Your report of " + donorName + " (donor) regarding their donation of " + foodName + " has been rejected because: " + reasons, getApplication(), ReviewReport.this);
                        sender.sendNotification();
                        // allow recipient to submit another report
                        order.setReported(false);
                        ordersRef.child("Completed").child(recipientId).child(slotId).child(foodId).setValue(order);

                        // remove rejected report from Database
                        DatabaseReference currReportRef = reportsRef.child(donorId).child(reportId);
                        currReportRef.removeValue();

                        // return to View Reports page
                        Intent intent = new Intent(ReviewReport.this, AdminViewReportedDonors.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}