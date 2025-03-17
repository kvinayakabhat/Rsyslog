package com.sclera.rule_engine.syslog.dto;

import java.util.List;

import com.sclera.rule_engine.syslog.model.Asset;
import lombok.Data;

@Data
public class CategoryDTO {
  private String title;
  private String severity;
  private List<String> keywords;
  private Asset asset;
}