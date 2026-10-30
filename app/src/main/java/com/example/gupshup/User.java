package com.example.gupshup;

public class User {

    private String uid;
    private String name;
    private String email;
    private String image;      // Keeping this for safety
    private String profilePic; // 🔥 ADDED: This matches your Firebase "profilePic" key

    private boolean online;
    private boolean typing;
    private long lastSeen;
    private String status;

    public User() {
    }

    public User(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.online = true;
        this.status = "online";
        this.lastSeen = System.currentTimeMillis();
    }

    // ================= GETTERS =================

    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getUsername() { return name; }
    public String getEmail() { return email; }

    // 🔥 UPDATED GETTER: Checks both fields to ensure the image displays
    public String getProfilePic() {
        if (profilePic != null) return profilePic;
        return image;
    }

    public String getImage() { return image; }
    public boolean isOnline() { return online; }
    public boolean isTyping() { return typing; }
    public long getLastSeen() { return lastSeen; }

    public String getStatus() {
        if (status != null) return status;
        return online ? "online" : "offline";
    }

    // ================= SETTERS =================

    public void setUid(String uid) { this.uid = uid; }
    public void setName(String name) { this.name = name; }
    public void setUsername(String username) { this.name = username; }
    public void setEmail(String email) { this.email = email; }

    // 🔥 ADDED SETTER: This allows Firebase to automatically fill the profilePic variable
    public void setProfilePic(String profilePic) { this.profilePic = profilePic; }

    public void setImage(String image) { this.image = image; }
    public void setOnline(boolean online) {
        this.online = online;
        this.status = online ? "online" : "offline";
    }
    public void setTyping(boolean typing) { this.typing = typing; }
    public void setLastSeen(long lastSeen) { this.lastSeen = lastSeen; }
    public void setStatus(String status) {
        this.status = status;
        this.online = "online".equals(status);
    }
}