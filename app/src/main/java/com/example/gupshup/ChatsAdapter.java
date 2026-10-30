package com.example.gupshup;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

    private Context context;
    private List<User> userList;

    public ChatsAdapter(Context context, List<User> userList) {
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
        holder.userName.setText(user.getUsername());

        // Important: Pass BOTH UID and Name
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("uid", user.getUid());
            intent.putExtra("name", user.getUsername());
            context.startActivity(intent);
        });

        // Load online status dot
        FirebaseDatabase.getInstance().getReference("State").child(user.getUid())
                .child("status").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String status = snapshot.getValue(String.class);
                        holder.onlineDot.setVisibility("online".equals(status) ? View.VISIBLE : View.GONE);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    @Override
    public int getItemCount() { return userList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName, lastMessage;
        View onlineDot;
        ViewHolder(View view) {
            super(view);
            userName = view.findViewById(R.id.userName);
            lastMessage = view.findViewById(R.id.lastMessage);
            onlineDot = view.findViewById(R.id.onlineDot);
        }
    }
}
