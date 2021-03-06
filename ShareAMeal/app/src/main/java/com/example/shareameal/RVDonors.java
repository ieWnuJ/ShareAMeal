package com.example.shareameal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Filterable;
import android.widget.SearchView;
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

import org.jetbrains.annotations.NotNull;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class RVDonors extends AppCompatActivity {
    private RecyclerView recyclerView;
    private BottomNavigationView bottomNav;

    private DatabaseReference reference, foodReference, slotReference;
    private ArrayList<User> users = new ArrayList<>();
    private RVDonorsAdapter adapter;
    private int numOrdersLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rvdonors);

        getWindow().setStatusBarColor(Color.parseColor("#F6DABA"));

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F6DABA")));
        getSupportActionBar().setTitle("Food Donors");

        recyclerView = findViewById(R.id.rv);
        recyclerView.setHasFixedSize(true);
        adapter = new RVDonorsAdapter(this, users);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        reference = FirebaseDatabase.getInstance().getReference("Users");
        foodReference = FirebaseDatabase.getInstance().getReference("Foods");
        slotReference = FirebaseDatabase.getInstance().getReference("Slots").child("Pending");
        loadData();

        // get current userId to check the number of items they can still claim
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        reference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user.getYear() != currentYear || user.getMonth() != currentMonth
                        || user.getDayOfMonth() != currentDayOfMonth) {
                    user.setYear(currentYear);
                    user.setMonth(currentMonth);
                    user.setDayOfMonth(currentDayOfMonth);
                    user.setNumOrdersLeft(3);
                    numOrdersLeft = 3;
                    reference.child(userId).setValue(user);

                } else {
                    numOrdersLeft = user.getNumOrdersLeft();
                }

                if(numOrdersLeft == 0) {
                    Toast.makeText(RVDonors.this, "You cannot create any more orders today as you have reached your daily limit.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.claimFood);
        bottomNav.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        int curr = item.getItemId();
                        if (curr == R.id.home) {
                            Intent intent = new Intent(RVDonors.this, RecipientHomepageActivity.class);
                            startActivity(intent);
                            finish();
                        } else if (curr == R.id.schedule) {
                            Intent intent = new Intent(RVDonors.this, RecipientViewOrders.class);
                            startActivity(intent);
                            finish();
                        } else if (curr == R.id.profile) {
                            Intent intent = new Intent(RVDonors.this, RecipientUserPageActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        return true;
                    }
                });
    }

    private void loadData() {
        reference.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            User donor = data.getValue(User.class);
                            if (donor.getUserGroup().equals("donor")) {
                                // check if donor has any available food items
                                foodReference.child(donor.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                        if(snapshot.exists()) {
                                            // check if donor has any available time slots
                                            slotReference.child(donor.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                    if(snapshot.exists()) {
                                                        users.add(donor);
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                    }
                                });
                            }
                        }

                        recyclerView = findViewById(R.id.rv);
                        recyclerView.setHasFixedSize(true);
                        adapter = new RVDonorsAdapter(RVDonors.this, users);
                        recyclerView.setAdapter(adapter);
                        LinearLayoutManager manager = new LinearLayoutManager(RVDonors.this);
                        recyclerView.setLayoutManager(manager);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu_changelistings, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint("Search food donor here");

        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        adapter.getFilter().filter(newText);
                        return false;
                    }
                });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sort) {
            showSortDialog();
            return true;
        } else if (id == R.id.action_changeListings) {
            showChangeListingsDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSortDialog() {
        // Options to display in dialog
        String[] sortOptions = {"Closest to me"};

        // Create alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle("Sort by")
                .setIcon(R.drawable.ic_filter)
                .setItems(
                        sortOptions,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    FirebaseUser recipient = FirebaseAuth.getInstance().getCurrentUser();
                                    String recipientId = recipient.getUid();
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                    ref.child(recipientId)
                                            .get()
                                            .addOnCompleteListener(
                                                    new OnCompleteListener<DataSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                                                            if (!task.isSuccessful()) {
                                                                Log.e("Firebase", "Error getting data", task.getException());
                                                            } else {
                                                                User user = task.getResult().getValue(User.class);
                                                                double recipientAddressLat = user.getAddressLatitude();
                                                                double recipientAddressLong = user.getAddressLongitude();

                                                                Collections.sort(
                                                                        users,
                                                                        new Comparator<User>() {
                                                                            @Override
                                                                            public int compare(User o1, User o2) {
                                                                                double o1Lat = o1.getAddressLatitude();
                                                                                double o1Long = o1.getAddressLongitude();
                                                                                double o2Lat = o2.getAddressLatitude();
                                                                                double o2Long = o2.getAddressLongitude();
                                                                                float[] o1result = new float[1];
                                                                                float[] o2result = new float[1];
                                                                                Location.distanceBetween(
                                                                                        recipientAddressLat,
                                                                                        recipientAddressLong,
                                                                                        o1Lat,
                                                                                        o1Long,
                                                                                        o1result);
                                                                                Location.distanceBetween(
                                                                                        recipientAddressLat,
                                                                                        recipientAddressLong,
                                                                                        o2Lat,
                                                                                        o2Long,
                                                                                        o2result);
                                                                                float o1distance = o1result[0];
                                                                                float o2distance = o2result[0];
                                                                                if (o1distance > o2distance) {
                                                                                    return 1;
                                                                                } else if (o1distance == o2distance) {
                                                                                    return 0;
                                                                                } else {
                                                                                    return -1;
                                                                                }
                                                                            }
                                                                        });

                                                                adapter.notifyDataSetChanged();
                                                            }
                                                        }
                                                    });
                                }
                            }
                        });
        builder.show();
    }

    private void showChangeListingsDialog() {
        String[] options = {"View by food donors", "View by donated foods"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle("Choose Listing")
                .setIcon(R.drawable.ic_sort_visible)
                .setItems(
                        options,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    Toast.makeText(RVDonors.this, "Already viewing food donors", Toast.LENGTH_SHORT)
                                            .show();
                                } else if (which == 1) {
                                    Intent intent = new Intent(RVDonors.this, RVDonatedFoodListings.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
        builder.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(RVDonors.this, RecipientHomepageActivity.class);
        startActivity(intent);
        finish();
    }
}
