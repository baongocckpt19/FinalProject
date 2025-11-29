/******************************************************
 *  ESP32 + AS608 + HTTP Verify Attendance
 *  - Quét vân tay
 *  - Tìm ID trên sensor (fingerID = sensorSlot)
 *  - Gọi API /api/fingerprint/verify
 *  - Hiển thị LCD + buzzer theo kết quả
 ******************************************************/

#include <WiFi.h>
#include <HTTPClient.h>
#include <Adafruit_Fingerprint.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>

/*========================
  1) CẤU HÌNH CƠ BẢN
  ========================*/

// WiFi
const char *WIFI_SSID = "Ngoi Nha Chung";
const char *WIFI_PASSWORD = "123456798";

// API backend
const char *API_URL = "http://192.168.1.60:8080/api/fingerprint/verify";  // ĐỔI IP SERVER
const char *DEVICE_CODE = "ESP_ROOM_LAB1";                                // phải trùng DeviceCode trong DB

// Chân UART2 cho AS608 trên ESP32
// RX của ESP32 nối với TX của AS608
// TX của ESP32 nối với RX của AS608
#define FP_RX_PIN 16  // nhận từ TX sensor
#define FP_TX_PIN 17  // gửi tới RX sensor

HardwareSerial FingerSerial(2);
Adafruit_Fingerprint finger = Adafruit_Fingerprint(&FingerSerial);

// LCD 16x2 I2C (địa chỉ tùy module của bạn, hay dùng 0x27)
LiquidCrystal_I2C lcd(0x27, 16, 2);

// Buzzer
const int BUZZER_PIN = 25;

// Đèn báo (tuỳ chọn)
const int LED_OK_PIN = 2;    // LED xanh (onboard)
const int LED_ERR_PIN = 15;  // LED đỏ (nếu có gắn ngoài)

/*========================
  2) HÀM HỖ TRỢ
  ========================*/

void beepSuccess() {
  // 2 beep ngắn
  for (int i = 0; i < 2; i++) {
    tone(BUZZER_PIN, 2000);
    delay(100);
    noTone(BUZZER_PIN);
    delay(100);
  }
}

void beepDuplicate() {
  // 1 beep nhẹ
  tone(BUZZER_PIN, 1500);
  delay(80);
  noTone(BUZZER_PIN);
}

void beepError() {
  // 1 beep dài
  tone(BUZZER_PIN, 800);
  delay(400);
  noTone(BUZZER_PIN);
}

void showLcdMessage(const String &line1, const String &line2 = "") {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print(line1.substring(0, 16));
  lcd.setCursor(0, 1);
  lcd.print(line2.substring(0, 16));
}

void showWelcome(const String &fullName, const String &statusText) {
  // Màn 1: Xin chào + tên
  showLcdMessage("Xin chao:", fullName);
  delay(1500);

  // Màn 2: trạng thái
  showLcdMessage(statusText);
  delay(1500);
}

/**
 * Hàm helper rất đơn giản để lấy value của một key JSON:
 * json: {"status":"present","fullName":"Nguyen Van A"}
 * extractJsonValue(json, "status") -> "present"
 */
String extractJsonValue(const String &json, const String &key) {
  String search = "\"" + key + "\":";
  int idx = json.indexOf(search);
  if (idx == -1) return "";

  idx += search.length();

  // Bỏ qua khoảng trắng
  while (idx < json.length() && (json[idx] == ' ')) {
    idx++;
  }

  String value = "";

  if (idx < json.length() && json[idx] == '\"') {
    // String
    idx++;
    while (idx < json.length() && json[idx] != '\"') {
      value += json[idx++];
    }
  } else {
    // số / boolean / null
    while (idx < json.length() && json[idx] != ',' && json[idx] != '}' && json[idx] != ' ' && json[idx] != '\n' && json[idx] != '\r') {
      value += json[idx++];
    }
  }

  return value;
}

/*========================
  3) GỬI VERIFY LÊN SERVER
  ========================*/

void handleVerifyResponse(const String &payload) {
  Serial.println("[RESP] " + payload);

  String status = extractJsonValue(payload, "status");
  String fullName = extractJsonValue(payload, "fullName");
  String message = extractJsonValue(payload, "message");

  Serial.println("[PARSE] status   = " + status);
  Serial.println("[PARSE] fullName = " + fullName);
  Serial.println("[PARSE] message  = " + message);

  if (status == "present") {
    digitalWrite(LED_OK_PIN, HIGH);
    digitalWrite(LED_ERR_PIN, LOW);
    showWelcome(fullName, "Diem danh: Co mat");
    beepSuccess();
  } else if (status == "late") {
    digitalWrite(LED_OK_PIN, HIGH);
    digitalWrite(LED_ERR_PIN, LOW);
    showWelcome(fullName, "Diem danh: Muon");
    beepSuccess();
  } else if (status == "duplicate") {
    digitalWrite(LED_OK_PIN, HIGH);
    digitalWrite(LED_ERR_PIN, LOW);
    showLcdMessage("Ban da diem danh", "truoc do");
    beepDuplicate();
    delay(1500);
  } else {  // fail hoặc bất cứ gì khác
    digitalWrite(LED_OK_PIN, LOW);
    digitalWrite(LED_ERR_PIN, HIGH);
    showLcdMessage("Khong co tiet hoc", "");
    beepError();
    delay(1500);
  }

  // trở lại màn hình chờ
  showLcdMessage("Quet van tay", "de diem danh");
  digitalWrite(LED_OK_PIN, LOW);
  digitalWrite(LED_ERR_PIN, LOW);
}

void sendVerifyToServer(int sensorSlot) {
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("[NET] WiFi not connected");
    showLcdMessage("Loi WiFi", "Thu lai sau");
    beepError();
    delay(2000);
    showLcdMessage("Quet van tay", "de diem danh");
    return;
  }

  HTTPClient http;
  http.begin(API_URL);
  http.addHeader("Content-Type", "application/json");

  String body = "{";
  body += "\"deviceCode\":\"" + String(DEVICE_CODE) + "\",";
  body += "\"sensorSlot\":" + String(sensorSlot);
  // nếu muốn gửi timestamp từ ESP:
  // body += ",\"timestamp\":\"2025-11-29T09:00:00\"";
  body += "}";

  Serial.println("[HTTP] POST " + String(API_URL));
  Serial.println("[HTTP] Body: " + body);

  int httpCode = http.POST(body);

  if (httpCode > 0) {
    Serial.printf("[HTTP] code: %d\n", httpCode);
    String payload = http.getString();
    handleVerifyResponse(payload);
  } else {
    Serial.printf("[HTTP] POST failed: %s\n", http.errorToString(httpCode).c_str());
    showLcdMessage("Loi ket noi", "server");
    beepError();
    delay(1500);
    showLcdMessage("Quet van tay", "de diem danh");
  }

  http.end();
}

/*========================
  4) HÀM QUÉT VÂN TAY
  ========================*/

int getFingerprintID() {
  // Ở đây GIẢ ĐỊNH là đã gọi getImage() OK trước đó
  int p = finger.image2Tz();
  if (p != FINGERPRINT_OK) {
    Serial.print("[FP] image2Tz error: ");
    Serial.println(p);
    return -1;
  }

  p = finger.fingerFastSearch();
  if (p != FINGERPRINT_OK) {
    Serial.println("[FP] finger not found in DB");
    return 0; // không có trong thư viện
  }

  Serial.print("[FP] Found ID #");
  Serial.print(finger.fingerID);
  Serial.print(" with confidence ");
  Serial.println(finger.confidence);

  return finger.fingerID;
}


/*========================
  5) SETUP
  ========================*/

void connectWiFi() {
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("[WiFi] Connecting");
  showLcdMessage("Dang ket noi", "WiFi...");

  int retry = 0;
  while (WiFi.status() != WL_CONNECTED && retry < 40) {
    delay(500);
    Serial.print(".");
    retry++;
  }
  Serial.println();

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("[WiFi] Connected!");
    Serial.print("[WiFi] IP: ");
    Serial.println(WiFi.localIP());
    showLcdMessage("WiFi OK", WiFi.localIP().toString());
    delay(1500);
  } else {
    Serial.println("[WiFi] Failed!");
    showLcdMessage("WiFi Failed", "Mode offline");
    delay(1500);
  }
}

void setup() {
  Serial.begin(115200);
  delay(1000);

  // LCD
  lcd.init();
  lcd.backlight();
  showLcdMessage("ESP32 Finger", "Initializing");

  // Buzzer & LED
  pinMode(BUZZER_PIN, OUTPUT);
  pinMode(LED_OK_PIN, OUTPUT);
  pinMode(LED_ERR_PIN, OUTPUT);
  digitalWrite(LED_OK_PIN, LOW);
  digitalWrite(LED_ERR_PIN, LOW);

  // WiFi
  connectWiFi();

  // Serial cho AS608
  FingerSerial.begin(57600, SERIAL_8N1, FP_RX_PIN, FP_TX_PIN);
  delay(200);

  finger.begin(57600);
  delay(50);

  Serial.println("[FP] Verifying password...");
  if (finger.verifyPassword()) {
    Serial.println("[FP] Found fingerprint sensor!");
    showLcdMessage("Sensor OK", "");
    beepSuccess();
    delay(1000);
  } else {
    Serial.println("[FP] Did not find fingerprint sensor :(");
    showLcdMessage("Loi sensor", "Kiem tra day");
    beepError();
    while (true) {
      delay(1000);  // treo luôn để bạn biết là có lỗi
    }
  }

  // Màn hình chờ
  showLcdMessage("Quet van tay", "de diem danh");
}

/*========================
  6) LOOP CHÍNH
  ========================*/

void loop() {
  // 1) Thử lấy ảnh
  int p = finger.getImage();

  if (p == FINGERPRINT_NOFINGER) {
    // chưa có ngón tay
    delay(50);
    return;
  }

  if (p != FINGERPRINT_OK) {
    // lỗi khác
    Serial.print("[FP] getImage error: ");
    Serial.println(p);
    delay(200);
    return;
  }

  // 2) Đã có ảnh -> tiếp tục chuyển template + tìm trong DB
  showLcdMessage("Dang kiem tra...", "");
  int sensorSlot = getFingerprintID();

  if (sensorSlot == -1) {
    // lỗi xử lý
    showLcdMessage("Loi doc van tay", "");
    beepError();
    delay(1000);
    showLcdMessage("Quet van tay", "de diem danh");
  } else if (sensorSlot == 0) {
    // ảnh có thật nhưng không match trong DB (vân tay chưa enroll)
    showLcdMessage("Van tay khong", "trong DB");
    beepError();
    delay(1500);
    showLcdMessage("Quet van tay", "de diem danh");
  } else {
    // 3) Có ID -> gửi verify
    Serial.printf("[FP] Sending verify, sensorSlot = %d\n", sensorSlot);
    sendVerifyToServer(sensorSlot);
  }

  // 4) Chờ nhấc tay ra
  while (finger.getImage() != FINGERPRINT_NOFINGER) {
    delay(50);
  }
}

