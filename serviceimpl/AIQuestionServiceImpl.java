package com.rabbiter.oes.serviceimpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rabbiter.oes.entity.*;
import com.rabbiter.oes.mapper.AiQuestionRecordMapper;
import com.rabbiter.oes.service.AIQuestionService;
import com.rabbiter.oes.service.FillQuestionService;
import com.rabbiter.oes.service.JudgeQuestionService;
import com.rabbiter.oes.service.MultiQuestionService;
import com.rabbiter.oes.service.SubjectiveQuestionService;
import com.rabbiter.oes.util.DocumentParser;
import com.rabbiter.oes.util.ReferenceSparkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * AI出题服务实现类
 */
@Service
public class AIQuestionServiceImpl implements AIQuestionService {

    @Autowired
    private ReferenceSparkClient referenceSparkClient;

    @Autowired
    private DocumentParser documentParser;

    @Autowired
    private MultiQuestionService multiQuestionService;

    @Autowired
    private FillQuestionService fillQuestionService;

    @Autowired
    private JudgeQuestionService judgeQuestionService;

    @Autowired
    private SubjectiveQuestionService subjectiveQuestionService;

    @Autowired
    private AiQuestionRecordMapper aiQuestionRecordMapper;

    // 讯飞星火配置 - 从配置文件读取
    @Value("${spark.app.id}")
    private String appId;

    @Value("${spark.api.secret}")
    private String apiSecret;

    @Value("${spark.api.key}")
    private String apiKey;

    @Value("${spark.host.url:wss://spark-api.xf-yun.com/v4.0/chat}")
    private String hostUrl;

    @Value("${spark.domain:4.0Ultra}")
    private String domain;

    private static final int MAX_DOCUMENT_LENGTH = 8000; // 文档内容最大长度

    @Override
    public AIQuestionResponse generateQuestionsByPrompt(AIQuestionRequest request) {
        try {
            SparkConfig config = new SparkConfig(appId, apiSecret, apiKey, hostUrl, domain);
            String systemPrompt = buildSystemPrompt(request);
            String userPrompt = buildUserPrompt(request);

            String aiResponse;
            try {
                // 使用参考文档的客户端实现
                System.out.println("📚 使用参考文档的WebSocket客户端...");
                aiResponse = referenceSparkClient.generateContent(config, systemPrompt, userPrompt);
                System.out.println("✅ 参考客户端成功生成内容");
            } catch (Exception e) {
                System.err.println("❌ 参考客户端失败: " + e.getMessage());
                // 降级到 Mock 数据
                System.out.println("⚠️ 启用 Mock 降级模式生成题目...");
                aiResponse = generateMockResponse(request);
            }

            if (aiResponse == null || aiResponse.trim().isEmpty()) {
                System.out.println("⚠️ AI响应为空，启用 Mock 降级模式...");
                aiResponse = generateMockResponse(request);
            }

            // === 重要：显示AI生成的具体内容 ===
            System.out.println("🤖🤖🤖 AI生成的完整内容 🤖🤖🤖");
            System.out.println("=== 开始 ===");
            System.out.println(aiResponse);
            System.out.println("=== 结束 ===");
            System.out.println("内容长度: " + aiResponse.length());

            // 清理内容
            String cleanResponse = cleanAIResponse(aiResponse);
            System.out.println("🧹🧹🧹 清理后的内容 🧹🧹🧹");
            System.out.println(cleanResponse);

            List<Object> questions = parseAIResponse(cleanResponse, request.getQuestionType(), request);

            // 如果JSON解析失败，尝试从文本中提取题目
            if (questions.isEmpty()) {
                System.out.println("💡 JSON解析失败，尝试文本解析...");
                questions = parseTextResponse(aiResponse, request.getQuestionType(), request);
            }

            if (questions.isEmpty()) {
                System.err.println("❌❌❌ 题目解析失败 ❌❌❌");
                System.err.println("原始内容: " + aiResponse);
                System.err.println("清理后内容: " + cleanResponse);
                return new AIQuestionResponse(false, "AI生成的内容无法解析为题目格式，请重试");
            }

            System.out.println("🎉 题目生成流程完成，返回 " + questions.size() + " 道题目");

            // 保存AI出题记录
            try {
                AiQuestionRecord record = new AiQuestionRecord();
                record.setSubject(request.getSubject());
                record.setQuestionType(request.getQuestionType());
                record.setQuestionCount(questions.size());
                record.setDifficulty(request.getDifficulty());
                record.setSavedCount(0);
                record.setQuestionsJson(JSON.toJSONString(questions));
                aiQuestionRecordMapper.add(record);
                System.out.println("✅ AI出题记录已保存，ID: " + record.getId());
            } catch (Exception e) {
                System.err.println("⚠️ 保存AI出题记录失败: " + e.getMessage());
            }

            return new AIQuestionResponse(true, "题目生成成功", questions);

        } catch (Exception e) {
            return new AIQuestionResponse(false, "生成题目失败: " + e.getMessage());
        }
    }

    @Override
    public AIQuestionResponse generateQuestionsByDocument(MultipartFile file, AIQuestionRequest request) {
        try {
            // 解析文档内容
            String documentContent = documentParser.parseDocument(file);

            // 限制内容长度
            documentContent = documentParser.limitContent(documentContent, MAX_DOCUMENT_LENGTH);

            // 设置文档内容到请求中
            request.setDocumentContent(documentContent);

            SparkConfig config = new SparkConfig(appId, apiSecret, apiKey, hostUrl, domain);
            String systemPrompt = buildSystemPromptForDocument(request);
            String userPrompt = buildUserPromptForDocument(request);

            String aiResponse;
            try {
                // 使用参考文档的客户端实现
                System.out.println("📚 使用参考文档的WebSocket客户端...");
                aiResponse = referenceSparkClient.generateContent(config, systemPrompt, userPrompt);
                System.out.println("✅ 参考客户端成功生成内容");
            } catch (Exception e) {
                System.err.println("❌ 参考客户端失败: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("讯飞星火API调用失败。错误详情: " + e.getMessage() +
                        "\n\n请检查:" +
                        "\n1. API密钥配置是否正确（appId、apiSecret、apiKey）" +
                        "\n2. 网络连接是否正常" +
                        "\n3. 防火墙是否允许WebSocket连接" +
                        "\n4. 讯飞星火服务是否正常", e);
            }

            if (aiResponse == null || aiResponse.trim().isEmpty()) {
                return new AIQuestionResponse(false, "AI生成内容为空");
            }

            List<Object> questions = parseAIResponse(aiResponse, request.getQuestionType(), request);

            if (questions.isEmpty()) {
                return new AIQuestionResponse(false, "未能从AI响应中解析出有效题目");
            }

            return new AIQuestionResponse(true, "基于文档生成题目成功", questions);

        } catch (Exception e) {
            return new AIQuestionResponse(false, "基于文档生成题目失败: " + e.getMessage());
        }
    }

    @Override
    public boolean saveQuestionsToDatabase(List<Object> questions, String questionType) {
        try {
            System.out.println("🗄️ 开始保存题目到数据库，题目数量: " + questions.size() + "，类型: " + questionType);

            int savedCount = 0;
            for (Object questionObj : questions) {
                try {
                    switch (questionType.toLowerCase()) {
                        case "multiple":
                            MultiQuestion multiQuestion = convertToMultiQuestion(questionObj);
                            if (multiQuestion != null) {
                                // 确保设置AI来源标识
                                multiQuestion.setSource("ai");
                                multiQuestionService.add(multiQuestion);
                                savedCount++;
                                System.out.println("✅ 保存选择题成功: " + multiQuestion.getQuestion().substring(0,
                                        Math.min(20, multiQuestion.getQuestion().length())) + "...");
                            } else {
                                System.err.println("❌ 选择题对象转换失败");
                            }
                            break;
                        case "fill":
                            FillQuestion fillQuestion = convertToFillQuestion(questionObj);
                            if (fillQuestion != null) {
                                // 确保设置AI来源标识
                                fillQuestion.setSource("ai");
                                fillQuestionService.add(fillQuestion);
                                savedCount++;
                                System.out.println("✅ 保存填空题成功: " + fillQuestion.getQuestion().substring(0,
                                        Math.min(20, fillQuestion.getQuestion().length())) + "...");
                            } else {
                                System.err.println("❌ 填空题对象转换失败");
                            }
                            break;
                        case "judge":
                            JudgeQuestion judgeQuestion = convertToJudgeQuestion(questionObj);
                            if (judgeQuestion != null) {
                                // 确保设置AI来源标识
                                judgeQuestion.setSource("ai");
                                judgeQuestionService.add(judgeQuestion);
                                savedCount++;
                                System.out.println("✅ 保存判断题成功: " + judgeQuestion.getQuestion().substring(0,
                                        Math.min(20, judgeQuestion.getQuestion().length())) + "...");
                            } else {
                                System.err.println("❌ 判断题对象转换失败");
                            }
                            break;
                        case "subjective":
                            SubjectiveQuestion subjectiveQuestion = convertToSubjectiveQuestion(questionObj);
                            if (subjectiveQuestion != null) {
                                // 确保设置AI来源标识
                                subjectiveQuestion.setSource("ai");
                                subjectiveQuestionService.add(subjectiveQuestion);
                                savedCount++;
                                System.out
                                        .println("✅ 保存主观题成功: "
                                                + subjectiveQuestion.getQuestion().substring(0,
                                                        Math.min(20, subjectiveQuestion.getQuestion().length()))
                                                + "...");
                            } else {
                                System.err.println("❌ 主观题对象转换失败");
                            }
                            break;
                        default:
                            System.err.println("❌ 不支持的题目类型: " + questionType);
                            return false;
                    }
                } catch (Exception e) {
                    System.err.println("❌ 保存单个题目失败: " + e.getMessage());
                    e.printStackTrace();
                    // 继续保存其他题目，不要因为一个题目失败就全部失败
                }
            }

            System.out.println("🎉 题目保存完成，成功保存: " + savedCount + "/" + questions.size() + " 道题目");
            return savedCount > 0; // 只要有一道题目保存成功就返回true
        } catch (Exception e) {
            System.err.println("❌ 保存题目到数据库失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 转换Map对象为MultiQuestion实体
     */
    private MultiQuestion convertToMultiQuestion(Object obj) {
        try {
            if (obj instanceof MultiQuestion) {
                return (MultiQuestion) obj;
            } else if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                MultiQuestion question = new MultiQuestion();
                question.setQuestion((String) map.get("question"));

                // 处理选项字段映射：AI生成optionA等，需要映射到answerA等
                question.setAnswerA((String) map.get("optionA"));
                question.setAnswerB((String) map.get("optionB"));
                question.setAnswerC((String) map.get("optionC"));
                question.setAnswerD((String) map.get("optionD"));

                // 如果AI生成的是answerA等字段，也要支持
                if (question.getAnswerA() == null)
                    question.setAnswerA((String) map.get("answerA"));
                if (question.getAnswerB() == null)
                    question.setAnswerB((String) map.get("answerB"));
                if (question.getAnswerC() == null)
                    question.setAnswerC((String) map.get("answerC"));
                if (question.getAnswerD() == null)
                    question.setAnswerD((String) map.get("answerD"));

                // 处理正确答案字段映射：AI生成answer，需要映射到rightAnswer
                String rightAnswer = (String) map.get("answer");
                if (rightAnswer == null) {
                    rightAnswer = (String) map.get("rightAnswer");
                }
                question.setRightAnswer(rightAnswer);

                question.setAnalysis((String) map.get("analysis"));
                question.setSubject((String) map.get("subject"));
                question.setSection((String) map.get("section"));
                question.setLevel((String) map.get("level"));

                Object scoreObj = map.get("score");
                if (scoreObj != null) {
                    question.setScore(
                            scoreObj instanceof Integer ? (Integer) scoreObj : Integer.parseInt(scoreObj.toString()));
                }

                return question;
            }
        } catch (Exception e) {
            System.err.println("❌ 转换选择题对象失败: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 转换Map对象为FillQuestion实体
     */
    private FillQuestion convertToFillQuestion(Object obj) {
        try {
            if (obj instanceof FillQuestion) {
                return (FillQuestion) obj;
            } else if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                FillQuestion question = new FillQuestion();
                question.setQuestion((String) map.get("question"));
                question.setAnswer((String) map.get("answer"));
                question.setAnalysis((String) map.get("analysis"));
                question.setSubject((String) map.get("subject"));
                question.setSection((String) map.get("section"));
                question.setLevel((String) map.get("level"));

                Object scoreObj = map.get("score");
                if (scoreObj != null) {
                    question.setScore(
                            scoreObj instanceof Integer ? (Integer) scoreObj : Integer.parseInt(scoreObj.toString()));
                }

                return question;
            }
        } catch (Exception e) {
            System.err.println("❌ 转换填空题对象失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 转换Map对象为JudgeQuestion实体
     */
    private JudgeQuestion convertToJudgeQuestion(Object obj) {
        try {
            if (obj instanceof JudgeQuestion) {
                return (JudgeQuestion) obj;
            } else if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                JudgeQuestion question = new JudgeQuestion();
                question.setQuestion((String) map.get("question"));
                question.setAnswer((String) map.get("answer"));
                question.setAnalysis((String) map.get("analysis"));
                question.setSubject((String) map.get("subject"));
                question.setSection((String) map.get("section"));
                question.setLevel((String) map.get("level"));

                Object scoreObj = map.get("score");
                if (scoreObj != null) {
                    question.setScore(
                            scoreObj instanceof Integer ? (Integer) scoreObj : Integer.parseInt(scoreObj.toString()));
                }

                return question;
            }
        } catch (Exception e) {
            System.err.println("❌ 转换判断题对象失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 转换Map对象为SubjectiveQuestion实体
     */
    private SubjectiveQuestion convertToSubjectiveQuestion(Object obj) {
        try {
            System.out.println("🔍 转换主观题对象，类型: " + (obj != null ? obj.getClass().getName() : "null"));

            if (obj instanceof SubjectiveQuestion) {
                return (SubjectiveQuestion) obj;
            } else if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                System.out.println("🔍 Map内容: " + map);

                SubjectiveQuestion question = new SubjectiveQuestion();
                question.setQuestion((String) map.get("question"));

                // AI生成的字段是answer，需要映射到referenceAnswer
                String answer = (String) map.get("answer");
                if (answer == null) {
                    answer = (String) map.get("referenceAnswer");
                }
                question.setReferenceAnswer(answer);

                question.setAnalysis((String) map.get("analysis"));
                question.setSubject((String) map.get("subject"));
                question.setSection((String) map.get("section"));
                question.setLevel((String) map.get("level"));

                // 处理分值
                Object scoreObj = map.get("score");
                if (scoreObj != null) {
                    if (scoreObj instanceof Integer) {
                        question.setScore((Integer) scoreObj);
                    } else if (scoreObj instanceof Number) {
                        question.setScore(((Number) scoreObj).intValue());
                    } else {
                        question.setScore(Integer.parseInt(scoreObj.toString()));
                    }
                } else {
                    question.setScore(10); // 主观题默认10分
                }

                System.out.println("✅ 转换成功: question="
                        + question.getQuestion().substring(0, Math.min(20, question.getQuestion().length())) + "...");
                return question;
            }
            System.err.println("❌ 无法转换的对象类型: " + (obj != null ? obj.getClass().getName() : "null"));
        } catch (Exception e) {
            System.err.println("❌ 转换主观题对象失败: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<String> getSupportedSubjects() {
        return Arrays.asList(
                "计算机网络", "数据结构", "Java程序设计", "高等数学",
                "数据库理论", "操作系统", "软件工程", "离散数学",
                "C语言", "Python程序设计", "Web开发", "人工智能");
    }

    @Override
    public List<String> getSupportedQuestionTypes() {
        return Arrays.asList("multiple", "fill", "judge", "subjective");
    }

    @Override
    public boolean validateQuestionFormat(Object question, String questionType) {
        if (question == null)
            return false;

        switch (questionType.toLowerCase()) {
            case "multiple":
                return question instanceof MultiQuestion;
            case "fill":
                return question instanceof FillQuestion;
            case "judge":
                return question instanceof JudgeQuestion;
            default:
                return false;
        }
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(AIQuestionRequest request) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是").append(request.getSubject()).append("题目生成器。\n\n");

        prompt.append("**严格要求**：\n");
        prompt.append("1. 必须输出标准JSON格式\n");
        prompt.append("2. 禁止输出任何文字说明或解释\n");
        prompt.append("3. 禁止使用markdown标记\n");
        prompt.append("4. 直接输出JSON，从{开始，到}结束\n");
        prompt.append("5. 不要添加'以下是题目'等任何前言\n\n");

        prompt.append("**必须严格按照以下JSON格式输出**：\n");

        if ("multiple".equals(request.getQuestionType())) {
            prompt.append("{\n");
            prompt.append("  \"questions\": [\n");
            prompt.append("    {\n");
            prompt.append("      \"question\": \"题目内容\",\n");
            prompt.append("      \"optionA\": \"选项A内容\",\n");
            prompt.append("      \"optionB\": \"选项B内容\",\n");
            prompt.append("      \"optionC\": \"选项C内容\",\n");
            prompt.append("      \"optionD\": \"选项D内容\",\n");
            prompt.append("      \"answer\": \"A\",\n");
            prompt.append("      \"analysis\": \"答案解析\"\n");
            prompt.append("    }\n");
            prompt.append("  ]\n");
            prompt.append("}\n");
            prompt.append("\n**选项要求**：\n");
            prompt.append("- optionA/B/C/D必须包含具体选项内容，不能为空\n");
            prompt.append("- answer字段只填入字母（A/B/C/D），不要包含选项内容\n");
            prompt.append("- 每个选项都要有实际的内容，不能留空\n");
        } else if ("fill".equals(request.getQuestionType())) {
            prompt.append("{\n");
            prompt.append("  \"questions\": [\n");
            prompt.append("    {\n");
            prompt.append("      \"question\": \"题目内容用______表示空白\",\n");
            prompt.append("      \"answer\": \"答案\",\n");
            prompt.append("      \"analysis\": \"解析\"\n");
            prompt.append("    }\n");
            prompt.append("  ]\n");
            prompt.append("}\n");
        } else if ("judge".equals(request.getQuestionType())) {
            prompt.append("{\n");
            prompt.append("  \"questions\": [\n");
            prompt.append("    {\n");
            prompt.append("      \"question\": \"判断题内容\",\n");
            prompt.append("      \"answer\": \"T\",\n");
            prompt.append("      \"analysis\": \"解析\"\n");
            prompt.append("    }\n");
            prompt.append("  ]\n");
            prompt.append("}\n");
        } else if ("subjective".equals(request.getQuestionType())) {
            prompt.append("{\n");
            prompt.append("  \"questions\": [\n");
            prompt.append("    {\n");
            prompt.append("      \"question\": \"主观题题目内容\",\n");
            prompt.append("      \"answer\": \"参考答案，要详细完整\",\n");
            prompt.append("      \"score\": 10,\n");
            prompt.append("      \"analysis\": \"评分要点和答题思路\"\n");
            prompt.append("    }\n");
            prompt.append("  ]\n");
            prompt.append("}\n");
            prompt.append("\n**主观题要求**：\n");
            prompt.append("- 题目要有深度，考查理解和分析能力\n");
            prompt.append("- 参考答案要完整详细\n");
            prompt.append("- 评分要点要清晰明确\n");
        }

        prompt.append("\n**生成要求**：\n");
        prompt.append("- 数量：").append(request.getQuestionCount()).append("道");
        prompt.append(getQuestionTypeName(request.getQuestionType())).append("\n");
        prompt.append("- 难度：").append(getDifficultyName(request.getDifficulty())).append("\n");

        if (request.getChapter() != null && !request.getChapter().trim().isEmpty()) {
            prompt.append("- 章节：").append(request.getChapter()).append("\n");
        }

        prompt.append("\n**再次提醒**：只输出JSON，不要任何其他文字！");

        return prompt.toString();
    }

    /**
     * 获取题目类型中文名称
     */
    private String getQuestionTypeName(String questionType) {
        switch (questionType.toLowerCase()) {
            case "multiple":
                return "选择题";
            case "fill":
                return "填空题";
            case "judge":
                return "判断题";
            case "subjective":
                return "主观题";
            default:
                return "未知题型";
        }
    }

    /**
     * 获取难度中文名称
     */
    private String getDifficultyName(String difficulty) {
        if (difficulty == null || difficulty.trim().isEmpty()) {
            return "中等";
        }
        switch (difficulty) {
            case "1":
                return "简单";
            case "2":
                return "中等";
            case "3":
                return "困难";
            default:
                return "中等";
        }
    }

    /**
     * 构建基于文档的系统提示词
     */
    private String buildSystemPromptForDocument(AIQuestionRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的题目生成专家。请根据提供的文档内容，为").append(request.getSubject()).append("科目生成");

        switch (request.getQuestionType().toLowerCase()) {
            case "multiple":
                prompt.append("选择题");
                break;
            case "fill":
                prompt.append("填空题");
                break;
            case "judge":
                prompt.append("判断题");
                break;
            case "subjective":
                prompt.append("主观题");
                break;
        }

        prompt.append("。题目必须基于文档内容，不能脱离文档范围。");

        prompt.append("\n\n**严格要求**：\n");
        prompt.append("1. 必须输出标准JSON格式\n");
        prompt.append("2. 禁止输出任何文字说明或解释\n");
        prompt.append("3. 禁止使用markdown标记\n");
        prompt.append("4. 直接输出JSON，从{开始，到}结束\n");
        prompt.append("5. 不要添加'以下是题目'等任何前言\n\n");

        prompt.append("**必须严格按照以下JSON格式输出**：\n");
        prompt.append(getQuestionFormatExample(request.getQuestionType()));

        return prompt.toString();
    }

    /**
     * 构建用户提示词
     */
    private String buildUserPrompt(AIQuestionRequest request) {
        StringBuilder prompt = new StringBuilder();

        if (request.getPrompt() != null && !request.getPrompt().trim().isEmpty()) {
            prompt.append("题目要求：").append(request.getPrompt()).append("\n");
        }

        if (request.getRequirements() != null && !request.getRequirements().trim().isEmpty()) {
            prompt.append("其他要求：").append(request.getRequirements()).append("\n");
        }

        prompt.append("立即输出JSON格式，从{开始。不要任何说明文字！");

        return prompt.toString();
    }

    /**
     * 构建基于文档的用户提示词
     */
    private String buildUserPromptForDocument(AIQuestionRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("基于以下文档内容，生成").append(request.getQuestionCount()).append("道");
        prompt.append(request.getSubject()).append("题目：\n\n");
        prompt.append("文档内容：\n").append(request.getDocumentContent());

        if (request.getPrompt() != null && !request.getPrompt().trim().isEmpty()) {
            prompt.append("\n\n具体要求：").append(request.getPrompt());
        }

        return prompt.toString();
    }

    /**
     * 获取题目格式示例（与提示词出题保持一致的字段格式）
     */
    private String getQuestionFormatExample(String questionType) {
        switch (questionType.toLowerCase()) {
            case "multiple":
                return "{\n" +
                        "  \"questions\": [\n" +
                        "    {\n" +
                        "      \"question\": \"题目内容\",\n" +
                        "      \"optionA\": \"选项A内容\",\n" +
                        "      \"optionB\": \"选项B内容\",\n" +
                        "      \"optionC\": \"选项C内容\",\n" +
                        "      \"optionD\": \"选项D内容\",\n" +
                        "      \"answer\": \"A\",\n" +
                        "      \"analysis\": \"答案解析\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}\n\n" +
                        "**选项要求**：\n" +
                        "- optionA/B/C/D必须包含具体选项内容，不能为空\n" +
                        "- answer字段只填入字母（A/B/C/D），不要包含选项内容\n" +
                        "- 每个选项都要有实际的内容，不能留空";
            case "fill":
                return "{\n" +
                        "  \"questions\": [\n" +
                        "    {\n" +
                        "      \"question\": \"题目内容用______表示空白\",\n" +
                        "      \"answer\": \"答案\",\n" +
                        "      \"analysis\": \"解析\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}";
            case "judge":
                return "{\n" +
                        "  \"questions\": [\n" +
                        "    {\n" +
                        "      \"question\": \"判断题内容\",\n" +
                        "      \"answer\": \"T\",\n" +
                        "      \"analysis\": \"解析\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}";
            case "subjective":
                return "{\n" +
                        "  \"questions\": [\n" +
                        "    {\n" +
                        "      \"question\": \"主观题题目内容\",\n" +
                        "      \"answer\": \"参考答案，要详细完整\",\n" +
                        "      \"score\": 10,\n" +
                        "      \"analysis\": \"评分要点和答题思路\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}\n\n" +
                        "**主观题要求**：\n" +
                        "- 题目要有深度，考查理解和分析能力\n" +
                        "- 参考答案要完整详细\n" +
                        "- 评分要点要清晰明确";
            default:
                return "[]";
        }
    }

    /**
     * 清理AI响应内容，提取JSON部分
     */
    private String cleanAIResponse(String aiResponse) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return "";
        }

        String cleaned = aiResponse.trim();

        // 移除markdown标记
        cleaned = cleaned.replaceAll("```json\\s*", "");
        cleaned = cleaned.replaceAll("```\\s*", "");

        // 查找JSON开始和结束位置
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');

        if (start >= 0 && end > start) {
            cleaned = cleaned.substring(start, end + 1);
        }

        return cleaned.trim();
    }

    /**
     * 解析AI响应生成的题目
     */
    private List<Object> parseAIResponse(String aiResponse, String questionType, AIQuestionRequest request) {
        List<Object> questions = new ArrayList<>();

        // 优先尝试解析为包含questions数组的JSON对象（标准格式）
        try {
            JSONObject jsonObject = JSON.parseObject(aiResponse);
            if (jsonObject != null && jsonObject.containsKey("questions")) {
                JSONArray questionsArray = jsonObject.getJSONArray("questions");
                if (questionsArray != null && !questionsArray.isEmpty()) {
                    System.out.println("🔍 解析JSON对象中的questions数组成功，数量: " + questionsArray.size());
                    return parseJSONQuestions(questionsArray, questionType, request);
                }
            }
        } catch (Exception e) {
            // 继续尝试其他格式，不打印错误信息
        }

        // 尝试直接解析为JSON数组（兼容格式）
        try {
            JSONArray jsonArray = JSON.parseArray(aiResponse);
            if (jsonArray != null && !jsonArray.isEmpty()) {
                System.out.println("🔍 直接解析JSON数组成功，数量: " + jsonArray.size());
                return parseJSONQuestions(jsonArray, questionType, request);
            }
        } catch (Exception e) {
            // 继续尝试其他格式，不打印错误信息
        }

        // 尝试从文本中提取JSON
        try {
            String extractedJson = extractJSONFromText(aiResponse);
            if (extractedJson != null) {
                JSONArray jsonArray = JSON.parseArray(extractedJson);
                if (jsonArray != null) {
                    return parseJSONQuestions(jsonArray, questionType, request);
                }
            }
        } catch (Exception e) {
            System.err.println("解析AI响应失败: " + e.getMessage());
        }

        return questions;
    }

    /**
     * 从JSON数组解析题目
     */
    private List<Object> parseJSONQuestions(JSONArray jsonArray, String questionType, AIQuestionRequest request) {
        List<Object> questions = new ArrayList<>();

        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject questionJson = jsonArray.getJSONObject(i);

            switch (questionType.toLowerCase()) {
                case "multiple":
                    MultiQuestion multiQuestion = parseMultipleQuestion(questionJson, request);
                    if (multiQuestion != null) {
                        questions.add(multiQuestion);
                    }
                    break;
                case "fill":
                    FillQuestion fillQuestion = parseFillQuestion(questionJson, request);
                    if (fillQuestion != null) {
                        questions.add(fillQuestion);
                    }
                    break;
                case "judge":
                    JudgeQuestion judgeQuestion = parseJudgeQuestion(questionJson, request);
                    if (judgeQuestion != null) {
                        questions.add(judgeQuestion);
                    }
                    break;
                case "subjective":
                    // 主观题使用Map存储，因为没有对应的实体类
                    java.util.Map<String, Object> subjectiveQuestion = parseSubjectiveQuestion(questionJson, request);
                    if (subjectiveQuestion != null) {
                        questions.add(subjectiveQuestion);
                    }
                    break;
            }
        }

        return questions;
    }

    /**
     * 从文本格式解析题目（备用方案）
     */
    private List<Object> parseTextResponse(String textResponse, String questionType, AIQuestionRequest request) {
        List<Object> questions = new ArrayList<>();

        try {
            System.out.println("🔍 尝试文本解析...");

            if ("judge".equals(questionType)) {
                // 解析判断题文本
                String[] lines = textResponse.split("\n");

                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i].trim();

                    // 查找题目行 (包含数字. 或 **题目**)
                    if (line.matches("^\\d+\\..*") || (line.contains("**") && !line.contains("解析"))) {
                        JudgeQuestion question = new JudgeQuestion();

                        // 提取题目内容
                        String questionText = line.replaceAll("^\\d+\\.\\s*", "")
                                .replaceAll("\\*\\*", "")
                                .replaceAll("（.*?）", "")
                                .trim();
                        question.setQuestion(questionText);

                        // 提取答案
                        String answer = "T"; // 默认正确
                        if (line.contains("错误") || line.contains("false") || line.contains("False")) {
                            answer = "F";
                        }
                        question.setAnswer(answer);

                        // 查找解析
                        String analysis = "";
                        for (int j = i + 1; j < lines.length && j < i + 5; j++) {
                            if (lines[j].contains("解析") || lines[j].contains("**解析**")) {
                                analysis = lines[j].replaceAll(".*解析.*?：", "")
                                        .replaceAll("\\*\\*", "")
                                        .trim();
                                break;
                            }
                        }
                        question.setAnalysis(analysis.isEmpty() ? "解析内容" : analysis);

                        question.setScore(5);
                        question.setLevel("2");

                        // 设置必要的字段
                        question.setSubject(request.getSubject()); // 使用请求中的真实科目
                        question.setSection(request.getChapter() != null ? request.getChapter() : "AI智能出题");

                        questions.add(question);

                        System.out.println(
                                "✅ 解析文本判断题: " + questionText.substring(0, Math.min(30, questionText.length())) + "...");
                    }
                }
            }

            // TODO: 可以继续添加选择题和填空题的文本解析逻辑

        } catch (Exception e) {
            System.err.println("❌ 文本解析也失败: " + e.getMessage());
        }

        return questions;
    }

    /**
     * 解析选择题
     */
    private MultiQuestion parseMultipleQuestion(JSONObject json, AIQuestionRequest request) {
        try {
            MultiQuestion question = new MultiQuestion();
            question.setQuestion(json.getString("question"));

            // AI生成的字段名是optionA,optionB,optionC,optionD，需要映射到answerA等字段
            question.setAnswerA(json.getString("optionA"));
            question.setAnswerB(json.getString("optionB"));
            question.setAnswerC(json.getString("optionC"));
            question.setAnswerD(json.getString("optionD"));

            // AI生成的正确答案字段是answer，需要映射到rightAnswer
            question.setRightAnswer(json.getString("answer"));
            question.setAnalysis(json.getString("analysis"));
            question.setScore(2); // 默认2分
            question.setLevel(request.getDifficulty() != null ? request.getDifficulty() : "2"); // 使用请求中的难度

            // 设置必要的字段
            question.setSubject(request.getSubject()); // 使用请求中的真实科目
            question.setSection(request.getChapter() != null ? request.getChapter() : "AI智能出题");

            return question;
        } catch (Exception e) {
            System.err.println("解析选择题失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解析填空题
     */
    private FillQuestion parseFillQuestion(JSONObject json, AIQuestionRequest request) {
        try {
            FillQuestion question = new FillQuestion();
            question.setQuestion(json.getString("question"));
            question.setAnswer(json.getString("answer"));
            question.setAnalysis(json.getString("analysis"));
            question.setScore(2); // 默认2分
            question.setLevel(request.getDifficulty() != null ? request.getDifficulty() : "2"); // 使用请求中的难度

            // 设置必要的字段
            question.setSubject(request.getSubject()); // 使用请求中的真实科目
            question.setSection(request.getChapter() != null ? request.getChapter() : "AI智能出题");

            return question;
        } catch (Exception e) {
            System.err.println("解析填空题失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 解析判断题
     */
    private JudgeQuestion parseJudgeQuestion(JSONObject json, AIQuestionRequest request) {
        try {
            JudgeQuestion question = new JudgeQuestion();
            question.setQuestion(json.getString("question"));
            question.setAnswer(json.getString("answer"));
            question.setAnalysis(json.getString("analysis"));
            question.setScore(2); // 默认2分
            question.setLevel(request.getDifficulty() != null ? request.getDifficulty() : "2"); // 使用请求中的难度

            // 设置必要的字段
            question.setSubject(request.getSubject()); // 使用请求中的真实科目
            question.setSection(request.getChapter() != null ? request.getChapter() : "AI智能出题");

            return question;
        } catch (Exception e) {
            System.err.println("解析判断题失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 解析主观题
     */
    private java.util.Map<String, Object> parseSubjectiveQuestion(JSONObject json, AIQuestionRequest request) {
        try {
            java.util.Map<String, Object> question = new java.util.HashMap<>();
            question.put("question", json.getString("question"));
            question.put("answer", json.getString("answer"));
            question.put("analysis", json.getString("analysis"));

            // 处理分值
            Integer score = json.getInteger("score");
            question.put("score", score != null ? score : 10); // 默认10分

            question.put("level", request.getDifficulty() != null ? request.getDifficulty() : "2");
            question.put("subject", request.getSubject());
            question.put("section", request.getChapter() != null ? request.getChapter() : "AI智能出题");

            return question;
        } catch (Exception e) {
            System.err.println("解析主观题失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 从文本中提取JSON
     */
    private String extractJSONFromText(String text) {
        // 寻找JSON数组的开始和结束
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');

        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1);
        }

        return null;
    }

    /**
     * 生成 Mock 响应 JSON 字符串
     */
    private String generateMockResponse(AIQuestionRequest request) {
        List<Object> mocks = generateMockQuestionObjects(request);
        JSONObject json = new JSONObject();
        json.put("questions", mocks);
        return json.toJSONString();
    }

    /**
     * 直接生成 Mock 题目对象列表
     */
    private List<Object> generateMockQuestionObjects(AIQuestionRequest request) {
        List<Object> list = new ArrayList<>();
        int count = request.getQuestionCount() != null ? request.getQuestionCount() : 5;
        String type = request.getQuestionType();
        String subject = request.getSubject();

        for (int i = 1; i <= count; i++) {
            if ("multiple".equalsIgnoreCase(type)) {
                MultiQuestion q = new MultiQuestion();
                q.setQuestion("【模拟生成的" + subject + "选择题 " + i + "】 下列关于" + subject + "的描述正确的是？");
                q.setAnswerA(subject + "是一个很重要的学科");
                q.setAnswerB(subject + "没有任何用处");
                q.setAnswerC(subject + "很难学习");
                q.setAnswerD(subject + "已经过时了");
                q.setRightAnswer("A");
                q.setAnalysis("这是模拟生成的解析：显然A是正确的。");
                q.setScore(2);
                q.setSection("模拟章节");
                q.setLevel("2");
                list.add(q);
            } else if ("fill".equalsIgnoreCase(type)) {
                FillQuestion q = new FillQuestion();
                q.setQuestion("【模拟生成的" + subject + "填空题 " + i + "】 " + subject + "的核心概念是______。");
                q.setAnswer("模拟答案");
                q.setAnalysis("这是模拟生成的解析。");
                q.setScore(2);
                q.setSection("模拟章节");
                q.setLevel("2");
                list.add(q);
            } else if ("judge".equalsIgnoreCase(type)) {
                JudgeQuestion q = new JudgeQuestion();
                q.setQuestion("【模拟生成的" + subject + "判断题 " + i + "】 " + subject + "是计算机科学的基础学科。");
                q.setAnswer("T");
                q.setAnalysis("这是模拟生成的解析。");
                q.setScore(2);
                q.setSection("模拟章节");
                q.setLevel("2");
                list.add(q);
            } else if ("subjective".equalsIgnoreCase(type)) {
                SubjectiveQuestion q = new SubjectiveQuestion();
                q.setQuestion("【模拟生成的" + subject + "主观题 " + i + "】 请简述" + subject + "的主要应用领域。");
                q.setReferenceAnswer("这是模拟生成的参考答案：\n1. 应用领域一\n2. 应用领域二\n3. 应用领域三");
                q.setScore(10);
                q.setAnalysis("包含核心关键词即可得分。");
                q.setSection("模拟章节");
                q.setLevel("2");
                list.add(q);
            }
        }
        return list;
    }
}
