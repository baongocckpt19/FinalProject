#include <Arduino.h>
#include <Adafruit_Fingerprint.h>

#define FP_RX_PIN 16
#define FP_TX_PIN 17

HardwareSerial FingerSerial(2);
Adafruit_Fingerprint finger = Adafruit_Fingerprint(&FingerSerial);

void setup() {
  Serial.begin(115200);
  delay(1000);

  Serial.println("=== RESET FINGERPRINT SENSOR ===");

  FingerSerial.begin(57600, SERIAL_8N1, FP_RX_PIN, FP_TX_PIN);
  finger.begin(57600);
  delay(100);

  if (!finger.verifyPassword()) {
    Serial.println("❌ Sensor NOT found!");
    while (1) delay(1000);
  }

  Serial.println("✅ Sensor found");

  Serial.println("⚠️ Deleting ALL fingerprint templates...");
  uint8_t p = finger.emptyDatabase();

  if (p == FINGERPRINT_OK) {
    Serial.println("✅ EMPTY DATABASE SUCCESS");
  } else {
    Serial.print("❌ EMPTY DATABASE FAILED, code = ");
    Serial.println(p);
  }

  // kiểm tra lại
  Serial.println("🔍 Checking slots...");
  for (int i = 1; i <= 100; i++) {
    int r = finger.loadModel(i);
    if (r == FINGERPRINT_OK) {
      Serial.print("❌ Slot ");
      Serial.print(i);
      Serial.println(" STILL USED");
    }
  }

  Serial.println("🎉 SENSOR READY (ALL CLEAN)");
}

void loop() {}
