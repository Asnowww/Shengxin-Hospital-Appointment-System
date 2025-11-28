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

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ConsultationResponse chat(ConsultationRequest request) {
        try {
            // Construct the request body for Gemini API
            ObjectNode requestBody = objectMapper.createObjectNode();
            ArrayNode contents = requestBody.putArray("contents");

            // Add system instruction (as the first user message or system instruction if
            // supported)
            // Gemini 1.5/2.0 supports system instructions, but for simplicity we can
            // prepend it to the history or use a specific field.
            // Let's use a simple approach: Prepend a system prompt to the conversation
            // history if it's the start,
            // or just rely on the context.
            // Better: Add a system instruction in the "system_instruction" field if using
            // v1beta API with 1.5/2.0 models.
            // However, to be safe and simple with standard generateContent, we can just
            // guide the model in the prompt.

            String systemPrompt = "You are a helpful medical consultation assistant for a hospital appointment system. "
                    +
                    "Your goal is to collect symptom information from the patient and provide a preliminary analysis. "
                    +
                    "Ask follow-up questions if the information is insufficient. " +
                    "If you have enough information, provide a summary report and recommend a hospital department. " +
                    "Format your final response with a JSON block at the end if you are ready to give a report, like this: "
                    +
                    "```json\n{\"report\": \"...\", \"department\": \"...\"}\n``` " +
                    "Otherwise, just chat normally.";

            // Add history
            if (request.getHistory() != null) {
                for (ConsultationRequest.MessageHistory msg : request.getHistory()) {
                    ObjectNode part = contents.addObject();
                    part.put("role", msg.getRole());
                    part.putArray("parts").addObject().put("text", msg.getContent());
                }
            }

            // Add current user message
            ObjectNode userMessage = contents.addObject();
            userMessage.put("role", "user");
            ObjectNode parts = userMessage.putArray("parts").addObject();
            // Prepend system prompt to the latest message if history is empty, or just
            // append it?
            // Actually, for the API, we should send the system prompt as a separate system
            // instruction or just part of the first message.
            // Let's just prepend it to the current message text for simplicity if history
            // is empty,
            // or assume the model remembers if we send full history.
            // But wait, we are sending full history every time (stateless backend).
            // So we should probably inject the system prompt at the beginning of the
            // history.

            // Let's reconstruct the contents array properly.
            contents.removeAll(); // Clear what we added

            // Add system prompt as a 'user' message at the very beginning if we want to
            // enforce behavior,
            // or use the 'system_instruction' field if the API version supports it.
            // Let's try adding it as the first user message.
            ObjectNode systemPart = contents.addObject();
            systemPart.put("role", "user");
            systemPart.putArray("parts").addObject().put("text", systemPrompt);

            ObjectNode modelAck = contents.addObject();
            modelAck.put("role", "model");
            modelAck.putArray("parts").addObject().put("text", "Understood. I am ready to help.");

            if (request.getHistory() != null) {
                for (ConsultationRequest.MessageHistory msg : request.getHistory()) {
                    ObjectNode part = contents.addObject();
                    part.put("role", msg.getRole());
                    part.putArray("parts").addObject().put("text", msg.getContent());
                }
            }

            ObjectNode currentUserMsg = contents.addObject();
            currentUserMsg.put("role", "user");
            currentUserMsg.putArray("parts").addObject().put("text", request.getMessage());

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // API key is usually passed as query param ?key=... which is already in apiUrl

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

            // Execute request
            String finalUrl = apiUrl + apiKey;
            ResponseEntity<String> response = restTemplate.postForEntity(finalUrl, entity, String.class);

            // Parse response
            JsonNode root = objectMapper.readTree(response.getBody());
            String text = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

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
            errorResponse.setReply("Sorry, I encountered an error processing your request. Please try again.");
            return errorResponse;
        }
    }
}
