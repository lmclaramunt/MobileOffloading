package com.example.mobileoffloading;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobileoffloading.Utils.Server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * @author Luis Claramunt
 *         Daniel Evans
 *         Ting Xia
 *         Jianlun Li
 * Activity where the servants wait for the master to send matrices and processes them once they
 * receive them
 */
public class Servant extends AppCompatActivity {
    private Socket socket;
    private TextView tvMultiplication, tvEqual, tvFirstMat, tvSecondMat, tvResult,
            tvAnalysis, tvTime, tvPower, tvSerFm, tvSerSm, tvSerRm, tvSerTestTrue, tvSerTestFalse;
    private long startTime, finishTime;
    private boolean testing;
    private float startBattery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servant);
        setTitle("Servant");
        initializeUI();
        startBattery = Lobby.getBattery();
        Server server = (Server) getApplication();
        socket = server.getSocket();
        socket.on("servant matrices", onMatricesArrived);
        socket.on("servant test", onServantTest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.off("servant matrices", onMatricesArrived);
        socket.off("servant test", onServantTest);
    }

    /**
     * Find elements by ID
     */
    private void initializeUI(){
        tvFirstMat = findViewById(R.id.tvFirstMatrixServant);
        tvSecondMat = findViewById(R.id.tvSecondMatrixServant);
        tvResult = findViewById(R.id.tvResultServant);
        tvMultiplication = findViewById(R.id.tvServantMultiplication);
        tvEqual = findViewById(R.id.tvServantEqual);
        tvAnalysis = findViewById(R.id.tvServantProcess);
        tvTime = findViewById(R.id.tvServantTime);
        tvPower = findViewById(R.id.tvServantPower);
        tvSerFm = findViewById(R.id.tvSerFm);
        tvSerSm = findViewById(R.id.tvSerSm);
        tvSerRm = findViewById(R.id.tvSerRm);
        tvSerTestTrue = findViewById(R.id.tvSerTestTrue);
        tvSerTestFalse = findViewById(R.id.tvSerTestFalse);
    }

    /**
     * Get the matrices sent by the master, multiply them, and send the results
     */
    private final Emitter.Listener onMatricesArrived = args -> runOnUiThread(() -> {
        try {
            testing = false;
            JSONObject matricesInfo = (JSONObject) args[0];
            int firstMatRows = matricesInfo.getInt("finalRows");
            int secondMatRows = matricesInfo.getInt("secondRows");
            int secondMatCol = matricesInfo.getInt("secondColumns");
            int firstRowAssigned = matricesInfo.getInt("firstRow");
            int lastRowAssigned = matricesInfo.getInt("lastRow");
            JSONArray firstArray = matricesInfo.getJSONArray("firstMatrix");
            JSONArray secondArray = matricesInfo.getJSONArray("secondMatrix");
            int[][] firstMatrix = getMatrix(firstArray, secondMatRows);
            int[][] secondMatrix = getMatrix(secondArray, secondMatCol);
            startTime = System.nanoTime();
            int[][] multiplication = matrixMultiplication(firstMatrix, secondMatrix);
            finishTime = System.nanoTime();
            displayMatrix(firstMatrix, tvFirstMat);
            displayMatrix(secondMatrix, tvSecondMat);
            displayMatrix(multiplication, tvResult);
            displayResults();
            JSONArray mult = arrayToJSON(multiplication);
            JSONObject resultObj = new JSONObject();
            resultObj.put("rows", firstMatRows);
            resultObj.put("columns", secondMatCol);
            resultObj.put("firstRowAssigned", firstRowAssigned);
            resultObj.put("lastRowAssigned", lastRowAssigned);
            resultObj.put("multiplication", mult);
            socket.emit("multiplication result", resultObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    });

    /**
     * The master send the entire matrices to the servants, multiply them and display
     * time and power consumption info
     */
    private final Emitter.Listener onServantTest = args -> runOnUiThread(() -> {
        try {
            testing = true;
            JSONObject matricesInfo = (JSONObject) args[0];
            int firstMatColumns = matricesInfo.getInt("firstColumns");
            int secondMatCol = matricesInfo.getInt("secondColumns");
            JSONArray firstArray = matricesInfo.getJSONArray("firstMatrix");
            JSONArray secondArray = matricesInfo.getJSONArray("secondMatrix");
            int[][] firstMatrix = getMatrix(firstArray, firstMatColumns);
            int[][] secondMatrix = getMatrix(secondArray, secondMatCol);
            startTime = System.nanoTime();
            int[][] multiplication = matrixMultiplication(firstMatrix, secondMatrix);
            finishTime = System.nanoTime();
            displayMatrix(firstMatrix, tvFirstMat);
            displayMatrix(secondMatrix, tvSecondMat);
            displayMatrix(multiplication, tvResult);
            displayResults();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    });

    /**
     * Get matix from JSON Object
     * @param array - JSON Array that contains array's info
     * @param columns - number of columns of the array
     * @return - 2D array
     * @throws JSONException - Exception
     */
    public static int[][] getMatrix(JSONArray array, int columns) throws JSONException {
        int[][] matrix = new int[array.length()][columns];
        for(int i = 0; i < array.length(); i++){
            JSONArray row = array.getJSONArray(i);
            for(int j = 0; j < row.length(); j++){
                matrix[i][j] = row.getInt(j);
            }
        }
        return matrix;
    }

    /**
     * Multiply two matrices
     * @param firstMatrix - first matrix
     * @param secondMatrix - second matrix
     * @return - matrix after multiplying both matrices
     */
    private int[][] matrixMultiplication(int[][] firstMatrix, int[][] secondMatrix){
        int[][] multiplication = new int[firstMatrix.length][secondMatrix[0].length];
        for(int row = 0; row < firstMatrix.length; row++){
            for(int column = 0; column < secondMatrix[0].length; column++){
                int cell = 0;
                for(int i = 0; i < firstMatrix[0].length; i++){
                    cell += firstMatrix[row][i] * secondMatrix[i][column];
                }
                multiplication[row][column] = cell;
            }
        }
        return multiplication;
    }

    /**
     * Display a Matrix in assigned TextView
     * @param matrix - matrix as a 2D array
     * @param tv - TextView it will be displayed
     */
    public static void displayMatrix(int[][] matrix, TextView tv){
        StringBuilder builder = new StringBuilder();
        for (int[] ints : matrix) {
            for (int anInt : ints) {
                builder.append(anInt).append(" ");
            }
            builder.append("\n");
        }
        tv.setText(builder.toString());
    }

    /**
     * Turn a Java 2D array into a JSON Array so it can be properly handled to the server
     * @param matrix - 2D array matrix
     * @return - JSON Array
     */
    private JSONArray arrayToJSON(int[][] matrix){
        JSONArray finalArray = new JSONArray();
        for(int[] row: matrix){
            JSONArray rowArray = new JSONArray();
            for(int col: row){
                rowArray.put(col);
            }
            finalArray.put(rowArray);
        }
        return finalArray;
    }

    /**
     * Display results in UI after the matrices have been multiplied
     */
    @SuppressLint("SetTextI18n")
    private void displayResults(){
        float finishBattery = Lobby.getBattery();      // Battery when process ends
        if(testing){
            tvSerTestTrue.setVisibility(View.VISIBLE);
            tvSerTestFalse.setVisibility(View.GONE);
        }else{
            tvSerTestFalse.setVisibility(View.VISIBLE);
            tvSerTestTrue.setVisibility(View.GONE);
        }
        tvEqual.setVisibility(View.VISIBLE);
        tvMultiplication.setVisibility(View.VISIBLE);
        tvResult.setVisibility(View.VISIBLE);
        tvAnalysis.setVisibility(View.VISIBLE);
        tvTime.setVisibility(View.VISIBLE);
        tvPower.setVisibility(View.VISIBLE);
        tvSerFm.setVisibility(View.VISIBLE);
        tvSerSm.setVisibility(View.VISIBLE);
        tvSerRm.setVisibility(View.VISIBLE);
        double elapseTime = (finishTime - startTime) / 1_000_000_000.0;
        tvTime.setText("\t\t\tTime elapsed: " + elapseTime + " s");
        float changeBattery = finishBattery - startBattery;
        tvPower.setText("\t\t\tPower used: " + changeBattery + "% of battery");
        startBattery = finishBattery;       // In case the process restarts
    }
}