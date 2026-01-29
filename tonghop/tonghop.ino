#include <Arduino.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <Adafruit_Fingerprint.h>

/* ===================== CONFIG CHUNG ===================== */

// WiFi
const char *WIFI_SSID = "baongocneee";
const char *WIFI_PASSWORD = "040612@@";

// API backend
const char *BACKEND_BASE_URL = "http://172.20.10.2:8080";  // base
const char *DEVICE_CODE = "ESP_ROOM_LAB1";

// API verify điểm danh
const char *VERIFY_API_URL = "http://172.20.10.2:8080/api/fingerprint/verify";

// AS608 Config
#define FP_RX_PIN 16  // AS608 TX -> ESP32 RX2
#define FP_TX_PIN 17  // AS608 RX -> ESP32 TX2
HardwareSerial FingerSerial(2);
Adafruit_Fingerprint finger = Adafruit_Fingerprint(&FingerSerial);

// LCD 16x2
LiquidCrystal_I2C lcd(0x27, 16, 2);

// Buzzer
#define BUZZER_PIN 5

// Button (4 chân) – dùng INPUT_PULLUP, 1 chân nối GND, 1 chân nối GPIO14
#define BUTTON_PIN 14

// (Nếu muốn LED báo)
#define LED_OK_PIN 2
#define LED_ERR_PIN 15

/* ===================== STATE MACHINE ===================== */

enum Mode {
  MODE_ATTEND = 0,      // chế độ điểm danh
  MODE_WAIT_ENROLL = 1  // chế độ chờ lệnh ENROLL
};

Mode currentMode = MODE_ATTEND;

// Thời gian chờ lệnh ENROLL (1 phút)
const unsigned long ENROLL_WAIT_TIMEOUT = 60UL * 1000UL;
const unsigned long ENROLL_POLL_INTERVAL = 2000UL;  // 2 giây hỏi server 1 lần
unsigned long enrollWaitStart = 0;
unsigned long lastEnrollPoll = 0;

// Biến cho button
bool lastButtonState = HIGH;

/* ===================== BASE64 HELPER (ENROLL) ===================== */

uint8_t templateBuffer[512];

const char b64_chars[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

String base64_encode(uint8_t *data, size_t length) {
  String ret = "";
  int i = 0, j = 0;
  uint8_t char_array_3[3], char_array_4[4];

  while (length--) {
    char_array_3[i++] = *(data++);
    if (i == 3) {
      char_array_4[0] = (char_array_3[0] & 0xfc) >> 2;
      char_array_4[1] = ((char_array_3[0] & 0x03) << 4) + ((char_array_3[1] & 0xf0) >> 4);
      char_array_4[2] = ((char_array_3[1] & 0x0f) << 2) + ((char_array_3[2] & 0xc0) >> 6);
      char_array_4[3] = char_array_3[2] & 0x3f;
      for (i = 0; (i < 4); i++) ret += b64_chars[char_array_4[i]];
      i = 0;
    }
  }
  if (i) {
    for (j = i; j < 3; j++) char_array_3[j] = '\0';
    char_array_4[0] = (char_array_3[0] & 0xfc) >> 2;
    char_array_4[1] = ((char_array_3[0] & 0x03) << 4) + ((char_array_3[1] & 0xf0) >> 4);
    char_array_4[2] = ((char_array_3[1] & 0x0f) << 2) + ((char_array_3[2] & 0xc0) >> 6);
    char_array_4[3] = char_array_3[2] & 0x3f;
    for (j = 0; (j < i + 1); j++) ret += b64_chars[char_array_4[j]];
    while ((i++ < 3)) ret += '=';
  }
  return ret;
}

int b64_pos(char c) {
  if (c >= 'A' && c <= 'Z') return c - 'A';
  if (c >= 'a' && c <= 'z') return c - 'a' + 26;
  if (c >= '0' && c <= '9') return c - '0' + 52;
  if (c == '+') return 62;
  if (c == '/') return 63;
  return -1;
}

size_t base64_decode(String input, uint8_t *output) {
  int in_len = input.length();
  int i = 0, j = 0, in_ = 0;
  uint8_t char_array_4[4], char_array_3[3];
  size_t out_len = 0;

  while (in_len-- && (input[in_] != '=') && (b64_pos(input[in_]) != -1)) {
    char_array_4[i++] = input[in_];
    in_++;
    if (i == 4) {
      for (i = 0; i < 4; i++) char_array_4[i] = b64_pos(char_array_4[i]);
      char_array_3[0] = (char_array_4[0] << 2) + ((char_array_4[1] & 0x30) >> 4);
      char_array_3[1] = ((char_array_4[1] & 0xf) << 4) + ((char_array_4[2] & 0x3c) >> 2);
      char_array_3[2] = ((char_array_4[2] & 0x3) << 6) + char_array_4[3];
      for (i = 0; (i < 3); i++) output[out_len++] = char_array_3[i];
      i = 0;
    }
  }
  if (i) {
    for (j = i; j < 4; j++) char_array_4[j] = 0;
    for (j = 0; j < 4; j++) char_array_4[j] = b64_pos(char_array_4[j]);
    char_array_3[0] = (char_array_4[0] << 2) + ((char_array_4[1] & 0x30) >> 4);
    char_array_3[1] = ((char_array_4[1] & 0xf) << 4) + ((char_array_4[2] & 0x3c) >> 2);
    char_array_3[2] = ((char_array_4[2] & 0x3) << 6) + char_array_4[3];
    for (j = 0; (j < i - 1); j++) output[out_len++] = char_array_3[j];
  }
  return out_len;
}

/* ===================== DEFINE DOWNLOAD/UPLOAD ===================== */

#ifndef FINGERPRINT_DOWNLOAD
#define FINGERPRINT_DOWNLOAD 0x09
#endif
#ifndef FINGERPRINT_UPLOAD
#define FINGERPRINT_UPLOAD 0x08
#endif

/* ===================== UI & UTILS ===================== */

void lcdShow(const String &line1, const String &line2 = "") {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print(line1.substring(0, 16));
  lcd.setCursor(0, 1);
  lcd.print(line2.substring(0, 16));
}

void buzzerOn(int ms) {
  digitalWrite(BUZZER_PIN, HIGH);
  delay(ms);
  digitalWrite(BUZZER_PIN, LOW);
}

void beepSuccess() {
  buzzerOn(100);
  delay(50);
  buzzerOn(100);
}

void beepError() {
  buzzerOn(500);
}

void beepDuplicate() {
  buzzerOn(80);
}

/* ===================== WiFi ===================== */

void connectWiFi() {
  if (WiFi.status() == WL_CONNECTED) return;
  lcdShow("WiFi Connecting", WIFI_SSID);
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  int retry = 0;
  while (WiFi.status() != WL_CONNECTED && retry < 40) {
    delay(500);
    retry++;
  }
  if (WiFi.status() == WL_CONNECTED) {
    lcdShow("WiFi OK", WiFi.localIP().toString());
    beepSuccess();
    delay(1000);
  } else {
    lcdShow("WiFi FAIL", "Offline Mode");
    beepError();
    delay(1000);
  }
}

/* ===================== FINGERPRINT CORE ===================== */

bool initFingerprint() {
  FingerSerial.begin(57600, SERIAL_8N1, FP_RX_PIN, FP_TX_PIN);
  finger.begin(57600);
  if (finger.verifyPassword()) {
    lcdShow("Sensor OK", "Ready");
    return true;
  }
  lcdShow("Sensor ERROR", "Check wiring");
  beepError();
  return false;
}

int16_t findNextFreeSlot() {
  for (uint16_t slot = 1; slot <= finger.capacity; slot++) {
    if (finger.loadModel(slot) != FINGERPRINT_OK) return slot;
  }
  return -1;
}

/* ===================== LOW LEVEL EXTRACT/INSERT TEMPLATE ===================== */

// Extract template từ buffer sensor vào RAM
bool extractTemplate(uint8_t *buffer) {
  uint8_t cmdData[] = { FINGERPRINT_UPLOAD, 0x01 };  // 0x01 = BufferID
  Adafruit_Fingerprint_Packet packet(FINGERPRINT_COMMANDPACKET, sizeof(cmdData), cmdData);
  finger.writeStructuredPacket(packet);

  uint8_t dummy[10];
  Adafruit_Fingerprint_Packet recvPacket(FINGERPRINT_ACKPACKET, 0, dummy);
  delay(50);
  if (finger.getStructuredPacket(&recvPacket) != FINGERPRINT_OK) return false;
  if (recvPacket.type != FINGERPRINT_ACKPACKET) return false;

  int index = 0;
  for (int i = 0; i < 4; i++) {
    unsigned long start = millis();
    while (FingerSerial.available() < 12) {
      if (millis() - start > 1000) return false;
    }
    for (int k = 0; k < 9; k++) FingerSerial.read();
    for (int j = 0; j < 128; j++) {
      while (!FingerSerial.available())
        ;
      buffer[index++] = FingerSerial.read();
    }
    while (!FingerSerial.available())
      ;
    FingerSerial.read();
    while (!FingerSerial.available())
      ;
    FingerSerial.read();
  }
  return true;
}

// Insert template từ RAM vào buffer sensor
bool insertTemplate(uint8_t *data) {
  for (int attempt = 1; attempt <= 2; attempt++) {
    while (FingerSerial.available()) FingerSerial.read();
    delay(30);

    uint8_t cmdData[] = { FINGERPRINT_DOWNLOAD, 0x01 };
    Adafruit_Fingerprint_Packet cmdPacket(FINGERPRINT_COMMANDPACKET, sizeof(cmdData), cmdData);
    finger.writeStructuredPacket(cmdPacket);

    Adafruit_Fingerprint_Packet reply(FINGERPRINT_ACKPACKET, 0, nullptr);
    uint8_t rc = finger.getStructuredPacket(&reply);

    if (rc != FINGERPRINT_OK) {
      if (attempt == 1) continue;
      return false;
    }

    if (reply.type != FINGERPRINT_ACKPACKET || reply.length < 1) {
      if (attempt == 1) continue;
      return false;
    }

    uint8_t confirmCode = reply.data[0];
    if (confirmCode != 0x00) {
      return false;
    }

    // gửi 4 gói data
    for (int i = 0; i < 4; i++) {
      uint8_t pid = (i == 3) ? FINGERPRINT_ENDDATAPACKET : FINGERPRINT_DATAPACKET;
      uint8_t packetData[128];
      memcpy(packetData, data + i * 128, 128);
      Adafruit_Fingerprint_Packet dataPacket(pid, 128, packetData);
      finger.writeStructuredPacket(dataPacket);
      delay(40);
    }
    return true;
  }
  return false;
}

/* ===================== LOGIC ENROLL & SYNC ===================== */

int enrollAndUpload(const String &sessionCode) {
  lcdShow("Enroll Session", sessionCode);
  int id = findNextFreeSlot();
  if (id < 0) {
    lcdShow("Full Memory", "");
    beepError();
    return -1;
  }

  int p = -1;
  lcdShow("Dat ngon tay", "Lan 1 (Slot " + String(id) + ")");
  while (p != FINGERPRINT_OK) {
    p = finger.getImage();
    if (p != FINGERPRINT_OK && p != FINGERPRINT_NOFINGER) {
      beepError();
      return -1;
    }
  }
  if (finger.image2Tz(1) != FINGERPRINT_OK) return -1;

  lcdShow("Bo tay ra", "");
  beepSuccess();
  delay(1000);
  while (finger.getImage() != FINGERPRINT_NOFINGER)
    ;

  lcdShow("Dat lai tay", "Lan 2");
  p = -1;
  while (p != FINGERPRINT_OK) {
    p = finger.getImage();
  }
  if (finger.image2Tz(2) != FINGERPRINT_OK) return -1;

  if (finger.createModel() != FINGERPRINT_OK) {
    lcdShow("Khong khop", "Thu lai");
    beepError();
    return -1;
  }

  if (finger.storeModel(id) != FINGERPRINT_OK) {
    lcdShow("Loi luu Flash", "");
    beepError();
    return -1;
  }

  // Extract template
  finger.loadModel(id);
  if (!extractTemplate(templateBuffer)) {
    lcdShow("Extract Fail", "");
    beepError();
    return -1;
  }

  String base64Template = base64_encode(templateBuffer, 512);

  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    http.begin(String(BACKEND_BASE_URL) + "/api/fingerprint/enroll/upload-from-device");
    http.addHeader("Content-Type", "application/json");

    String json = "{";
    json += "\"sessionCode\":\"" + sessionCode + "\",";
    json += "\"deviceCode\":\"" + String(DEVICE_CODE) + "\",";
    json += "\"sensorSlot\":" + String(id) + ",";
    json += "\"templateBase64\":\"" + base64Template + "\"";
    json += "}";

    int httpCode = http.POST(json);
    http.end();

    if (httpCode == 200) {
      lcdShow("Upload OK", "Slot " + String(id));
      beepSuccess();
    } else {
      lcdShow("Upload Fail", String(httpCode));
      beepError();
    }
  }
  return 0;
}

void syncFromSession(const String &sessionCode) {
  lcdShow("Dong bo...", "Tai data");
  if (WiFi.status() != WL_CONNECTED) return;

  HTTPClient http;
  String url = String(BACKEND_BASE_URL) + "/api/fingerprint/sync/data?sessionCode=" + sessionCode;
  http.begin(url);
  int httpCode = http.GET();

  if (httpCode == 200) {
    String payload = http.getString();
    size_t len = base64_decode(payload, templateBuffer);
    if (len == 512) {
      int slot = findNextFreeSlot();
      if (slot < 0) {
        lcdShow("Full Memory", "Sync Fail");
        http.end();
        return;
      }

      if (insertTemplate(templateBuffer)) {
        if (finger.storeModel(slot) == FINGERPRINT_OK) {
          lcdShow("Sync OK", "Saved Slot " + String(slot));
          beepSuccess();

          HTTPClient http2;
          http2.begin(String(BACKEND_BASE_URL) + "/api/fingerprint/sync/result");
          http2.addHeader("Content-Type", "application/json");

          String body = "{";
          body += "\"sessionCode\":\"" + sessionCode + "\",";
          body += "\"sensorSlot\":" + String(slot);
          body += "}";

          http2.POST(body);
          http2.end();
        } else {
          lcdShow("Store Fail", "");
        }
      } else {
        lcdShow("Insert Fail", "Sensor Err");
      }
    } else {
      lcdShow("Bad Data Len", String(len));
    }
  } else {
    lcdShow("Sync Fail", "HTTP " + String(httpCode));
  }
  http.end();
}

/* ===================== JSON HELPER (VERIFY) ===================== */

String extractJsonValue(const String &json, const String &key) {
  String search = "\"" + key + "\":";
  int idx = json.indexOf(search);
  if (idx == -1) return "";

  idx += search.length();
  while (idx < json.length() && (json[idx] == ' ')) idx++;

  String value = "";
  if (idx < json.length() && json[idx] == '\"') {
    idx++;
    while (idx < json.length() && json[idx] != '\"') {
      value += json[idx++];
    }
  } else {
    while (idx < json.length() && json[idx] != ',' && json[idx] != '}' && json[idx] != ' ' && json[idx] != '\n' && json[idx] != '\r') {
      value += json[idx++];
    }
  }
  return value;
}

/* ===================== HANDLE VERIFY RESPONSE ===================== */

void showWelcome(const String &fullName, const String &statusText) {
  lcdShow("Xin chao:", fullName);
  delay(1500);
  lcdShow(statusText, "");
  delay(1500);
}

void handleVerifyResponse(const String &payload) {
  String status = extractJsonValue(payload, "status");
  String fullName = extractJsonValue(payload, "fullName");
  String message = extractJsonValue(payload, "message");

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
    lcdShow("Ban da diem danh", "truoc do");
    beepDuplicate();
    delay(1500);
  } else {
    digitalWrite(LED_OK_PIN, LOW);
    digitalWrite(LED_ERR_PIN, HIGH);
    lcdShow("Khong co tiet hoc", "");
    beepError();
    delay(1500);
  }

  lcdShow("Quet van tay", "de diem danh");
  digitalWrite(LED_OK_PIN, LOW);
  digitalWrite(LED_ERR_PIN, LOW);
}

/* ===================== SEND VERIFY ===================== */

void sendVerifyToServer(int sensorSlot) {
  if (WiFi.status() != WL_CONNECTED) {
    lcdShow("Loi WiFi", "Thu lai sau");
    beepError();
    delay(2000);
    lcdShow("Quet van tay", "de diem danh");
    return;
  }

  HTTPClient http;
  http.begin(VERIFY_API_URL);
  http.addHeader("Content-Type", "application/json");

  String body = "{";
  body += "\"deviceCode\":\"" + String(DEVICE_CODE) + "\",";
  body += "\"sensorSlot\":" + String(sensorSlot);
  body += "}";

  int httpCode = http.POST(body);

  if (httpCode > 0) {
    String payload = http.getString();
    handleVerifyResponse(payload);
  } else {
    lcdShow("Loi ket noi", "server");
    beepError();
    delay(1500);
    lcdShow("Quet van tay", "de diem danh");
  }

  http.end();
}

/* ===================== GET FINGERPRINT ID (VERIFY) ===================== */

int getFingerprintID() {
  int p = finger.image2Tz();
  if (p != FINGERPRINT_OK) {
    return -1;
  }

  p = finger.fingerFastSearch();
  if (p != FINGERPRINT_OK) {
    return 0;  // không có trong DB
  }
  return finger.fingerID;
}

/* ===================== ATTENDANCE STEP ===================== */

void handleAttendanceMode() {
  int p = finger.getImage();

  if (p == FINGERPRINT_NOFINGER) {
    delay(50);
    return;
  }

  if (p != FINGERPRINT_OK) {
    delay(200);
    return;
  }

  lcdShow("Dang kiem tra...", "");
  int sensorSlot = getFingerprintID();

  if (sensorSlot == -1) {
    lcdShow("Loi doc van tay", "");
    beepError();
    delay(1000);
    lcdShow("Quet van tay", "de diem danh");
  } else if (sensorSlot == 0) {
    lcdShow("Van tay khong", "trong DB");
    beepError();
    delay(1500);
    lcdShow("Quet van tay", "de diem danh");
  } else {
    sendVerifyToServer(sensorSlot);
  }

  while (finger.getImage() != FINGERPRINT_NOFINGER) {
    delay(50);
  }
}

/* ===================== WAIT ENROLL MODE ===================== */

void handleWaitEnrollMode() {
  // timeout 1 phút
  if (millis() - enrollWaitStart > ENROLL_WAIT_TIMEOUT) {
    lcdShow("Het thoi gian", "Quay lai diem danh");
    beepError();
    delay(1500);
    currentMode = MODE_ATTEND;
    lcdShow("Quet van tay", "de diem danh");
    return;
  }

  // Poll server 2s/lần
  if (millis() - lastEnrollPoll < ENROLL_POLL_INTERVAL) return;
  lastEnrollPoll = millis();

  connectWiFi();

  if (WiFi.status() != WL_CONNECTED) {
    lcdShow("WiFi loi", "Dang doi lenh...");
    return;
  }

  HTTPClient http;
  String url = String(BACKEND_BASE_URL) + "/api/fingerprint/enroll/next-command?deviceCode=" + String(DEVICE_CODE);
  http.begin(url);
  int httpCode = http.GET();

  if (httpCode == 200) {
    String payload = http.getString();
    int separator = payload.indexOf('|');
    if (separator > 0) {
      String type = payload.substring(0, separator);
      String sessionCode = payload.substring(separator + 1);

      if (type == "ENROLL") {
        enrollAndUpload(sessionCode);
        // Sau khi enroll xong -> về chế độ điểm danh
        currentMode = MODE_ATTEND;
        lcdShow("Quet van tay", "de diem danh");
      } else if (type == "SYNC") {
        syncFromSession(sessionCode);
        // tuỳ bạn: ở đây mình cũng cho về chế độ điểm danh
        currentMode = MODE_ATTEND;
        lcdShow("Quet van tay", "de diem danh");
      }
    }
  } else if (httpCode == 204) {
    // Không có lệnh → bình thường, tiếp tục chờ
    // Không hiển thị lỗi để khỏi rối
  } else {
    // Các lỗi khác – chỉ hiển thị nhẹ nhàng, vẫn tiếp tục chờ tới khi timeout
    lcdShow("Next cmd HTTP", String(httpCode));
    delay(500);
  }

  http.end();
}

/* ===================== SETUP & LOOP ===================== */

void setup() {
  Serial.begin(115200);
  Wire.begin();
  lcd.init();
  lcd.backlight();

  pinMode(BUZZER_PIN, OUTPUT);
  pinMode(BUTTON_PIN, INPUT_PULLUP);
  pinMode(LED_OK_PIN, OUTPUT);
  pinMode(LED_ERR_PIN, OUTPUT);
  digitalWrite(LED_OK_PIN, LOW);
  digitalWrite(LED_ERR_PIN, LOW);

  lcdShow("System Init", "...");
  initFingerprint();
  connectWiFi();
  lcdShow("Quet van tay", "de diem danh");
}

void loop() {
  // Đọc button (nhấn = LOW)
  bool btn = (digitalRead(BUTTON_PIN) == LOW);

  // Bắt cạnh xuống: từ HIGH -> LOW
  if (!btn && lastButtonState) {
    // nothing
  }
  if (btn && !lastButtonState) {
    // vừa nhấn
    if (currentMode == MODE_ATTEND) {
      currentMode = MODE_WAIT_ENROLL;
      enrollWaitStart = millis();
      lastEnrollPoll = 0;
      lcdShow("CHO LENH ENROLL", "tu server (1p)");
      beepSuccess();
    }
  }
  lastButtonState = btn;

  // Xử lý theo mode
  if (currentMode == MODE_ATTEND) {
    handleAttendanceMode();
  } else if (currentMode == MODE_WAIT_ENROLL) {
    handleWaitEnrollMode();
  }
}
