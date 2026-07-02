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

    // ========== 科室专属 Prompt 模板 ==========

    private static final Map<String, String> DEPARTMENT_PROMPT_EXTENSIONS = new HashMap<>();

    static {
        DEPARTMENT_PROMPT_EXTENSIONS.put("儿科",
                "【儿科专项要求】\n" +
                "1. 注意使用通俗易懂的语言描述病情，方便家长理解\n" +
                "2. 重点评估：发热情况（体温、热型）、饮食、睡眠、大小便\n" +
                "3. 用药剂量需标注体重参考范围\n" +
                "4. 重点关注传染病排查（手足口、水痘、流感等）\n");

        DEPARTMENT_PROMPT_EXTENSIONS.put("心内科",
                "【心内科专项要求】\n" +
                "1. 重点描述：胸痛性质（压榨样/刺痛/闷痛）、放射部位、持续时间\n" +
                "2. 心血管危险因素：高血压、糖尿病、血脂异常、吸烟史、家族史\n" +
                "3. 重点关注：心电图、心肌酶谱、血压监测结果\n" +
                "4. 心功能分级评估（NYHA分级）\n");

        DEPARTMENT_PROMPT_EXTENSIONS.put("骨科",
                "【骨科专项要求】\n" +
                "1. 重点描述：受伤机制、疼痛部位、肿胀程度、活动受限范围\n" +
                "2. 详细记录：影像学检查结果（X线/CT/MRI）\n" +
                "3. 评估神经血管功能：远端血运、感觉、运动功能\n" +
                "4. 关注：骨折分型、关节稳定性、石膏/手术方案选择\n");

        DEPARTMENT_PROMPT_EXTENSIONS.put("呼吸内科",
                "【呼吸内科专项要求】\n" +
                "1. 重点描述：咳嗽性质（干咳/咳痰）、痰液性状颜色、咯血\n" +
                "2. 评估：呼吸困难程度、发热曲线、胸痛与呼吸的关系\n" +
                "3. 重点关注：血常规、CRP、胸部影像学、血气分析结果\n" +
                "4. 传染病排查：结核、新冠病毒等\n");

        DEPARTMENT_PROMPT_EXTENSIONS.put("消化内科",
                "【消化内科专项要求】\n" +
                "1. 重点描述：腹痛部位（上腹/下腹/脐周）、性质、与饮食关系\n" +
                "2. 消化道症状：恶心呕吐、腹泻便秘、便血、黄疸\n" +
                "3. 重点关注：胃镜/肠镜结果、幽门螺杆菌检测、肝功能\n" +
                "4. 饮食与生活习惯评估\n");

        DEPARTMENT_PROMPT_EXTENSIONS.put("神经内科",
                "【神经内科专项要求】\n" +
                "1. 重点描述：头痛性质（胀痛/搏动性）、眩晕特点、意识状态\n" +
                "2. 详细评估：神经系统定位体征（颅神经、肌力、感觉、共济运动）\n" +
                "3. 重点关注：头颅CT/MRI、脑电图、脑脊液检查\n" +
                "4. 脑血管病危险因素评估\n");

        DEPARTMENT_PROMPT_EXTENSIONS.put("皮肤科",
                "【皮肤科专项要求】\n" +
                "1. 重点描述：皮疹形态（斑疹/丘疹/水疱）、分布部位、瘙痒程度\n" +
                "2. 评估：发病诱因（食物/药物/接触物）、病程演变\n" +
                "3. 重点关注：皮肤科专科检查描述（皮损类型、边界、表面特征）\n");

        DEPARTMENT_PROMPT_EXTENSIONS.put("妇科",
                "【妇科专项要求】\n" +
                "1. 重点描述：月经情况（周期/经量/痛经）、阴道分泌物性状\n" +
                "2. 评估：下腹痛与月经周期的关系、异常出血\n" +
                "3. 重点关注：妇科B超、宫颈筛查、性激素检查\n" +
                "4. 孕产史、避孕情况记录\n");

        DEPARTMENT_PROMPT_EXTENSIONS.put("眼科",
                "【眼科专项要求】\n" +
                "1. 重点描述：视力变化（突发/渐进）、视野缺损、眼痛性质\n" +
                "2. 评估：眼压、眼底检查、屈光状态\n" +
                "3. 重点关注：视力表、裂隙灯、眼底镜检查结果\n");
    }

    /**
     * 获取科室专属的 Prompt 扩展内容
     * 无对应科室时返回空字符串使用通用模板
     */
    private static String getDepartmentPromptExtension(String department) {
        if (department == null || department.isBlank()) return "";
        String normalized = DEPARTMENT_ALIASES.getOrDefault(department, department);
        return DEPARTMENT_PROMPT_EXTENSIONS.getOrDefault(normalized, "");
    }

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

        String deptExtension = getDepartmentPromptExtension(department);
        if (!deptExtension.isEmpty()) {
            promptBuilder.append(deptExtension);
        }

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
                "请以JSON格式返回审核结果，不要添加任何其他文字。JSON格式如下：\n" +
                "{\n" +
                "  \"checkResult\": \"审核结论（如：审核通过/有风险需注意/不建议使用）\",\n" +
                "  \"riskLevel\": \"风险等级（低风险/中风险/高风险）\",\n" +
                "  \"medicationSuggestions\": \"用药建议\",\n" +
                "  \"interactionDetection\": \"药物相互作用检测\",\n" +
                "  \"riskHints\": \"风险提示\"\n" +
                "}",
                medicineList, patientInfo
        );

        long startTime = System.currentTimeMillis();
        try {
            String response = callDeepSeekApi(prompt);
            long cost = System.currentTimeMillis() - startTime;

            log.info("处方审核成功，耗时: {}ms", cost);
            log.debug("审核结果: {}", response);
            log.info("========== AI处方审核结束 ==========");
            return response;
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

    public void streamMedicalRecord(String dialogueText, String symptoms, String department,
                                    java.util.function.Consumer<String> onChunk,
                                    Runnable onComplete,
                                    java.util.function.Consumer<Exception> onError) {
        log.info("========== AI病历流式生成开始 ==========");

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

        String deptExtension = getDepartmentPromptExtension(department);
        if (!deptExtension.isEmpty()) {
            promptBuilder.append(deptExtension);
        }

        promptBuilder.append("4. 只返回JSON，不要其他任何文字");

        String prompt = promptBuilder.toString();
        String url = baseUrl + "/chat/completions";

        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Accept", "text/event-stream");

            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("model", model);
            body.put("messages", java.util.List.of(java.util.Map.of("role", "user", "content", prompt)));
            body.put("max_tokens", 2000);
            body.put("temperature", 0.1);
            body.put("stream", true);

            org.springframework.http.HttpEntity<java.util.Map<String, Object>> entity =
                    new org.springframework.http.HttpEntity<>(body, headers);

            restTemplate.execute(url, org.springframework.http.HttpMethod.POST,
                    request -> {
                        request.getHeaders().addAll(headers);
                        try (java.io.OutputStream os = request.getBody()) {
                            objectMapper.writeValue(os, body);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    },
                    response -> {
                        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                                new java.io.InputStreamReader(response.getBody(), java.nio.charset.StandardCharsets.UTF_8))) {
                            String line;
                            StringBuilder contentBuilder = new StringBuilder();
                            while ((line = reader.readLine()) != null) {
                                if (line.startsWith("data: ")) {
                                    String data = line.substring(6);
                                    if ("[DONE]".equals(data)) {
                                        onComplete.run();
                                        return null;
                                    }
                                    try {
                                        com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(data);
                                        String delta = root.path("choices")
                                                .path(0)
                                                .path("delta")
                                                .path("content")
                                                .asText();
                                        if (delta != null && !delta.isEmpty()) {
                                            contentBuilder.append(delta);
                                            onChunk.accept(delta);
                                        }
                                    } catch (Exception e) {
                                        log.debug("解析SSE数据块失败: {}", data);
                                    }
                                }
                            }
                            onComplete.run();
                        } catch (Exception e) {
                            log.error("流式读取失败", e);
                            onError.accept(e);
                        }
                        return null;
                    }
            );
        } catch (Exception e) {
            log.error("流式调用AI失败", e);
            onError.accept(e);
        }
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
