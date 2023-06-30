package com.example.elultimo;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


//Class that will open the BT Socket to the Arduino BT Module
//Given a BT device, the UUID and a Handler to set the results
public class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private static final String TAG = "FrugalLogs";
    public static Handler handler;
    private final static int ERROR_READ = 0;
    private String valueRead;


    @SuppressLint("MissingPermission")
    public ConnectThread(BluetoothDevice device, UUID MY_UUID, Handler handler) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;
        ConnectThread.handler =handler;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    @SuppressLint("MissingPermission")
    public void run() {
        Log.d(TAG, "run: ARRANCA");
        InputStream mmInStream;
        int bytes;
        byte[] buffer = new byte[256];

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
            mmInStream = mmSocket.getInputStream();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            handler.obtainMessage(ERROR_READ, "Unable to connect to the BT device").sendToTarget();
            Log.e(TAG, "connectException: " + connectException);
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }
/*
        while (true)
        {
            try
            {
                //se leen los datos del Bluethoot
                bytes = mmInStream.read(buffer);
                String readMessage;
                readMessage = new String(buffer, 0, bytes);
                valueRead = readMessage;
                //se muestran en el layout de la activity, utilizando el handler del hilo
                // principal antes mencionado
            } catch (IOException e) {
                break;
            }

        }
 */



        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        //manageMyConnectedSocket(mmSocket);
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }

    public BluetoothSocket getMmSocket(){
        return mmSocket;
    }

    public void write(String input) throws IOException {
        OutputStream mmOutStream = mmSocket.getOutputStream();
        byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
        try {
            mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
        } catch (IOException e) {
            Log.d(TAG, "write: " + e);

        }
    }

    public String getValueRead() {
        return valueRead;
    }

}
