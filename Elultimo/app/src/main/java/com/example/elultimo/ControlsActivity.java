package com.example.elultimo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class ControlsActivity extends AppCompatActivity implements SensorEventListener
{

    private static final String TAG = "InfluLogs";
    private final static float ACC = 30;
    ConnectThread connectThread;
    public static Handler handler;

    private StringBuilder recDataString = new StringBuilder();
    // SPP UUID service  - Funciona en la mayoria de los dispositivos
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    Handler bluetoothIn;
    final int handlerState = 0; //used to identify handler message
    UUID arduinoUUID = BTMODULEUUID;

    private SensorManager sensor;

    private String mode;
    BluetoothDevice arduinoBTModule = null;

    TextView state;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controls);


        //defino el Handler de comunicacion entre el hilo Principal  el secundario.
        //El hilo secundario va a mostrar informacion al layout atraves utilizando indeirectamente a este handler
        bluetoothIn = Handler_Msg_Hilo_Principal();

        sensor = (SensorManager) getSystemService(SENSOR_SERVICE);
        registerSenser();

        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        final Button button_change_mode = findViewById(R.id.change_mode);
        final Button button_left = findViewById(R.id.button4);
        final Button button_right = findViewById(R.id.button5);
        final Button change_light_mode = findViewById(R.id.change_light_mode);
        state = findViewById(R.id.estado);
        final Button onOff = findViewById(R.id.OnOff);
        final Button automatic = findViewById(R.id.Automatic);

        state.setText("Espere..");

        if (bluetoothAdapter == null) // Device doesn't support Bluetooth
        {
            Log.d(TAG, "Device doesn't support Bluetooth");
        }
        else
        {
            Log.d(TAG, "Device support Bluetooth");
            if (!bluetoothAdapter.isEnabled()) //Check BT enabled. If disabled, we ask the user to enable BT
            {
                Log.d(TAG, "Bluetooth is disabled");

                Toast.makeText(getApplicationContext(), "Bluetooth no activado", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Log.d(TAG, "Bluetooth is enabled");
                if (ContextCompat.checkSelfPermission(ControlsActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED)
                {
                    ActivityCompat.requestPermissions(ControlsActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                    return;
                }
            }

            StringBuilder btDevicesString = new StringBuilder();
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0)
            {
                for (BluetoothDevice device : pairedDevices) // There are paired devices. Get the name and address of each paired device.
                {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    //We append all devices to a String that we will display in the UI
                    btDevicesString.append(deviceName).append(" || ").append(deviceHardwareAddress).append("\n");
                    //If we find the HC 05 device (the Arduino BT module)
                    //We assign the device value to the Global variable BluetoothDevice
                    //We enable the button "Connect to HC 05 device"
                    if (deviceName.equals(getString(R.string.nombre_hc05)))
                    {
                        Log.d(TAG, "HC-05 found");
                        arduinoUUID = device.getUuids()[0].getUuid();
                        arduinoBTModule = device;
                        //HC -05 Found, enabling the button to read results
                    }
                }
            }
            // Perform action on click
            connectThread = new ConnectThread(arduinoBTModule, arduinoUUID, handler);
            connectThread.run();
            if (connectThread.getMmSocket().isConnected())
            {
                Log.d(TAG, "Calling ConnectedThread class");
            }
            Log.d(TAG, "Bluetooth connected");
            try
            {
                button_change_mode.setEnabled(true);
                change_light_mode.setEnabled(true);
                onOff.setEnabled(true);
                byte aux =  connectThread.getValueRead();
                mode = connectThread.getServoState(aux);
                state.setText("State:"+ mode);
                if(Objects.equals(mode, getString(R.string.SERVO_MANUAL_MODE)))
                {
                    button_left.setEnabled(true);
                    button_right.setEnabled(true);
                }
                else
                {
                    button_left.setEnabled(false);
                    button_right.setEnabled(false);
                }
                mode =  connectThread.getLightstate(aux);
                Log.d(TAG, mode);

                if(Objects.equals(mode, getString(R.string.AUTO_LIGHTS)))
                {
                    onOff.setEnabled(false);
                    automatic.setEnabled(true);
                }
                else if(Objects.equals(mode, getString(R.string.LIGHTS_ON)))
                {
                    onOff.setEnabled(true);
                    automatic.setEnabled(false);
                }
                else if(Objects.equals(mode, getString(R.string.LIGHTS_OFF)))
                {
                    onOff.setEnabled(false);
                    automatic.setEnabled(false);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        button_change_mode.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                try {
                    connectThread.write(Resources.getSystem().getString(R.string.CHANGE_SERVO_MODE));
                    byte aux =  connectThread.getValueRead();
                    mode =  connectThread.getServoState(aux);
                    Log.d(TAG, mode);
                    state.setText("State:"+ mode);
                    if(Objects.equals(mode, Resources.getSystem().getString(R.string.SERVO_MANUAL_MODE)))
                    {
                        button_left.setEnabled(true);
                        button_right.setEnabled(true);
                    }
                    else
                    {
                        button_left.setEnabled(false);
                        button_right.setEnabled(false);
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });

        button_left.setOnClickListener(v -> {
            try
            {
                connectThread.write(Resources.getSystem().getString(R.string.MOVE_SERVO_LEFT));
                Log.d(TAG, "left");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });

        button_right.setOnClickListener(v -> {
            try
            {
                connectThread.write(Resources.getSystem().getString(R.string.MOVE_SERVO_RIGHT));
                Log.d(TAG, "right");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });

        change_light_mode.setOnClickListener(v -> {
            try
            {
                connectThread.write(Resources.getSystem().getString(R.string.CHANGE_LIGHTS_MODE));
                byte aux =  connectThread.getValueRead();
                mode =  connectThread.getLightstate(aux);
                Log.d(TAG, mode);

                if(Objects.equals(mode, Resources.getSystem().getString(R.string.AUTO_LIGHTS)))
                {
                    onOff.setEnabled(false);
                    automatic.setEnabled(true);
                }
                else if(Objects.equals(mode, Resources.getSystem().getString(R.string.LIGHTS_ON)))
                {
                    onOff.setEnabled(true);
                    automatic.setEnabled(false);
                }
                else if(Objects.equals(mode, Resources.getSystem().getString(R.string.LIGHTS_OFF)))
                {
                    onOff.setEnabled(false);
                    automatic.setEnabled(false);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        int sensorType = event.sensor.getType();
        float[] values = event.values;

        if (sensorType == Sensor.TYPE_ACCELEROMETER)
        {
            if ((Math.abs(values[0]) > ACC || Math.abs(values[1]) > ACC || Math.abs(values[2]) > ACC))
            {
                Log.i(TAG, "cambio");
                try
                {
                    connectThread.write(Resources.getSystem().getString(R.string.CHANGE_SERVO_MODE));

                    Log.d(TAG, "Cambio por sensor");
                    if(Objects.equals(mode, Resources.getSystem().getString(R.string.SERVO_INFLUENCER_MODE))){
                        //TODO ver q onda
                    }

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }


    //Handler que permite mostrar datos en el Layout al hilo secundario
    private Handler Handler_Msg_Hilo_Principal ()
    {
        return new Handler()
        {
            public void handleMessage(android.os.Message msg)
            {
                //si se recibio un msj del hilo secundario
                if (msg.what == handlerState)
                {
                    //voy concatenando el msj
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);
                    int endOfLineIndex = recDataString.indexOf("\r\n");

                    //cuando recibo toda una linea la muestro en el layout
                    if (endOfLineIndex > 0)
                    {
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);
                        recDataString.delete(0, recDataString.length());
                    }
                }
            }
        };
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void registerSenser()
    {
        boolean done;
        done = sensor.registerListener(this, sensor.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        if (!done)
        {
            Log.i(TAG, "not register");
        }
        Log.i(TAG, "register");
    }

    private void unregisterSenser()
    {
        //TODO hacer en el Ondestroy o ONstop
        sensor.unregisterListener(this);
        Log.i(TAG, "unregister");
    }
}