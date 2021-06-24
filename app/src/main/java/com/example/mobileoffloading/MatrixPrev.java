package com.example.mobileoffloading;

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
 * Let the Admin Preview both matrixes that will be multiplied
 */
public class MatrixPrev extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matrix_prev);
        TextView tvFirstMatrix = findViewById(R.id.tvFirstMatrix);
        TextView tvSecondMatrix = findViewById(R.id.tvSecondMatrix);
        setTitle("Admin");
        ArrayList<ArrayList<Integer>> firstMatrix = FirstMatrix.getMatrix();
        ArrayList<ArrayList<Integer>> secondMatrix = SecondMatrix.getMatrix();
        displayMatrixPreview(firstMatrix, tvFirstMatrix);
        displayMatrixPreview(secondMatrix, tvSecondMatrix);
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
    public void uploadMatrix(View view) {
    }

}