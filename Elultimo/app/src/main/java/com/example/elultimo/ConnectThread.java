package com.example.elultimo;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;

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

    private final static int TOTAL_BYTES_SIZE = 1024;
    private final static int MANUAL_AUTO = -122;
    private final static int MANUAL_ON = -113;
    private final static int MANUAL_OFF = -121;
    private final static int CAMARA_AUTO = -8;
    private final static int CAMARA_ON = -116;
    private final static int CAMARA_OFF = -29;
    private final static int INFLUENCER_AUTO = -8;
    private final static int INFLUENCER_ON = -116;
    private final static int INFLUENCER_OFF = -29;

    private final static String SERVO_INFLUENCER_MODE = "Influencer";

    private final static String SERVO_MANUAL_MODE = "Manual";

    private final static String SERVO_CAM_MODE = "Camara";

    private final static String AUTO_LIGHTS = "AUTO";

    private final static String LIGHTS_ON = "ON";

    private final static String LIGHTS_OFF = "OFF";
    @SuppressLint("MissingPermission")
    public ConnectThread(BluetoothDevice device, UUID MY_UUID, Handler handler)
    {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;
        ConnectThread.handler =handler;
        try
        {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        }
        catch (IOException e)
        {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    @SuppressLint("MissingPermission")
    public void run()
    {
        //InputStream mmInStream;
        int bytes;
        byte[] buffer = new byte[256];

        try
        {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
            //mmInStream = mmSocket.getInputStream();
        }
        catch (IOException connectException)
        {
            // Unable to connect; close the socket and return.
            handler.obtainMessage(ERROR_READ, "Unable to connect to the BT device").sendToTarget();
            Log.e(TAG, "connectException: " + connectException);
            try
            {
                mmSocket.close();
            }
            catch (IOException closeException)
            {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel()
    {
        try
        {
            mmSocket.close();
        }
        catch (IOException e)
        {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }

    public BluetoothSocket getMmSocket()
    {
        return mmSocket;
    }

    public void write(String input) throws IOException
    {
        OutputStream mmOutStream = mmSocket.getOutputStream();
        byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
        try
        {
            mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
        }
        catch (IOException e)
        {
            Log.d(TAG, "write: " + e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public byte getValueRead() throws IOException
    {
        InputStream inputStream = mmSocket.getInputStream();
        inputStream.skip(inputStream.available());
        byte b = (byte) inputStream.read();
        return b;
    }

    public String getServoState(byte byteServoState)
    {
        Log.d(TAG,byteServoState+"");
        if(byteServoState == MANUAL_AUTO || byteServoState == MANUAL_ON | byteServoState == MANUAL_OFF)
        {
            return SERVO_MANUAL_MODE;
        }
        if(byteServoState == CAMARA_AUTO || byteServoState == CAMARA_ON || byteServoState == CAMARA_OFF)
        {
            return SERVO_CAM_MODE;
        }
        if(byteServoState == INFLUENCER_AUTO || byteServoState ==  INFLUENCER_ON || byteServoState == INFLUENCER_OFF)
        {
            return SERVO_INFLUENCER_MODE;
        }
        return byteServoState+"";
    }
    public String getLightstate(byte byteLightsState)
    {
        Log.d(TAG,byteLightsState+"");
        if(byteLightsState == MANUAL_AUTO || byteLightsState == CAMARA_AUTO || byteLightsState == INFLUENCER_AUTO)
        {
            return AUTO_LIGHTS;
        }
        if(byteLightsState == MANUAL_ON || byteLightsState == CAMARA_ON || byteLightsState == INFLUENCER_ON)
        {
            return LIGHTS_ON;
        }
        if(byteLightsState == MANUAL_OFF || byteLightsState == CAMARA_OFF || byteLightsState == INFLUENCER_OFF)
        {
            return LIGHTS_OFF;
        }
        return byteLightsState+"";
    }
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public String getValueReadOLD() throws IOException
    {
        InputStream inputStream = mmSocket.getInputStream();
        int bytes = 0;
        byte[] buffer = new byte[TOTAL_BYTES_SIZE];
        int numberOfReadings = 0; //to control the number of readings from the Arduino
        String readMessage = "";

        while (numberOfReadings < 1)
        {
            buffer[bytes] = (byte) inputStream.read();
            readMessage = new String(buffer, 0, bytes); // If I detect a "\n" means I already read a full measurement
            Log.d(TAG, "buffer[bytes] = " + readMessage );
            if (buffer[bytes] == '\n')
            {
                readMessage = new String(buffer, 0, bytes);
                Log.d(TAG, "Message: " + readMessage); //Value to be read by the Observer streamed by the Obervable
                bytes = 0;
                numberOfReadings++;
            } else
            {
                bytes++;
            }
        }
        return readMessage;
    }
}
