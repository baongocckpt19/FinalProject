#include <Arduino.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <Adafruit_Fingerprint.h>

// ===================== CONFIG =====================
// WiFi + Backend
const char* WIFI_SSID        = "Ngoi Nha Chung";
const char* WIFI_PASSWORD    = "123456798";
const char* BACKEND_BASE_URL = "http://192.168.1.60:8080";
const char* DEVICE_CODE      = "ESP_ROOM_LAB2";   // TRÙNG VỚI DeviceCode trong DB

// AS608 on UART2
#define FP_RX_PIN 16   // AS608 TX -> ESP32 RX2
#define FP_TX_PIN 17   // AS608 RX -> ESP32 TX2

// Buzzer (đổi lại nếu dùng chân khác)
#define BUZZER_PIN 25

// LCD I2C: nếu module bạn là 0x3F thì sửa 0x27 -> 0x3F
LiquidCrystal_I2C lcd(0x27, 16, 2);

HardwareSerial FingerSerial(2);
Adafruit_Fingerprint finger = Adafruit_Fingerprint(&FingerSerial);

// ===================== BUZZER =====================
void buzzerOn(int ms = 80) {
  digitalWrite(BUZZER_PIN, HIGH);
  delay(ms);
  digitalWrite(BUZZER_PIN, LOW);
}

void beepPrompt() {      // báo hành động (đặt tay, bắt đầu enroll...)
  buzzerOn(80);
}

void beepSuccess() {     // báo thành công
  buzzerOn(80);
  delay(60);
  buzzerOn(80);
}

void beepError() {       // báo lỗi
  buzzerOn(250);
}

// ===================== LCD HELPERS =====================

// Dùng String để tránh lỗi StringSumHelper
void lcdShow(const String &line1, const String &line2 = "") {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print(line1);
  lcd.setCursor(0, 1);
  lcd.print(line2);
}

// Helper: show message + Slot=x
void lcdShowSlot(const String &msg, int slot) {
  String line2 = "Slot=" + String(slot);
  lcdShow(msg, line2);
}

// ===================== WIFI =====================
void connectWiFi() {
  lcdShow("WiFi: Connecting", WIFI_SSID);
  Serial.println(F("[WiFi] Dang ket noi..."));
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  int retry = 0;
  while (WiFi.status() != WL_CONNECTED && retry < 30) {
    delay(500);
    Serial.print(".");
    retry++;
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println();
    Serial.print(F("[WiFi] OK, IP: "));
    Serial.println(WiFi.localIP());
    lcdShow("WiFi OK", WiFi.localIP().toString());
    beepSuccess();
  } else {
    Serial.println();
    Serial.println(F("[WiFi] FAIL!"));
    lcdShow("WiFi FAILED", "Chi dung offline");
    beepError();
  }

  delay(800);
}

// ===================== FINGERPRINT =====================
bool initFingerprint() {
  FingerSerial.begin(57600, SERIAL_8N1, FP_RX_PIN, FP_TX_PIN);
  finger.begin(57600);

  Serial.println(F("[FP] Dang kiem tra cam bien..."));
  lcdShow("Check sensor...", "");

  if (!finger.verifyPassword()) {
    Serial.println(F("[FP] LOI: Khong tim thay AS608!"));
    lcdShow("FP ERROR", "Check wiring!");
    beepError();
    return false;
  }

  finger.getParameters();
  Serial.println(F("[FP] Connected AS608 OK."));
  Serial.print(F("Capacity: "));
  Serial.println(finger.capacity);

  lcdShow("FP OK", "Cap: " + String(finger.capacity));
  beepSuccess();
  delay(800);
  return true;
}

// Tìm slot TRỐNG đầu tiên (quét từ 1 → capacity)
int16_t findNextFreeSlot() {
  if (finger.getTemplateCount() != FINGERPRINT_OK) {
    Serial.println(F("[FP] getTemplateCount ERROR."));
    lcdShow("FP ERROR", "getTemplateCnt");
    beepError();
    return -1;
  }

  uint16_t total = finger.templateCount;
  uint16_t capacity = finger.capacity > 0 ? finger.capacity : 200;

  if (total >= capacity) {
    Serial.println(F("[FP] FULL - no free slot."));
    lcdShow("FP FULL", "No free slot");
    beepError();
    return -1;
  }

  Serial.print(F("[FP] total="));
  Serial.print(total);
  Serial.println(F(", finding free slot..."));

  for (uint16_t slot = 1; slot <= capacity; slot++) {
    int p = finger.loadModel(slot);
    if (p == FINGERPRINT_OK) {
      // slot đang dùng
      continue;
    } else {
      Serial.print(F("[FP] Free slot = "));
      Serial.println(slot);
      lcdShowSlot("Free slot found", slot);

      return (int16_t)slot;
    }
  }

  Serial.println(F("[FP] No free slot found."));
  lcdShow("No free slot", "");
  beepError();
  return -1;
}

// Enroll vân tay vào 1 slot cho trước
int enrollToSlot(uint16_t id) {
  int p = -1;
  Serial.println();
  Serial.print(F("[ENROLL] Slot #"));
  Serial.println(id);

  // Lần 1
  Serial.println(F("Place finger (1)..."));
  lcdShow("Quet lan 1", "Dat ngon tay");
  
  beepPrompt();
  delay(800);

  while (p != FINGERPRINT_OK) {
    p = finger.getImage();
    if (p == FINGERPRINT_NOFINGER) {
      delay(50);
      continue;
    } else if (p == FINGERPRINT_PACKETRECIEVEERR) {
      Serial.println(F("Err: getImage #1"));
      lcdShow("Err getImage", "#1");
      beepError();
      return p;
    } else if (p == FINGERPRINT_IMAGEFAIL) {
      Serial.println(F("Err: IMAGEFAIL #1"));
      lcdShow("Err IMAGE", "#1");
      beepError();
      return p;
    }
  }

  p = finger.image2Tz(1);
  if (p != FINGERPRINT_OK) {
    Serial.println(F("Err: image2Tz(1)"));
    lcdShow("Err image2Tz", "(1)");
    beepError();
    return p;
  }

  Serial.println(F("Remove finger..."));
  lcdShow("Nho tay ra", "");
  beepSuccess();
  delay(1500);

  // Lần 2
  Serial.println(F("Place SAME finger (2)..."));
  lcdShow("Quet lan 2", "Dat lai tay");
  beepPrompt();

  p = -1;
  while (p != FINGERPRINT_OK) {
    p = finger.getImage();
    if (p == FINGERPRINT_NOFINGER) {
      delay(50);
      continue;
    } else if (p == FINGERPRINT_PACKETRECIEVEERR) {
      Serial.println(F("Err: getImage #2"));
      lcdShow("Err getImage", "#2");
      beepError();
      return p;
    } else if (p == FINGERPRINT_IMAGEFAIL) {
      Serial.println(F("Err: IMAGEFAIL #2"));
      lcdShow("Err IMAGE", "#2");
      beepError();
      return p;
    }
  }

  p = finger.image2Tz(2);
  if (p != FINGERPRINT_OK) {
    Serial.println(F("Err: image2Tz(2)"));
    lcdShow("Err image2Tz", "(2)");
    beepError();
    return p;
  }

  // Ghép model
  p = finger.createModel();
  if (p == FINGERPRINT_OK) {
    Serial.println(F("Model OK."));
    lcdShow("Tao model OK", "");
    delay(800);
  } else if (p == FINGERPRINT_ENROLLMISMATCH) {
    Serial.println(F("ENROLL MISMATCH."));
    lcdShow("LOI", "Khong trung nhau");
    beepError();
    return p;
  } else {
    Serial.println(F("createModel ERROR."));
    lcdShow("Err createModel", "");
    beepError();
    return p;
  }

  // Lưu vào slot
  p = finger.storeModel(id);
  if (p == FINGERPRINT_OK) {
    Serial.print(F("Store OK at slot "));
    Serial.println(id);
    delay(800);
    lcdShowSlot("Luu thanh cong", id);
    beepSuccess();
    delay(800);
    return FINGERPRINT_OK;
  } else {
    Serial.print(F("Err storeModel("));
    Serial.print(id);
    Serial.println(F(")"));
    lcdShowSlot("Err storeModel", id);
    beepError();
    return p;
  }
}

// Gửi slot + sessionCode + deviceCode lên backend
void notifyBackendEnroll(uint16_t slot, const String& sessionCode) {
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println(F("[API] WiFi NOT connected."));
    lcdShow("API skipped", "No WiFi");
    beepError();
    return;
  }

  if (sessionCode.length() == 0) {
    Serial.println(F("[API] sessionCode empty."));
    lcdShow("API ERROR", "Session empty");
    beepError();
    return;
  }

  HTTPClient http;
  String url = String(BACKEND_BASE_URL) + "/api/fingerprint/enroll/upload-from-device";

  Serial.println();
  Serial.print(F("[API] POST "));
  Serial.println(url);
  lcdShowSlot("Gui len server", slot);

  http.begin(url);
  http.addHeader("Content-Type", "application/json");

  String payload = "{";
  payload += "\"sessionCode\":\"" + sessionCode + "\",";
  payload += "\"deviceCode\":\"" + String(DEVICE_CODE) + "\",";
  payload += "\"sensorSlot\":" + String(slot);
  payload += "}";

  Serial.print(F("[API] Body: "));
  Serial.println(payload);

  int httpCode = http.POST(payload);
  if (httpCode > 0) {
    Serial.print(F("[API] HTTP code: "));
    Serial.println(httpCode);
    String resp = http.getString();
    Serial.print(F("[API] Response: "));
    Serial.println(resp);

    if (httpCode >= 200 && httpCode < 300) {
      lcdShowSlot("API OK", slot);
      delay(800);
      beepSuccess();
    } else {
      lcdShow("API ERR code", String(httpCode));
      beepError();
    }
  } else {
    Serial.print(F("[API] HTTP ERROR: "));
    Serial.println(http.errorToString(httpCode));
    lcdShow("API HTTP ERR", "");
    beepError();
  }
  http.end();
}

// ===================== POLL COMMAND FROM SERVER =====================
// Gọi: GET /api/fingerprint/enroll/next-command?deviceCode=ESP_ROOM_LAB1
//  - 200 OK + body = sessionCode -> có lệnh
//  - 204 No Content             -> không có lệnh
String pollCommandFromServer() {
  if (WiFi.status() != WL_CONNECTED) {
    connectWiFi(); // thử nối lại
    if (WiFi.status() != WL_CONNECTED) {
      return "";
    }
  }

  HTTPClient http;
  String url = String(BACKEND_BASE_URL) +
               "/api/fingerprint/enroll/next-command?deviceCode=" +
               DEVICE_CODE;

  Serial.print(F("[CMD] GET "));
  Serial.println(url);

  http.begin(url);
  int httpCode = http.GET();

  String sessionCode = "";

  if (httpCode == 200) {
    sessionCode = http.getString();
    sessionCode.trim();
    Serial.print(F("[CMD] Got sessionCode: "));
    Serial.println(sessionCode);
  } else if (httpCode == 204) {
    Serial.println(F("[CMD] No command (204)."));
  } else {
    Serial.print(F("[CMD] HTTP error: "));
    Serial.println(httpCode);
  }

  http.end();
  return sessionCode;
}

// Chạy full flow cho 1 sessionCode (được lấy từ server)
void runEnrollForSession(const String& sessionCode) {
  if (sessionCode.length() == 0) return;

  Serial.println();
  Serial.println(F("==== ENROLL SESSION ===="));
  Serial.print(F("SessionCode = "));
  Serial.println(sessionCode);

  lcdShow("Enroll started", sessionCode);

  beepPrompt();
  delay(800);

  // Tìm slot trống
  int16_t slot = findNextFreeSlot();
  if (slot < 0) {
    Serial.println(F("[FLOW] No free slot, cancel."));
    return;
  }

  // Enroll
  int result = enrollToSlot((uint16_t)slot);
  if (result != FINGERPRINT_OK) {
    Serial.println(F("[FLOW] Enroll FAIL, not sending API."));
    return;
  }

  // Gửi server
  notifyBackendEnroll((uint16_t)slot, sessionCode);

  Serial.println();
  Serial.println(F("[FLOW] DONE. Waiting next command..."));
  lcdShow("DONE", "Cho lenh moi");
  delay(800);
  delay(1000);
}

// ===================== SETUP / LOOP =====================
void setup() {
  // LCD
  Wire.begin();
  lcd.init();
  lcd.backlight();
  lcdShow("ESP Fingerprint", "Starting...");

  // Serial
  Serial.begin(115200);
  delay(1000);
  Serial.println();
  Serial.println(F("==== ESP32 + AS608 - AUTO ENROLL + LCD ===="));

  pinMode(BUZZER_PIN, OUTPUT);
  digitalWrite(BUZZER_PIN, LOW);

  if (!initFingerprint()) {
    // Không khởi tạo được cảm biến thì thôi, đứng im
    while (true) {
      delay(1000);
    }
  }

  connectWiFi();
  lcdShow("Cho lenh enroll", "Tu server...");
}

void loop() {
  // Mỗi vòng:
  //  - Hỏi server xem có lệnh mới không
  //  - Nếu có -> thực hiện 1 phiên enroll -> rồi quay về chờ lệnh
  String sessionCode = pollCommandFromServer();
  if (sessionCode.length() > 0) {
    runEnrollForSession(sessionCode);
    lcdShow("Cho lenh enroll", "Tu server...");
  } else {
    // Không có lệnh -> chờ 1 chút rồi hỏi lại
    delay(1000);
  }
}
