package com.example.mobileoffloading;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobileoffloading.Utils.Server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * @author Luis Claramunt
 *         Daniel Evans
 *         Ting Xia
 *         Jianlun Li
 * Let the Admin Preview both matrixes that will be multiplied
 */
public class MatrixPrev extends AppCompatActivity {
    private ArrayList<ArrayList<Integer>> firstMatrix, secondMatrix;
    private Socket socket;
    private int firstRows, secondRows, secondColumns, servants, resultsReceived;
    private int[][] multiplicationResults;
    private TextView tvEqual, tvResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matrix_prev);
        setTitle("Admin");
        TextView tvFirstMatrix = findViewById(R.id.tvFirstMatrix);
        TextView tvSecondMatrix = findViewById(R.id.tvSecondMatrix);
        tvEqual = findViewById(R.id.tvMasterEqual);
        tvResults = findViewById(R.id.tvMasterResults);
        Intent intent = getIntent();
        firstRows = intent.getIntExtra(FirstMatrix.FIRST_MATRIX_ROWS, 0);
        secondRows = intent.getIntExtra(SecondMatrix.SECOND_MATRIX_ROWS, 0);
        secondColumns = intent.getIntExtra(SecondMatrix.SECOND_MATRIX_COLUMNS, 0);
        multiplicationResults = new int[firstRows][secondColumns];
        servants = intent.getIntExtra(Lobby.SERVANTS, 0);
        multiplicationResults = new int[firstRows][secondColumns];
        firstMatrix = FirstMatrix.getMatrix();
        secondMatrix = SecondMatrix.getMatrix();
        displayMatrixPreview(firstMatrix, tvFirstMatrix);
        displayMatrixPreview(secondMatrix, tvSecondMatrix);
        resultsReceived = 0;        // Result received from servants
        Server server = (Server) getApplication();
        socket = server.getSocket();
        socket.on("partial results", onPartialResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.on("partial results", onPartialResults);
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
    public void uploadMatrix(View view) throws JSONException {
        secondMatrix = reArrangeMatrix(secondMatrix);
        JSONArray matrixArray = new JSONArray();
        matrixArray.put(0, firstMatrix);
        matrixArray.put(1, secondMatrix);
        matrixArray.put(2, firstRows);
        matrixArray.put(3, secondRows);
        matrixArray.put(4, secondColumns);
        socket.emit("new matrices", matrixArray);
    }

    /**
     * Used for the second Matrix, which is given as a list of numbers in each rows, but for multiplying
     * matrices it will be more useful to have it as a list of numbers in each column
     * @param matrix - original matrix
     * @return - matrix in terms of numbers in each column
     */
    private ArrayList<ArrayList<Integer>> reArrangeMatrix(ArrayList<ArrayList<Integer>> matrix){
        ArrayList<ArrayList<Integer>> columnMatrix = new ArrayList<>();
        for(int row = 0; row < matrix.get(0).size(); row++) {
            ArrayList<Integer> rowNum = new ArrayList<>();
            for (int column = 0; column < matrix.size(); column++) {
                rowNum.add(matrix.get(column).get(row));
            }
            columnMatrix.add(rowNum);
        }
        return columnMatrix;
    }

    /**
     * When a servant reports matrix multiplication results
     */
    private final Emitter.Listener onPartialResults = args -> runOnUiThread(() -> {
        try{
            JSONObject matrixInfo = (JSONObject) args[0];
            int rows = matrixInfo.getInt("rows");
            int columns = matrixInfo.getInt("columns");
            int firstRow = matrixInfo.getInt("firstRowAssigned");
            int lastRow = matrixInfo.getInt("lastRowAssigned");
            JSONArray matrixArray = matrixInfo.getJSONArray("multiplication");
            int[][] partialResult = Servant.getMatrix(matrixArray, columns);
            updateResultMatrix(partialResult, firstRow, lastRow);
            resultsReceived++;
            if(resultsReceived == servants){
                Servant.displayMatrix(multiplicationResults, tvResults);
                tvEqual.setVisibility(View.VISIBLE);
                tvResults.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    });

    private void updateResultMatrix(int[][] results, int startRow, int endRow){
        for(int row = startRow; row <= endRow; row++){
            for(int col = 0; col < results[0].length; col++){
                multiplicationResults[row][col] = results[row-startRow][col];
            }
        }
    }

}