package com.example.mobileoffloading;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servant);
        String id = getIntent().getStringExtra("id");
        TextView tx = findViewById(R.id.textView5);
        tx.setText("Hello Servant " + id);
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
            int lastRowAssigned = matricesInfo.getInt("lastRow");
            JSONArray firstArray = matricesInfo.getJSONArray("firstMatrix");
            JSONArray secondArray = matricesInfo.getJSONArray("secondMatrix");
            int[][] firstMatrix = getMatrix(firstArray, secondMatCol);
            int[][] secondMatrix = getMatrix(secondArray, secondMatRows);
            int[][] multiplication = matrixMultiplication(firstMatrix, secondMatrix);
            JSONObject resultObj = new JSONObject();
            resultObj.put("rows", firstMatRows);
            resultObj.put("columns", secondMatCol);
            resultObj.put("lastRowAssigned", lastRowAssigned);
            resultObj.put("multiplication", multiplication);
            socket.emit("multiplication result", resultObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    });

    /**
     * Get matix from JSON Object
     * @param array - JSON Array that contains array's info
     * @param rows - number of rows of the array
     * @return - 2D array
     * @throws JSONException - Exception
     */
    private int[][] getMatrix(JSONArray array, int rows) throws JSONException {
        int[][] matrix = new int[array.length()][rows];
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
        for(int i = 0; i < firstMatrix.length; i++){
            for(int j = 0; j < secondMatrix[0].length; j++){
                for(int k = 0; k < firstMatrix[i][k]; k++){
                    multiplication[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
                }
            }
        }
        return multiplication;
    }
}