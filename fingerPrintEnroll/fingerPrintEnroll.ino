#include <Arduino.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <Adafruit_Fingerprint.h>

// ===================== CONFIG =====================
const char* WIFI_SSID = "baongocneee";
const char* WIFI_PASSWORD = "040612@@";
const char* BACKEND_BASE_URL = "http://172.20.10.2:8080";
const char* DEVICE_CODE = "ESP_ROOM_LAB1";

// AS608 Config
#define FP_RX_PIN 16  // AS608 TX -> ESP32 RX2
#define FP_TX_PIN 17  // AS608 RX -> ESP32 TX2
HardwareSerial FingerSerial(2);
Adafruit_Fingerprint finger = Adafruit_Fingerprint(&FingerSerial);

// LCD Config
LiquidCrystal_I2C lcd(0x27, 16, 2);

// Buzzer
#define BUZZER_PIN 25

// Global Buffer
uint8_t templateBuffer[512];

// ===================== FIX DEFINE =====================
// Định nghĩa lệnh Download nếu thư viện thiếu
#ifndef FINGERPRINT_DOWNLOAD
#define FINGERPRINT_DOWNLOAD 0x09
#endif
#ifndef FINGERPRINT_UPLOAD
#define FINGERPRINT_UPLOAD 0x08
#endif

// ===================== BASE64 HELPER =====================
const char b64_chars[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

String base64_encode(uint8_t* data, size_t length) {
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

size_t base64_decode(String input, uint8_t* output) {
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

// ===================== LOW LEVEL FINGERPRINT HELPERS =====================

// HÀM QUAN TRỌNG: Tải template từ Sensor về ESP (Extract)
bool extractTemplate(uint8_t* buffer) {
  // 1. Gửi lệnh UpChar (0x08) cho Buffer 1
  // SỬA LỖI: Tạo mảng dữ liệu trước
  uint8_t cmdData[] = { FINGERPRINT_UPLOAD, 0x01 };  // 0x01 là BufferID

  // SỬA LỖI: Truyền 3 tham số (Type, Length, Data*)
  Adafruit_Fingerprint_Packet packet(FINGERPRINT_COMMANDPACKET, sizeof(cmdData), cmdData);
  finger.writeStructuredPacket(packet);

  // 2. Đọc phản hồi (Header + Data)
  // Cần tạo một packet rỗng để nhận phản hồi lệnh ACK
  uint8_t dummy[10];
  Adafruit_Fingerprint_Packet recvPacket(FINGERPRINT_ACKPACKET, 0, dummy);

  delay(50);
  if (finger.getStructuredPacket(&recvPacket) != FINGERPRINT_OK) return false;
  if (recvPacket.type != FINGERPRINT_ACKPACKET) return false;

  // Bây giờ đọc stream dữ liệu thô (4 gói x 128 bytes)
  int index = 0;
  for (int i = 0; i < 4; i++) {
    unsigned long start = millis();
    while (FingerSerial.available() < 12) {
      if (millis() - start > 1000) return false;
    }

    // Bỏ qua Header(2) + Addr(4) + Type(1) + Len(2) = 9 bytes
    for (int k = 0; k < 9; k++) FingerSerial.read();

    // Đọc 128 byte data
    for (int j = 0; j < 128; j++) {
      while (!FingerSerial.available())
        ;
      buffer[index++] = FingerSerial.read();
    }

    // Đọc Checksum (2 byte)
    while (!FingerSerial.available())
      ;
    FingerSerial.read();
    while (!FingerSerial.available())
      ;
    FingerSerial.read();
  }
  return true;
}

// HÀM QUAN TRỌNG: Nạp template từ ESP vào Sensor (Insert)
bool insertTemplate(uint8_t* data) {
  // Thử tối đa 2 lần (lần 1 fail như bạn đang bị, lần 2 tự động retry)
  for (int attempt = 1; attempt <= 2; attempt++) {
    Serial.print("[INS] Attempt ");
    Serial.println(attempt);

    // 0. XÓA RÁC TRONG SERIAL CỦA SENSOR
    while (FingerSerial.available()) {
      FingerSerial.read();
    }
    delay(30);  // cho chắc

    // 1. GỬI LỆNH DOWNLOAD (DownChar) VÀO BUFFER 1
    uint8_t cmdData[] = { FINGERPRINT_DOWNLOAD, 0x01 };  // 0x01 = CharBuffer1

    Adafruit_Fingerprint_Packet cmdPacket(
      FINGERPRINT_COMMANDPACKET,
      sizeof(cmdData),
      cmdData);
    finger.writeStructuredPacket(cmdPacket);

    Serial.println("[INS] DOWNLOAD cmd sent, waiting ACK...");

    // 2. ĐỢI ACK
    Adafruit_Fingerprint_Packet reply(FINGERPRINT_ACKPACKET, 0, nullptr);
    uint8_t rc = finger.getStructuredPacket(&reply);

    if (rc != FINGERPRINT_OK) {
      Serial.print("[INS] getStructuredPacket rc = ");
      Serial.println(rc);  // 0xFE = BADPACKET, 0xFF = TIMEOUT,...

      // Nếu lần 1 fail -> continue để thử lại lần 2
      if (attempt == 1) {
        Serial.println("[INS] BADPACKET, retry once...");
        continue;
      } else {
        Serial.println("[INS] Failed even after retry.");
        return false;
      }
    }

    if (reply.type != FINGERPRINT_ACKPACKET) {
      Serial.print("[INS] Unexpected packet type: 0x");
      Serial.println(reply.type, HEX);
      if (attempt == 1) {
        Serial.println("[INS] Retry due to wrong packet type...");
        continue;
      } else {
        return false;
      }
    }

    if (reply.length < 1) {
      Serial.println("[INS] ACK packet length too short");
      if (attempt == 1) {
        Serial.println("[INS] Retry due to short ACK...");
        continue;
      } else {
        return false;
      }
    }

    uint8_t confirmCode = reply.data[0];
    Serial.print("[INS] ConfirmCode=0x");
    Serial.println(confirmCode, HEX);

    if (confirmCode != 0x00) {
      Serial.println("[INS] Sensor reported error on DOWNLOAD");
      // Ở đây thường không phải lỗi rác mà là lỗi thực sự → không retry
      return false;
    }

    // === ĐẾN ĐÂY: ACK OK, THOÁT KHỎI FOR VÀ GỬI DATA ===
    Serial.println("[INS] CMD accepted, sending data...");

    // 3. GỬI 4 GÓI DỮ LIỆU (4 x 128 = 512 BYTES)
    for (int i = 0; i < 4; i++) {
      uint8_t pid = (i == 3) ? FINGERPRINT_ENDDATAPACKET : FINGERPRINT_DATAPACKET;

      uint8_t packetData[128];
      memcpy(packetData, data + i * 128, 128);

      Adafruit_Fingerprint_Packet dataPacket(pid, 128, packetData);
      finger.writeStructuredPacket(dataPacket);

      Serial.print("[INS] Sent chunk ");
      Serial.println(i + 1);
      delay(40);  // cho sensor nuốt data
    }

    return true;  // thành công
  }

  // Lý thuyết không tới đây, nhưng cho chắc:
  return false;
}
// ===================== UI & UTILS =====================
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

void lcdShow(const String& line1, const String& line2 = "") {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print(line1);
  lcd.setCursor(0, 1);
  lcd.print(line2);
}

void connectWiFi() {
  if (WiFi.status() == WL_CONNECTED) return;
  lcdShow("WiFi Connecting", WIFI_SSID);
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  int retry = 0;
  while (WiFi.status() != WL_CONNECTED && retry < 20) {
    delay(500);
    retry++;
  }
  if (WiFi.status() == WL_CONNECTED) {
    lcdShow("WiFi OK", WiFi.localIP().toString());
    beepSuccess();
  } else {
    lcdShow("WiFi FAIL", "Offline Mode");
    beepError();
  }
}

// ===================== FINGERPRINT CORE =====================
bool initFingerprint() {
  FingerSerial.begin(57600, SERIAL_8N1, FP_RX_PIN, FP_TX_PIN);
  finger.begin(57600);
  if (finger.verifyPassword()) {
    lcdShow("Sensor OK", "Ready");
    return true;
  }
  lcdShow("Sensor ERROR", "Check wiring");
  return false;
}

int16_t findNextFreeSlot() {
  for (uint16_t slot = 1; slot <= finger.capacity; slot++) {
    if (finger.loadModel(slot) != FINGERPRINT_OK) return slot;
  }
  return -1;
}

// ===================== LOGIC 1: ENROLL & UPLOAD =====================
int enrollAndUpload(const String& sessionCode) {
  lcdShow("Enroll Session", sessionCode);
  int id = findNextFreeSlot();
  if (id < 0) {
    lcdShow("Full Memory", "");
    beepError();
    return -1;
  }

  // 1. Thu thập
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
  while (finger.getImage() != FINGERPRINT_NOFINGER);

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

  // 2. Lưu vào Flash
  if (finger.storeModel(id) != FINGERPRINT_OK) {
    lcdShow("Loi luu Flash", "");
    beepError();
    return -1;
  }

  // 3. EXTRACT RAW DATA
  finger.loadModel(id);
  if (!extractTemplate(templateBuffer)) {
    lcdShow("Extract Fail", "");
    beepError();
    return -1;
  }

  // 4. Gửi lên Server
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

// ===================== LOGIC 2: SYNC (DOWNLOAD & SAVE) =====================
void syncFromSession(const String& sessionCode) {
  lcdShow("Dong bo...", "Tai data");
  if (WiFi.status() != WL_CONNECTED) return;

  HTTPClient http;
  String url = String(BACKEND_BASE_URL) + "/api/fingerprint/sync/data?sessionCode=" + sessionCode;
  http.begin(url);
  int httpCode = http.GET();

  if (httpCode == 200) {
    String payload = http.getString();
    size_t len = base64_decode(payload, templateBuffer);
    Serial.print("Decoded length: ");
    Serial.println(len);

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

          // ==== BÁO NGƯỢC VỀ SERVER SLOT VỪA LƯU ====
          HTTPClient http2;
          http2.begin(String(BACKEND_BASE_URL) + "/api/fingerprint/sync/result");
          http2.addHeader("Content-Type", "application/json");

          String body = "{";
          body += "\"sessionCode\":\"" + sessionCode + "\",";
          body += "\"sensorSlot\":" + String(slot);
          body += "}";

          int code2 = http2.POST(body);
          Serial.print("[SYNC] POST /sync/result code = ");
          Serial.println(code2);
          http2.end();
          // ===========================================
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


// ===================== MAIN LOOP =====================
void setup() {
  Serial.begin(115200);
  Wire.begin();
  lcd.init();
  lcd.backlight();
  pinMode(BUZZER_PIN, OUTPUT);

  lcdShow("System Init", "...");
  initFingerprint();
  connectWiFi();
  lcdShow("Ready", "Waiting cmd...");
}

void loop() {
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    http.begin(String(BACKEND_BASE_URL) + "/api/fingerprint/enroll/next-command?deviceCode=" + String(DEVICE_CODE));
    int httpCode = http.GET();

    if (httpCode == 200) {
      String payload = http.getString();
      int separator = payload.indexOf('|');
      if (separator > 0) {
        String type = payload.substring(0, separator);
        String sessionCode = payload.substring(separator + 1);

        if (type == "ENROLL") {
          enrollAndUpload(sessionCode);
        } else if (type == "SYNC") {
          syncFromSession(sessionCode);
        }
      }
    }
    http.end();
  }
  delay(2000);
}