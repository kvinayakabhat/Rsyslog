package com.sclera.rule_engine.syslog.dto;

import com.sclera.rule_engine.syslog.Severity;
import lombok.Data;

import java.util.List;

@Data
public class CategoryDetailDTO {
    private Long id;
    private String name;
    private int keyword_count;
    private Severity severity;
    private List<String> keywords;
}