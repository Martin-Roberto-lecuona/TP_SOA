package com.example.elultimo;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class ControlsActivity extends AppCompatActivity implements SensorEventListener{

    private static final String TAG = "InfluLogs";
    private final static float ACC = 30;
    ConnectThread connectThread;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    // SPP UUID service  - Funciona en la mayoria de los dispositivos
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    Handler bluetoothIn;
    final int handlerState = 0; //used to identify handler message

    private SensorManager sensor;

    // String for MAC address del Hc05
    private static String address = null;
    private String mode;
    private String lights;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controls);
        //obtengo el adaptador del bluethoot
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        //defino el Handler de comunicacion entre el hilo Principal  el secundario.
        //El hilo secundario va a mostrar informacion al layout atraves utilizando indeirectamente a este handler
        bluetoothIn = Handler_Msg_Hilo_Principal();

        sensor = (SensorManager) getSystemService(SENSOR_SERVICE);
        registerSenser();

        final Button button_influencer_mode = (Button) findViewById(R.id.influencer_mode);
        final Button button_manual_mode = (Button) findViewById(R.id.manual_mode);
        final Button button_security_cam = (Button) findViewById(R.id.security_cam);
        final Button button_left = (Button) findViewById(R.id.button4);
        final Button button_right = (Button) findViewById(R.id.button5);
        final Switch switch_automatic_lights = (Switch) findViewById(R.id.automatic_lights);
        final TextView state = (TextView) findViewById(R.id.estado);

        state.setText("Estado: Influencer auto lights");
        mode = "Influencer";
        button_manual_mode.setEnabled(true);
        button_influencer_mode.setEnabled(false);
        button_security_cam.setEnabled(false);
        button_left.setEnabled(false);
        button_right.setEnabled(false);

        button_influencer_mode.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                try {
                    connectThread.write("S");
                    button_manual_mode.setEnabled(true);
                    button_influencer_mode.setEnabled(false);
                    Log.d(TAG, "Modo manual");
                    mode = "Manual";
                    state.setText("Estado:"+ mode + lights);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        button_manual_mode.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                try {
                    connectThread.write("S");
                    button_security_cam.setEnabled(true);
                    button_left.setEnabled(true);
                    button_right.setEnabled(false);
                    button_manual_mode.setEnabled(false);
                    mode = "Security cam";
                    state.setText("Estado:"+ mode + lights);
                    Log.d(TAG, "Modo security");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        button_security_cam.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                try {
                    connectThread.write("S");
                    button_influencer_mode.setEnabled(true);
                    button_security_cam.setEnabled(false);
                    button_left.setEnabled(false);
                    button_right.setEnabled(false);
                    Log.d(TAG, "Modo influ");
                    mode = "Influencer";
                    state.setText("Estado:"+ mode + lights);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        button_left.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                try {
                    connectThread.write("L");
                    Log.d(TAG, "Izquierda");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        button_right.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                try {
                    connectThread.write("R");
                    Log.d(TAG, "Derecha");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        switch_automatic_lights.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Perform actions based on the switch state
                if (isChecked) {
                    try {
                        connectThread.write("Z");
                        lights = "auto lights";
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        connectThread.write("Z");
                        lights = "manual lights";
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        int sensorType = event.sensor.getType();

        float[] values = event.values;

        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            if ((Math.abs(values[0]) > ACC || Math.abs(values[1]) > ACC || Math.abs(values[2]) > ACC)) {
                Log.i(TAG, "cambio");
                try {
                    connectThread.write("S");
                    Log.d(TAG, "Cambio por sensor");
                    if( mode == "Influencer"){

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Handler que sirve que permite mostrar datos en el Layout al hilo secundario
    private Handler Handler_Msg_Hilo_Principal ()
    {
        return new Handler() {
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
                        //.setText(dataInPrint);

                        recDataString.delete(0, recDataString.length());
                    }
                }
            }
        };
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

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
        sensor.unregisterListener(this);
        Log.i(TAG, "unregister");
    }


}