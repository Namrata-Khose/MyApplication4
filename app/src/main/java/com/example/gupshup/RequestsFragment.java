package com.example.gupshup;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class RequestsFragment extends Fragment {

    private RecyclerView recyclerView;
    private RequestAdapter adapter;
    private List<User> requestList;
    private String currentUid;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_requests, container, false);

        recyclerView = view.findViewById(R.id.recyclerrequest);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        requestList = new ArrayList<>();
        adapter = new RequestAdapter(getContext(), requestList);
        recyclerView.setAdapter(adapter);

        currentUid = FirebaseAuth.getInstance().getUid();

        loadRequests();

        return view;
    }

    private void loadRequests() {

        FirebaseDatabase.getInstance()
                .getReference("FriendRequests")   // ✅ FIXED
                .child(currentUid)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        requestList.clear();
                        Log.d("REQUESTS", "Count: " + snapshot.getChildrenCount());

                        for (DataSnapshot ds : snapshot.getChildren()) {

                            String senderUid = ds.getKey();

                            FirebaseDatabase.getInstance()
                                    .getReference("Users")
                                    .child(senderUid)
                                    .addListenerForSingleValueEvent(
                                            new ValueEventListener() {

                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snap) {
                                                    User user = snap.getValue(User.class);
                                                    if (user != null) {
                                                        requestList.add(user);
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {}
                                            }
                                    );
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
