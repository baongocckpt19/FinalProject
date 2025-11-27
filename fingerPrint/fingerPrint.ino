#include <WiFi.h>
#include <HTTPClient.h>
#include <Adafruit_Fingerprint.h>
#include <HardwareSerial.h>
#include <time.h>

// ================== WIFI & API CONFIG ==================
const char* ssid     = "Ngoi Nha Chung";
const char* password = "123456798";

String apiUrl       = "http://192.168.1.60:8080/api/device/attendance";
String deviceSecret = "MY_SUPER_DEVICE_KEY_123";  // phải giống backend

// ================== CẢM BIẾN VÂN TAY (AS608) ==================
// Dùng UART2 của ESP32
// ⚠️ CHỈNH LẠI CHÂN THEO MẠCH CỦA BẠN
#define FP_RX_PIN 16   // ESP32 RX2 nối TX của AS608
#define FP_TX_PIN 17   // ESP32 TX2 nối RX của AS608

HardwareSerial FingerSerial(2);
Adafruit_Fingerprint finger = Adafruit_Fingerprint(&FingerSerial);

// ================== NTP TIME CONFIG ==================
const char* ntpServer = "pool.ntp.org";
const long  gmtOffset_sec = 7 * 3600;  // GMT+7
const int   daylightOffset_sec = 0;

// ================== MAP FINGER ID → USER ==================
struct FingerUser {
  uint8_t fingerId;
  const char* username;
  int classId;
  const char* sessionStart;
  const char* sessionEnd;
};

// ⚠️ CHỈNH LẠI MAP NÀY THEO CÁCH BẠN ENROLL TRÊN CẢM BIẾN
FingerUser users[] = {
  {1, "student01", 5, "07:00", "09:00"},
  {2, "student02", 5, "07:00", "09:00"},
  {3, "student03", 5, "07:00", "09:00"},
  {4, "student04", 2, "10:00", "11:30"},
  {5, "student12", 5, "07:00", "09:00"},
  // thêm tùy ý
};
const int USER_COUNT = sizeof(users) / sizeof(users[0]);

// Tìm user theo fingerId
FingerUser* findUserByFingerId(uint8_t id) {
  for (int i = 0; i < USER_COUNT; i++) {
    if (users[i].fingerId == id) return &users[i];
  }
  return nullptr;
}

// ================== HÀM LẤY THỜI GIAN THỰC ==================
bool getCurrentDateTime(String &dateOut, String &timeOut) {
  struct tm timeinfo;
  if (!getLocalTime(&timeinfo)) {
    Serial.println("❌ Không lấy được thời gian từ NTP");
    return false;
  }

  char dateBuf[11];  // yyyy-mm-dd
  char timeBuf[9];   // HH:MM:SS

  strftime(dateBuf, sizeof(dateBuf), "%Y-%m-%d", &timeinfo);
  strftime(timeBuf, sizeof(timeBuf), "%H:%M:%S", &timeinfo);

  dateOut = String(dateBuf);
  timeOut = String(timeBuf);
  return true;
}

// ================== HÀM GỬI ĐIỂM DANH ==================
bool sendAttendance(const String& studentUsername,
                    int classId,
                    const String& attendanceDate,
                    const String& attendanceTime,
                    const String& sessionStart,
                    const String& sessionEnd) {
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("❌ WiFi không kết nối, không gửi được!");
    return false;
  }

  HTTPClient http;
  http.begin(apiUrl);
  http.addHeader("Content-Type", "application/json");
  http.addHeader("X-DEVICE-KEY", deviceSecret);  // header backend yêu cầu

  // Build JSON
  String payload = "{";
  payload += "\"studentUsername\":\"" + studentUsername + "\",";
  payload += "\"classId\":" + String(classId) + ",";
  payload += "\"attendanceDate\":\"" + attendanceDate + "\",";
  payload += "\"attendanceTime\":\"" + attendanceTime + "\",";
  payload += "\"sessionStart\":\"" + sessionStart + "\",";
  payload += "\"sessionEnd\":\"" + sessionEnd + "\"";
  payload += "}";

  Serial.println("===== GỬI ĐIỂM DANH LÊN SERVER =====");
  Serial.println(payload);

  int httpCode = http.POST(payload);

  Serial.print("HTTP Response Code: ");
  Serial.println(httpCode);

  if (httpCode > 0) {
    String response = http.getString();
    Serial.println("Server Response:");
    Serial.println(response);

    if (httpCode == 200) {
      Serial.println("✅ Điểm danh thành công!");
      http.end();
      return true;
    } else {
      Serial.println("❌ Server trả về lỗi (không phải 200)");
      http.end();
      return false;
    }
  } else {
    Serial.println("❌ Lỗi khi gửi POST request!");
    http.end();
    return false;
  }
}

// ================== KHỞI TẠO VÂN TAY ==================
void initFingerprintSensor() {
  FingerSerial.begin(57600, SERIAL_8N1, FP_RX_PIN, FP_TX_PIN);
  delay(100);

  finger.begin(57600);
  delay(100);

  Serial.println("Đang kiểm tra cảm biến vân tay...");

  if (finger.verifyPassword()) {
    Serial.println("✅ Kết nối cảm biến vân tay OK");
  } else {
    Serial.println("❌ Không tìm thấy cảm biến vân tay / sai baud / sai dây RX/TX");
    Serial.println("Kiểm tra lại dây nối RX/TX và nguồn");
  }
}

// ================== HÀM CHỜ VÂN TAY ==================
int getFingerId() {
  // 1) Chờ có ngón tay
  uint8_t p = finger.getImage();
  if (p == FINGERPRINT_NOFINGER) {
    return -1; // không có ngón tay
  } else if (p != FINGERPRINT_OK) {
    Serial.println("Lỗi getImage() = " + String(p));
    return -1;
  }

  // 2) Chuyển ảnh sang template
  p = finger.image2Tz();
  if (p != FINGERPRINT_OK) {
    Serial.println("Lỗi image2Tz() = " + String(p));
    return -1;
  }

  // 3) So khớp với thư viện trong cảm biến
  p = finger.fingerFastSearch();
  if (p == FINGERPRINT_OK) {
    Serial.println("✅ Tìm thấy vân tay!");
    Serial.print("Finger ID: ");
    Serial.println(finger.fingerID);
    Serial.print("Confidence: ");
    Serial.println(finger.confidence);
    return finger.fingerID;
  } else {
    Serial.println("❌ Không khớp vân tay nào trong sensor (code " + String(p) + ")");
    return -1;
  }
}

// ================== SETUP ==================
void setup() {
  Serial.begin(115200);
  delay(500);

  Serial.println();
  Serial.print("Kết nối WiFi tới: ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);

  unsigned long start = millis();
  while (WiFi.status() != WL_CONNECTED && millis() - start < 20000) {
    Serial.print(".");
    delay(500);
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\n✅ WiFi connected!");
    Serial.print("ESP32 IP: ");
    Serial.println(WiFi.localIP());
  } else {
    Serial.println("\n❌ Không kết nối được WiFi");
  }

  // NTP
  configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);
  Serial.println("Đang đồng bộ thời gian NTP...");
  String d, t;
  if (getCurrentDateTime(d, t)) {
    Serial.print("⏰ Thời gian hiện tại: ");
    Serial.print(d);
    Serial.print(" ");
    Serial.println(t);
  }

  // Fingerprint
  initFingerprintSensor();

  Serial.println();
  Serial.println("===== HỆ THỐNG SẴN SÀNG =====");
  Serial.println("Đặt ngón tay đã enroll lên cảm biến để điểm danh");
}

// ================== LOOP ==================
void loop() {
  int id = getFingerId();
  if (id > 0) {
    FingerUser* user = findUserByFingerId(id);
    if (user == nullptr) {
      Serial.print("❌ Không tìm thấy mapping cho fingerID = ");
      Serial.println(id);
    } else {
      // Lấy giờ thực
      String dateStr, timeStr;
      if (getCurrentDateTime(dateStr, timeStr)) {
        Serial.print("⏰ Sử dụng thời gian: ");
        Serial.print(dateStr);
        Serial.print(" ");
        Serial.println(timeStr);

        // Gửi lên server
        sendAttendance(
          user->username,
          user->classId,
          dateStr,
          timeStr,
          user->sessionStart,
          user->sessionEnd
        );
      } else {
        Serial.println("❌ Không lấy được thời gian, bỏ qua lần điểm danh này.");
      }
    }

    // chờ ngón tay nhấc ra để tránh quét liên tục
    delay(1000);
    while (finger.getImage() != FINGERPRINT_NOFINGER) {
      delay(100);
    }
    Serial.println("Bạn có thể đặt ngón tay tiếp theo...");
  }

  delay(200); // tránh loop quá nhanh
}
