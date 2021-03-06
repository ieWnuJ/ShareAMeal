package com.example.shareameal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SignupActivity_getUserName extends AppCompatActivity {

    private String username, restaurant;
    private EditText edtUsername, edtRestaurant;
    private Button nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_get_user_name);

        edtUsername = findViewById(R.id.edtUsername);
        edtRestaurant = findViewById(R.id.edtRestaurant);

        nextBtn = findViewById(R.id.nextBtn1);
        nextBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        username = edtUsername.getText().toString().trim();
                        restaurant = edtRestaurant.getText().toString().trim();

                        if (TextUtils.isEmpty(username)) {
                            Toast.makeText(
                                    SignupActivity_getUserName.this,
                                    "Please fill in your username",
                                    Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            Intent intent =
                                    new Intent(SignupActivity_getUserName.this, SignupActivity_getAddress.class);
                            intent.putExtra("userGroup", "donor");
                            intent.putExtra("username", username);
                            intent.putExtra("restaurant", restaurant);
                            startActivity(intent);
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(SignupActivity_getUserName.this, "Please complete account registration process!", Toast.LENGTH_SHORT).show();
    }
}
