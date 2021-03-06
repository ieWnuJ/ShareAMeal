package com.example.shareameal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DonorsScheduleActivity extends AppCompatActivity
        implements RVSlotsAdapter.OnSlotClickListener {

    private FloatingActionButton btnAddTimeSlot;
    private RecyclerView recyclerView;
    private BottomNavigationView bottomNav;

    private DatabaseReference reference;
    private ArrayList<Slot> slots = new ArrayList<>();
    private RVSlotsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donors_schedule);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F6DABA")));
        getSupportActionBar().setTitle("Time Slots for Food Donation");

        getWindow().setStatusBarColor(Color.parseColor("#F6DABA"));

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        String donorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef.child(donorId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                int numberOfReports = user.getNumberOfReports();
                if (numberOfReports >= 3) {
                    Intent intent = new Intent(DonorsScheduleActivity.this, DonorHomepageActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });

        recyclerView = findViewById(R.id.rv);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        adapter = new RVSlotsAdapter(this, this);
        recyclerView.setAdapter(adapter);

        reference = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        reference = reference.child("Slots").child("Pending").child(userId);

        loadData();

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.schedule);
        bottomNav.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        int curr = item.getItemId();
                        if (curr == R.id.profile) {
                            Intent intent = new Intent(DonorsScheduleActivity.this, DonorUserPageActivity.class);
                            startActivity(intent);
                            finish();
                        } else if (curr == R.id.food) {
                            Intent intent = new Intent(DonorsScheduleActivity.this, DonateFoodActivity.class);
                            startActivity(intent);
                            finish();
                        } else if (curr == R.id.home) {
                            Intent intent = new Intent(DonorsScheduleActivity.this, DonorHomepageActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        return true;
                    }
                });

        btnAddTimeSlot = findViewById(R.id.btnAddTimeSlot);
        btnAddTimeSlot.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(DonorsScheduleActivity.this, AddNewTimeSlotActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    private void loadData() {
        reference.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Slot slot = data.getValue(Slot.class);
                            slots.add(slot);
                        }
                        Collections.sort(slots, new Comparator<Slot>() {
                            @Override
                            public int compare(Slot o1, Slot o2) {
                                if(o1.getYear() < o2.getYear()) {
                                    return -1;
                                } else if (o1.getYear() > o2.getYear()) {
                                    return 1;
                                } else {
                                    if(o1.getMonth() < o2.getMonth()) {
                                        return -1;
                                    } else if (o1.getMonth() > o2.getMonth()) {
                                        return 1;
                                    } else {
                                        if(o1.getDayOfMonth() < o2.getDayOfMonth()) {
                                            return -1;
                                        } else if(o1.getDayOfMonth() > o2.getDayOfMonth()) {
                                            return 1;
                                        } else {
                                            if(o1.getStartHour() < o2.getStartHour()) {
                                                return -1;
                                            } else if (o1.getStartHour() > o2.getStartHour()) {
                                                return 1;
                                            } else {
                                                return 0;
                                            }
                                        }
                                    }
                                }
                            }
                        });
                        adapter.setItems(slots);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    }
                });
    }

    @Override
    public void onSlotClick(int position) {
        Intent intent = new Intent(DonorsScheduleActivity.this, DonorViewSlot.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("slot", slots.get(position));
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(DonorsScheduleActivity.this, DonorHomepageActivity.class);
        startActivity(intent);
        finish();
    }
}
