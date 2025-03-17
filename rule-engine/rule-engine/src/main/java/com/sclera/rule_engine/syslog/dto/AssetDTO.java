package com.sclera.rule_engine.syslog.dto;

import java.util.List;
import lombok.Data;

@Data
public class AssetDTO {

  private String type;
  private List<CategoryDTO> categories;
}
