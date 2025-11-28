package org.example.backend.dto;

import lombok.Data;

@Data
public class ConsultationResponse {
    private String reply;
    private String report; // Optional, populated when consultation is finished
    private String recommendedDepartment; // Optional
}
