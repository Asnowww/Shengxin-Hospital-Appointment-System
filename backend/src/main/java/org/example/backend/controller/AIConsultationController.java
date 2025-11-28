package org.example.backend.controller;

import org.example.backend.dto.ConsultationRequest;
import org.example.backend.dto.ConsultationResponse;
import org.example.backend.service.AIConsultationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consultation")
@CrossOrigin(originPatterns = "*") // Allow CORS for development
public class AIConsultationController {

    @Autowired
    private AIConsultationService aiConsultationService;

    @PostMapping("/chat")
    public ResponseEntity<ConsultationResponse> chat(@RequestBody ConsultationRequest request) {
        ConsultationResponse response = aiConsultationService.chat(request);
        return ResponseEntity.ok(response);
    }
}
