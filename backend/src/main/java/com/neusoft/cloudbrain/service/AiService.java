package com.neusoft.cloudbrain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Slf4j
public class AiService {

    private static final Map<String, String> DEPARTMENT_ALIASES = new HashMap<>();

    static {
        DEPARTMENT_ALIASES.put("骨科", "骨科");
        DEPARTMENT_ALIASES.put("骨外科", "骨科");
        DEPARTMENT_ALIASES.put("骨伤科", "骨科");
        DEPARTMENT_ALIASES.put("骨与关节", "骨科");
        DEPARTMENT_ALIASES.put("创伤骨科", "骨科");

        DEPARTMENT_ALIASES.put("心内科", "心内科");
        DEPARTMENT_ALIASES.put("心血管内科", "心内科");
        DEPARTMENT_ALIASES.put("心血管", "心内科");

        DEPARTMENT_ALIASES.put("呼吸内科", "呼吸内科");
        DEPARTMENT_ALIASES.put("呼吸科", "呼吸内科");
        DEPARTMENT_ALIASES.put("呼吸系统", "呼吸内科");

        DEPARTMENT_ALIASES.put("神经内科", "神经内科");
        DEPARTMENT_ALIASES.put("神内科", "神经内科");
        DEPARTMENT_ALIASES.put("脑科", "神经内科");

        DEPARTMENT_ALIASES.put("消化内科", "消化内科");
        DEPARTMENT_ALIASES.put("消化科", "消化内科");
        DEPARTMENT_ALIASES.put("肠胃科", "消化内科");

        DEPARTMENT_ALIASES.put("儿科", "儿科");
        DEPARTMENT_ALIASES.put("小儿科", "儿科");

        DEPARTMENT_ALIASES.put("急诊科", "急诊科");
        DEPARTMENT_ALIASES.put("急诊", "急诊科");

        DEPARTMENT_ALIASES.put("普外科", "普外科");
        DEPARTMENT_ALIASES.put("外科", "普外科");

        DEPARTMENT_ALIASES.put("内科", "内科");
        DEPARTMENT_ALIASES.put("全科", "内科");
    }

    private static final Set<String> VALID_DEPARTMENTS = new HashSet<>(Arrays.asList(
            "心内科", "呼吸内科", "神经内科", "消化内科", "骨科", "儿科", "内科", "普外科", "急诊科"
    ));

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.base-url}")
    private String baseUrl;

    @Value("${deepseek.model}")
    private String model;

    public String recommendDepartment(String symptoms, Integer age, String gender) {
        String aiDepartment = callAiForDepartment(symptoms, age, gender);
        log.info("AI 返回科室: {}", aiDepartment);

        String normalized = normalizeDepartment(aiDepartment, symptoms);
        log.info("标准化后科室: {}", normalized);

        return normalized;
    }

    private String callAiForDepartment(String symptoms, Integer age, String gender) {
        String prompt = String.format(
                "你是医疗分诊专家。根据患者症状判断最合适的就诊科室。\n" +
                "患者：%d岁%s。\n" +
                "症状：%s\n\n" +
                "请从以下科室中选择一个最合适的，只返回科室名称，不要其他文字：\n" +
                "心内科、呼吸内科、神经内科、消化内科、骨科、儿科、普外科、内科、急诊科\n\n" +
                "注意：脚崴、手腕扭伤、骨折、骨痛、腰痛、关节痛请选择【骨科】。",
                age, gender, symptoms
        );

        try {
            String response = callDeepSeekApi(prompt);
            return parseDepartmentFromResponse(response);
        } catch (Exception e) {
            log.error("调用 DeepSeek API 失败，使用 fallback 逻辑", e);
            return fallbackDepartmentRecommendation(symptoms);
        }
    }

    private String callDeepSeekApi(String prompt) throws Exception {
        URI uri = new URI(baseUrl + "/chat/completions");
        URL url = uri.toURL();

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        String jsonBody = String.format("""
            {
                "model": "%s",
                "messages": [
                    {"role": "user", "content": "%s"}
                ],
                "max_tokens": 20,
                "temperature": 0.1
            }
            """, model, prompt.replace("\"", "\\\""));

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("API 返回错误码: " + responseCode);
        }

        StringBuilder response = new StringBuilder();
        try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
        }

        return response.toString();
    }

    private String parseDepartmentFromResponse(String jsonResponse) {
        try {
            int contentStart = jsonResponse.indexOf("\"content\":\"");
            if (contentStart == -1) {
                return "内科";
            }
            contentStart += 11;
            int contentEnd = jsonResponse.indexOf("\"", contentStart);
            String content = jsonResponse.substring(contentStart, contentEnd);
            content = content.replace("\\n", "").replace("\\\"", "\"").replace(" ", "").trim();
            return content;
        } catch (Exception e) {
            log.warn("解析 AI 响应失败: {}", jsonResponse);
            return "内科";
        }
    }

    private String normalizeDepartment(String aiResult, String symptoms) {
        if (aiResult == null || aiResult.isBlank()) {
            return fallbackDepartmentRecommendation(symptoms);
        }

        String cleaned = aiResult.replace(" ", "").replace("\"", "").trim();

        if (VALID_DEPARTMENTS.contains(cleaned)) {
            return cleaned;
        }

        String normalized = DEPARTMENT_ALIASES.get(cleaned);
        if (normalized != null) {
            return normalized;
        }

        for (Map.Entry<String, String> entry : DEPARTMENT_ALIASES.entrySet()) {
            if (cleaned.contains(entry.getKey()) || entry.getKey().contains(cleaned)) {
                return entry.getValue();
            }
        }

        log.warn("无法识别的科室: {}，使用 fallback", aiResult);
        return fallbackDepartmentRecommendation(symptoms);
    }

    private String fallbackDepartmentRecommendation(String symptoms) {
        if (symptoms == null || symptoms.isBlank()) {
            return "内科";
        }

        String s = symptoms.toLowerCase();

        if (s.contains("骨折") || s.contains("骨") || s.contains("腰") || s.contains("关节") ||
            s.contains("扭伤") || s.contains("崴") || s.contains("挫伤") || s.contains("拉伤")) {
            return "骨科";
        }
        if (s.contains("胸痛") || s.contains("心悸") || s.contains("高血压") || s.contains("心")) {
            return "心内科";
        }
        if (s.contains("咳嗽") || s.contains("发热") || s.contains("呼吸") || s.contains("肺") || s.contains("感冒") || s.contains("新冠")) {
            return "呼吸内科";
        }
        if (s.contains("头痛") || s.contains("头晕") || s.contains("神经") || s.contains("脑") || s.contains("面瘫")) {
            return "神经内科";
        }
        if (s.contains("腹痛") || s.contains("腹泻") || s.contains("胃") || s.contains("消化") || s.contains("便秘")) {
            return "消化内科";
        }
        if (s.contains("儿") || s.contains("小孩") || s.contains("幼儿") || s.contains("儿童")) {
            return "儿科";
        }
        if (s.contains("急诊") || s.contains("急性") || s.contains("出血") || s.contains("昏迷")) {
            return "急诊科";
        }

        return "内科";
    }
}
