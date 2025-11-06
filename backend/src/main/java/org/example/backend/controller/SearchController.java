package org.example.backend.controller;

import jakarta.annotation.Resource;
import org.example.backend.dto.Result;
import org.example.backend.service.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/patient/search")
public class SearchController {

    @Resource
    private SearchService searchService;

    @GetMapping
    public Result<?> search(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.error("请输入搜索关键字");
        }

        Map<String, Object> result = searchService.searchByKeyword(keyword.trim());
        return Result.success(result);
    }
}

