package com.example.gupshup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

// Note: Replace these with the actual names of your Fragment classes
// import com.example.gupshup.Fragments.ChatsFragment;
// import com.example.gupshup.Fragments.RequestsFragment;
// import com.example.gupshup.Fragments.FindFriendsFragment;

public class MainPageAdapter extends FragmentStateAdapter {

    public MainPageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // This determines which fragment to show for each tab
        switch (position) {
            case 0:
                return new ChatsListFragment(); // First Tab
            case 1:
                return new RequestsFragment(); // Second Tab
            case 2:
                return new FindFriendsFragment(); // Third Tab
            default:
                return new ChatsListFragment();
        }
    }

    @Override
    public int getItemCount() {
        // We have 3 tabs: Chats, Requests, and Find Friends
        return 3;
    }
}