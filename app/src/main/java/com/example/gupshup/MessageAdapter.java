package com.example.gupshup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    ArrayList<MessageModel> messageList;
    String senderRoom, receiverRoom;

    int ITEM_SENT = 1;
    int ITEM_RECEIVE = 2;

    public MessageAdapter(Context context, ArrayList<MessageModel> messageList, String senderRoom, String receiverRoom) {
        this.context = context;
        this.messageList = messageList;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_right, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_left, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageModel message = messageList.get(position);

        // --- Long Click Deletion Logic ---
        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setItems(new String[]{"Delete for me", "Delete for everyone", "Cancel"}, (dialog, which) -> {
                        String msgId = message.getMessageId();
                        if (which == 0) { // Delete for me
                            FirebaseDatabase.getInstance().getReference().child("Messages")
                                    .child(senderRoom).child(msgId).removeValue();
                        } else if (which == 1) { // Delete for everyone
                            FirebaseDatabase.getInstance().getReference().child("Messages")
                                    .child(senderRoom).child(msgId).removeValue();
                            FirebaseDatabase.getInstance().getReference().child("Messages")
                                    .child(receiverRoom).child(msgId).removeValue();
                        }
                    }).show();
            return true;
        });

        // --- Image vs Text Display Logic ---
        if (holder.getClass() == SentViewHolder.class) {
            SentViewHolder sentHolder = (SentViewHolder) holder;

            if ("image".equals(message.getType())) {
                sentHolder.imgSent.setVisibility(View.VISIBLE);
                sentHolder.tvMessageRight.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl()).placeholder(R.drawable.logo).into(sentHolder.imgSent);
            } else {
                sentHolder.imgSent.setVisibility(View.GONE);
                sentHolder.tvMessageRight.setVisibility(View.VISIBLE);
                sentHolder.tvMessageRight.setText(message.getMessage());
            }
        } else {
            ReceiverViewHolder recHolder = (ReceiverViewHolder) holder;

            if ("image".equals(message.getType())) {
                recHolder.imgReceive.setVisibility(View.VISIBLE);
                recHolder.tvMessageLeft.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl()).placeholder(R.drawable.logo).into(recHolder.imgReceive);
            } else {
                recHolder.imgReceive.setVisibility(View.GONE);
                recHolder.tvMessageLeft.setVisibility(View.VISIBLE);
                recHolder.tvMessageLeft.setText(message.getMessage());
            }
        }
    }

    @Override
    public int getItemCount() { return messageList.size(); }

    @Override
    public int getItemViewType(int position) {
        if (FirebaseAuth.getInstance().getUid().equals(messageList.get(position).getSenderId())) {
            return ITEM_SENT;
        } else {
            return ITEM_RECEIVE;
        }
    }

    // ViewHolder for item_chat_right.xml
    class SentViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageRight;
        ImageView imgSent;
        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageRight = itemView.findViewById(R.id.txtMessageRight);
            imgSent = itemView.findViewById(R.id.imgSent); // Make sure this ID is in item_chat_right.xml
        }
    }

    // ViewHolder for item_chat_left.xml
    class ReceiverViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageLeft;
        ImageView imgReceive;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageLeft = itemView.findViewById(R.id.txtMessage);
            imgReceive = itemView.findViewById(R.id.imgSent); // Using same ID name for consistency
        }
    }
}