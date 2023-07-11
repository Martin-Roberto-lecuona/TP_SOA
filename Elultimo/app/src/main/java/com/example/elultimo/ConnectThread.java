package com.example.elultimo;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.Manifest;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ConnectThread extends Thread {
    private static final int REQUEST_CODE = 1;
    private BluetoothSocket mmSocket = null;
    private static final String TAG = "FrugalLogs";
    private static final int ERROR_READ = 0;
    private static final int TOTAL_BYTES_SIZE = 1024;
    private static final byte MANUAL_AUTO = -128;
    private static final byte MANUAL_ON = -113;
    private static final byte MANUAL_OFF = -121;
    private static final byte CAMARA_AUTO = -8;
    private static final byte CAMARA_ON = -116;
    private static final byte CAMARA_OFF = -29;
    private static final byte INFLUENCER_AUTO = -32;
    private static final byte INFLUENCER_ON = -125;
    private static final byte INFLUENCER_OFF = -16;

    private static final String SERVO_INFLUENCER_MODE = "Influencer";
    private static final String SERVO_MANUAL_MODE = "Manual";
    private static final String SERVO_CAM_MODE = "Camara";
    private static final String AUTO_LIGHTS = "AUTO";
    private static final String LIGHTS_ON = "ON";
    private static final String LIGHTS_OFF = "OFF";
    private final Context context;

    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private Handler handler;

    public ConnectThread(BluetoothDevice device, Handler handler, Context context) {
        BluetoothSocket tmp = null;
        this.handler = handler;
        this.context = context;
        try {
            if (ContextCompat.checkSelfPermission(this.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) this.context, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_CODE);
                return;
            }
            tmp = device.createRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    public void run() {
        try {
            if (ContextCompat.checkSelfPermission(this.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) this.context, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_CODE);
                return;
            }
            mmSocket.connect();
            mmInStream = mmSocket.getInputStream();
            mmOutStream = mmSocket.getOutputStream();
        } catch (IOException e) {
            handler.obtainMessage(ERROR_READ, "Unable to connect to the BT device").sendToTarget();
            Log.e(TAG, "connectException: " + e);
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }

        byte[] buffer = new byte[1];
        int bytes;

        while (true) {
            try {
                bytes = mmInStream.read(buffer);
                handler.obtainMessage(ERROR_READ, bytes, -1, buffer[0]).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error reading from input stream", e);
                break;
            }
        }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }

    public void write(String input) throws IOException {
        mmOutStream.write(input.getBytes());
    }

    public byte getValueRead() throws IOException {
        return (byte) mmInStream.read();
    }

    public String getServoState(byte byteServoState) {
        if (byteServoState == MANUAL_AUTO || byteServoState == MANUAL_ON || byteServoState == MANUAL_OFF) {
            return SERVO_MANUAL_MODE;
        }
        if (byteServoState == CAMARA_AUTO || byteServoState == CAMARA_ON || byteServoState == CAMARA_OFF) {
            return SERVO_CAM_MODE;
        }
        if (byteServoState == INFLUENCER_AUTO || byteServoState == INFLUENCER_ON || byteServoState == INFLUENCER_OFF) {
            return SERVO_INFLUENCER_MODE;
        }
        return String.valueOf(byteServoState);
    }

    public String getLightstate(byte byteLightsState) {
        if (byteLightsState == MANUAL_AUTO || byteLightsState == CAMARA_AUTO || byteLightsState == INFLUENCER_AUTO) {
            return AUTO_LIGHTS;
        }
        if (byteLightsState == MANUAL_ON || byteLightsState == CAMARA_ON || byteLightsState == INFLUENCER_ON) {
            return LIGHTS_ON;
        }
        if (byteLightsState == MANUAL_OFF || byteLightsState == CAMARA_OFF || byteLightsState == INFLUENCER_OFF) {
            return LIGHTS_OFF;
        }
        return String.valueOf(byteLightsState);
    }
}
