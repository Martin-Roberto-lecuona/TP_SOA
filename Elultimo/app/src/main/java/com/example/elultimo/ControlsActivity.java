package com.example.elultimo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class ControlsActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "InfluLogs";
    private final static float ACC = 30;
    ConnectThread connectThread;
    public static Handler handler;

    private StringBuilder recDataString = new StringBuilder();
    // SPP UUID service  - Funciona en la mayoria de los dispositivos
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private Handler bluetoothIn;

    BluetoothAdapter bluetoothAdapter = null;
    final int handlerState = 0; //used to identify handler message
    UUID arduinoUUID = BTMODULEUUID;

    private SensorManager sensor;

    private String mode;
    BluetoothDevice arduinoBTModule = null;
    private BluetoothSocket btSocket = null;
    private static String address = null;
    private ConnectedThread mConnectedThread;
    TextView state;
    TextView lightState;
    Button button_left;
    Button button_right;

    private final static String SERVO_MANUAL_MODE = "Manual";
    private final static String CHANGE_SERVO_MODE = "S";
    private final static String GET_SERVO_MODE = "X";

    private final static String CHANGE_LIGHTS_MODE = "Z";
    private final static String MOVE_SERVO_LEFT = "L";
    private final static String MOVE_SERVO_RIGHT = "R";

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controls);

        sensor = (SensorManager) getSystemService(SENSOR_SERVICE);
        registerSenser();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        bluetoothIn = Handler_Msg_Hilo_Principal();

        final Button button_change_mode = findViewById(R.id.change_mode);
        button_left = findViewById(R.id.LeftButton);
        button_right = findViewById(R.id.RightButton);
        final Button change_light_mode = findViewById(R.id.change_light_mode);
        final Button reload = findViewById(R.id.Reload);
        lightState = findViewById(R.id.LightState);
        state = findViewById(R.id.state);

        state.setText("Espere..");

        button_change_mode.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                    mConnectedThread.write(CHANGE_SERVO_MODE);
                    //byte aux = connectThread.getValueRead();
                    //mode = connectThread.getServoState(aux);
                    //Log.d(TAG, mode);
                    //state.setText("State:" + mode);
                    setManualButtons(button_left, button_right);
            }
        });

        button_left.setOnClickListener(v -> {
            mConnectedThread.write(MOVE_SERVO_LEFT);
            Log.d(TAG, "left");
        });

        button_right.setOnClickListener(v -> {
            mConnectedThread.write(MOVE_SERVO_RIGHT);
            Log.d(TAG, "right");
        });

        change_light_mode.setOnClickListener(v -> {
                mConnectedThread.write(CHANGE_LIGHTS_MODE);
                //byte aux = connectThread.getValueRead();
                //mode = connectThread.getLightstate(aux);
                lightState.setText("Light mode: " + mode);
                Log.d(TAG, mode);
        });
        reload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                    mConnectedThread.write(GET_SERVO_MODE);
                    //byte aux = connectThread.getValueRead();
                    //mode = connectThread.getServoState(aux);
                    //Log.d(TAG, mode);
                    //state.setText("State:" + mode);
                    //setManualButtons(button_left, button_right);
                    //mode = connectThread.getLightstate(aux);
                    //lightState.setText("Light mode: " + mode);
            }
        });
    }

    public void onResume() {
        super.onResume();

        final Button button_change_mode = findViewById(R.id.change_mode);
        button_left = findViewById(R.id.LeftButton);
        button_right = findViewById(R.id.RightButton);
        final Button change_light_mode = findViewById(R.id.change_light_mode);
        final Button reload = findViewById(R.id.Reload);
        lightState = findViewById(R.id.LightState);
        state = findViewById(R.id.state);

        //Obtengo el parametro, aplicando un Bundle, que me indica la Mac Adress del HC05
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        address = extras.getString("Direccion_Bluethoot");
        Log.d(TAG, "Obtuvo la direccion bluetooth"+ address);

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        //se realiza la conexion del Bluethoot crea y se conectandose a atraves de un socket
        try {
            Log.d(TAG, "entró al switch");
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Log.d(TAG, "Falló bluetooth");
        }
        Log.d(TAG, "salio del try");

        // Establish the Bluetooth socket connection.
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                //insert code to deal with this
            }
        }
        //Una establecida la conexion con el Hc05 se crea el hilo secundario, el cual va a recibir
        // los datos de Arduino atraves del bluethoot
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
    }

    @Override
    //Cuando se ejecuta el evento onPause se cierra el socket Bluethoot, para no recibiendo datos
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        float[] values = event.values;

        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            if ((Math.abs(values[0]) > ACC || Math.abs(values[1]) > ACC || Math.abs(values[2]) > ACC)) {
                Log.i(TAG, "cambio");

                    mConnectedThread.write(CHANGE_SERVO_MODE);
                    //byte aux = connectThread.getValueRead();
                    //mode = connectThread.getServoState(aux);
                    Log.d(TAG, mode);
                    //state.setText("State:" + mode);
                    //setManualButtons(button_left, button_right);
                    //mode = connectThread.getLightstate(aux);
                    //lightState.setText("Light mode: " + mode)
            }
        }
    }


    //Handler que permite mostrar datos en el Layout al hilo secundario
    private Handler Handler_Msg_Hilo_Principal() {
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
                        state.setText(dataInPrint);
                        Log.d(TAG, dataInPrint);
                        recDataString.delete(0, recDataString.length());
                    }
                }
            }
        };
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details
        }
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
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
    @Override
    protected  void onDestroy()
    {
        super.onDestroy();
        unregisterSenser();
        connectThread.cancel();
    }
    private void unregisterSenser()
    {
        //TODO hacer en el Ondestroy o ONstop
        sensor.unregisterListener(this);
        Log.i(TAG, "unregister");
    }

    public void setManualButtons(Button left, Button right)
    {
        if(Objects.equals(mode, SERVO_MANUAL_MODE))
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

    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //Constructor de la clase del hilo secundario
        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try
            {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        //metodo run del hilo, que va a entrar en una espera activa para recibir los msjs del HC05
        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            //el hilo secundario se queda esperando mensajes del HC05
            while (true)
            {
                try
                {
                    //se leen los datos del Bluethoot
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);

                    //se muestran en el layout de la activity, utilizando el handler del hilo
                    // principal antes mencionado
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }


        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                //showToast("La conexion fallo");
                Log.d(TAG, "La creacción del Socket fallo");
                finish();
            }
        }
    }
}