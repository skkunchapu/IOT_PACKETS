#include "BluetoothSerial.h"
#include <ArduinoJson.h>

// Define LED pin numbers (ESP32-specific GPIOs)
#define PHONE_LED 2    // Blue LED for Phone (GPIO 2 is often used as the onboard LED)
#define MESSAGE_LED 4   // Green LED for Message (GPIO 4)
#define WHATSAPP_LED 5     // Red LED for WhatsApp (GPIO 5)
#define GMAIL_LED 18 // Yellow LED for Gmail (GPIO 18)

// Directions LEDs
#define NORTH_LED 19    // Blue LED for North direction (GPIO 19)
#define SOUTH_LED 21   // Green LED for South direction (GPIO 21)
#define EAST_LED 22   // Red LED for East direction (GPIO 22)
#define WEST_LED 23  // Yellow LED for West direction (GPIO 23)

// Button pin for accepting/rejecting calls
#define ACCEPT_BUTTON_PIN 15  // GPIO 15 for the button
#define REJECT_BUTTON_PIN 35  // GPIO 35 for the button

BluetoothSerial SerialBT;

String latestMessage = "";  // To store the latest message

// Function to blink an LED 3 times
void blinkLED(int ledPin) {
    for (int i = 0; i < 3; i++) {
        digitalWrite(ledPin, HIGH);  // Turn LED on
        delay(500);                  // Wait for 500ms
        digitalWrite(ledPin, LOW);   // Turn LED off
        delay(500);                  // Wait for 500ms
    }
}

// Function to send call response (accept/reject)
void sendCallResponse(String response) {
    SerialBT.println(response); // Send the response to Android app via Bluetooth
    Serial.print("Sent response: ");
    Serial.println(response);
}

void setup() {
    Serial.begin(115200);
    SerialBT.begin("ESP32_BT"); // Bluetooth device name
    Serial.println("Bluetooth started. Waiting for messages...");

    // Set LED pins as output
    pinMode(PHONE_LED, OUTPUT);
    pinMode(MESSAGE_LED, OUTPUT);
    pinMode(WHATSAPP_LED, OUTPUT);
    pinMode(GMAIL_LED, OUTPUT);

    pinMode(NORTH_LED, OUTPUT);
    pinMode(SOUTH_LED, OUTPUT);
    pinMode(EAST_LED, OUTPUT);
    pinMode(WEST_LED, OUTPUT);

    // Set the button pin as input with pull-up resistor
    pinMode(ACCEPT_BUTTON_PIN, INPUT_PULLUP);  // Using internal pull-up resistor
    pinMode(REJECT_BUTTON_PIN, INPUT_PULLUP);  // Using internal pull-up resistor
}

void loop() {
    // Check if ACCEPT_BUTTON_PIN is pressed at any time
    if (digitalRead(ACCEPT_BUTTON_PIN) == LOW) {
        // Send "ANSWER_CALL" response if button is pressed
        sendCallResponse("ANSWER_CALL");
        delay(1000); // Debounce delay to prevent multiple triggers
    }

    if (SerialBT.available()) {
        String message = SerialBT.readString();
        // Only update if the message is different from the last one
        if (message != latestMessage) {
            latestMessage = message;  // Update to the latest message
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

            // Control LEDs based on app name
            if (strcmp(appName, "Phone") == 0) {
                blinkLED(PHONE_LED);  // Blue LED for Phone

                // Check if the app name is Phone and send call response
                if (strcmp(text, "Incoming call") == 0) {
                    // Assuming there are buttons connected to accept or reject calls
                    // Here, we simulate accepting or rejecting based on some condition
                    delay(3000);
                    // Check if the button is pressed
                    if (digitalRead(ACCEPT_BUTTON_PIN) == LOW) {
                        // Simulate Accepting the call if the button is pressed
                        sendCallResponse("ANSWER_CALL");
                    } else if (digitalRead(REJECT_BUTTON_PIN) == LOW) {
                        // Simulate Rejecting the call if the button is not pressed
                        sendCallResponse("REJECT_CALL");
                    }
                    delay(1000);
                }
                if (strcmp(text, "Ongoing call") == 0) {
                    // Assuming there are buttons connected to accept or reject calls
                    // Here, we simulate accepting or rejecting based on some condition
                    delay(3000);
                    if (digitalRead(REJECT_BUTTON_PIN) == LOW) {
                        // Simulate Rejecting the call if the button is not pressed
                        sendCallResponse("REJECT_CALL");
                    }
                    delay(1000);
                }
            }
            else if (strcmp(appName, "Message") == 0) {
                blinkLED(MESSAGE_LED); // Green LED for Message
            }
            else if (strcmp(appName, "Maps") == 0) {
                blinkLED(WHATSAPP_LED); 
                // Control LEDs based on directions
                if (strcmp(mapText, "Head north") == 0) {
                    blinkLED(NORTH_LED);
                }
                else if (strcmp(mapText, "Head south") == 0) {
                    blinkLED(SOUTH_LED);
                }
                else if (strcmp(mapText, "Head east") == 0) {
                    blinkLED(EAST_LED);
                }
                else if (strcmp(mapText, "Head west") == 0) {
                    blinkLED(WEST_LED);
                }
            }
            else if (strcmp(appName, "Gmail") == 0) {
                blinkLED(GMAIL_LED); // Yellow LED for Gmail
            }
        }
    }

    // Add a delay to avoid continuous prints
    delay(1000);  // Delay for 1 second
}
