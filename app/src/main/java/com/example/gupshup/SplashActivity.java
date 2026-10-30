package com.example.gupshup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.imgLogo);
        TextView title = findViewById(R.id.txtTitle);
        TextView subtitle = findViewById(R.id.txtSubtitle);

        Animation logoAnim = AnimationUtils.loadAnimation(this, R.anim.logo_anim);
        Animation textAnim = AnimationUtils.loadAnimation(this, R.anim.text_anim);

        logo.startAnimation(logoAnim);
        title.startAnimation(textAnim);
        subtitle.startAnimation(textAnim);

        new Handler().postDelayed(() -> {

            FirebaseAuth mAuth = FirebaseAuth.getInstance();

            if (mAuth.getCurrentUser() != null) {
                // User already logged in
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                // User not logged in
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }

            finish();

        }, 3000);
    }
}
