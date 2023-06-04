package com.example.elultimo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "InfluLogs";
    public static Handler handler;
    private final static int ERROR_READ = 0;
    private final String[] permissions = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_SCAN};
    private static final int REQUEST_ENABLE_BT = 1;
    BluetoothDevice arduinoBTModule = null;
    public static final int MULTIPLE_PERMISSIONS = 10;
    UUID arduinoUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    ConnectedThread connectedThread;
    ConnectThread connectThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        final Button button_search = (Button) findViewById(R.id.search);
        final Button button_connect = (Button) findViewById(R.id.connect);
        final Button button_controls = (Button) findViewById(R.id.controls);
        final TextView text_linked = (TextView) findViewById(R.id.linked_devices);

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {

                    case ERROR_READ:
                        String arduinoMsg = msg.obj.toString(); // Read message from Arduino
                        text_linked.setText(arduinoMsg);
                        break;
                }
            }
        };


        button_controls.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent activityChangeIntent = new Intent(MainActivity.this, ControlsActivity.class);

                MainActivity.this.startActivity(activityChangeIntent);

            }
        });

        button_search.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //Check if the phone supports BT
                if (bluetoothAdapter == null) {
                    // Device doesn't support Bluetooth
                    Log.d(TAG, "Device doesn't support Bluetooth");
                } else {
                    Log.d(TAG, "Device support Bluetooth");
                    //Check BT enabled. If disabled, we ask the user to enable BT
                    if (!bluetoothAdapter.isEnabled()) {
                        Log.d(TAG, "Bluetooth is disabled");
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            Log.d(TAG, "We don't have BT Permissions");
                            //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                            //Log.d(TAG, "Bluetooth is enabled now");
                        } else {
                            Log.d(TAG, "We have BT Permissions");
                            //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                            //Log.d(TAG, "Bluetooth is enabled now");
                        }


                    } else {
                        Log.d(TAG, "Bluetooth is enabled");
                    }
                    String btDevicesString = "";
                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

                    if (pairedDevices.size() > 0) {
                        Log.d(TAG, "Entro aca");
                        // There are paired devices. Get the name and address of each paired device.
                        for (BluetoothDevice device : pairedDevices) {
                            String deviceName = device.getName();
                            String deviceHardwareAddress = device.getAddress(); // MAC address
                            Log.d(TAG, "deviceName:" + deviceName);
                            Log.d(TAG, "deviceHardwareAddress:" + deviceHardwareAddress);
                            //We append all devices to a String that we will display in the UI
                            btDevicesString = btDevicesString + deviceName + " || " + deviceHardwareAddress + "\n";
                            //If we find the HC 05 device (the Arduino BT module)
                            //We assign the device value to the Global variable BluetoothDevice
                            //We enable the button "Connect to HC 05 device"
                            if (deviceName.equals("EC744")) {
                                Log.d(TAG, "HC-05 found");
                                arduinoUUID = device.getUuids()[0].getUuid();
                                arduinoBTModule = device;
                                //HC -05 Found, enabling the button to read results
                                button_connect.setEnabled(true);
                            }
                            text_linked.setText(btDevicesString);
                        }
                    }
                }
                Log.d(TAG, "Button Pressed");
            }
        });

        button_connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                connectThread.start();
                Log.d(TAG, "CONECTO");

            }
        });

    }


}