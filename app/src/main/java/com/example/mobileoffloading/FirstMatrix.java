package com.example.mobileoffloading;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileoffloading.Adapters.MatrixRecyclerViewAdapter;

import java.util.ArrayList;

/**
 * @author Luis Claramunt
 *         Daniel Evans
 *         Ting Xia
 *         Jianlun Li
 * First activity for the Master. Here he/she will be able to create the first matrix
 */
public class FirstMatrix extends AppCompatActivity {
    private static ArrayList<ArrayList<Integer>> rowList;
    private MatrixRecyclerViewAdapter adapter;
    private int servants;
    public static String FIRST_MATRIX_COLUMNS = "first_columns";
    public static String FIRST_MATRIX_ROWS = "first_rows";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_matrix);
        setTitle("Admin");
        servants = getIntent().getIntExtra(Lobby.SERVANTS, 0);
        rowList = new ArrayList<>();
//        for(int i = 0; i < servants; i++){
//            rowList.add(new ArrayList<>());
//        }
        ArrayList<Integer> list = new ArrayList<>();    // Lines to facilitate debugging
        list.add(1);
        list.add(2);
        list.add(3);
        rowList.add(list);
        rowList.add(list);
        list = new ArrayList<>();    // Lines to facilitate debugging
        list.add(4);
        list.add(5);
        list.add(6);
        rowList.add(list);
        rowList.add(list);
        initRecyclerView();
    }


    /**
     * Initialize the RecyclerView that displays the User's
     */
    private void initRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.matrix_list_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new MatrixRecyclerViewAdapter(rowList, servants);
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
                adapter.rowRemoved(position, getApplicationContext());
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

    /**
     * OnClick Listener - The master finished writing the first matrix
     * @param view - for button
     */
    public void submitFirstMatrix(View view) {
        if(adapter.validateMatrix(getApplicationContext())){
            int columns = adapter.getColumns();
            int rows = adapter.getRows();
            Intent intent = new Intent(this, SecondMatrix.class);
            intent.putExtra(FIRST_MATRIX_COLUMNS, columns);
            intent.putExtra(FIRST_MATRIX_ROWS, rows);
            intent.putExtra(Lobby.SERVANTS, servants);
            startActivity(intent);
        }
    }

    /**
     * Get the matrix as a 2D Array
     * @return - First Matrix
     */
    public static ArrayList<ArrayList<Integer>> getMatrix(){ return rowList;}
}