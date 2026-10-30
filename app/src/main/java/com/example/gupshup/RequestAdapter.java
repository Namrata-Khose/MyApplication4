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
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    private Context context;
    private List<User> requestList;
    private String currentUid;

    public RequestAdapter(Context context, List<User> requestList) {
        this.context = context;
        this.requestList = requestList;
        currentUid = FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.row_request_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        User user = requestList.get(position);

        holder.userName.setText(user.getUsername());

        String senderUid = user.getUid();

        // ✅ ACCEPT REQUEST
        holder.btnAccept.setOnClickListener(v -> {

            // 1️⃣ Add each other to ChatList
            FirebaseDatabase.getInstance().getReference("ChatList")
                    .child(currentUid)
                    .child(senderUid)
                    .setValue(true);

            FirebaseDatabase.getInstance().getReference("ChatList")
                    .child(senderUid)
                    .child(currentUid)
                    .setValue(true);

            // 2️⃣ Remove Friend Request (BOTH SIDES)
            FirebaseDatabase.getInstance().getReference("FriendRequests")
                    .child(currentUid)
                    .child(senderUid)
                    .removeValue();

            FirebaseDatabase.getInstance().getReference("FriendRequests")
                    .child(senderUid)
                    .child(currentUid)
                    .removeValue();

            Toast.makeText(context, "Request Accepted", Toast.LENGTH_SHORT).show();
        });

        // ❌ REJECT REQUEST
        holder.btnReject.setOnClickListener(v -> {

            FirebaseDatabase.getInstance().getReference("FriendRequests")
                    .child(currentUid)
                    .child(senderUid)
                    .removeValue();

            FirebaseDatabase.getInstance().getReference("FriendRequests")
                    .child(senderUid)
                    .child(currentUid)
                    .removeValue();

            Toast.makeText(context, "Request Rejected", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView profileImage;
        TextView userName;
        Button btnAccept, btnReject;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.profileImage);
            userName = itemView.findViewById(R.id.userName);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
