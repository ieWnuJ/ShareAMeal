package com.example.shareameal;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RVDonorsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private Context context;
    private ArrayList<User> list = new ArrayList<>();
    private ArrayList<User> listFull;
    private OnDonorClickListener onDonorClickListener;

    public RVDonorsAdapter(Context ctx, OnDonorClickListener onDonorClickListener) {
        this.context = ctx;
        this.onDonorClickListener = onDonorClickListener;
    }

    public void setItems(ArrayList<User> users) {
        list.addAll(users);
        listFull = new ArrayList<>(list);
    }

    public class DonorVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView txt_donor, txt_donor_address, txt_donor_distance;
        public ImageView image;
        public OnDonorClickListener onDonorClickListener;

        public DonorVH(@NonNull @NotNull View itemView, OnDonorClickListener onDonorClickListener) {
            super(itemView);
            txt_donor = itemView.findViewById(R.id.txt_donor);
            txt_donor_address = itemView.findViewById(R.id.txt_donor_address);
            txt_donor_distance = itemView.findViewById(R.id.txt_donor_distance);
            image = itemView.findViewById(R.id.img_donor);
            this.onDonorClickListener = onDonorClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onDonorClickListener.onDonorClick(getBindingAdapterPosition());
        }
    }

    @NonNull
    @org.jetbrains.annotations.NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @org.jetbrains.annotations.NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_rvdonor_item, parent, false);
        return new DonorVH(view, onDonorClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull @org.jetbrains.annotations.NotNull RecyclerView.ViewHolder holder, int position) {
        DonorVH vh = (DonorVH) holder;
        // probably change to donor later
        User donor = list.get(position);
        if (TextUtils.isEmpty(donor.getRestaurant())) {
            vh.txt_donor.setText(donor.getName());
        } else {
            vh.txt_donor.setText(donor.getRestaurant());
        }
        vh.txt_donor_address.setText(donor.getAddress());

        FirebaseUser recipient = FirebaseAuth.getInstance().getCurrentUser();
        String recipientId = recipient.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(recipientId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("Firebase", "Error getting data", task.getException());
                } else {
                    User user = task.getResult().getValue(User.class);
                    double recipientAddressLat = user.getAddressLatitude();
                    double recipientAddressLong = user.getAddressLongitude();
                    double donorAddressLat = donor.getAddressLatitude();
                    double donorAddressLong = donor.getAddressLongitude();
                    float[] result = new float[1];

                    // distanceBtwn is in metres
                    Location.distanceBetween(recipientAddressLat, recipientAddressLong, donorAddressLat, donorAddressLong, result);
                    float distanceBtwnInMetres = result[0];
                    float distanceBtwnInKm = distanceBtwnInMetres / 1000;
                    DecimalFormat df = new DecimalFormat("#.##");
                    df.setRoundingMode(RoundingMode.CEILING);
                    String distanceStr = String.valueOf(df.format(distanceBtwnInKm)) + " km away";
                    vh.txt_donor_distance.setText(distanceStr);
                }
            }
        });

        if (donor.getImageUrl() == null) {
            vh.image.setImageResource(R.drawable.profile128px);
        } else {
            if (donor.getImageUrl().equals("null")) {
                vh.image.setImageResource(R.drawable.profile128px);
            } else {
                Picasso.get().load(donor.getImageUrl()).into(vh.image);
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnDonorClickListener {
        void onDonorClick(int position);
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<User> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(listFull);
            } else {
                String filteredPattern = constraint.toString().toLowerCase().trim();

                for (User user : listFull) {
                    if (user.getRestaurant() == null || TextUtils.isEmpty(user.getRestaurant())) {
                        if (user.getName().toLowerCase().contains(filteredPattern)) {
                            filteredList.add(user);
                        }
                    } else {
                        if (user.getRestaurant().toLowerCase().contains(filteredPattern)) {
                            filteredList.add(user);
                        }
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            results.count = filteredList.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            list.clear();
            list.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

}
