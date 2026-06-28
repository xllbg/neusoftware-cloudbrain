package com.neusoft.cloudbrain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Pattern;

@Service
    @Slf4j
    @RequiredArgsConstructor
    public class AiService {

    // AI分析结果内部类
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class TriageAnalysisResult {
        private String department;
        private String reasoning;
        private Integer confidence;      // 0-100
        private Boolean needFollowUp;    // 是否需要追问
        private List<String> followUpQuestions; // 追问问题列表
        private Boolean invalidInput;     // 是否无效输入
    }

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

    @Value("${deepseek.model}")
    private String model;

    /**
     * 注入攻击检测正则：匹配常见的 prompt 注入模式
     */
    private static final Pattern INJECTION_PATTERN = Pattern.compile(
            ".*?(忽略|不顾|不要|无须|违反|绕过| вместо |instead of |ignore|disregard|override|bypass|forget).*?",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    /**
     * 过滤用户输入中的危险字符，防止 prompt 注入
     */
    private String sanitizeUserInput(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String sanitized = input.trim();
        // 移除可能的 prompt 注入指令（忽略、返回指定内容、扮演等）
        sanitized = sanitized.replaceAll("(?i)忽略.*", "");
        sanitized = sanitized.replaceAll("(?i)不顾.*", "");
        sanitized = sanitized.replaceAll("(?i)不要.*", "");
        sanitized = sanitized.replaceAll("(?i)无须.*", "");
        sanitized = sanitized.replaceAll("(?i)违反.*", "");
        sanitized = sanitized.replaceAll("(?i)忽略以上.*", "");
        sanitized = sanitized.replaceAll("(?i)ignore\\s+this.*", "");
        sanitized = sanitized.replaceAll("(?i)disregard\\s+.*", "");
        sanitized = sanitized.replaceAll("(?i)override\\s+.*", "");
        // 移除多余的空白但保留症状描述的自然结构
        sanitized = sanitized.replaceAll("\\s+", " ").trim();
        // 限制最大长度，防止超长输入
        if (sanitized.length() > 500) {
            sanitized = sanitized.substring(0, 500);
        }
        return sanitized;
    }

    public String recommendDepartment(String symptoms, Integer age, String gender) {
        log.info("========== AI分诊开始 ==========");
        String safeSymptoms = sanitizeUserInput(symptoms);
        log.info("患者信息: {}岁{}，症状: {}", age, gender, safeSymptoms);

        // 优先走本地规则匹配，避免AI注入风险
        String localResult = localDepartmentRecommend(safeSymptoms);
        if (localResult != null) {
            log.info("本地规则匹配科室: {}，症状: {}", localResult, safeSymptoms);
            log.info("========== AI分诊结束 ==========");
            return localResult;
        }

        long startTime = System.currentTimeMillis();
        String aiDepartment;
        try {
            aiDepartment = callAiForDepartment(safeSymptoms, age, gender);
            log.info("AI原始返回: {}", aiDepartment);
        } catch (Exception e) {
            log.error("AI分诊异常，使用fallback", e);
            aiDepartment = fallbackDepartmentRecommendation(safeSymptoms);
        }

        String normalized = normalizeDepartment(aiDepartment, safeSymptoms);
        long cost = System.currentTimeMillis() - startTime;

        log.info("标准化后科室: {}，耗时: {}ms", normalized, cost);
        log.info("========== AI分诊结束 ==========");

        return normalized;
    }

    /**
     * 本地规则优先匹配，覆盖常见明确症状，避免对AI的依赖
     */
    private String localDepartmentRecommend(String symptoms) {
        if (symptoms == null || symptoms.isBlank()) {
            return null;
        }
        String s = symptoms.toLowerCase();

        // 骨折、骨痛、关节痛、扭伤、腰痛 → 骨科
        if (s.contains("骨折") || s.contains("骨痛") || s.contains("关节痛") ||
            s.contains("扭伤") || s.contains("崴") || s.contains("挫伤") ||
            s.contains("拉伤") || s.contains("腰") || s.contains("骨裂")) {
            return "骨科";
        }
        // 胸痛伴气短、心悸、高血压 → 心内科
        if (s.contains("胸痛") && (s.contains("气短") || s.contains("胸闷"))) {
            return "心内科";
        }
        if (s.contains("心悸") || s.contains("高血压") || s.contains("心慌")) {
            return "心内科";
        }
        // 发热、咳嗽、呼吸困难、感冒、肺 → 呼吸内科
        if (s.contains("发热") || s.contains("发烧") || s.contains("高烧") ||
            s.contains("咳嗽") || s.contains("呼吸困难") || s.contains("气短") ||
            s.contains("感冒") || s.contains("新冠")) {
            return "呼吸内科";
        }
        // 头痛、头晕、面瘫、神经症状 → 神经内科
        if (s.contains("头痛") && !s.contains("发热")) {
            return "神经内科";
        }
        if (s.contains("头晕") || s.contains("面瘫") || s.contains("麻木")) {
            return "神经内科";
        }
        // 腹痛、腹泻、胃痛、便秘 → 消化内科
        if (s.contains("腹痛") || s.contains("腹泻") || s.contains("胃痛") ||
            s.contains("便秘") || s.contains("消化不良")) {
            return "消化内科";
        }
        // 儿童相关症状 → 儿科
        if (s.contains("儿") || s.contains("小孩") || s.contains("幼儿") || s.contains("儿童")) {
            return "儿科";
        }
        // 月经不调、痛经、妇科炎症 → 妇科
        if (s.contains("月经不调") || s.contains("痛经") || s.contains("妇科") ||
            s.contains("阴道") || s.contains("子宫") || s.contains("卵巢")) {
            return "妇科";
        }
        // 皮肤瘙痒、皮疹、皮肤病 → 皮肤科
        if (s.contains("皮疹") || s.contains("瘙痒") || s.contains("皮肤病") ||
            s.contains("湿疹") || s.contains("荨麻疹")) {
            return "皮肤科";
        }

        return null;
    }

    private String callAiForDepartment(String symptoms, Integer age, String gender) {
        String prompt = String.format(
                "你是医疗分诊专家。根据患者症状判断最合适的就诊科室。\n" +
                "患者信息：%d岁%s。\n" +
                "症状描述：%s\n\n" +
                "请从以下科室列表中选择一个最合适的，只返回科室名称，不要任何其他文字、标点或说明：\n" +
                "心内科、呼吸内科、神经内科、消化内科、骨科、儿科、普外科、内科、急诊科、妇科、产科、皮肤科、眼科、耳鼻喉科、口腔科、泌尿外科、胸外科、血液科、内分泌科、肿瘤科\n\n" +
                "分诊规则：\n" +
                "- 骨折、骨痛、关节痛、扭伤、腰痛 → 骨科\n" +
                "- 胸痛伴气短、心悸、高血压 → 心内科\n" +
                "- 发热、咳嗽、呼吸困难 → 呼吸内科（非紧急情况不去急诊）\n" +
                "- 头痛、头晕、面瘫 → 神经内科\n" +
                "- 腹痛、腹泻、胃痛 → 消化内科\n" +
                "- 月经不调、痛经、妇科问题 → 妇科\n" +
                "- 皮肤瘙痒、皮疹 → 皮肤科\n" +
                "- 儿童疾病 → 儿科\n" +
                "- 急诊科仅用于：急性大出血、昏迷、呼吸心跳骤停、急性过敏性休克、剧烈胸痛伴出汗等生命体征危急的情况\n" +
                "- 普通的发烧、感冒、咳嗽不要去急诊，去呼吸内科\n\n" +
                "只输出科室名称，例如：骨科",
                age, gender, symptoms
        );

        log.debug("发送AI请求，prompt长度: {}", prompt.length());
        String response = callDeepSeekApi(prompt);
        log.debug("AI原始响应: {}", response);
        return parseDepartmentFromResponse(response);
    }

    /**
     * 增强版症状分析：返回完整分析结果（置信度、追问、无意义输入检测）
     * @param isFollowUp 是否为追问后的最终分析（true=强制返回最终结果，不再追问）
     */
    public TriageAnalysisResult analyzeSymptomsForTriage(String symptoms, Integer age, String gender, boolean isFollowUp) {
        log.info("========== AI增强分诊开始 ==========");
        log.info("患者信息: {}岁{}，症状: {}，isFollowUp: {}", age, gender, symptoms, isFollowUp);

        // 检测无效输入（纯乱码、无意义字符）
        if (isInvalidInput(symptoms)) {
            log.info("检测到无效输入: {}", symptoms);
            TriageAnalysisResult result = new TriageAnalysisResult();
            result.setDepartment("内科");
            result.setReasoning("很抱歉，我没能完全理解您的描述。请您用更清晰的语言描述一下您的身体不适，比如'头痛'、'发烧'等。如需紧急帮助，请立即拨打120。");
            result.setConfidence(0);
            result.setNeedFollowUp(false);
            result.setInvalidInput(true);
            result.setFollowUpQuestions(Collections.singletonList("请用中文描述您的主要不适症状，如：头痛、发热、腹痛等"));
            return result;
        }

        String prompt = isFollowUp ? buildFinalPrompt(symptoms, age, gender) : buildEnhancedPrompt(symptoms, age, gender);
        log.debug("发送增强AI请求，prompt长度: {}, isFollowUp: {}", prompt.length(), isFollowUp);

        try {
            String response = callDeepSeekApi(prompt);
            TriageAnalysisResult result = parseEnhancedResponse(response, symptoms, isFollowUp);
            log.info("========== AI增强分诊结束 ==========");
            return result;
        } catch (Exception e) {
            log.error("AI增强分诊异常，使用fallback", e);
            return fallbackAnalysisResult(symptoms);
        }
    }

    /**
     * 重载方法，默认不是追问
     */
    public TriageAnalysisResult analyzeSymptomsForTriage(String symptoms, Integer age, String gender) {
        return analyzeSymptomsForTriage(symptoms, age, gender, false);
    }

    /**
     * 检测是否为无效输入（无意义乱码）
     */
    private boolean isInvalidInput(String symptoms) {
        if (symptoms == null || symptoms.isBlank()) return true;

        // 移除空格和常见标点
        String cleaned = symptoms.replaceAll("[\\s\\p{Punct}]", "");

        // 检测是否包含英文字母（正常中文症状可能包含少量英文，但全英文乱码概率大）
        boolean hasEnglishOnly = cleaned.matches("^[a-zA-Z]+$");

        // 检测纯数字或符号
        boolean isNumericOrPunctuation = cleaned.matches("^[0-9\\p{Punct}]+$");

        // 检测字符重复度（巴巴博一这种重复字符模式）
        boolean hasRepeatingPattern = cleaned.matches(".*(.)\\1{3,}.*");

        // 检测有意义的中文词（简单检测是否像中文）
        int chineseCharCount = 0;
        for (char c : cleaned.toCharArray()) {
            if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                chineseCharCount++;
            }
        }
        boolean hasLittleChinese = chineseCharCount < cleaned.length() * 0.3;

        // 综合判断
        if ((hasEnglishOnly || isNumericOrPunctuation || hasRepeatingPattern) && hasLittleChinese) {
            return true;
        }

        // 长度小于2的输入视为无效
        if (cleaned.length() < 2) return true;

        return false;
    }

    /**
     * 构建增强版Prompt（包含置信度和追问要求）
     */
    private String buildEnhancedPrompt(String symptoms, Integer age, String gender) {
        return String.format(
                "你是医疗分诊专家。根据患者症状判断最合适的就诊科室。\n\n" +
                "患者信息：%d岁%s\n" +
                "症状描述：%s\n\n" +
                "## 输出要求（严格按JSON格式返回，不要其他任何文字）：\n" +
                "{\n" +
                "  \"department\": \"推荐科室名称\",\n" +
                "  \"confidence\": 置信度数值(0-100整数),\n" +
                "  \"needFollowUp\": 是否需要追问(true/false),\n" +
                "  \"followUpQuestions\": [\"追问问题1\", \"追问问题2\"],\n" +
                "  \"reasoning\": \"简短诊断依据\"\n" +
                "}\n\n" +
                "## 科室列表：\n" +
                "心内科、呼吸内科、神经内科、消化内科、骨科、儿科、普外科、内科、妇科、产科、皮肤科、眼科、耳鼻喉科、口腔科、泌尿外科、胸外科、血液科、内分泌科、肿瘤科、急诊科\n\n" +
                "## 判断规则：\n" +
                "1. 症状模糊、涉及多系统、或无法准确判断时，设置 needFollowUp=true 并提供2-3个针对性追问问题\n" +
                "2. 症状明确且典型时，设置 needFollowUp=false，confidence=85-100\n" +
                "3. 症状涉及多科室时，推荐最可能的科室，confidence=60-80\n" +
                "4. 危急症状（如胸痛、大出血、呼吸困难、昏迷等）直接推荐\"急诊科\"\n" +
                "5. 追问问题示例：\"是上腹部还是下腹部疼痛？\"\"疼痛持续多长时间？\"\"有没有伴随发热？\"",
                age, gender, symptoms
        );
    }

    /**
     * 构建追问后的最终分析Prompt（强制返回最终结果，不再追问）
     */
    private String buildFinalPrompt(String symptoms, Integer age, String gender) {
        return String.format(
                "你是医疗分诊专家。根据患者补充的症状信息，给出最终分诊结论。\n\n" +
                "患者信息：%d岁%s\n" +
                "完整症状描述：%s\n\n" +
                "## 输出要求（严格按JSON格式返回，不要其他任何文字）：\n" +
                "{\n" +
                "  \"department\": \"推荐科室名称\",\n" +
                "  \"confidence\": 置信度数值(0-100整数)，根据已有信息给出最准确判断，60-100之间\n" +
                "  \"needFollowUp\": false，\n" +
                "  \"followUpQuestions\": []，\n" +
                "  \"reasoning\": \"简短诊断依据\"\n" +
                "}\n\n" +
                "## 科室列表：\n" +
                "心内科、呼吸内科、神经内科、消化内科、骨科、儿科、普外科、内科、妇科、产科、皮肤科、眼科、耳鼻喉科、口腔科、泌尿外科、胸外科、血液科、内分泌科、肿瘤科、急诊科\n\n" +
                "注意：这是最终分析，请根据患者补充的信息直接给出最合适的科室推荐，不要再追问。危急症状推荐急诊科。",
                age, gender, symptoms
        );
    }

    /**
     * 解析增强版AI响应
     * @param isFollowUp 是否为追问后的最终分析
     */
    private TriageAnalysisResult parseEnhancedResponse(String jsonResponse, String originalSymptoms, boolean isFollowUp) {
        TriageAnalysisResult result = new TriageAnalysisResult();
        result.setNeedFollowUp(false);
        result.setInvalidInput(false);
        result.setFollowUpQuestions(new ArrayList<>());

        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            String content = root.path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText();

            if (content == null || content.isBlank()) {
                content = root.path("choices")
                        .path(0)
                        .path("message")
                        .path("reasoning_content")
                        .asText();
            }

            if (content == null || content.isBlank()) {
                log.warn("AI响应为空，使用fallback");
                return fallbackAnalysisResult(originalSymptoms);
            }

            // 提取JSON（可能有markdown包装）
            content = content.trim();
            if (content.startsWith("```json")) {
                content = content.substring(7);
            }
            if (content.startsWith("```")) {
                content = content.substring(3);
            }
            if (content.endsWith("```")) {
                content = content.substring(0, content.length() - 3);
            }
            content = content.trim();

            JsonNode analysisNode = objectMapper.readTree(content);

            result.setDepartment(normalizeDepartment(
                    analysisNode.path("department").asText("内科"), originalSymptoms));

            result.setConfidence(analysisNode.path("confidence").asInt(75));

            // 追问后的最终分析，强制不追问
            if (isFollowUp) {
                result.setNeedFollowUp(false);
                result.setFollowUpQuestions(new ArrayList<>());
            } else {
                result.setNeedFollowUp(analysisNode.path("needFollowUp").asBoolean(false));
            }

            JsonNode questionsNode = analysisNode.path("followUpQuestions");
            if (questionsNode.isArray()) {
                List<String> questions = new ArrayList<>();
                for (JsonNode q : questionsNode) {
                    questions.add(q.asText());
                }
                result.setFollowUpQuestions(questions);
            }

            String reasoning = analysisNode.path("reasoning").asText("");
            if (reasoning.isBlank()) {
                reasoning = "根据您描述的症状[" + originalSymptoms + "]，推荐就诊" + result.getDepartment();
            }
            result.setReasoning(reasoning);

            log.debug("增强分析结果: department={}, confidence={}, needFollowUp={}",
                    result.getDepartment(), result.getConfidence(), result.getNeedFollowUp());

        } catch (Exception e) {
            log.warn("解析增强响应失败: {}，原始响应: {}", e.getMessage(), jsonResponse);
            return fallbackAnalysisResult(originalSymptoms);
        }

        return result;
    }

    /**
     * Fallback分析结果（当AI不可用时）
     */
    private TriageAnalysisResult fallbackAnalysisResult(String symptoms) {
        String dept = fallbackDepartmentRecommendation(symptoms);
        TriageAnalysisResult result = new TriageAnalysisResult();
        result.setDepartment(dept);
        result.setConfidence(50);
        result.setNeedFollowUp(false);
        result.setInvalidInput(false);
        result.setFollowUpQuestions(new ArrayList<>());
        result.setReasoning("根据您描述的症状[" + symptoms + "]，推荐就诊" + dept + "（AI服务暂时不可用）");
        return result;
    }

    private String callDeepSeekApi(String prompt) {
        String url = "https://api.deepseek.com/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        body.put("max_tokens", 1000);
        body.put("temperature", 0.1);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        long apiStart = System.currentTimeMillis();
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            long apiCost = System.currentTimeMillis() - apiStart;
            log.debug("API调用成功，耗时: {}ms", apiCost);

            if (responseEntity.getBody() != null) {
                log.trace("响应内容: {}", responseEntity.getBody());
            }
            return responseEntity.getBody();
        } catch (Exception e) {
            long apiCost = System.currentTimeMillis() - apiStart;
            log.error("API调用失败，耗时: {}ms，错误: {}", apiCost, e.getMessage());
            throw e;
        }
    }

    private String parseDepartmentFromResponse(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.isBlank()) {
            log.warn("AI响应为空，使用默认内科");
            return "内科";
        }
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            String content = root.path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText();

            if (content == null || content.isBlank()) {
                content = root.path("choices")
                        .path(0)
                        .path("message")
                        .path("reasoning_content")
                        .asText();
                log.debug("content为空，从reasoning_content读取");
            }

            if (content == null || content.isBlank()) {
                log.warn("AI响应content和reasoning_content均为空，使用默认内科");
                return "内科";
            }
            // 只提取中文科室名，移除所有空白、标点、换行
            String cleaned = content.replaceAll("[\\s\\n\\r\\t\"'，。、,.!?，。：:]", "").trim();
            // 再次检查是否包含注入内容
            if (INJECTION_PATTERN.matcher(cleaned).matches()) {
                log.warn("检测到潜在注入攻击，返回默认内科");
                return "内科";
            }
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

        // 注入检测
        if (INJECTION_PATTERN.matcher(cleaned).matches()) {
            log.warn("标准化后发现注入风险，返回fallback");
            return fallbackDepartmentRecommendation(symptoms);
        }

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

    public String generateMedicalRecord(String dialogueText) {
        log.info("========== AI病历生成开始 ==========");
        String safeText = sanitizeUserInput(dialogueText);
        log.info("对话记录长度: {} 字符", safeText.length());

        String prompt = String.format(
                "你是医疗病历生成专家。根据以下医患对话记录，生成结构化病历。\n" +
                "请以纯JSON格式返回，包含以下字段，禁止输出任何JSON以外的文字：\n" +
                "presentIllness（现病史）、pastHistory（既往史）、physicalExamination（体格检查）、diagnosis（初步诊断）、treatmentPlan（治疗意见）\n\n" +
                "对话记录：\n%s\n\n" +
                "输出格式示例：\n" +
                "{\"presentIllness\":\"...\",\"pastHistory\":\"...\",\"physicalExamination\":\"...\",\"diagnosis\":\"...\",\"treatmentPlan\":\"...\"}",
                safeText
        );

        long startTime = System.currentTimeMillis();
        try {
            String response = callDeepSeekApi(prompt);
            String content = parseJsonFromResponse(response);
            long cost = System.currentTimeMillis() - startTime;

            log.info("病历生成成功，耗时: {}ms", cost);
            log.debug("生成病历内容: {}", content);
            log.info("========== AI病历生成结束 ==========");
            return content;
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - startTime;
            log.error("病历生成失败，耗时: {}ms", cost, e);
            log.info("========== AI病历生成异常 ==========");
            return "{\"presentIllness\":\"\",\"pastHistory\":\"\",\"physicalExamination\":\"\",\"diagnosis\":\"AI服务暂时不可用\",\"treatmentPlan\":\"请手动填写\"}";
        }
    }

    public String checkPrescription(String medicineList, String patientInfo) {
        log.info("========== AI处方审核开始 ==========");
        String safeMedicine = sanitizeUserInput(medicineList);
        String safePatient = sanitizeUserInput(patientInfo);
        log.info("药品列表: {}", safeMedicine);
        log.info("患者信息: {}", safePatient);

        String prompt = String.format(
                "你是医疗处方审核专家。请审核以下处方并提供用药建议。\n\n" +
                "药品列表：\n%s\n\n" +
                "患者信息：\n%s\n\n" +
                "请提供JSON格式输出，包含：\n" +
                "suggestion（用药建议）、interactions（药物相互作用检测）、riskLevel（风险等级：low/medium/high）、warnings（风险提示数组）\n\n" +
                "输出格式：\n" +
                "{\"suggestion\":\"...\",\"interactions\":\"...\",\"riskLevel\":\"low\",\"warnings\":[]}",
                safeMedicine, safePatient
        );

        long startTime = System.currentTimeMillis();
        try {
            String response = callDeepSeekApi(prompt);
            String content = parseJsonFromResponse(response);
            long cost = System.currentTimeMillis() - startTime;

            log.info("处方审核成功，耗时: {}ms", cost);
            log.debug("审核结果: {}", content);
            log.info("========== AI处方审核结束 ==========");
            return content;
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - startTime;
            log.error("处方审核失败，耗时: {}ms", cost, e);
            log.info("========== AI处方审核异常 ==========");
            return "{\"suggestion\":\"AI服务暂时不可用，请手动审核\",\"interactions\":\"\",\"riskLevel\":\"high\",\"warnings\":[\"AI服务异常，建议人工审核\"]}";
        }
    }

    private String parseJsonFromResponse(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.isBlank()) {
            log.warn("AI响应为空");
            return jsonResponse;
        }
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            String content = root.path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText();

            if (content == null || content.isBlank()) {
                content = root.path("choices")
                        .path(0)
                        .path("message")
                        .path("reasoning_content")
                        .asText();
                log.debug("content为空，从reasoning_content读取");
            }

            if (content == null || content.isBlank()) {
                log.warn("AI响应content和reasoning_content均为空");
                return jsonResponse;
            }
            String cleaned = content.trim();
            // 移除 markdown 代码块包裹
            cleaned = cleaned.replaceAll("(?s)\\`\\`\\`json\\s*", "").replaceAll("(?s)\\s*\\`\\`\\`", "");
            cleaned = cleaned.replaceAll("(?s)\\`\\`\\s*", "").replaceAll("(?s)\\s*\\`\\`", "");
            log.debug("清洗后内容长度: {} 字符", cleaned.length());
            return cleaned;
        } catch (Exception e) {
            log.warn("解析AI响应失败: {}", e.getMessage());
            return jsonResponse;
        }
    }
}
