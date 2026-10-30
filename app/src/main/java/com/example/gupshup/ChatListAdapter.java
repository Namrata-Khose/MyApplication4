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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    private final Context context;
    private final List<User> userList;

    public ChatListAdapter(Context context, List<User> userList) {
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

        // 1. Set User Name
        holder.userName.setText(user.getName());

        // 2. Load Profile Image using getImage()
        Glide.with(context)
                .load(user.getImage())
                .placeholder(R.drawable.ic_user)
                .into(holder.profileImage);

        // 3. Status logic
        holder.onlineDot.setVisibility("online".equals(user.getStatus()) ? View.VISIBLE : View.GONE);

        // 4. Fetch dynamic message and real-time timestamp
        loadLastMessage(user.getUid(), holder.lastMessage, holder.time);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("uid", user.getUid());
            intent.putExtra("name", user.getName());
            intent.putExtra("profilePic", user.getImage());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    // 🔥 FIXED: Variable declarations inside the class resolve symbol errors
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        View onlineDot;
        TextView userName, lastMessage, time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Links to your provided row_chat_user.xml IDs
            profileImage = itemView.findViewById(R.id.profileImage);
            onlineDot = itemView.findViewById(R.id.onlineDot);
            userName = itemView.findViewById(R.id.userName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            time = itemView.findViewById(R.id.time);
        }
    }

    private void loadLastMessage(String otherUid, TextView lastMsgText, TextView timeText) {
        String myUid = FirebaseAuth.getInstance().getUid();
        if (myUid == null) return;

        String chatRoom = myUid + otherUid;

        FirebaseDatabase.getInstance().getReference("Messages")
                .child(chatRoom)
                .orderByChild("timestamp")
                .limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                // Logic to fetch the real message and time
                                String msg = ds.child("message").getValue(String.class);
                                Long timestamp = ds.child("timestamp").getValue(Long.class);

                                if (msg != null) lastMsgText.setText(msg);
                                if (timestamp != null) timeText.setText(formatDate(timestamp));
                            }
                        } else {
                            lastMsgText.setText("Tap to start chatting");
                            timeText.setText("");
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private String formatDate(long timestamp) {
        Calendar msgDate = Calendar.getInstance();
        msgDate.setTimeInMillis(timestamp);
        Calendar now = Calendar.getInstance();

        if (now.get(Calendar.DATE) == msgDate.get(Calendar.DATE)) {
            return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(timestamp));
        } else if (now.get(Calendar.DAY_OF_YEAR) - msgDate.get(Calendar.DAY_OF_YEAR) == 1) {
            return "Yesterday";
        } else {
            return new SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(new Date(timestamp));
        }
    }
}
