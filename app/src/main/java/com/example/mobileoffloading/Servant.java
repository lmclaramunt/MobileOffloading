package com.example.mobileoffloading;

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
    private TextView tvMultiplication, tvEqual, tvFirstMat, tvSecondMat, tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servant);
        setTitle("Servant");
        String id = getIntent().getStringExtra("id");
        tvFirstMat = findViewById(R.id.tvFirstMatrixServant);
        tvSecondMat = findViewById(R.id.tvSecondMatrixServant);
        tvResult = findViewById(R.id.tvResultServant);
        tvMultiplication = findViewById(R.id.tvServantMultiplication);
        tvEqual = findViewById(R.id.tvServantEqual);
        Server server = (Server) getApplication();
        socket = server.getSocket();
        socket.on("servant matrices", onMatricesArrived);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.on("servant matrices", onMatricesArrived);
    }

    /**
     * Get the matrices sent by the master, multiply them, and send the results
     */
    private final Emitter.Listener onMatricesArrived = args -> runOnUiThread(() -> {
        try {
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
            displayMatrix(firstMatrix, tvFirstMat);
            displayMatrix(secondMatrix, tvSecondMat);
            tvMultiplication.setVisibility(View.VISIBLE);
            int[][] multiplication = matrixMultiplication(firstMatrix, secondMatrix);
            displayMatrix(multiplication, tvResult);
            tvEqual.setVisibility(View.VISIBLE);
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
        int[][] multiplication = new int[firstMatrix.length][secondMatrix.length];
        for(int row = 0; row < firstMatrix.length; row++){
            for(int column = 0; column < secondMatrix[row].length; column++){
                for(int i = 0; i < secondMatrix.length; i++){
                    multiplication[row][column] += firstMatrix[row][i] * secondMatrix[i][column];
                }
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
        for(int row = 0; row < matrix.length; row++){
            for(int column = 0; column < matrix[row].length; column++){
                builder.append(matrix[row][column]);
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
}