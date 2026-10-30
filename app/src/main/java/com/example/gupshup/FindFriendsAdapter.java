package com.example.gupshup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class FindFriendsAdapter extends RecyclerView.Adapter<FindFriendsAdapter.ViewHolder> {

    private Context context;
    private List<User> userList;
    private String currentUserId;

    public FindFriendsAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        currentUserId = FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.row_find_friends_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        User user = userList.get(position);

        // ❌ skip current user
        if (user.getUid().equals(currentUserId)) {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            return;
        }

        holder.username.setText(user.getUsername());
        holder.email.setText(user.getEmail());
        holder.statusDot.setVisibility(View.GONE);

        checkFriendStatus(user.getUid(), holder);
    }

    private void checkFriendStatus(String otherUid, ViewHolder holder) {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        // ✅ Check if already friends
        rootRef.child("Friends")
                .child(currentUserId)
                .child(otherUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            holder.btnAddFriend.setText("Friends");
                            holder.btnAddFriend.setEnabled(false);
                            return;
                        }

                        // ✅ Check request sent
                        rootRef.child("FriendRequests")
                                .child(currentUserId)
                                .child(otherUid)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snap) {

                                        if (snap.exists()) {
                                            holder.btnAddFriend.setText("Sent");
                                            holder.btnAddFriend.setEnabled(false);
                                        } else {

                                            // ✅ Check request received
                                            rootRef.child("FriendRequests")
                                                    .child(otherUid)
                                                    .child(currentUserId)
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot ds) {

                                                            if (ds.exists()) {
                                                                holder.btnAddFriend.setText("Accept");
                                                                holder.btnAddFriend.setEnabled(true);
                                                                holder.btnAddFriend.setOnClickListener(v ->
                                                                        acceptFriendRequest(otherUid)
                                                                );
                                                            } else {
                                                                holder.btnAddFriend.setText("Add Friend");
                                                                holder.btnAddFriend.setEnabled(true);
                                                                holder.btnAddFriend.setOnClickListener(v ->
                                                                        sendFriendRequest(otherUid)
                                                                );
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

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void sendFriendRequest(String receiverId) {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("FriendRequests");

        ref.child(receiverId)
                .child(currentUserId)
                .setValue("pending");

        ref.child(currentUserId)
                .child(receiverId)
                .setValue("sent");

        Toast.makeText(context, "Friend request sent", Toast.LENGTH_SHORT).show();
    }

    private void acceptFriendRequest(String otherUid) {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        // Remove requests
        rootRef.child("FriendRequests").child(currentUserId).child(otherUid).removeValue();
        rootRef.child("FriendRequests").child(otherUid).child(currentUserId).removeValue();

        // Add friends
        rootRef.child("Friends").child(currentUserId).child(otherUid).setValue(true);
        rootRef.child("Friends").child(otherUid).child(currentUserId).setValue(true);

        Toast.makeText(context, "Friend added", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView profileImage;
        TextView username, email;
        View statusDot;
        Button btnAddFriend;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.tv_username);
            email = itemView.findViewById(R.id.tv_email);
            statusDot = itemView.findViewById(R.id.status_dot);
            btnAddFriend = itemView.findViewById(R.id.btn_add_friend);
        }
    }
}
