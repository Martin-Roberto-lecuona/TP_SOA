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

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "InfluLogs";
    public static Handler handler;
    private final static int ERROR_READ = 0;
    BluetoothDevice arduinoBTModule = null;
    UUID arduinoUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        final Button button_search = findViewById(R.id.search);
        final Button button_connect = findViewById(R.id.connect);
        final TextView text_linked = findViewById(R.id.linked_devices);

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == ERROR_READ) {
                    String arduinoMsg = msg.obj.toString(); // Leer mensaje desde Arduino
                    text_linked.setText(arduinoMsg);
                }
            }
        };

        button_search.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (bluetoothAdapter == null) {
                    Log.d(TAG, "Device doesn't support Bluetooth");
                    return;
                }

                if (!bluetoothAdapter.isEnabled()) {
                    Log.d(TAG, "Bluetooth is disabled");
                    Toast.makeText(getApplicationContext(), "Bluetooth no activado", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH}, 2);
                    return;
                }

                StringBuilder btDevicesString = new StringBuilder();
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

                if (pairedDevices.size() > 0) {
                    for (BluetoothDevice device : pairedDevices) {
                        String deviceName = device.getName();
                        String deviceHardwareAddress = device.getAddress(); // Dirección MAC

                        btDevicesString.append(deviceName).append(" || ").append(deviceHardwareAddress).append("\n");

                        if (deviceName.equals(getString(R.string.nombre_hc05))) {
                            Log.d(TAG, "HC-05 found");
                            arduinoUUID = device.getUuids()[0].getUuid();
                            arduinoBTModule = device;
                        }
                    }
                }

                text_linked.setText(btDevicesString.toString());
                button_connect.setEnabled(true);
                Log.d(TAG, "Button Pressed");
            }
        });

        button_connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (arduinoBTModule == null) {
                    Log.d(TAG, "Arduino BT module not found");
                    return;
                }

                Intent activityChangeIntent = new Intent(MainActivity.this, ControlsActivity.class);
                activityChangeIntent.putExtra("arduinoDevice", arduinoBTModule);
                MainActivity.this.startActivity(activityChangeIntent);
            }
        });
    }
}
