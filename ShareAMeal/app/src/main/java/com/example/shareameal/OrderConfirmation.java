package com.example.shareameal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shareameal.notifications.NotificationsSender;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

public class OrderConfirmation extends AppCompatActivity {

    private ImageView foodImage;
    private TextView foodNameTxt, foodDescriptionTxt, txtCurrentQuantity, txtSchedule, txtAddress;
    private EditText foodQuantityEdt;

    // data
    private DatabaseReference reference1, reference2, reference3, reference4;
    private Bundle bundle;
    private Slot slot;
    private String donorId, foodId, donorName, recipientId;
    private Food food;
    private User donor, recipient;
    private int orderQuantity;
    private int numOrdersLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F6DABA")));
        getSupportActionBar().setTitle("Confirm Order");
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backarrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        foodImage = findViewById(R.id.foodImage);
        foodNameTxt = findViewById(R.id.foodNameTxt);
        foodDescriptionTxt = findViewById(R.id.foodDescriptionTxt);
        txtCurrentQuantity = findViewById(R.id.txtOrderQuantity);
        txtSchedule = findViewById(R.id.txtSchedule);
        txtAddress = findViewById(R.id.txtAddress);
        foodQuantityEdt = findViewById(R.id.foodQuantityEdt);

        Intent intent = getIntent();
        bundle = intent.getExtras();
        slot = bundle.getParcelable("slot");
        donorId = bundle.getString("donorId");
        foodId = bundle.getString("foodId");
        donorName = bundle.getString("donorName");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        recipientId = user.getUid();

        reference1 = FirebaseDatabase.getInstance().getReference("Foods").child(donorId);

        reference1.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            if (data.getKey().equals(foodId)) {
                                food = data.getValue(Food.class);
                            }
                        }

                        if (food.getImageUrl() == null) {
                            foodImage.setImageResource(R.drawable.dish);
                        } else {
                            if (food.getImageUrl().equals("null")) {
                                foodImage.setImageResource(R.drawable.dish);
                            } else {
                                Picasso.get().load(food.getImageUrl()).into(foodImage);
                            }
                        }

                        reference2 = FirebaseDatabase.getInstance().getReference("Users");

                        reference2.addValueEventListener(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                        for (DataSnapshot data : snapshot.getChildren()) {
                                            if (data.getKey().equals(donorId)) {
                                                donor = data.getValue(User.class);
                                            }
                                            if (data.getKey().equals(recipientId)) {
                                                recipient = data.getValue(User.class);
                                                numOrdersLeft = recipient.getNumOrdersLeft();
                                            }
                                        }

                                        foodNameTxt.setText(food.getName());
                                        foodDescriptionTxt.setText(food.getDescription());
                                        txtCurrentQuantity.setText("Current quantity: " + food.getQuantity());
                                        txtSchedule.setText(
                                                "Scheduled for collection at:\n"
                                                        + slot.getStartTime()
                                                        + " - "
                                                        + slot.getEndTime()
                                                        + ", "
                                                        + slot.getDate());
                                        txtAddress.setText("Address: " + donor.getAddress());
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

    public void onConfirmOrder(View view) {
        if(numOrdersLeft == 0) {
            Toast.makeText(OrderConfirmation.this, "You cannot create any more orders today as you have reached your daily limit.", Toast.LENGTH_SHORT).show();
        } else {
            String qty = foodQuantityEdt.getText().toString();
            if (Integer.valueOf(qty) == 0 || TextUtils.isEmpty(qty)) {
                Toast.makeText(
                        OrderConfirmation.this, "Please enter a number larger than 0.", Toast.LENGTH_SHORT)
                        .show();
            } else if (Integer.valueOf(qty) > food.getQuantity()) {
                Toast.makeText(
                        OrderConfirmation.this,
                        "Please enter at most " + food.getQuantity() + ".",
                        Toast.LENGTH_SHORT)
                        .show();
            } else {
                orderQuantity = Integer.valueOf(qty);

                // create new order
                Order order = new Order();
                order.setDate(slot.getDate());
                order.setStartTime(slot.getStartTime());
                order.setEndTime(slot.getEndTime());
                order.setDonorId(donorId);
                order.setFoodId(foodId);
                order.setQuantity(orderQuantity);
                order.setFoodName(food.getName());
                order.setFoodImageURL(food.getImageUrl());
                order.setSlotId(slot.getSlotId());
                order.setYear(slot.getYear());
                order.setMonth(slot.getMonth());
                order.setDayOfMonth(slot.getDayOfMonth());
                order.setStartHour(slot.getStartHour());
                order.setStartMinute(slot.getStartMinute());

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String userId = user.getUid();
                reference4 = FirebaseDatabase.getInstance().getReference("Orders").child(userId);
                reference4.child(slot.getSlotId()).setValue(order);

                // update food item
                food.setQuantity(food.getQuantity() - orderQuantity);
                reference1.child(foodId).setValue(food);

                // update slot
                slot.setNumRecipients(slot.getNumRecipients() + 1);
                if(slot.getRecipientId1() == null) {
                    slot.setRecipientId1(recipientId);
                } else if(slot.getRecipientId2() == null) {
                    slot.setRecipientId2(recipientId);
                } else if(slot.getRecipientId3() == null) {
                    slot.setRecipientId3(recipientId);
                }
                reference3 = FirebaseDatabase.getInstance().getReference("Slots").child(donorId);
                reference3.child(slot.getSlotId()).setValue(slot);

                // update recipient info
                recipient.setNumOrdersLeft(numOrdersLeft - 1);
                reference2.child(recipientId).setValue(recipient);

                Toast.makeText(
                        OrderConfirmation.this,
                        "Your order has been successfully created.",
                        Toast.LENGTH_SHORT)
                        .show();

                // send notification to donor
                String token = donor.getFcmToken();
                String body = "A recipient has booked the time slot at " + slot.getStartTime() + " on " + slot.getDate();
                NotificationsSender notificationsSender = new NotificationsSender(token, getString(R.string.slot_booked), body, getApplicationContext(), OrderConfirmation.this);
                notificationsSender.sendNotification();

                Intent intent = new Intent(OrderConfirmation.this, RecipientViewOrders.class);
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(OrderConfirmation.this, ReserveFoodItem.class);
        intent.putExtra("donorId", donorId);
        intent.putExtra("foodId", foodId);
        intent.putExtra("donorName", donorName);
        startActivity(intent);
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(OrderConfirmation.this, ReserveFoodItem.class);
        intent.putExtra("donorId", donorId);
        intent.putExtra("foodId", foodId);
        intent.putExtra("donorName", donorName);
        startActivity(intent);
        finish();
    }
}
