package com.example.gupshup;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference userRef;

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        // 🔐 AUTH CHECK
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid);

        // 🔝 TOOLBAR
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 📑 TABS
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        MainPageAdapter adapter = new MainPageAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Chats");
                            break;
                        case 1:
                            tab.setText("Requests");
                            break;
                        case 2:
                            tab.setText("Find Friends");
                            break;
                    }
                }).attach();

        // 🔥 SAFETY WHEN APP KILLED
        userRef.child("status").onDisconnect().setValue("offline");
        userRef.child("lastSeen").onDisconnect()
                .setValue(System.currentTimeMillis());
    }

    // 🟢 ONLINE
    @Override
    protected void onStart() {
        super.onStart();
        setUserStatus("online");
    }

    // 🔴 OFFLINE
    @Override
    protected void onStop() {
        super.onStop();
        setUserStatus("offline");
    }

    private void setUserStatus(@NonNull String status) {
        userRef.child("status").setValue(status);
        if ("offline".equals(status)) {
            userRef.child("lastSeen")
                    .setValue(System.currentTimeMillis());
        }
    }

    // ☰ MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
