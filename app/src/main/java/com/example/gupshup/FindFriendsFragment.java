package com.example.gupshup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FindFriendsFragment extends Fragment {

    private RecyclerView recyclerView;
    private FindFriendsAdapter adapter;
    private List<User> userList;

    private DatabaseReference usersRef;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_find_friends, container, false);

        // ✅ CORRECT ID
        recyclerView = view.findViewById(R.id.recyclerFindFriends);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        userList = new ArrayList<>();
        adapter = new FindFriendsAdapter(getContext(), userList);
        recyclerView.setAdapter(adapter);

        currentUserId = FirebaseAuth.getInstance().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        loadUsers();

        return view;
    }

    private void loadUsers() {

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("FIND_FRIENDS", "Count: " + snapshot.getChildrenCount());


                userList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);

                    if (user != null && !user.getUid().equals(currentUserId)) {
                        userList.add(user);
                    }
                }

                adapter.notifyDataSetChanged();

                if (userList.isEmpty()) {
                    Toast.makeText(getContext(), "No users found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
