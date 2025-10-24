package org.example.backend.service;

import java.util.Map;

public interface SearchService {
    public Map<String, Object> searchByKeyword(String keyword);
}
