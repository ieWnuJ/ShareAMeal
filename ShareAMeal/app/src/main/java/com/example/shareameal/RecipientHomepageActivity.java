package com.example.shareameal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
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

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class RecipientHomepageActivity extends AppCompatActivity {
    // From Monday 00:00:00 to Sunday 23:59:59, 1 second = 1000 ms
    private final long ONE_WEEK_IN_MS = 604800000L;

    private BottomNavigationView bottomNav;
    private TextView userNameTxt, reminderTxt, refreshTimestampTxt, weeklyStatusTxt;
    private int numOrdersLeft, verificationState;
    private ConstraintLayout verifyMsg, verifyInProcessMsg, rejectMsg, unclearDocMsg;
    private AppCompatButton verifyBtn, verifyAgainBtn;
    private TextView leaderboardOneFirstTxt, leaderboardOneFirstQtyTxt, leaderboardOneSecondTxt, leaderboardOneSecondQtyTxt,
            leaderboardOneThirdTxt, leaderboardOneThirdQtyTxt, leaderboardOneFourthTxt, leaderboardOneFourthQtyTxt,
            leaderboardOneFifthTxt, leaderboardOneFifthQtyTxt;
    private TextView leaderboardTwoFirstTxt, leaderboardTwoFirstQtyTxt, leaderboardTwoSecondTxt, leaderboardTwoSecondQtyTxt,
            leaderboardTwoThirdTxt, leaderboardTwoThirdQtyTxt, leaderboardTwoFourthTxt, leaderboardTwoFourthQtyTxt,
            leaderboardTwoFifthTxt, leaderboardTwoFifthQtyTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipient_homepage);

        // Check if user is logged in
        // If user is not logged in, direct user to login page
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(RecipientHomepageActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        userNameTxt = findViewById(R.id.userNameTxt);
        reminderTxt = findViewById(R.id.reminderTxt);

        verifyMsg = findViewById(R.id.verifyMsg);
        verifyInProcessMsg = findViewById(R.id.verifyInProcessMsg);
        rejectMsg = findViewById(R.id.rejectMsg);
        unclearDocMsg = findViewById(R.id.unclearDocMsg);
        verifyBtn = findViewById(R.id.verifyBtn);
        verifyAgainBtn = findViewById(R.id.verifyAgainBtn);
        refreshTimestampTxt = findViewById(R.id.refreshTimestampTxt);
        weeklyStatusTxt = findViewById(R.id.weeklyStatusTxt);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userid = user.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        leaderboardOneFirstTxt = findViewById(R.id.leaderboardOneFirstTxt);
        leaderboardOneFirstQtyTxt = findViewById(R.id.leaderboardOneFirstQtyTxt);
        leaderboardOneSecondTxt = findViewById(R.id.leaderboardOneSecondTxt);
        leaderboardOneSecondQtyTxt = findViewById(R.id.leaderboardOneSecondQtyTxt);
        leaderboardOneThirdTxt = findViewById(R.id.leaderboardOneThirdTxt);
        leaderboardOneThirdQtyTxt = findViewById(R.id.leaderboardOneThirdQtyTxt);
        leaderboardOneFourthTxt = findViewById(R.id.leaderboardOneFourthTxt);
        leaderboardOneFourthQtyTxt = findViewById(R.id.leaderboardOneFourthQtyTxt);
        leaderboardOneFifthTxt = findViewById(R.id.leaderboardOneFifthTxt);
        leaderboardOneFifthQtyTxt = findViewById(R.id.leaderboardOneFifthQtyTxt);
        ArrayList<TextView> leaderboardOneTextViews = new ArrayList<>(Arrays.asList(leaderboardOneFirstTxt, leaderboardOneSecondTxt,
                leaderboardOneThirdTxt, leaderboardOneFourthTxt, leaderboardOneFifthTxt, leaderboardOneFirstQtyTxt, leaderboardOneSecondQtyTxt,
                leaderboardOneThirdQtyTxt, leaderboardOneFourthQtyTxt, leaderboardOneFifthQtyTxt));

        leaderboardTwoFirstTxt = findViewById(R.id.leaderboardTwoFirstTxt);
        leaderboardTwoFirstQtyTxt = findViewById(R.id.leaderboardTwoFirstQtyTxt);
        leaderboardTwoSecondTxt = findViewById(R.id.leaderboardTwoSecondTxt);
        leaderboardTwoSecondQtyTxt = findViewById(R.id.leaderboardTwoSecondQtyTxt);
        leaderboardTwoThirdTxt = findViewById(R.id.leaderboardTwoThirdTxt);
        leaderboardTwoThirdQtyTxt = findViewById(R.id.leaderboardTwoThirdQtyTxt);
        leaderboardTwoFourthTxt = findViewById(R.id.leaderboardTwoFourthTxt);
        leaderboardTwoFourthQtyTxt = findViewById(R.id.leaderboardTwoFourthQtyTxt);
        leaderboardTwoFifthTxt = findViewById(R.id.leaderboardTwoFifthTxt);
        leaderboardTwoFifthQtyTxt = findViewById(R.id.leaderboardTwoFifthQtyTxt);
        ArrayList<TextView> leaderboardTwoTextViews = new ArrayList<>(Arrays.asList(leaderboardTwoFirstTxt, leaderboardTwoSecondTxt,
                leaderboardTwoThirdTxt, leaderboardTwoFourthTxt, leaderboardTwoFifthTxt, leaderboardTwoFirstQtyTxt, leaderboardTwoSecondQtyTxt,
                leaderboardTwoThirdQtyTxt, leaderboardTwoFourthQtyTxt, leaderboardTwoFifthQtyTxt));

        // Setting up weekly leaderboard
        FirebaseDatabase.getInstance().getReference("WeeklyLeaderboard").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                DataSnapshot snapshot = task.getResult();
                long lastRefreshedTimeInMs = (long) snapshot.child("lastRefreshedTimeInMs").getValue();
                long currentTimeInMs = Calendar.getInstance().getTimeInMillis();
                long refreshIntervalInMin = (long) snapshot.child("refreshInterval").getValue();
                long refreshIntervalInMs = refreshIntervalInMin * 60000L;
                long startDateInMs = (long) snapshot.child("startDateInMs").getValue();

                // Resetting weekly points of donors if a week has passed since start date
                if (currentTimeInMs >= startDateInMs + ONE_WEEK_IN_MS) {
                    reference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                            DataSnapshot snapshot = task.getResult();
                            for (DataSnapshot data : snapshot.getChildren()) {
                                User currUser = data.getValue(User.class);
                                if (currUser.getUserGroup().equals("donor")) {
                                    String donorId = currUser.getUserId();
                                    reference.child(donorId).child("numberOfWeeklyPoints").setValue(0);
                                }
                            }
                        }
                    });
                    long timeDifferentialInMs = currentTimeInMs - startDateInMs;
                    long numberOfWeeksPassed = timeDifferentialInMs / ONE_WEEK_IN_MS;
                    long latestWeekInMs = startDateInMs + (numberOfWeeksPassed * ONE_WEEK_IN_MS);
                    String latestStartDate = getDate(latestWeekInMs, "dd MMM yyyy");
                    String latestStartDateAndTime = getDate(latestWeekInMs, "dd MMM yyyy HH:mm:ss");
                    String latestEndDate = getDate(latestWeekInMs + ONE_WEEK_IN_MS - 1000, "dd MMM yyyy");
                    FirebaseDatabase.getInstance().getReference("WeeklyLeaderboard").child("startDate").setValue(latestStartDateAndTime);
                    FirebaseDatabase.getInstance().getReference("WeeklyLeaderboard").child("startDateInMs").setValue(latestWeekInMs);
                    weeklyStatusTxt.setText("(" + latestStartDate + " - " + latestEndDate + ")");
                } else {
                    String startDate = getDate(startDateInMs, "dd MMM yyyy");
                    String endDate = getDate(startDateInMs + ONE_WEEK_IN_MS - 1000, "dd MMM yyyy");
                    weeklyStatusTxt.setText("(" + startDate + " - " + endDate + ")");
                }

                // Refreshes weekly leaderboard according to refresh interval
                if (currentTimeInMs > lastRefreshedTimeInMs + refreshIntervalInMs) {
                    reference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                            ArrayList<User> donorWeeklyRankings = new ArrayList<>();
                            DataSnapshot snapshot = task.getResult();
                            for (DataSnapshot data : snapshot.getChildren()) {
                                User currUser = data.getValue(User.class);
                                if (currUser.getUserGroup().equals("donor")) {
                                    if (currUser.getNumberOfReports() < 3) {
                                        donorWeeklyRankings.add(currUser);
                                    }
                                }
                            }
                            Collections.sort(donorWeeklyRankings, new Comparator<User>() {
                                @Override
                                public int compare(User o1, User o2) {
                                    if (o1.getNumberOfWeeklyPoints() < o2.getNumberOfWeeklyPoints()) {
                                        return 1;
                                    } else if (o1.getNumberOfWeeklyPoints() > o2.getNumberOfWeeklyPoints()) {
                                        return -1;
                                    } else {
                                        return 0;
                                    }
                                }
                            });
                            for (int i = 0; i < 5; i++) {
                                User rankedUser = donorWeeklyRankings.get(i);
                                LeaderboardRanking ranking = new LeaderboardRanking();
                                ranking.setPosition(i + 1);
                                ranking.setDonorId(rankedUser.getUserId());
                                if (rankedUser.getRestaurant() == null || TextUtils.isEmpty(rankedUser.getRestaurant())) {
                                    ranking.setDonorName(rankedUser.getName());
                                } else {
                                    ranking.setDonorName(rankedUser.getRestaurant());
                                }
                                ranking.setNumberOfPoints(rankedUser.getNumberOfWeeklyPoints());
                                FirebaseDatabase.getInstance().getReference("WeeklyLeaderboard").child(String.valueOf(i + 1)).setValue(ranking);
                            }
                            for (int i = 1; i <= 5; i++) {
                                int pos = i;
                                FirebaseDatabase.getInstance().getReference("WeeklyLeaderboard").child(String.valueOf(i)).get()
                                        .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                                                LeaderboardRanking currRanking = task.getResult().getValue(LeaderboardRanking.class);
                                                if (currRanking.getDonorId().equals(userid)) {
                                                    leaderboardOneTextViews.get(pos - 1).setText(String.valueOf(pos) + ". " + currRanking.getDonorName() + " (Me)");
                                                    leaderboardOneTextViews.get(pos - 1).setTextColor(Color.parseColor("#C45508"));
                                                    leaderboardOneTextViews.get(pos - 1).setTypeface(null, Typeface.BOLD);
                                                    leaderboardOneTextViews.get(pos + 4).setText(String.valueOf(currRanking.getNumberOfPoints()));
                                                    leaderboardOneTextViews.get(pos + 4).setTextColor(Color.parseColor("#C45508"));
                                                    leaderboardOneTextViews.get(pos + 4).setTypeface(null, Typeface.BOLD);
                                                } else {
                                                    leaderboardOneTextViews.get(pos - 1).setText(String.valueOf(pos) + ". " + currRanking.getDonorName());
                                                    leaderboardOneTextViews.get(pos + 4).setText(String.valueOf(currRanking.getNumberOfPoints()));
                                                }
                                            }
                                        });
                            }
                        }
                    });
                } else {
                    for (int i = 1; i <= 5; i++) {
                        int pos = i;
                        FirebaseDatabase.getInstance().getReference("WeeklyLeaderboard").child(String.valueOf(i)).get()
                                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                                        LeaderboardRanking currRanking = task.getResult().getValue(LeaderboardRanking.class);
                                        if (currRanking.getDonorId().equals(userid)) {
                                            leaderboardOneTextViews.get(pos - 1).setText(String.valueOf(pos) + ". " + currRanking.getDonorName() + " (Me)");
                                            leaderboardOneTextViews.get(pos - 1).setTextColor(Color.parseColor("#C45508"));
                                            leaderboardOneTextViews.get(pos - 1).setTypeface(null, Typeface.BOLD);
                                            leaderboardOneTextViews.get(pos + 4).setText(String.valueOf(currRanking.getNumberOfPoints()));
                                            leaderboardOneTextViews.get(pos + 4).setTextColor(Color.parseColor("#C45508"));
                                            leaderboardOneTextViews.get(pos + 4).setTypeface(null, Typeface.BOLD);
                                        } else {
                                            leaderboardOneTextViews.get(pos - 1).setText(String.valueOf(pos) + ". " + currRanking.getDonorName());
                                            leaderboardOneTextViews.get(pos + 4).setText(String.valueOf(currRanking.getNumberOfPoints()));
                                        }
                                    }
                                });
                    }
                }
            }
        });

        // Setting up all time leaderboard
        FirebaseDatabase.getInstance().getReference("AllTimeLeaderboard").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                DataSnapshot snapshot = task.getResult();
                long lastRefreshedTimeInMs = (long) snapshot.child("lastRefreshedTimeInMs").getValue();
                long currentTimeInMs = Calendar.getInstance().getTimeInMillis();
                long refreshIntervalInMin = (long) snapshot.child("refreshInterval").getValue();
                long refreshIntervalInMs = refreshIntervalInMin * 60000L;

                // If current system time has passed the last refreshed time by a set interval, refresh the leaderboard standings in database
                if (currentTimeInMs > lastRefreshedTimeInMs + refreshIntervalInMs) {
                    reference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                            ArrayList<User> donorRankings = new ArrayList<>();
                            DataSnapshot snapshot = task.getResult();
                            for (DataSnapshot data : snapshot.getChildren()) {
                                User currUser = data.getValue(User.class);
                                if (currUser.getUserGroup().equals("donor")) {
                                    if (currUser.getNumberOfReports() < 3) {
                                        donorRankings.add(currUser);
                                    }
                                }
                            }
                            Collections.sort(donorRankings, new Comparator<User>() {
                                @Override
                                public int compare(User o1, User o2) {
                                    if (o1.getNumberOfPoints() < o2.getNumberOfPoints()) {
                                        return 1;
                                    } else if (o1.getNumberOfPoints() > o2.getNumberOfPoints()) {
                                        return -1;
                                    } else {
                                        return 0;
                                    }
                                }
                            });
                            for (int i = 0; i < 5; i++) {
                                User rankedUser = donorRankings.get(i);
                                LeaderboardRanking ranking = new LeaderboardRanking();
                                ranking.setPosition(i + 1);
                                ranking.setDonorId(rankedUser.getUserId());
                                if (rankedUser.getRestaurant() == null || TextUtils.isEmpty(rankedUser.getRestaurant())) {
                                    ranking.setDonorName(rankedUser.getName());
                                } else {
                                    ranking.setDonorName(rankedUser.getRestaurant());
                                }
                                ranking.setNumberOfPoints(rankedUser.getNumberOfPoints());
                                FirebaseDatabase.getInstance().getReference("AllTimeLeaderboard").child(String.valueOf(i + 1)).setValue(ranking);
                            }

                            long timeDifferentialInMs = currentTimeInMs - lastRefreshedTimeInMs;
                            long numberOfIntervalsPassed = timeDifferentialInMs / refreshIntervalInMs;
                            long latestRefreshedTimeInMs = lastRefreshedTimeInMs + (numberOfIntervalsPassed * refreshIntervalInMs);
                            String latestRefreshedDateAndTime = getDate(latestRefreshedTimeInMs, "dd MMM yyyy HH:mm:ss");
                            FirebaseDatabase.getInstance().getReference("AllTimeLeaderboard").child("lastRefreshedTime").setValue(latestRefreshedDateAndTime);
                            FirebaseDatabase.getInstance().getReference("AllTimeLeaderboard").child("lastRefreshedTimeInMs").setValue(latestRefreshedTimeInMs);
                            FirebaseDatabase.getInstance().getReference("WeeklyLeaderboard").child("lastRefreshedTime").setValue(latestRefreshedDateAndTime);
                            FirebaseDatabase.getInstance().getReference("WeeklyLeaderboard").child("lastRefreshedTimeInMs").setValue(latestRefreshedTimeInMs);
                            refreshTimestampTxt.setText("Last refreshed at: " + latestRefreshedDateAndTime + "\n(Refreshed every " +
                                    String.valueOf(refreshIntervalInMin) + " mins)");

                            for (int i = 1; i <= 5; i++) {
                                int pos = i;
                                FirebaseDatabase.getInstance().getReference("AllTimeLeaderboard").child(String.valueOf(i)).get()
                                        .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                                                LeaderboardRanking currRanking = task.getResult().getValue(LeaderboardRanking.class);
                                                if (currRanking.getDonorId().equals(userid)) {
                                                    leaderboardTwoTextViews.get(pos - 1).setText(String.valueOf(pos) + ". " + currRanking.getDonorName() + " (Me)");
                                                    leaderboardTwoTextViews.get(pos - 1).setTextColor(Color.parseColor("#C45508"));
                                                    leaderboardTwoTextViews.get(pos - 1).setTypeface(null, Typeface.BOLD);
                                                    leaderboardTwoTextViews.get(pos + 4).setText(String.valueOf(currRanking.getNumberOfPoints()));
                                                    leaderboardTwoTextViews.get(pos + 4).setTextColor(Color.parseColor("#C45508"));
                                                    leaderboardTwoTextViews.get(pos + 4).setTypeface(null, Typeface.BOLD);
                                                } else {
                                                    leaderboardTwoTextViews.get(pos - 1).setText(String.valueOf(pos) + ". " + currRanking.getDonorName());
                                                    leaderboardTwoTextViews.get(pos + 4).setText(String.valueOf(currRanking.getNumberOfPoints()));
                                                }
                                            }
                                        });
                            }
                        }
                    });
                } else {
                    for (int i = 1; i <= 5; i++) {
                        int pos = i;
                        FirebaseDatabase.getInstance().getReference("AllTimeLeaderboard").child(String.valueOf(i)).get()
                                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                                        LeaderboardRanking currRanking = task.getResult().getValue(LeaderboardRanking.class);
                                        if (currRanking.getDonorId().equals(userid)) {
                                            leaderboardTwoTextViews.get(pos - 1).setText(String.valueOf(pos) + ". " + currRanking.getDonorName() + " (Me)");
                                            leaderboardTwoTextViews.get(pos - 1).setTextColor(Color.parseColor("#C45508"));
                                            leaderboardTwoTextViews.get(pos - 1).setTypeface(null, Typeface.BOLD);
                                            leaderboardTwoTextViews.get(pos + 4).setText(String.valueOf(currRanking.getNumberOfPoints()));
                                            leaderboardTwoTextViews.get(pos + 4).setTextColor(Color.parseColor("#C45508"));
                                            leaderboardTwoTextViews.get(pos + 4).setTypeface(null, Typeface.BOLD);
                                        } else {
                                            leaderboardTwoTextViews.get(pos - 1).setText(String.valueOf(pos) + ". " + currRanking.getDonorName());
                                            leaderboardTwoTextViews.get(pos + 4).setText(String.valueOf(currRanking.getNumberOfPoints()));
                                        }
                                    }
                                });
                    }
                    FirebaseDatabase.getInstance().getReference("AllTimeLeaderboard").get()
                            .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                                    refreshTimestampTxt.setText("Last refreshed at: " + task.getResult().child("lastRefreshedTime").getValue()
                                            + "\n(Refreshed every " + String.valueOf(task.getResult().child("refreshInterval").getValue()) + " mins)");
                                }
                            });
                }
            }
        });

        leaderboardOneFirstTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecipientHomepageActivity.this, ShowUserProfile.class);
                intent.putExtra("leaderboard", "weekly");
                intent.putExtra("rank", "1");
                startActivity(intent);
                finish();
            }
        });
        leaderboardOneSecondTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecipientHomepageActivity.this, ShowUserProfile.class);
                intent.putExtra("leaderboard", "weekly");
                intent.putExtra("rank", "2");
                startActivity(intent);
                finish();
            }
        });
        leaderboardOneThirdTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecipientHomepageActivity.this, ShowUserProfile.class);
                intent.putExtra("leaderboard", "weekly");
                intent.putExtra("rank", "3");
                startActivity(intent);
                finish();
            }
        });
        leaderboardOneFourthTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecipientHomepageActivity.this, ShowUserProfile.class);
                intent.putExtra("leaderboard", "weekly");
                intent.putExtra("rank", "4");
                startActivity(intent);
                finish();
            }
        });
        leaderboardOneFifthTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecipientHomepageActivity.this, ShowUserProfile.class);
                intent.putExtra("leaderboard", "weekly");
                intent.putExtra("rank", "5");
                startActivity(intent);
                finish();
            }
        });

        leaderboardTwoFirstTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecipientHomepageActivity.this, ShowUserProfile.class);
                intent.putExtra("leaderboard", "all");
                intent.putExtra("rank", "1");
                startActivity(intent);
                finish();
            }
        });
        leaderboardTwoSecondTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecipientHomepageActivity.this, ShowUserProfile.class);
                intent.putExtra("leaderboard", "all");
                intent.putExtra("rank", "2");
                startActivity(intent);
                finish();
            }
        });
        leaderboardTwoThirdTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecipientHomepageActivity.this, ShowUserProfile.class);
                intent.putExtra("leaderboard", "all");
                intent.putExtra("rank", "3");
                startActivity(intent);
                finish();
            }
        });
        leaderboardTwoFourthTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecipientHomepageActivity.this, ShowUserProfile.class);
                intent.putExtra("leaderboard", "all");
                intent.putExtra("rank", "4");
                startActivity(intent);
                finish();
            }
        });
        leaderboardTwoFifthTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecipientHomepageActivity.this, ShowUserProfile.class);
                intent.putExtra("leaderboard", "all");
                intent.putExtra("rank", "5");
                startActivity(intent);
                finish();
            }
        });

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecipientHomepageActivity.this, RecipientSendVerification.class);
                startActivity(intent);
                finish();
            }
        });

        verifyAgainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecipientHomepageActivity.this, RecipientSendVerification.class);
                startActivity(intent);
                finish();
            }
        });

        reference
                .child(userid)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User user = snapshot.getValue(User.class);
                                userNameTxt.setText(user.getName());

                                if (user.getYear() != currentYear || user.getMonth() != currentMonth
                                   || user.getDayOfMonth() != currentDayOfMonth) {
                                    user.setYear(currentYear);
                                    user.setMonth(currentMonth);
                                    user.setDayOfMonth(currentDayOfMonth);
                                    user.setNumOrdersLeft(3);
                                    numOrdersLeft = 3;
                                    reference.child(userid).setValue(user);

                                } else {
                                    numOrdersLeft = user.getNumOrdersLeft();
                                }

                                reminderTxt.setText("Daily claimable food items left: " + numOrdersLeft);

                                verificationState = user.getVerificationState();
                                if (verificationState == 0) {
                                    verifyInProcessMsg.setVisibility(View.GONE);
                                    rejectMsg.setVisibility(View.GONE);
                                    unclearDocMsg.setVisibility(View.GONE);
                                } else if (verificationState == 1) {
                                    verifyMsg.setVisibility(View.GONE);
                                    rejectMsg.setVisibility(View.GONE);
                                    unclearDocMsg.setVisibility(View.GONE);
                                } else if (verificationState == 2) {
                                    verifyInProcessMsg.setVisibility(View.GONE);
                                    verifyMsg.setVisibility(View.GONE);
                                    rejectMsg.setVisibility(View.GONE);
                                    unclearDocMsg.setVisibility(View.GONE);
                                } else if (verificationState == 3) {
                                    verifyInProcessMsg.setVisibility(View.GONE);
                                    verifyMsg.setVisibility(View.GONE);
                                    unclearDocMsg.setVisibility(View.GONE);
                                } else if (verificationState == 4) {
                                    verifyInProcessMsg.setVisibility(View.GONE);
                                    verifyMsg.setVisibility(View.GONE);
                                    rejectMsg.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        int curr = item.getItemId();
                        if (curr == R.id.claimFood) {
                            if (verificationState == 2) {
                                Intent intent = new Intent(RecipientHomepageActivity.this, RVDonors.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(RecipientHomepageActivity.this, "You are not allowed access to this page", Toast.LENGTH_SHORT).show();
                                bottomNav.setSelectedItemId(R.id.home);
                            }
                        } else if (curr == R.id.schedule) {
                            if (verificationState == 2) {
                                Intent intent = new Intent(RecipientHomepageActivity.this, RecipientViewOrders.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(RecipientHomepageActivity.this, "You are not allowed access to this page", Toast.LENGTH_SHORT).show();
                                bottomNav.setSelectedItemId(R.id.home);
                            }
                        } else if (curr == R.id.profile) {
                            Intent intent =
                                    new Intent(RecipientHomepageActivity.this, RecipientUserPageActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        return true;
                    }
                });
    }

    @Override
    public void onBackPressed() {

    }

    private String getDate(long milliSeconds, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}
