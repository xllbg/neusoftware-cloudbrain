package com.neusoft.cloudbrain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
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

        DEPARTMENT_ALIASES.put("妇科", "妇科");
        DEPARTMENT_ALIASES.put("妇产科", "妇科");
        DEPARTMENT_ALIASES.put("妇科内分泌", "妇科");

        DEPARTMENT_ALIASES.put("产科", "产科");
        DEPARTMENT_ALIASES.put("产前检查", "产科");

        DEPARTMENT_ALIASES.put("皮肤科", "皮肤科");
        DEPARTMENT_ALIASES.put("皮肤病", "皮肤科");
        DEPARTMENT_ALIASES.put("皮肤", "皮肤科");

        DEPARTMENT_ALIASES.put("眼科", "眼科");
        DEPARTMENT_ALIASES.put("眼病", "眼科");
        DEPARTMENT_ALIASES.put("视力", "眼科");

        DEPARTMENT_ALIASES.put("耳鼻喉科", "耳鼻喉科");
        DEPARTMENT_ALIASES.put("五官科", "耳鼻喉科");
        DEPARTMENT_ALIASES.put("耳科", "耳鼻喉科");
        DEPARTMENT_ALIASES.put("鼻科", "耳鼻喉科");
        DEPARTMENT_ALIASES.put("喉科", "耳鼻喉科");

        DEPARTMENT_ALIASES.put("口腔科", "口腔科");
        DEPARTMENT_ALIASES.put("牙科", "口腔科");
        DEPARTMENT_ALIASES.put("牙病", "口腔科");

        DEPARTMENT_ALIASES.put("泌尿外科", "泌尿外科");
        DEPARTMENT_ALIASES.put("泌尿科", "泌尿外科");
        DEPARTMENT_ALIASES.put("肾科", "泌尿外科");

        DEPARTMENT_ALIASES.put("胸外科", "胸外科");
        DEPARTMENT_ALIASES.put("心外科", "胸外科");

        DEPARTMENT_ALIASES.put("血液科", "血液科");
        DEPARTMENT_ALIASES.put("血液病", "血液科");

        DEPARTMENT_ALIASES.put("内分泌科", "内分泌科");
        DEPARTMENT_ALIASES.put("内分泌", "内分泌科");
        DEPARTMENT_ALIASES.put("糖尿病", "内分泌科");
        DEPARTMENT_ALIASES.put("甲亢", "内分泌科");

        DEPARTMENT_ALIASES.put("肿瘤科", "肿瘤科");
        DEPARTMENT_ALIASES.put("肿瘤", "肿瘤科");
        DEPARTMENT_ALIASES.put("癌症", "肿瘤科");
        DEPARTMENT_ALIASES.put("化疗", "肿瘤科");
    }

    private static final Set<String> VALID_DEPARTMENTS = new HashSet<>(Arrays.asList(
            "心内科", "呼吸内科", "神经内科", "消化内科", "骨科", "儿科", "内科", "普外科", "急诊科", "妇科", "产科", "皮肤科", "眼科", "耳鼻喉科", "口腔科", "泌尿外科", "胸外科", "血液科", "内分泌科", "肿瘤科"
    ));

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.base-url}")
    private String baseUrl;

    @Value("${deepseek.model}")
    private String model;

    public String recommendDepartment(String symptoms, Integer age, String gender) {
        log.info("========== AI分诊开始 ==========");
        log.info("患者信息: {}岁{}，症状: {}", age, gender, symptoms);

        long startTime = System.currentTimeMillis();
        String aiDepartment;
        try {
            aiDepartment = callAiForDepartment(symptoms, age, gender);
            log.info("AI原始返回: {}", aiDepartment);
        } catch (Exception e) {
            log.error("AI分诊异常，使用fallback", e);
            aiDepartment = fallbackDepartmentRecommendation(symptoms);
        }

        String normalized = normalizeDepartment(aiDepartment, symptoms);
        long cost = System.currentTimeMillis() - startTime;

        log.info("标准化后科室: {}，耗时: {}ms", normalized, cost);
        log.info("========== AI分诊结束 ==========");

        return normalized;
    }

    private String callAiForDepartment(String symptoms, Integer age, String gender) {
        String prompt = String.format(
                "你是医疗分诊专家。根据患者症状判断最合适的就诊科室。\n" +
                "患者：%d岁%s。\n" +
                "症状：%s\n\n" +
                "请从以下科室中选择一个最合适的，只返回科室名称，不要其他文字：\n" +
                "心内科、呼吸内科、神经内科、消化内科、骨科、儿科、普外科、内科、急诊科、妇科、产科、皮肤科、眼科、耳鼻喉科、口腔科、泌尿外科、胸外科、血液科、内分泌科、肿瘤科\n\n" +
                "注意：脚崴、手腕扭伤、骨折、骨痛、腰痛、关节痛请选择【骨科】。月经不调、痛经、妇科炎症请选择【妇科】。",
                age, gender, symptoms
        );

        log.info("发送AI请求，prompt长度: {}", prompt.length());
        String response = callDeepSeekApi(prompt);
        log.info("AI原始响应: {}", response);
        return parseDepartmentFromResponse(response);
    }

    private String callDeepSeekApi(String prompt) {
        String url = baseUrl + "/chat/completions";
        log.info("DeepSeek API URL: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        body.put("max_tokens", 1000);
        body.put("temperature", 0.1);

        log.info("请求体: model={}, max_tokens=1000, temperature=0.1", model);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        long apiStart = System.currentTimeMillis();
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            long apiCost = System.currentTimeMillis() - apiStart;
            log.debug("API调用成功，耗时: {}ms", apiCost);

            if (response.getBody() != null) {
                log.trace("响应内容: {}", response.getBody());
            }
            return response.getBody();
        } catch (RestClientException e) {
            long apiCost = System.currentTimeMillis() - apiStart;
            log.error("API调用失败，耗时: {}ms，错误: {}", apiCost, e.getMessage());
            throw e;
        }
    }

    private String parseDepartmentFromResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            String content = root.path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText();

            if (content == null || content.isBlank()) {
                log.warn("AI响应content为空，使用默认内科");
                return "内科";
            }
            String cleaned = content.replace("\\n", "").replace("\"", "").replace(" ", "").trim();
            log.debug("解析后的科室: {}", cleaned);
            return cleaned;
        } catch (Exception e) {
            log.warn("解析AI响应失败: {}，原始响应: {}", e.getMessage(), jsonResponse);
            return "内科";
        }
    }

    private String normalizeDepartment(String aiResult, String symptoms) {
        if (aiResult == null || aiResult.isBlank()) {
            log.warn("AI结果为空，启用fallback，症状: {}", symptoms);
            return fallbackDepartmentRecommendation(symptoms);
        }

        String cleaned = aiResult.replace(" ", "").replace("\"", "").trim();
        log.debug("标准化输入: {} -> {}", aiResult, cleaned);

        if (VALID_DEPARTMENTS.contains(cleaned)) {
            log.debug("直接匹配有效科室: {}", cleaned);
            return cleaned;
        }

        String normalized = DEPARTMENT_ALIASES.get(cleaned);
        if (normalized != null) {
            log.debug("别名映射: {} -> {}", cleaned, normalized);
            return normalized;
        }

        for (Map.Entry<String, String> entry : DEPARTMENT_ALIASES.entrySet()) {
            if (cleaned.contains(entry.getKey()) || entry.getKey().contains(cleaned)) {
                log.debug("模糊匹配: {} -> {}", entry.getKey(), entry.getValue());
                return entry.getValue();
            }
        }

        log.warn("无法识别的科室: {}，启用fallback", aiResult);
        return fallbackDepartmentRecommendation(symptoms);
    }

    private String fallbackDepartmentRecommendation(String symptoms) {
        if (symptoms == null || symptoms.isBlank()) {
            return "内科";
        }

        String s = symptoms.toLowerCase();
        log.debug("执行fallback匹配，症状: {}", symptoms);

        if (s.contains("骨折") || s.contains("骨") || s.contains("腰") || s.contains("关节") ||
            s.contains("扭伤") || s.contains("崴") || s.contains("挫伤") || s.contains("拉伤")) {
            log.debug("fallback匹配: 骨科");
            return "骨科";
        }
        if (s.contains("胸痛") || s.contains("心悸") || s.contains("高血压") || s.contains("心")) {
            log.debug("fallback匹配: 心内科");
            return "心内科";
        }
        if (s.contains("咳嗽") || s.contains("发热") || s.contains("呼吸") || s.contains("肺") || s.contains("感冒") || s.contains("新冠")) {
            log.debug("fallback匹配: 呼吸内科");
            return "呼吸内科";
        }
        if (s.contains("头痛") || s.contains("头晕") || s.contains("神经") || s.contains("脑") || s.contains("面瘫")) {
            log.debug("fallback匹配: 神经内科");
            return "神经内科";
        }
        if (s.contains("腹痛") || s.contains("腹泻") || s.contains("胃") || s.contains("消化") || s.contains("便秘")) {
            log.debug("fallback匹配: 消化内科");
            return "消化内科";
        }
        if (s.contains("儿") || s.contains("小孩") || s.contains("幼儿") || s.contains("儿童")) {
            log.debug("fallback匹配: 儿科");
            return "儿科";
        }
        if (s.contains("急诊") || s.contains("急性") || s.contains("出血") || s.contains("昏迷")) {
            log.debug("fallback匹配: 急诊科");
            return "急诊科";
        }

        log.debug("fallback匹配: 默认内科");
        return "内科";
    }

    public String generateMedicalRecord(String dialogueText) {
        return generateMedicalRecordWithSymptoms(dialogueText, null, null);
    }

    public String generateMedicalRecordWithSymptoms(String dialogueText, String symptoms, String department) {
        log.info("========== AI病历生成开始 ==========");
        log.info("对话记录长度: {} 字符", dialogueText != null ? dialogueText.length() : 0);
        log.info("患者自述症状: {}", symptoms);
        log.info("科室: {}", department);

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("你是资深病历书写专家。请根据以下患者信息，生成规范、完整的结构化电子病历。\n\n");

        promptBuilder.append("【患者信息】\n");
        if (department != null && !department.isBlank()) {
            promptBuilder.append("就诊科室：").append(department).append("\n");
        }
        if (symptoms != null && !symptoms.isBlank()) {
            promptBuilder.append("患者自述症状：").append(symptoms).append("\n");
        }

        if (dialogueText != null && !dialogueText.isBlank()) {
            promptBuilder.append("\n【医患对话记录】\n").append(dialogueText).append("\n");
        }

        promptBuilder.append("\n【病历生成要求】\n");
        promptBuilder.append("请以JSON格式返回，包含以下字段：\n");
        promptBuilder.append("chiefComplaint: 主诉（简明扼要，20字以内）\n");
        promptBuilder.append("presentIllness: 现病史（详细描述发病时间、诱因、主要症状特点、伴随症状、诊治经过等）\n");
        promptBuilder.append("pastHistory: 既往史（既往健康状况、疾病史、手术史、过敏史等）\n");
        promptBuilder.append("physicalExamination: 体格检查（根据症状推断相关检查项目和可能的结果）\n");
        promptBuilder.append("diagnosis: 初步诊断（根据信息给出最可能的诊断）\n");
        promptBuilder.append("treatmentPlan: 治疗意见（包括药物治疗、生活方式建议、复诊建议等）\n\n");
        promptBuilder.append("要求：\n");
        promptBuilder.append("1. 内容要真实、合理、符合医学规范\n");
        promptBuilder.append("2. 根据提供的信息合理推断，信息不足的地方可以留空或写\"待进一步检查\"\n");
        promptBuilder.append("3. 语言专业、准确，符合病历书写规范\n");
        promptBuilder.append("4. 只返回JSON，不要其他任何文字");

        String prompt = promptBuilder.toString();

        long startTime = System.currentTimeMillis();
        try {
            String response = callDeepSeekApi(prompt);
            String content = parseContentFromResponse(response);
            long cost = System.currentTimeMillis() - startTime;

            log.info("病历生成成功，耗时: {}ms", cost);
            log.debug("生成病历内容: {}", content);
            log.info("========== AI病历生成结束 ==========");
            return content;
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - startTime;
            log.error("病历生成失败，耗时: {}ms", cost, e);
            log.info("========== AI病历生成异常 ==========");
            return "{\"error\": \"AI服务暂时不可用，请手动填写病历\"}";
        }
    }

    public String checkPrescription(String medicineList, String patientInfo) {
        log.info("========== AI处方审核开始 ==========");
        log.info("药品列表: {}", medicineList);
        log.info("患者信息: {}", patientInfo);

        String prompt = String.format(
                "你是医疗处方审核专家。请审核以下处方并提供用药建议。\n\n" +
                "药品列表：\n%s\n\n" +
                "患者信息：\n%s\n\n" +
                "请提供：\n" +
                "1. 用药建议\n" +
                "2. 药物相互作用检测\n" +
                "3. 风险等级（low/medium/high）\n" +
                "4. 风险提示",
                medicineList, patientInfo
        );

        long startTime = System.currentTimeMillis();
        try {
            String response = callDeepSeekApi(prompt);
            String content = parseContentFromResponse(response);
            long cost = System.currentTimeMillis() - startTime;

            log.info("处方审核成功，耗时: {}ms", cost);
            log.debug("审核结果: {}", content);
            log.info("========== AI处方审核结束 ==========");
            return content;
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - startTime;
            log.error("处方审核失败，耗时: {}ms", cost, e);
            log.info("========== AI处方审核异常 ==========");
            return "{\"error\": \"AI服务暂时不可用，请手动审核\"}";
        }
    }

    public String recommendMedicine(String symptoms, String diagnosis, String department) {
        return recommendMedicineWithDialogue(symptoms, diagnosis, department, null);
    }

    public String recommendMedicineWithDialogue(String symptoms, String diagnosis, String department, String dialogueText) {
        log.info("========== AI药品推荐开始 ==========");
        log.info("症状: {}", symptoms);
        log.info("诊断: {}", diagnosis);
        log.info("科室: {}", department);
        log.info("对话记录长度: {}", dialogueText != null ? dialogueText.length() : 0);

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("你是经验丰富的临床用药专家。请根据以下患者信息，谨慎、精准地推荐合适的药品。\n\n");
        promptBuilder.append("【患者基本信息】\n");
        promptBuilder.append("科室：").append(department).append("\n");
        promptBuilder.append("患者自述症状：").append(symptoms).append("\n");
        if (diagnosis != null && !diagnosis.isBlank()) {
            promptBuilder.append("初步诊断：").append(diagnosis).append("\n");
        }

        if (dialogueText != null && !dialogueText.isBlank()) {
            promptBuilder.append("\n【医患对话记录】\n").append(dialogueText).append("\n");
        }

        promptBuilder.append("\n【用药原则】\n");
        promptBuilder.append("1. 有效性：严格对症下药，根据患者具体症状和诊断选择最适合的药品\n");
        promptBuilder.append("2. 安全性：充分考虑可能的禁忌症、过敏风险和不良反应\n");
        promptBuilder.append("3. 合理性：避免重复用药，注意药物相互作用，剂量准确\n");
        promptBuilder.append("4. 经济性：在保证疗效的前提下，优先选择常用、性价比高的药品\n");
        promptBuilder.append("5. 严谨性：如信息不足无法确定，宁可少推荐也不盲目推荐\n\n");

        promptBuilder.append("请以JSON数组格式返回推荐的药品列表，每个药品包含以下字段：\n");
        promptBuilder.append("name: 药品名称（通用名，不要商品名）\n");
        promptBuilder.append("dose: 剂量（如：2片/次，10mg/次）\n");
        promptBuilder.append("frequency: 用法频次（如：每日3次，饭后口服）\n\n");
        promptBuilder.append("要求：\n");
        promptBuilder.append("- 只推荐确实对症的药品，不要为了凑数而推荐\n");
        promptBuilder.append("- 药品数量根据病情需要，一般2-5种为宜\n");
        promptBuilder.append("- 只返回JSON数组，不要其他任何文字说明");

        String prompt = promptBuilder.toString();

        long startTime = System.currentTimeMillis();
        try {
            String response = callDeepSeekApi(prompt);
            String content = parseContentFromResponse(response);
            long cost = System.currentTimeMillis() - startTime;

            log.info("药品推荐成功，耗时: {}ms", cost);
            log.debug("推荐结果: {}", content);
            log.info("========== AI药品推荐结束 ==========");
            return content;
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - startTime;
            log.error("药品推荐失败，耗时: {}ms", cost, e);
            log.info("========== AI药品推荐异常 ==========");
            return "[]";
        }
    }

    private String parseContentFromResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            String content = root.path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText();

            if (content == null || content.isBlank()) {
                log.warn("AI响应content为空");
                return jsonResponse;
            }
            String cleaned = content.replace("\\n", "").replace("\\\"", "\"");
            log.debug("解析内容长度: {} 字符", cleaned.length());
            return cleaned;
        } catch (Exception e) {
            log.warn("解析AI响应失败: {}", e.getMessage());
            return jsonResponse;
        }
    }

    /**
     * 公开方法：调用DeepSeek API（用于问诊记录AI推荐）
     */
    public String callDeepSeekApiForConsultation(String prompt) {
        return callDeepSeekApi(prompt);
    }

    public String optimizeMedicalRecord(String chiefComplaint, String presentIllness,
            String pastHistory, String physicalExamination, String diagnosis,
            String treatmentPlan) {
        log.info("========== AI优化病历开始 ==========");

        StringBuilder sb = new StringBuilder();
        sb.append("你是资深病历审核专家。请根据以下问诊记录，优化生成完整规范的电子病历。\n\n");
        sb.append("当前问诊记录：\n");
        if (chiefComplaint != null && !chiefComplaint.isBlank()) {
            sb.append("主诉：").append(chiefComplaint).append("\n");
        }
        if (presentIllness != null && !presentIllness.isBlank()) {
            sb.append("现病史：").append(presentIllness).append("\n");
        }
        if (pastHistory != null && !pastHistory.isBlank()) {
            sb.append("既往史：").append(pastHistory).append("\n");
        }
        if (physicalExamination != null && !physicalExamination.isBlank()) {
            sb.append("体格检查：").append(physicalExamination).append("\n");
        }
        if (diagnosis != null && !diagnosis.isBlank()) {
            sb.append("初步诊断：").append(diagnosis).append("\n");
        }
        if (treatmentPlan != null && !treatmentPlan.isBlank()) {
            sb.append("治疗意见：").append(treatmentPlan).append("\n");
        }
        sb.append("\n请对以上内容进行医学规范化优化，要求：\n");
        sb.append("1. 保持原有核心信息不变，只做规范化、专业化润色\n");
        sb.append("2. 补充合理的医学细节，使病历更完整规范\n");
        sb.append("3. 语言专业、准确、符合病历书写规范\n\n");
        sb.append("请以JSON格式返回，包含以下字段：\n");
        sb.append("chiefComplaint: 主诉\n");
        sb.append("presentIllness: 现病史\n");
        sb.append("pastHistory: 既往史\n");
        sb.append("physicalExamination: 体格检查\n");
        sb.append("diagnosis: 初步诊断\n");
        sb.append("treatmentPlan: 治疗意见\n\n");
        sb.append("只返回JSON，不要其他文字。");

        String prompt = sb.toString();

        long startTime = System.currentTimeMillis();
        try {
            String response = callDeepSeekApi(prompt);
            String content = parseContentFromResponse(response);
            long cost = System.currentTimeMillis() - startTime;

            log.info("病历优化成功，耗时: {}ms", cost);
            log.info("========== AI优化病历结束 ==========");
            return content;
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - startTime;
            log.error("病历优化失败，耗时: {}ms", cost, e);
            log.info("========== AI优化病历异常 ==========");
            return "{\"error\": \"AI服务暂时不可用\"}";
        }
    }
}
