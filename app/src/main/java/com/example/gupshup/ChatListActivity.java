package com.example.gupshup;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class ChatListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ChatsAdapter adapter; // Ensure this matches your Adapter class name
    ArrayList<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        recyclerView = findViewById(R.id.recyclerChats);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        // Note: Using ChatsAdapter which we optimized earlier
        adapter = new ChatsAdapter(this, userList);
        recyclerView.setAdapter(adapter);

        loadChatList();
    }

    private void loadChatList() {
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid == null) return;

        // 1. Get all Users first (to show available chats)
        FirebaseDatabase.getInstance().getReference("Users")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            User user = ds.getValue(User.class);

                            // Don't show yourself in the chat list
                            if (user != null && !user.getUid().equals(currentUid)) {
                                // Check if a message room exists for this user pair
                                checkMessageHistory(currentUid, user);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void checkMessageHistory(String myUid, User otherUser) {
        String chatRoom = myUid + otherUser.getUid();

        // We only add the user to the list if a chat room exists
        FirebaseDatabase.getInstance().getReference("Messages")
                .child(chatRoom)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Only add if not already in list
                            if (!userList.contains(otherUser)) {
                                userList.add(otherUser);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
