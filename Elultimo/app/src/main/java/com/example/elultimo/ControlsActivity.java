package com.example.elultimo;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class ControlsActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "InfluLogs";
    private final static float ACC = 30;
    private ConnectThread connectThread;
    private Handler handler;

    private TextView state;
    private TextView lightState;
    private Button button_left;
    private Button button_right;

    private final static String SERVO_MANUAL_MODE = "Manual";
    private final static String CHANGE_SERVO_MODE = "S";
    private final static String GET_SERVO_MODE = "X";
    private final static String CHANGE_LIGHTS_MODE = "Z";
    private final static String MOVE_SERVO_LEFT = "L";
    private final static String MOVE_SERVO_RIGHT = "R";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controls);

        state = findViewById(R.id.state);
        lightState = findViewById(R.id.LightState);
        button_left = findViewById(R.id.LeftButton);
        button_right = findViewById(R.id.RightButton);

        Intent intent = getIntent();
        BluetoothDevice arduinoDevice = intent.getParcelableExtra("arduinoDevice");
        if (arduinoDevice == null) {
            Log.d(TAG, "Arduino device not found");
            Toast.makeText(getApplicationContext(), "Arduino device not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        handler = new Handler();
        connectThread = new ConnectThread(arduinoDevice, handler, this);
        connectThread.start();

        Button button_change_mode = findViewById(R.id.change_mode);
        button_change_mode.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    connectThread.write(CHANGE_SERVO_MODE);
                    byte aux = connectThread.getValueRead();
                    String mode = connectThread.getServoState(aux);
                    Log.d(TAG, mode);
                    state.setText("State: " + mode);
                    setManualButtons(mode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        button_left.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    connectThread.write(MOVE_SERVO_LEFT);
                    Log.d(TAG, "left");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        button_right.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    connectThread.write(MOVE_SERVO_RIGHT);
                    Log.d(TAG, "right");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Button change_light_mode = findViewById(R.id.change_light_mode);
        change_light_mode.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    connectThread.write(CHANGE_LIGHTS_MODE);
                    byte aux = connectThread.getValueRead();
                    String mode = connectThread.getLightstate(aux);
                    lightState.setText("Light mode: " + mode);
                    Log.d(TAG, mode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Button reload = findViewById(R.id.Reload);
        reload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    connectThread.write(GET_SERVO_MODE);
                    byte aux = connectThread.getValueRead();
                    String mode = connectThread.getServoState(aux);
                    Log.d(TAG, mode);
                    state.setText("State: " + mode);
                    setManualButtons(mode);
                    mode = connectThread.getLightstate(aux);
                    lightState.setText("Light mode: " + mode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        float[] values = event.values;

        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            if (Math.abs(values[0]) > ACC || Math.abs(values[1]) > ACC || Math.abs(values[2]) > ACC) {
                Log.i(TAG, "cambio");
                try {
                    connectThread.write(CHANGE_SERVO_MODE);
                    byte aux = connectThread.getValueRead();
                    String mode = connectThread.getServoState(aux);
                    Log.d(TAG, mode);
                    state.setText("State: " + mode);
                    setManualButtons(mode);
                    mode = connectThread.getLightstate(aux);
                    lightState.setText("Light mode: " + mode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectThread != null) {
            connectThread.cancel();
        }
    }

    private void setManualButtons(String mode) {
        if (mode.equals(SERVO_MANUAL_MODE)) {
            button_left.setEnabled(true);
            button_right.setEnabled(true);
        } else {
            button_left.setEnabled(false);
            button_right.setEnabled(false);
        }
    }
}
