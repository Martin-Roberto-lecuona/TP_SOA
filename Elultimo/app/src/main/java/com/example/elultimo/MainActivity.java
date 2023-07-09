package com.example.elultimo;

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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "InfluLogs";
    public static Handler handler;
    private final static int ERROR_READ = 0;
    BluetoothDevice arduinoBTModule = null;
    UUID arduinoUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        final Button button_search = findViewById(R.id.search);
        final Button button_connect = findViewById(R.id.connect);
        final TextView text_linked = findViewById(R.id.linked_devices);

        handler = new Handler(Looper.getMainLooper())
        {
            @Override
            public void handleMessage(Message msg)
            {
                if (msg.what == ERROR_READ)
                {
                    String arduinoMsg = msg.obj.toString(); // Read message from Arduino
                    text_linked.setText(arduinoMsg);
                }
            }
        };

        button_search.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                if (bluetoothAdapter == null) //Check if the phone supports BT
                {
                    Log.d(TAG, "Device doesn't support Bluetooth");
                }
                else
                {
                    Log.d(TAG, "Device support Bluetooth");
                    if (!bluetoothAdapter.isEnabled())  //Check BT enabled. If disabled, we ask the user to enable BT
                    {
                        Log.d(TAG, "Bluetooth is disabled");
                        Toast.makeText(getApplicationContext(), "Bluetooth no activado", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Log.d(TAG, "Bluetooth is enabled");
                        if (ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED)
                        {
                            int requestCode = 2;
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.BLUETOOTH_CONNECT}, requestCode);
                            return;
                        }
                    }
                    String btDevicesString = "";
                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

                    if (pairedDevices.size() > 0)
                    {
                        for (BluetoothDevice device : pairedDevices)
                        {
                            // There are paired devices. Get the name and address of each paired device.
                            String deviceName = device.getName();
                            String deviceHardwareAddress = device.getAddress(); // MAC address

                            //We append all devices to a String that we will display in the UI
                            btDevicesString = btDevicesString + deviceName + " || " + deviceHardwareAddress + "\n";

                            //If we find the HC 05 device (the Arduino BT module)
                            //We assign the device value to the Global variable BluetoothDevice
                            //We enable the button "Connect to HC 05 device"
                            if (deviceName.equals(getString(R.string.nombre_hc05)))
                            {
                                Log.d(TAG, "HC-05 found");
                                arduinoUUID = device.getUuids()[0].getUuid();
                                arduinoBTModule = device;
                            }
                            text_linked.setText(btDevicesString);
                        }
                    }
                }
                button_connect.setEnabled(true);
                Log.d(TAG, "Button Pressed");
            }
        });

        button_connect.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent activityChangeIntent = new Intent(MainActivity.this, ControlsActivity.class);
                MainActivity.this.startActivity(activityChangeIntent);
            }
        });
    }
}