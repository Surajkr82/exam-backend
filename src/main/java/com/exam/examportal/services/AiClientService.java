package com.exam.examportal.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiClientService {

    @Value("${ai.base-url:https://api.sarvam.ai}")
    private String baseUrl;

    @Value("${ai.chat-completions-path:/v1/chat/completions}")
    private String chatCompletionsPath;

    @Value("${ai.model:sarvam-m}")
    private String defaultModel;

    @Value("${ai.auth.header:Authorization}")
    private String authHeader;

    @Value("${ai.auth.scheme:Bearer}")
    private String authScheme;

    @Value("${ai.api.key:}")
    private String configuredApiKey;

    public String generateText(String prompt, double temperature, int maxTokens, String overrideApiKey) {
        if (!hasText(prompt)) {
            throw new IllegalArgumentException("Prompt is required.");
        }

        String apiKey = resolveApiKey(overrideApiKey);
        String url = buildChatCompletionsUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        applyAuth(headers, apiKey);

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(message);

        Map<String, Object> body = new HashMap<>();
        body.put("model", defaultModel);
        body.put("messages", messages);
        body.put("temperature", temperature);
        body.put("max_tokens", maxTokens);

        RestTemplate restTemplate = new RestTemplate();

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            return extractTextContent(response);
        } catch (HttpStatusCodeException ex) {
            throw new AiProviderException(ex.getStatusCode(),
                    "AI provider returned " + ex.getStatusCode() + ": " + ex.getResponseBodyAsString(), ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to call AI provider: " + ex.getMessage(), ex);
        }
    }

    private String buildChatCompletionsUrl() {
        String normalizedBase = this.baseUrl == null ? "" : this.baseUrl.trim();
        if (normalizedBase.endsWith("/")) {
            normalizedBase = normalizedBase.substring(0, normalizedBase.length() - 1);
        }

        String normalizedPath = this.chatCompletionsPath == null ? "/v1/chat/completions"
                : this.chatCompletionsPath.trim();
        if (!normalizedPath.startsWith("/")) {
            normalizedPath = "/" + normalizedPath;
        }

        return normalizedBase + normalizedPath;
    }

    private void applyAuth(HttpHeaders headers, String apiKey) {
        String headerName = hasText(this.authHeader) ? this.authHeader.trim() : "Authorization";
        String scheme = hasText(this.authScheme) ? this.authScheme.trim() : "Bearer";

        if ("Authorization".equalsIgnoreCase(headerName)) {
            if ("none".equalsIgnoreCase(scheme)) {
                headers.set(headerName, apiKey);
            } else if ("Bearer".equalsIgnoreCase(scheme)) {
                headers.setBearerAuth(apiKey);
            } else {
                headers.set(headerName, scheme + " " + apiKey);
            }
            return;
        }

        if ("none".equalsIgnoreCase(scheme)) {
            headers.set(headerName, apiKey);
        } else {
            headers.set(headerName, scheme + " " + apiKey);
        }
    }

    private String resolveApiKey(String overrideApiKey) {
        if (hasText(overrideApiKey)) {
            return overrideApiKey.trim();
        }

        if (hasText(this.configuredApiKey)) {
            return this.configuredApiKey.trim();
        }

        String envServamKey = System.getenv("SERVAM_API_KEY");
        if (hasText(envServamKey)) {
            return envServamKey.trim();
        }

        String envOpenAiKey = System.getenv("OPENAI_API_KEY");
        if (hasText(envOpenAiKey)) {
            return envOpenAiKey.trim();
        }

        throw new IllegalStateException("AI API key missing. Set SERVAM_API_KEY (or OPENAI_API_KEY) or ai.api.key.");
    }

    private String extractTextContent(Map<String, Object> response) {
        if (response == null) {
            throw new IllegalStateException("AI provider returned an empty response.");
        }

        Object errorObj = response.get("error");
        if (errorObj instanceof Map) {
            Map<?, ?> errorMap = (Map<?, ?>) errorObj;
            Object message = errorMap.get("message");
            throw new IllegalStateException("AI provider error: " + (message != null ? message.toString() : errorMap));
        }

        Object choicesObj = response.get("choices");
        if (choicesObj instanceof List) {
            List<?> choices = (List<?>) choicesObj;
            if (!choices.isEmpty() && choices.get(0) instanceof Map) {
                Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);

                Object messageObj = firstChoice.get("message");
                if (messageObj instanceof Map) {
                    Map<?, ?> messageMap = (Map<?, ?>) messageObj;
                    Object contentObj = messageMap.get("content");

                    if (contentObj instanceof String && hasText((String) contentObj)) {
                        return (String) contentObj;
                    }

                    if (contentObj instanceof List) {
                        List<?> contentList = (List<?>) contentObj;
                        StringBuilder contentBuilder = new StringBuilder();
                        for (Object item : contentList) {
                            if (item instanceof Map) {
                                Object text = ((Map<?, ?>) item).get("text");
                                if (text != null) {
                                    contentBuilder.append(text.toString());
                                }
                            }
                        }
                        if (hasText(contentBuilder.toString())) {
                            return contentBuilder.toString();
                        }
                    }
                }

                Object textObj = firstChoice.get("text");
                if (textObj instanceof String && hasText((String) textObj)) {
                    return (String) textObj;
                }
            }
        }

        Object replyObj = response.get("reply");
        if (replyObj != null && hasText(replyObj.toString())) {
            return replyObj.toString();
        }

        throw new IllegalStateException("AI response did not contain text content.");
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}