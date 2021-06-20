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
 *         Daniel Evans
 *         Ting Xia
 *         Jianlun Li
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
        public TextView userName, userLatitude, userLongitude, userBattery, userAdmin;

        public UserViewHolder(@NonNull View itemView, OnItemClickListener clickListener) {
            super(itemView);
            userName = itemView.findViewById(R.id.tvUsername);
            userLatitude = itemView.findViewById(R.id.tvLatitude);
            userLongitude = itemView.findViewById(R.id.tvLongitude);
            userBattery = itemView.findViewById(R.id.tvBattery);
            userAdmin = itemView.findViewById(R.id.txtAdmin);

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
        int adminVis = (user.isAdmin()) ? View.VISIBLE : View.GONE;
        holder.userAdmin.setVisibility(adminVis);
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
     * An user's info has been updated, therefore update the arrayList with this info
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
     * Remove the current Admin in the ArrayList
     */
    public void removeAdmin(){
        for(int i = 0; i < userList.size(); i++){
            if(userList.get(i).isAdmin()){
                userList.get(i).setAdmin(false);
                notifyItemChanged(i);
                break;
            }
        }
    }

    /**
     * There should only be one admin, check if you are logged in as it
     * @return
     */
    public String getAdminUsername(){
        for(User user: userList){
            if(user.isAdmin())
                return user.getUsername();
        }
        return null;
    }

    /**
     * Update the batter of the User under the given username
     * @param username - User's username
     * @param battery - new battery level
     */
    public void updateBattery(String username, float battery){
        for(int i = 0; i < userList.size(); i++){
            if(userList.get(i).getUsername().equals(username)){
               userList.get(i).setBattery(battery);
                notifyItemChanged(i);
                break;
            }
        }
    }

    /**
     * Update who is the admin in the ArrayList
     * @param username - name of the new Admin
     */
    public void updateAdmin(String username){
        for(int i = 0; i < userList.size(); i++){
            if(userList.get(i).getUsername().equals(username)){
                userList.get(i).setAdmin(true);
                notifyItemChanged(i);
                break;
            }
        }
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
