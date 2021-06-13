package com.example.mobileoffloading;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

/**
 * @author Luis Claramunt
 * RecyclerView Adapter to display User Data:
 *      username, location, and battery
 */
public class UserRecyclerViewAdapter extends RecyclerView.Adapter<UserRecyclerViewAdapter.UserViewHolder> implements Filter {

    private ArrayList<User> userList;
    private OnItemClickListener listener;


    /**
     * OnItemClickListener for which item is clicked in the RecyclerView
     */
    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    /**
     * Set OnClickListener for the rows that are clicked on the RecyclerView
     * @param clickListener - OnItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener clickListener){
        listener = clickListener;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{
        public TextView userName, userLatitude, userLongitude, userBattery;

        public UserViewHolder(@NonNull View itemView, OnItemClickListener clickListener) {
            super(itemView);
            userName = itemView.findViewById(R.id.tvUsername);
            userLatitude = itemView.findViewById(R.id.tvLatitude);
            userLongitude = itemView.findViewById(R.id.tvLongitude);
            userBattery = itemView.findViewById(R.id.tvBattery);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if(clickListener != null && position != RecyclerView.NO_POSITION){
                    clickListener.onItemClick(position);
                }
            });
        }
    }

    public UserRecyclerViewAdapter(ArrayList<User> userArrayList){
        userList = userArrayList;
    }

    /**
     * Pass the layout of a row to the adapter
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_users_row, parent, false);
        return new UserViewHolder(view, listener);
    }

    /**
     * Pass values to the App's TextViews displayed in the RecyclerView rows
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.userName.setText(user.getUsername());
        holder.userBattery.setText(user.getBattery() + "%");
        holder.userLatitude.setText(String.valueOf(user.getLatitude()));
        holder.userLongitude.setText(String.valueOf(user.getLongitude()));
    }

    /**
     * An user has logged in. Update the arrayList with this info
     * @param user - New user
     */
    public void addUser(User user){
        userList.add(user);
        notifyItemInserted(userList.size()-1);
    }

    /**
     * An user's info has been updated, therefore the arrayList with this info
     * @param position - position in the arrayList
     * @param user - Updated user
     */
    public void userEdited(int position, User user){
        userList.set(position, user);
        notifyItemChanged(position);
    }

    /**
     * An user has been logged out. Update the arrayList
     * @param position - position in the arrayList
     */
    public void userRemoved(int position){
        userList.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * Get the number of user displayed in the RecyclerView
     * @return - ArrayList size
     */
    @Override
    public int getItemCount() {
        return userList.size();
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        return false;
    }

}
