package org.example.backend.service;

import org.example.backend.dto.ConsultationRequest;
import org.example.backend.dto.ConsultationResponse;

public interface AIConsultationService {
    ConsultationResponse chat(ConsultationRequest request);
}
