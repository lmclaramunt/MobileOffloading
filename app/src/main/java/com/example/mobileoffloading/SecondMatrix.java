package com.example.mobileoffloading;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.mobileoffloading.Adapters.MatrixRecyclerViewAdapter;

import java.util.ArrayList;

/**
 * @author Luis Claramunt
 *         Daniel Evans
 *         Ting Xia
 *         Jianlun Li
 * Second activity for the Master. Here he/she will be able to create the second matrix
 */
public class SecondMatrix extends AppCompatActivity {
    private static ArrayList<ArrayList<Integer>> rowList;
    private MatrixRecyclerViewAdapter adapter;
    private int firstRows;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_matrix);
        firstRows = getIntent().getIntExtra(FirstMatrix.FIRST_MATRIX_ROWS, 0);
        rowList = new ArrayList<>();
        setTitle("Admin");
        initRecyclerView();
    }

    /**
     * Initialize the RecyclerView that displays the User's
     */
    private void initRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.second_matrix_list_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new MatrixRecyclerViewAdapter(rowList);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        //Add swipe functionality to delete Symptoms
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                adapter.rowRemoved(position);
            }
        });
        helper.attachToRecyclerView(recyclerView);
    }

    /**
     * Select which menu to be displayed in the toolbar
     * @param menu - menu
     * @return - boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_menu, menu);
        return true;
    }

    /**
     * Handle when actionbar menu items are selected
     * @param item - selected
     * @return - boolean
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.item_plus: {
                adapter.initializeRow();
                return true;
            }
            case android.R.id.home: {
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
            default: return super.onOptionsItemSelected(item);
        }
    }

    public void submitSecondMatrix(View view) {
        if(adapter.validateMatrix(getApplicationContext())){
            int columns = adapter.getColumns();
            if(columns == firstRows) {
                startActivity(new Intent(this, MatrixPrev.class));
            }else{
                Toast.makeText(getApplicationContext(), "Invalid dimensions based on Matrix 1 # of rows",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Get the matrix as a 2D Array
     * @return - Second Matrix
     */
    public static ArrayList<ArrayList<Integer>> getMatrix(){ return rowList;}
}