package com.example.gupshup;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context context;
    private List<User> userList;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_chat_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);

        // 1. Set Username
        holder.userName.setText(user.getUsername());

        // 2. Fetch Last Message and Time
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid != null) {
            String senderRoom = currentUid + user.getUid();
            FirebaseDatabase.getInstance().getReference()
                    .child("Messages").child(senderRoom)
                    .orderByKey().limitToLast(1)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists() && snapshot.hasChildren()) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    String lastMsg = ds.child("message").getValue(String.class);
                                    Long time = ds.child("timeStamp").getValue(Long.class);
                                    if (time == null) time = ds.child("timestamp").getValue(Long.class);

                                    holder.lastMessage.setText(lastMsg);
                                    if (time != null) holder.time.setText(formatTime(time));
                                }
                            } else {
                                holder.lastMessage.setText("Tap to chat");
                                holder.time.setText("");
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }

        // 3. Online Status Dot Logic (Presence)
        FirebaseDatabase.getInstance().getReference()
                .child("State").child(user.getUid()) // Changed 'Presence' to 'State' to match your ChatActivity logic
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String status = snapshot.child("status").getValue(String.class);
                            holder.onlineDot.setVisibility("online".equalsIgnoreCase(status) ? View.VISIBLE : View.GONE);
                        } else {
                            holder.onlineDot.setVisibility(View.GONE);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        // 4. Load Profile Image (CRITICAL FIX)
        // We use user.getProfilePic() to match the Firebase key we saved earlier
        String imageUrl = user.getProfilePic();
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageUrl = user.getImage(); // Fallback if your model uses 'image'
        }

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(holder.profileImage);

        // 5. Click to Chat
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("uid", user.getUid());
            intent.putExtra("name", user.getUsername());
            intent.putExtra("profilePic", user.getProfilePic()); // Ensure this matches ChatActivity expectation
            context.startActivity(intent);
        });
    }

    private String formatTime(long time) {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(time));
    }

    @Override
    public int getItemCount() { return userList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView userName, lastMessage, time;
        View onlineDot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            userName = itemView.findViewById(R.id.userName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            time = itemView.findViewById(R.id.time);
            onlineDot = itemView.findViewById(R.id.onlineDot);
        }
    }
}