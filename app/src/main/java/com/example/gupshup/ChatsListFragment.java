package com.example.gupshup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar; // Added for better UX

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatsListFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter adapter; // 🔥 Changed to UserAdapter for Profile Pic support
    private List<User> userList;
    private String currentUid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerChats);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        userList = new ArrayList<>();
        // 🔥 Using UserAdapter because we fixed the profilePic logic there
        adapter = new UserAdapter(getContext(), userList);
        recyclerView.setAdapter(adapter);

        currentUid = FirebaseAuth.getInstance().getUid();

        if (currentUid != null) {
            loadChatUsers();
        }

        return view;
    }

    private void loadChatUsers() {
        // 1. Listen to the ChatList node
        FirebaseDatabase.getInstance().getReference("ChatList")
                .child(currentUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // 🔥 Important: Only clear if we actually have new data to show
                        // This prevents the screen from going blank randomly
                        List<User> tempList = new ArrayList<>();

                        if (!snapshot.exists()) {
                            userList.clear();
                            adapter.notifyDataSetChanged();
                            return;
                        }

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String otherUid = ds.getKey();

                            // 2. Fetch User details for each ID in the list
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(otherUid)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snap) {
                                            User user = snap.getValue(User.class);
                                            if (user != null) {
                                                // Check for duplicates before adding
                                                boolean isExist = false;
                                                for(User u : userList){
                                                    if(u.getUid().equals(user.getUid())){
                                                        isExist = true;
                                                        break;
                                                    }
                                                }

                                                if(!isExist) {
                                                    userList.add(user);
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {}
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}