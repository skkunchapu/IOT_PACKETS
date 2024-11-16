package com.example.bluetoothtextsender;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;

public class BluetoothManager {
    private static BluetoothManager instance;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;

    private BluetoothManager() {}

    public static synchronized BluetoothManager getInstance() {
        if (instance == null) {
            instance = new BluetoothManager();
        }
        return instance;
    }

    public void setBluetoothSocket(BluetoothSocket socket) {
        this.bluetoothSocket = socket;
        try {
            this.outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            this.outputStream = null;
        }
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }
}

