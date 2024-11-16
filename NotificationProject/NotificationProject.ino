#include "BluetoothSerial.h"
#include <ArduinoJson.h>

BluetoothSerial SerialBT;

void setup() {
    Serial.begin(115200);
    SerialBT.begin("ESP32_BT"); // Bluetooth device name
    Serial.println("Bluetooth started. Waiting for messages...");
}

void loop() {
    if (SerialBT.available()) {
        String message = SerialBT.readString();
        Serial.print("Received message: ");
        Serial.println(message);

        // Parse the JSON message
        StaticJsonDocument<200> doc; // Adjust the size if needed for larger JSON
        DeserializationError error = deserializeJson(doc, message);

        if (error) {
            Serial.print("JSON parsing failed: ");
            Serial.println(error.c_str());
            return;
        }

        // Extract values from JSON
        const char* appName = doc["App Name"];
        const char* title = doc["Title"];
        const char* text = doc["Text"];
        const char* mapText = doc["MapText"];

        // Print the parsed values
        Serial.println("Parsed Values:");
        Serial.print("App Name: ");
        Serial.println(appName ? appName : "N/A");
        Serial.print("Title: ");
        Serial.println(title ? title : "N/A");
        Serial.print("Text: ");
        Serial.println(text ? text : "N/A");
        Serial.print("MapText: ");
        Serial.println(mapText ? mapText : "N/A");
    }
}
