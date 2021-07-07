package com.example.mobileoffloading;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
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
public class Master extends AppCompatActivity {
    private ArrayList<ArrayList<Integer>> firstMatrix, secondMatrix;
    private Socket socket;
    private int firstRows, secondRows, secondColumns, servants, resultsReceived;
    private int[][] multiplicationResults;
    private TextView tvEqual, tvResults, tvAnalysis, tvTime, tvPower, tvMatResult;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch swDistribute;
    private long startTime, finishTime;
    private float finishBattery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);
        setTitle("Admin");
        initiateUI();
        resultsReceived = 0;        // Result received from servants
        Server server = (Server) getApplication();
        socket = server.getSocket();
        socket.on("partial results", onPartialResults);
        finishBattery = -1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.on("partial results", onPartialResults);
    }

    /**
     * Initialize the User Interface (UI) by looking components by ID, getting Extras send in
     * intent and displaying matrices
     */
    private void initiateUI(){
        TextView tvFirstMatrix = findViewById(R.id.tvFirstMatrix);
        TextView tvSecondMatrix = findViewById(R.id.tvSecondMatrix);
        tvEqual = findViewById(R.id.tvMasterEqual);
        tvResults = findViewById(R.id.tvMasterResults);
        tvAnalysis = findViewById(R.id.tvMasterProcess);
        swDistribute = findViewById(R.id.swDistribute);
        tvTime = findViewById(R.id.tvMasterTime);
        tvPower = findViewById(R.id.tvMasterPower);
        tvMatResult = findViewById(R.id.tvMatResult);
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
     * The Admin is ready to go through the matrix multiplication process
     * @param view - for button
     */
    public void uploadMatrix(View view) {
        JSONArray matrixArray = new JSONArray();
        matrixArray.put(firstMatrix);
        matrixArray.put(secondMatrix);
        matrixArray.put(firstRows);
        matrixArray.put(secondRows);
        matrixArray.put(secondColumns);
        matrixArray.put(swDistribute.isChecked());
        socket.emit("new matrices", matrixArray);
        if(swDistribute.isChecked())
            startTime = System.nanoTime();          // Start keeping track of when the whole process started
        else{
            ArrayList<ArrayList<Integer>> diffSecondMatrix = reArrangeMatrix(secondMatrix);
            multiplyMatrices(diffSecondMatrix);
            Servant.displayMatrix(multiplicationResults, tvResults);
            displayResults();
        }
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
                finishTime = System.nanoTime();
                Servant.displayMatrix(multiplicationResults, tvResults);
                displayResults();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    });

    /**
     * Update master's matrix as servants' result are received
     * @param results - results from servant
     * @param startRow - first row the servant was in charge of
     * @param endRow - last row the servant was in charge of
     */
    private void updateResultMatrix(int[][] results, int startRow, int endRow){
        for(int row = startRow; row <= endRow; row++){
            System.arraycopy(results[row - startRow], 0, multiplicationResults[row], 0, results[0].length);
        }
    }

    /**
     * Function to do the matrix multiplication locally, without the servants
     */
    private void multiplyMatrices(ArrayList<ArrayList<Integer>> secondMatrix){
        startTime = System.nanoTime();
        for(int rowIndex = 0;  rowIndex < firstMatrix.size(); rowIndex++){
            ArrayList<Integer> row = firstMatrix.get(rowIndex);
            for(int colIndex = 0; colIndex < secondMatrix.size(); colIndex++){
                ArrayList<Integer> col = secondMatrix.get(colIndex);
                int cell = 0;
                for(int i = 0; i < secondMatrix.get(0).size(); i++){
                    cell += row.get(i) * col.get(i);
                }
                multiplicationResults[rowIndex][colIndex] = cell;
            }
        }
        finishTime = System.nanoTime();
    }

    /**
     * Display calculation results along with time and power
     */
    @SuppressLint("SetTextI18n")
    private void displayResults(){
        finishBattery = Lobby.getBattery();     //Battery at the end of the process
        tvEqual.setVisibility(View.VISIBLE);
        tvResults.setVisibility(View.VISIBLE);
        tvMatResult.setVisibility(View.VISIBLE);
        tvAnalysis.setVisibility(View.VISIBLE);
        tvTime.setVisibility(View.VISIBLE);
        tvPower.setVisibility(View.VISIBLE);
        double elapseTime = (finishTime - startTime) / 1_000_000_000.0;
        tvTime.setText("\t\tTime elapsed: " + elapseTime + " s");
        float batteryChange = finishBattery - FirstMatrix.startBattery;
        FirstMatrix.startBattery = finishBattery;           // In case the process the process starts again
        tvPower.setText("\t\tPower used: " + batteryChange + "% of battery");
        resultsReceived = 0;
    }

}