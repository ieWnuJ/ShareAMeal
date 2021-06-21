package com.example.shareameal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

public class ViewOrder extends AppCompatActivity {

    private Button btnBack, btnCancelOrder;
    private ImageView foodImage;
    private TextView foodNameTxt, foodDescriptionTxt, txtOrderQuantity, txtSchedule, txtAddress;

    private DatabaseReference reference1, reference2, reference3, reference4;
    private String donorId, foodId, slotId;
    private Order order;
    private Food food;
    private User donor;
    private int orderQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_order);

        btnBack = findViewById(R.id.btnBack);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);

        foodImage = findViewById(R.id.foodImage);
        foodNameTxt = findViewById(R.id.foodNameTxt);
        foodDescriptionTxt = findViewById(R.id.foodDescriptionTxt);
        txtOrderQuantity = findViewById(R.id.txtOrderQuantity);
        txtSchedule = findViewById(R.id.txtSchedule);
        txtAddress = findViewById(R.id.txtAddress);

        Intent intent = getIntent();

        donorId = intent.getStringExtra("donorId");
        foodId = intent.getStringExtra("foodId");
        slotId = intent.getStringExtra("slotId");

        reference1 = FirebaseDatabase.getInstance().getReference("Foods").child(donorId);
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot data : snapshot.getChildren()) {
                    if(data.getKey().equals(foodId)) {
                        food = data.getValue(Food.class);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();

        reference2 = FirebaseDatabase.getInstance().getReference("Orders").child(userId);
        reference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot data : snapshot.getChildren()) {
                    if(data.getKey().equals(slotId)) {
                        order = data.getValue(Order.class);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        reference3 = FirebaseDatabase.getInstance().getReference("Users");
        reference3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot data : snapshot.getChildren()) {
                    if(data.getKey().equals(donorId)) {
                        donor = data.getValue(User.class);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        if (food.getImageUrl() == null) {
            foodImage.setImageResource(R.drawable.dish);
        } else {
            if (food.getImageUrl().equals("null")) {
                foodImage.setImageResource(R.drawable.dish);
            } else {
                Picasso.get().load(food.getImageUrl()).into(foodImage);
            }
        }

        orderQuantity = order.getQuantity();

        foodNameTxt.setText(food.getName());
        foodDescriptionTxt.setText(food.getDescription());
        txtOrderQuantity.setText("Order quantity: " + orderQuantity);
        txtSchedule.setText("Scheduled for collection at:\n" + order.getStartTime() + " - " + order.getEndTime() + ", " + order.getDate());
        txtAddress.setText("Address: " + donor.getAddress());
    }

    public void onBackBtn(View view) {
        Intent intent = new Intent(ViewOrder.this, RecipientViewOrders.class);
        startActivity(intent);
        finish();
    }

    public void onCancelOrder(View view) {
        //remove order
        reference2.child(slotId).removeValue();

        //remove slot
        reference4 = FirebaseDatabase.getInstance().getReference("Slots").child(donorId);
        reference4.child(slotId).removeValue();

        //update food quantity
        food.setQuantity(food.getQuantity() + orderQuantity);
        reference1.child(foodId).setValue(food);

        Intent intent = new Intent(ViewOrder.this, RecipientViewOrders.class);
        startActivity(intent);
        finish();
    }
}