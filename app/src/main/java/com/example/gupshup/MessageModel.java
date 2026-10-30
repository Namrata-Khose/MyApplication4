package com.example.gupshup;

public class MessageModel {
    private String messageId;
    private String senderId;
    private String receiverId;
    private String message;
    private long timestamp;
    private boolean seen;
    private String imageUrl; // ADD THIS for images
    private String type;     // ADD THIS (e.g., "text" or "image")

    // Mandatory Empty Constructor
    public MessageModel() {}

    // Full Constructor
    public MessageModel(String senderId, String receiverId, String message, long timestamp, boolean seen) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
        this.seen = seen;
        this.type = "text"; // Default to text
    }

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isSeen() { return seen; }
    public void setSeen(boolean seen) { this.seen = seen; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}