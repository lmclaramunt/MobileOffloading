package com.example.mobileoffloading.Adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileoffloading.R;

import java.util.ArrayList;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

/**
 * @author Luis Claramunt
 *         Daniel Evans
 *         Ting Xia
 *         Jianlun Li
 * RecyclerView Adapter so admin can write matrixes
 */
public class MatrixRecyclerViewAdapter extends RecyclerView.Adapter<MatrixRecyclerViewAdapter.MatrixViewHolder> implements Filter {

    private ArrayList<ArrayList<Integer>> rowList;      // Double ArrayList
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

    public static class MatrixViewHolder extends RecyclerView.ViewHolder{
        public TextView rowName;
        public EditText rowText;

        public MatrixViewHolder(@NonNull View itemView, OnItemClickListener clickListener) {
            super(itemView);
            rowName = itemView.findViewById(R.id.txtMatRow);
            rowText = itemView.findViewById(R.id.editTextMatrixRow);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if(clickListener != null && position != RecyclerView.NO_POSITION){
                    clickListener.onItemClick(position);
                }
            });
        }
    }

    public MatrixRecyclerViewAdapter(ArrayList<ArrayList<Integer>> rowList){
        this.rowList = rowList;
    }

    /**
     * Pass the layout of a row to the adapter
     */
    @NonNull
    @Override
    public MatrixViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_matrix_row, parent, false);
        return new MatrixViewHolder(view, listener);
    }

    /**
     * Pass values to the Row's TextViews displayed in the RecyclerView rows
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MatrixViewHolder holder, int position) {
        holder.rowName.setText("Row " + (position+1));
    }

    /**
     * A matrix row has was written by the admin user. Update the arrayList with this info
     */
    public void initializeRow(){
        rowList.add(new ArrayList<>());
        notifyItemInserted(rowList.size()-1);
    }

    /**
     *
     * @param position - Position in the ArrayList
     * @param row - matrix's row that was updated
     */
    public void rowUpdated(int position, ArrayList<Integer> row){
        rowList.set(position, row);
        notifyItemChanged(position);
    }


    /**
     * An admin removed the row from the matrix. Update the RecyclerView
     * @param position - position in the arrayList
     */
    public void rowRemoved(int position){
        rowList.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * Get the number of user displayed in the RecyclerView
     * @return - ArrayList size
     */
    @Override
    public int getItemCount() {
        return rowList.size();
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        return false;
    }

}
