package com.example.gupshup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage; // ADDED
import com.google.firebase.storage.StorageReference; // ADDED

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private static final int FILE_PICK_CODE = 438;
    private RecyclerView recyclerMessages;
    private EditText edtMessage;
    private ImageButton btnSend, btnAttach, btnBack, btnDeleteChat;
    private TextView tvChatUserName, tvStatus;
    private CircleImageView imgProfileChat;

    private String senderUid, receiverUid, senderRoom, receiverRoom;
    private DatabaseReference dRef;
    private MessageAdapter messageAdapter;
    private ArrayList<MessageModel> messageList;
    private boolean isBlocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvChatUserName = findViewById(R.id.tvChatUserName);
        tvStatus = findViewById(R.id.tvStatus);
        imgProfileChat = findViewById(R.id.imgProfileChat);
        recyclerMessages = findViewById(R.id.recyclerMessages);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        btnAttach = findViewById(R.id.btnAttach);
        btnDeleteChat = findViewById(R.id.btnDeleteChat);

        receiverUid = getIntent().getStringExtra("uid");
        tvChatUserName.setText(getIntent().getStringExtra("name"));
        String profilePic = getIntent().getStringExtra("profilePic");

        Glide.with(this).load(profilePic).placeholder(R.drawable.ic_profile_placeholder).into(imgProfileChat);

        senderUid = FirebaseAuth.getInstance().getUid();
        dRef = FirebaseDatabase.getInstance().getReference();
        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        setupRecyclerView();
        loadMessages();
        updatePresenceStatus();
        setupTypingIndicator();
        checkBlockStatus();
        registerFCMToken();

        btnSend.setOnClickListener(v -> sendMessage());
        btnAttach.setOnClickListener(v -> {
            if (!isBlocked) openFilePicker();
            else Toast.makeText(this, "User is blocked", Toast.LENGTH_SHORT).show();
        });

        btnDeleteChat.setOnClickListener(v -> showWhatsAppDeleteDialog());
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList, senderRoom, receiverRoom);
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessages.setAdapter(messageAdapter);
    }

    private void sendMessage() {
        if (isBlocked) return;
        String message = edtMessage.getText().toString().trim();
        if (!message.isEmpty()) {
            String messageId = dRef.child("Messages").child(senderRoom).push().getKey();
            MessageModel model = new MessageModel(senderUid, receiverUid, message, new Date().getTime(), false);
            model.setMessageId(messageId);
            model.setType("text"); // Ensure text type is set

            if (messageId != null) {
                dRef.child("Messages").child(senderRoom).child(messageId).setValue(model)
                        .addOnSuccessListener(unused -> {
                            dRef.child("Messages").child(receiverRoom).child(messageId).setValue(model);
                            edtMessage.setText("");
                        });
            }
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), FILE_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                uploadImageToFirebase(fileUri); // Calls the upload function
            }
        }
    }

    private void uploadImageToFirebase(Uri uri) {
        Toast.makeText(this, "Uploading Image...", Toast.LENGTH_SHORT).show();

        // 1. Storage Reference with unique name
        String timeStamp = String.valueOf(new Date().getTime());
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("ChatImages")
                .child(timeStamp);

        // 2. Upload file
        storageRef.putFile(uri).addOnSuccessListener(taskSnapshot -> {
            // 3. Get URL
            storageRef.getDownloadUrl().addOnSuccessListener(url -> {
                String imageUrl = url.toString();

                // 4. Create MessageModel for Image
                String messageId = dRef.child("Messages").child(senderRoom).push().getKey();
                MessageModel model = new MessageModel(senderUid, receiverUid, "Sent an image", new Date().getTime(), false);
                model.setMessageId(messageId);
                model.setImageUrl(imageUrl);
                model.setType("image"); // This tells your Adapter to show the ImageView

                // 5. Update Database
                if (messageId != null) {
                    dRef.child("Messages").child(senderRoom).child(messageId).setValue(model)
                            .addOnSuccessListener(unused -> {
                                dRef.child("Messages").child(receiverRoom).child(messageId).setValue(model);
                            });
                }
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadMessages() {
        dRef.child("Messages").child(senderRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MessageModel model = ds.getValue(MessageModel.class);
                    if (model != null) {
                        model.setMessageId(ds.getKey());
                        messageList.add(model);
                    }
                }
                messageAdapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) recyclerMessages.scrollToPosition(messageList.size() - 1);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // --- (Rest of your methods: Status, Block, Delete Dialog) ---
    private void showWhatsAppDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Clear this chat?");
        String[] options = {"Delete for me", "Delete for everyone", "Cancel"};

        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                dRef.child("Messages").child(senderRoom).removeValue()
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Chat cleared for you", Toast.LENGTH_SHORT).show());
            }
            else if (which == 1) {
                dRef.child("Messages").child(senderRoom).removeValue();
                dRef.child("Messages").child(receiverRoom).removeValue()
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Chat deleted for everyone", Toast.LENGTH_SHORT).show());
            } else {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void registerFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                dRef.child("State").child(senderUid).child("token").setValue(task.getResult());
            }
        });
    }

    private void checkBlockStatus() {
        dRef.child("BlockedUsers").child(senderUid).child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isBlocked = snapshot.exists();
                edtMessage.setEnabled(!isBlocked);
                edtMessage.setHint(isBlocked ? "You blocked this user" : "Type a message...");
                invalidateOptionsMenu();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupTypingIndicator() {
        edtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isBlocked) {
                    dRef.child("State").child(senderUid).child("typingId")
                            .setValue(!s.toString().isEmpty() ? receiverUid : "none");
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void updatePresenceStatus() {
        dRef.child("State").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.child("status").getValue(String.class);
                    String typingId = snapshot.child("typingId").getValue(String.class);
                    tvStatus.setText(senderUid.equals(typingId) ? "typing..." : (status != null ? status : "offline"));
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        MenuItem blockItem = menu.findItem(R.id.menu_block);
        if (blockItem != null) blockItem.setTitle(isBlocked ? "Unblock User" : "Block User");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_clear) {
            showWhatsAppDeleteDialog();
            return true;
        }
        if (id == R.id.menu_block) {
            if (isBlocked) dRef.child("BlockedUsers").child(senderUid).child(receiverUid).removeValue();
            else dRef.child("BlockedUsers").child(senderUid).child(receiverUid).setValue(true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}