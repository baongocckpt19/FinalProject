//đây là GeminiService.java
package com.FinalProject.backend.Service;

import com.FinalProject.backend.Dto.AttendanceDto;
import com.FinalProject.backend.Dto.AttendanceAnalysis;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class GeminiService {

    @Value("${ai.key}")
    private String geminiApiKey;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Hàm dọn dẹp string trước khi parse JSON
     */
    private String cleanJsonString(String raw) {
        if (raw == null || raw.isEmpty()) return null;

        String cleaned = raw.trim();

        // Loại bỏ ```json ... ```
        if (cleaned.startsWith("```") && cleaned.endsWith("```")) {
            int firstNewline = cleaned.indexOf("\n");
            if (firstNewline > 0) {
                cleaned = cleaned.substring(firstNewline + 1, cleaned.length() - 3).trim();
            } else {
                cleaned = cleaned.substring(3, cleaned.length() - 3).trim();
            }
        }

        return cleaned;
    }

    public AttendanceAnalysis analyzeAttendance(List<AttendanceDto> attendanceList) throws Exception {

        String jsonData = mapper.writeValueAsString(attendanceList);

        // Prompt hướng dẫn trả về JSON theo schema để dễ hiển thị
        String prompt = "Phân tích danh sách điểm danh sinh viên ở dưới. Yêu cầu:\n" +
                "1. Xác định sinh viên nguy cơ cao nghỉ học hoặc vắng nhiều, liệt kê các field: studentCode, fullName, absentCount, lateCount, riskScore (0–100), riskReason.\n" +
                "2. Thống kê tổng quan, các field: totalRecords, presentCount, absentCount, lateCount, onLeaveCount, attendanceRate.\n" +
                "3. Phân tích xu hướng theo tuần, các field: weekStart, weekEnd, present, absent, late.\n" +
                "4. Đưa ra khuyến nghị, các field: recommendations (list of strings).\n" +
                "5. Trả về **CHỈ JSON thuần túy**, **không** kèm bất kỳ văn bản mô tả, chú thích, markdown, hoặc dấu ```.\n" +
                "6. Tên các trường JSON phải chính xác như sau: overallStatistics, highRiskStudents, weeklyTrend, perStudent, recommendations.\n\n" +
                "Dữ liệu đầu vào:\n" + jsonData;

        String jsonBody = "{ \"contents\": [{ \"parts\": [{\"text\": \"" +
                prompt.replace("\"", "\\\"") +
                "\"}]}]}";

        RequestBody body = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash-lite:generateContent?key=" + geminiApiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("Gemini API error: " + response.code() + " " + response.message());
                return new AttendanceAnalysis();
            }
            String responseBody = response.body().string();
            System.out.println("Gemini response body:" + responseBody);
            if (responseBody == null || responseBody.isEmpty()) {
                return new AttendanceAnalysis();
            }

            JsonNode root = mapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");

            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    String analysisJson = cleanJsonString(parts.get(0).path("text").asText());
                    if (analysisJson != null) {
                        return mapper.readValue(analysisJson, AttendanceAnalysis.class);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Exception during Gemini API call: " + e.getMessage());
            e.printStackTrace();
        }

        return new AttendanceAnalysis();
    }
}
