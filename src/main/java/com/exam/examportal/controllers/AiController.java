package com.exam.examportal.controllers;

import com.exam.examportal.services.AiClientService;
import com.exam.examportal.services.AiProviderException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = { "http://localhost:4200", "http://127.0.0.1:4200" }, allowedHeaders = "*")
@RequestMapping(path = "/ai")
public class AiController {

    @Autowired
    private AiClientService aiClientService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(path = "/text")
    public ResponseEntity<?> generateText(@RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-AI-API-KEY", required = false) String overrideApiKey) {

        String prompt = asString(request.get("prompt"));
        if (!hasText(prompt)) {
            return badRequest("'prompt' is required.");
        }

        double temperature = asDouble(request.get("temperature"), 0.7);
        int maxTokens = asInt(request.get("maxTokens"), 1024);

        try {
            String text = this.aiClientService.generateText(prompt, temperature, maxTokens, overrideApiKey);
            Map<String, Object> response = new HashMap<>();
            response.put("text", text);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return upstreamError(ex);
        }
    }

    @PostMapping(path = "/mcq")
    public ResponseEntity<?> generateMcqQuestions(@RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-AI-API-KEY", required = false) String overrideApiKey) {

        String topic = asString(request.get("topic"));
        int count = Math.max(1, Math.min(20, asInt(request.get("count"), 5)));

        if (!hasText(topic)) {
            return badRequest("'topic' is required.");
        }

        String prompt = "Generate " + count + " multiple-choice questions about \"" + topic + "\".\n\n"
                + "INSTRUCTIONS:\n"
                + "1. Create exactly " + count + " questions.\n"
                + "2. Each question must have 4 options named option1 to option4.\n"
                + "3. Include only ONE correct answer.\n"
                + "4. Respond ONLY as a JSON array with this schema:\n"
                + "[{\"content\":\"...\",\"option1\":\"...\",\"option2\":\"...\",\"option3\":\"...\",\"option4\":\"...\",\"answer\":\"1\"}]\n"
                + "5. For 'answer', use only 1, 2, 3, or 4.\n"
                + "6. Do not add markdown fences or explanatory text.";

        try {
            String modelText = this.aiClientService.generateText(prompt, 0.4, 2048, overrideApiKey);
            List<Map<String, Object>> parsedQuestions = parseJsonArray(modelText);
            List<Map<String, Object>> normalizedQuestions = normalizeMcqQuestions(parsedQuestions);

            if (normalizedQuestions.isEmpty()) {
                return badGateway("AI generated response could not be parsed into valid MCQs.");
            }

            return ResponseEntity.ok(normalizedQuestions);
        } catch (Exception ex) {
            return upstreamError(ex);
        }
    }

    @PostMapping(path = "/subjective")
    public ResponseEntity<?> generateSubjectiveQuestions(@RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-AI-API-KEY", required = false) String overrideApiKey) {

        String topic = asString(request.get("topic"));
        int count = Math.max(1, Math.min(10, asInt(request.get("count"), 3)));

        if (!hasText(topic)) {
            return badRequest("'topic' is required.");
        }

        String prompt = "Act as an expert academic examiner. Generate " + count
                + " highly advanced subjective questions about \"" + topic + "\".\n\n"
                + "INSTRUCTIONS:\n"
                + "1. Return exactly " + count + " questions.\n"
                + "2. Questions must test conceptual depth and practical understanding.\n"
                + "3. Respond ONLY as JSON array:\n"
                + "[{\"content\":\"...\"}]\n"
                + "4. Do not include markdown fences or extra text.";

        try {
            String modelText = this.aiClientService.generateText(prompt, 0.5, 2048, overrideApiKey);
            List<Map<String, Object>> parsedQuestions = parseJsonArray(modelText);
            List<Map<String, Object>> normalizedQuestions = normalizeSubjectiveQuestions(parsedQuestions);

            if (normalizedQuestions.isEmpty()) {
                return badGateway("AI generated response could not be parsed into valid subjective questions.");
            }

            return ResponseEntity.ok(normalizedQuestions);
        } catch (Exception ex) {
            return upstreamError(ex);
        }
    }

    @PostMapping(path = "/agent-plan")
    public ResponseEntity<?> generateAgentPlan(@RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-AI-API-KEY", required = false) String overrideApiKey) {

        String goal = asString(request.get("goal"));
        String context = asString(request.get("context"));

        if (!hasText(goal)) {
            return badRequest("'goal' is required.");
        }

        String prompt = "You are an agentic planner for an exam portal system.\n"
                + "Goal: \"" + goal + "\"\n"
                + "Context: \"" + (hasText(context) ? context : "none") + "\"\n\n"
                + "Return ONLY JSON object with this exact schema:\n"
                + "{\"objective\":\"...\",\"steps\":[{\"title\":\"...\",\"description\":\"...\"}],\"risks\":[\"...\"]}";

        try {
            String modelText = this.aiClientService.generateText(prompt, 0.2, 1024, overrideApiKey);
            Map<String, Object> parsedPlan = parseJsonObject(modelText);
            return ResponseEntity.ok(parsedPlan);
        } catch (Exception ex) {
            return upstreamError(ex);
        }
    }

    private List<Map<String, Object>> parseJsonArray(String text) throws Exception {
        String cleaned = cleanModelText(text);

        int firstBracket = cleaned.indexOf('[');
        int lastBracket = cleaned.lastIndexOf(']');

        if (firstBracket < 0 || lastBracket <= firstBracket) {
            throw new IllegalArgumentException("AI response did not contain a JSON array.");
        }

        String jsonArray = cleaned.substring(firstBracket, lastBracket + 1);
        return objectMapper.readValue(jsonArray, new TypeReference<List<Map<String, Object>>>() {
        });
    }

    private Map<String, Object> parseJsonObject(String text) throws Exception {
        String cleaned = cleanModelText(text);

        int firstBrace = cleaned.indexOf('{');
        int lastBrace = cleaned.lastIndexOf('}');

        if (firstBrace < 0 || lastBrace <= firstBrace) {
            throw new IllegalArgumentException("AI response did not contain a JSON object.");
        }

        String jsonObject = cleaned.substring(firstBrace, lastBrace + 1);
        return objectMapper.readValue(jsonObject, new TypeReference<Map<String, Object>>() {
        });
    }

    private String cleanModelText(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("```json", "")
                .replace("```", "")
                .trim();
    }

    private List<Map<String, Object>> normalizeMcqQuestions(List<Map<String, Object>> questions) {
        List<Map<String, Object>> normalized = new ArrayList<>();

        for (Map<String, Object> raw : questions) {
            if (raw == null) {
                continue;
            }

            String content = asString(raw.get("content"));
            if (!hasText(content)) {
                content = asString(raw.get("question"));
            }

            String option1 = asString(raw.get("option1"));
            String option2 = asString(raw.get("option2"));
            String option3 = asString(raw.get("option3"));
            String option4 = asString(raw.get("option4"));

            if ((!hasText(option1) || !hasText(option2) || !hasText(option3) || !hasText(option4)) &&
                    raw.get("options") instanceof List) {
                List<?> options = (List<?>) raw.get("options");
                if (options.size() >= 4) {
                    option1 = asString(options.get(0));
                    option2 = asString(options.get(1));
                    option3 = asString(options.get(2));
                    option4 = asString(options.get(3));
                }
            }

            if (!hasText(content) || !hasText(option1) || !hasText(option2) || !hasText(option3) || !hasText(option4)) {
                continue;
            }

            String answer = normalizeAnswer(raw.get("answer"), option1, option2, option3, option4);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("content", content);
            item.put("option1", option1);
            item.put("option2", option2);
            item.put("option3", option3);
            item.put("option4", option4);
            item.put("answer", answer);
            normalized.add(item);
        }

        return normalized;
    }

    private List<Map<String, Object>> normalizeSubjectiveQuestions(List<Map<String, Object>> questions) {
        List<Map<String, Object>> normalized = new ArrayList<>();

        for (Map<String, Object> raw : questions) {
            if (raw == null) {
                continue;
            }

            String content = asString(raw.get("content"));
            if (!hasText(content)) {
                content = asString(raw.get("question"));
            }

            if (!hasText(content)) {
                continue;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("content", content);
            item.put("type", "SUBJECTIVE");
            normalized.add(item);
        }

        return normalized;
    }

    private String normalizeAnswer(Object answerObj, String option1, String option2, String option3, String option4) {
        String answer = asString(answerObj);
        if (!hasText(answer)) {
            return "1";
        }

        String trimmed = answer.trim();

        if (trimmed.matches("^[1-4]$")) {
            return trimmed;
        }

        if (trimmed.matches("^[A-Da-d]$")) {
            return String.valueOf(Character.toUpperCase(trimmed.charAt(0)) - 'A' + 1);
        }

        if (trimmed.equalsIgnoreCase(option1)) {
            return "1";
        }
        if (trimmed.equalsIgnoreCase(option2)) {
            return "2";
        }
        if (trimmed.equalsIgnoreCase(option3)) {
            return "3";
        }
        if (trimmed.equalsIgnoreCase(option4)) {
            return "4";
        }

        return "1";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private int asInt(Object value, int fallback) {
        if (value == null) {
            return fallback;
        }

        try {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(value.toString());
        } catch (Exception ex) {
            return fallback;
        }
    }

    private double asDouble(Object value, double fallback) {
        if (value == null) {
            return fallback;
        }

        try {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            return Double.parseDouble(value.toString());
        } catch (Exception ex) {
            return fallback;
        }
    }

    private ResponseEntity<Map<String, Object>> badRequest(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        return ResponseEntity.badRequest().body(error);
    }

    private ResponseEntity<Map<String, Object>> badGateway(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    private ResponseEntity<Map<String, Object>> upstreamError(Exception ex) {
        if (ex instanceof AiProviderException) {
            AiProviderException providerException = (AiProviderException) ex;
            HttpStatus providerStatus = providerException.getStatus();
            HttpStatus responseStatus = providerStatus.is4xxClientError() ? providerStatus : HttpStatus.BAD_GATEWAY;

            Map<String, Object> providerError = new HashMap<>();
            providerError.put("error", "AI provider request failed.");
            providerError.put("detail", providerException.getMessage());
            providerError.put("providerStatus", providerStatus.value());
            return ResponseEntity.status(responseStatus).body(providerError);
        }

        Map<String, Object> error = new HashMap<>();
        error.put("error", "AI provider request failed.");
        error.put("detail", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }
}