package com.example.mobileoffloading;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

/**
 * @author Luis Claramunt
 *         Daniel Evans
 *         Ting Xia
 *         Jianlun Li
 * Let the Admin Preview the first matrix
 */
public class FirstMatrixPrev extends AppCompatActivity {
    private int firstRows;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_matrix_prev);
        TextView tvMatrix = findViewById(R.id.tvFirstMatrix);
        setTitle("Admin");
        Intent intent = getIntent();
        firstRows = intent.getIntExtra(FirstMatrix.FIRST_MATRIX_ROWS, 0);
        ArrayList<ArrayList<Integer>> firstMatrix = FirstMatrix.getFirstMatrix();
        displayMatrixPreview(firstMatrix, tvMatrix);
    }

    /**
     * Properly dislpay the first matrix
     * @param matrixList - matrix as 2D ArrayList
     * @param textView - TextView it will be displayed in
     */
    public static void displayMatrixPreview(ArrayList<ArrayList<Integer>> matrixList, TextView textView){
        StringBuilder matrix = new StringBuilder();
        for(ArrayList<Integer> row: matrixList){
            for(Integer num: row)
                matrix.append(num).append(" ");
            matrix.append("\n");
        }
        textView.setText(matrix);
    }

    /**
     * Let the Admin start the second matrix
     * @param view - for button
     */
    public void startSecondMatrix(View view) {
        Intent intent = new Intent(this, FirstMatrixPrev.class);
        intent.putExtra(FirstMatrix.FIRST_MATRIX_ROWS, firstRows);
        startActivity(intent);
    }
}