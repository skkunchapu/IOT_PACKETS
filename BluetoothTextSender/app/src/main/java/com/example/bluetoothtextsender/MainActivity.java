package com.example.bluetoothtextsender;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 2;
    private static final String TAG = "MainActivity";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private ArrayAdapter<String> deviceArrayAdapter;

    private EditText messageInput;
    private Button sendButton;
    private ListView bluetoothDeviceList;

    private static final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        bluetoothDeviceList = findViewById(R.id.bluetoothDeviceList);

        // Set up Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Request Notification Access
        requestNotificationAccess();

        // Check Bluetooth permissions and enable Bluetooth if needed
        checkBluetoothPermissions();

        // Set up paired devices list
        deviceArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        bluetoothDeviceList.setAdapter(deviceArrayAdapter);
        listPairedDevices();

        // Device selection and connection
        bluetoothDeviceList.setOnItemClickListener((parent, view, position, id) -> {
            String deviceInfo = deviceArrayAdapter.getItem(position);
            String deviceAddress = deviceInfo.substring(deviceInfo.length() - 17);
            connectToDevice(deviceAddress);
        });

        // Send message button functionality
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString();
            if (!message.isEmpty() && outputStream != null) {
                try {
                    outputStream.write(message.getBytes());
                    Toast.makeText(MainActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.e(TAG, "Error sending message", e);
                    Toast.makeText(MainActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Method to check Bluetooth permissions
    private void checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, REQUEST_BLUETOOTH_PERMISSIONS);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_BLUETOOTH_PERMISSIONS);
            }
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    // List paired Bluetooth devices with permission check
    private void listPairedDevices() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            try {
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    for (BluetoothDevice device : pairedDevices) {
                        String deviceName = device.getName();
                        String deviceAddress = device.getAddress();
                        deviceArrayAdapter.add(deviceName + "\n" + deviceAddress);
                    }
                } else {
                    Toast.makeText(this, "No paired Bluetooth devices found", Toast.LENGTH_SHORT).show();
                }
            } catch (SecurityException e) {
                Log.e(TAG, "Permission error when listing paired devices", e);
            }
        } else {
            Log.e(TAG, "Bluetooth permissions are not granted.");
        }
    }

    // Connect to the selected Bluetooth device with permission check
    /*private void connectToDevice(String deviceAddress) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID_SPP);
                bluetoothSocket.connect();
                outputStream = bluetoothSocket.getOutputStream();
                Toast.makeText(this, "Connected to " + device.getName(), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e(TAG, "Connection failed", e);
                Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Bluetooth permissions not granted", Toast.LENGTH_SHORT).show();
        }
    }*/

    private void connectToDevice(String deviceAddress) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID_SPP);
                bluetoothSocket.connect();

                // Share the Bluetooth socket via BluetoothManager
                BluetoothManager.getInstance().setBluetoothSocket(bluetoothSocket);

                Toast.makeText(this, "Connected to " + device.getName(), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e(TAG, "Connection failed", e);
                Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Bluetooth permissions not granted", Toast.LENGTH_SHORT).show();
        }
    }


    // Check if Notification Access is enabled
    private void requestNotificationAccess() {
        if (!Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners").contains(getPackageName())) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
            Toast.makeText(this, "Please enable notification access", Toast.LENGTH_LONG).show();
        }
    }

    // Handle Bluetooth permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            listPairedDevices();
        } else {
            Toast.makeText(this, "Bluetooth permissions are required", Toast.LENGTH_SHORT).show();
        }
    }

    // Close Bluetooth socket when activity is destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (outputStream != null) outputStream.close();
            if (bluetoothSocket != null) bluetoothSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Error closing socket", e);
        }
    }

}
