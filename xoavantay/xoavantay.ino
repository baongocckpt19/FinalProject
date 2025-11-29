/******************************************************
 *  ESP32 + AS608
 *  CHƯƠNG TRÌNH XÓA TOÀN BỘ VÂN TAY TRONG SENSOR
 *
 *  - Không dùng WiFi, LCD, server
 *  - Chỉ dùng UART2 để nói chuyện với AS608
 *  - Khi bật lên, nó sẽ:
 *      + Verify password (check sensor OK)
 *      + Gọi emptyDatabase() để xóa toàn bộ
 *      + In kết quả ra Serial
 ******************************************************/

#include <HardwareSerial.h>
#include <Adafruit_Fingerprint.h>

// UART2 của ESP32 cho AS608
// RX của ESP32 nối TX của AS608
// TX của ESP32 nối RX của AS608
#define FP_RX_PIN 16  // nhận từ TX sensor
#define FP_TX_PIN 17  // gửi tới RX sensor

HardwareSerial FingerSerial(2);
Adafruit_Fingerprint finger = Adafruit_Fingerprint(&FingerSerial);

// Hàm xóa toàn bộ vân tay
uint8_t clearAllFingerprints() {
  Serial.println("[FP] Dang xoa TOAN BO van tay trong sensor...");

  uint8_t p = finger.emptyDatabase();  // Lệnh xóa toàn bộ

  if (p == FINGERPRINT_OK) {
    Serial.println("[FP] THANH CONG: Da xoa tat ca van tay!");
  } else {
    Serial.print("[FP] THAT BAI: error code = ");
    Serial.println(p);
    Serial.println("  Go y: Kiem tra ket noi RX/TX, nguon, baudrate 57600.");
  }

  return p;
}

void setup() {
  Serial.begin(115200);
  delay(1000);

  Serial.println();
  Serial.println("=======================================");
  Serial.println("  ESP32 + AS608 - CLEAR FINGERPRINT DB  ");
  Serial.println("=======================================");
  Serial.println();

  // Khoi dong UART2 cho AS608
  FingerSerial.begin(57600, SERIAL_8N1, FP_RX_PIN, FP_TX_PIN);
  delay(200);

  // Khoi dong Adafruit_Fingerprint
  finger.begin(57600);
  delay(100);

  Serial.println("[FP] Dang verify password...");

  if (finger.verifyPassword()) {
    Serial.println("[FP] Tim thay cam bien van tay!");
  } else {
    Serial.println("[FP] KHONG tim thay cam bien van tay hoac sai mat khau!");
    Serial.println(" => Kiem tra lai day noi, chan RX/TX, baudrate.");
    // Treo luon vi sensor khong OK
    while (true) {
      delay(1000);
    }
  }

  // Xoa toan bo vân tay
  clearAllFingerprints();

  Serial.println();
  Serial.println("=== HOAN TAT CHUONG TRINH XOA VÂN TAY ===");
  Serial.println("Neu muon xoa lai, reset lai ESP32.");
}

void loop() {
  // Không làm gì trong loop
  // Chương trình xóa xong là xong, chỉ để yên
  delay(1000);
}
