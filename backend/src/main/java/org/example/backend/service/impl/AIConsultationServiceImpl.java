package org.example.backend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.backend.dto.ConsultationRequest;
import org.example.backend.dto.ConsultationResponse;
import org.example.backend.service.AIConsultationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AIConsultationServiceImpl implements AIConsultationService {

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.api.model}")
    private String apiModel;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ConsultationResponse chat(ConsultationRequest request) {
        try {
            // Construct the request body for DeepSeek API (OpenAI compatible)
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", apiModel);
            requestBody.put("stream", false);
            ArrayNode messages = requestBody.putArray("messages");

            String systemPrompt = "You are a helpful medical consultation assistant for a hospital appointment system. "
                    + "Your goal is to collect symptom information from the patient and provide a preliminary analysis. "
                    + "Ask follow-up questions if the information is insufficient. " +
                    "If you have enough information, provide a summary report and recommend a hospital department. " +
                    "Format your final response with a JSON block at the end if you are ready to give a report, like this: "
                    + "```json\n{\"report\": \"...\", \"department\": \"...\"}\n``` " +
                    "Otherwise, just chat normally.";

            // Add system message
            ObjectNode systemMessage = messages.addObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);

            // Add history
            if (request.getHistory() != null) {
                for (ConsultationRequest.MessageHistory msg : request.getHistory()) {
                    ObjectNode part = messages.addObject();
                    // Map "model" role to "assistant" for OpenAI format if necessary, though
                    // deepseek might handle 'model' or 'assistant'.
                    // OpenAI uses 'user', 'assistant', 'system'.
                    String role = "model".equals(msg.getRole()) ? "assistant" : msg.getRole();
                    part.put("role", role);
                    part.put("content", msg.getContent());
                }
            }

            // Add current user message
            ObjectNode userMessage = messages.addObject();
            userMessage.put("role", "user");
            userMessage.put("content", request.getMessage());

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

            // Execute request
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            // Parse response
            JsonNode root = objectMapper.readTree(response.getBody());
            // OpenAI format: choices[0].message.content
            String text = root.path("choices").get(0).path("message").path("content").asText();

            ConsultationResponse consultationResponse = new ConsultationResponse();

            // Check for JSON block in the response
            if (text.contains("```json")) {
                int start = text.indexOf("```json") + 7;
                int end = text.lastIndexOf("```");
                if (end > start) {
                    String jsonStr = text.substring(start, end).trim();
                    try {
                        JsonNode jsonReport = objectMapper.readTree(jsonStr);
                        consultationResponse.setReport(jsonReport.path("report").asText(null));
                        consultationResponse.setRecommendedDepartment(jsonReport.path("department").asText(null));
                        // Remove the JSON block from the reply text to show to user
                        text = text.substring(0, text.indexOf("```json")).trim();
                    } catch (Exception e) {
                        // Ignore parsing error, just return text
                    }
                }
            }

            consultationResponse.setReply(text);
            return consultationResponse;

        } catch (Exception e) {
            e.printStackTrace();
            ConsultationResponse errorResponse = new ConsultationResponse();
            errorResponse.setReply("Sorry, I encountered an error processing your request. Please try again. Error: "
                    + e.getMessage());
            return errorResponse;
        }
    }
}
