package com.sclera.rule_engine.syslog.service;

import com.sclera.rule_engine.syslog.Severity;
import com.sclera.rule_engine.syslog.dto.AssetDTO;
import com.sclera.rule_engine.syslog.dto.AssetTypeSummaryDTO;
import com.sclera.rule_engine.syslog.dto.CategoryDTO;
import com.sclera.rule_engine.syslog.dto.CategoryDetailDTO;
import com.sclera.rule_engine.syslog.model.Asset;
import com.sclera.rule_engine.syslog.model.Category;
import com.sclera.rule_engine.syslog.repository.AssetRepository;
import com.sclera.rule_engine.syslog.repository.CategoryRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AssetService {

  private final AssetRepository assetRepository;
  private final CategoryRepository categoryRepository;


  public boolean createAsset(AssetDTO assetDTO) {
    try {
      Optional<Asset> checkTypeExists = assetRepository.findByType(assetDTO.getType());
      if(checkTypeExists.isPresent()){
        return false;
      }
      Asset asset = this.convertToEntity(assetDTO);
      assetRepository.save(asset);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return true;
  }

  public void deleteAsset(String type) {
    try {
      Optional<Asset> asset = assetRepository.findByType(type);
      if (asset.isPresent()) {
        assetRepository.deleteById(type);
      } else {
        System.out.println("Asset type not found!");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public List<AssetDTO> getAssetsByTypes(List<String> types) {
    List<Asset> assets = assetRepository.findByTypeIn(types);
    return assets.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
  }

  public List<AssetDTO> getAssets() {
    List<Asset> assets = assetRepository.findAll();
    return assets.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
  }

  //newly added getCategories
  public List<AssetDTO> getCategories() {
    List<Asset> categories = assetRepository.findAll();
    return categories.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
  }

  private Asset convertToEntity(AssetDTO dto) {
    Asset asset = new Asset();
    asset.setType(dto.getType());

    if (dto.getCategories() != null) {
      List<Category> categories = dto.getCategories().stream()
              .map(categoryDTO -> {
                Category category = new Category();
                category.setTitle(categoryDTO.getTitle());
                category.setSeverity(Severity.valueOf(categoryDTO.getSeverity()));
                category.setKeywords(categoryDTO.getKeywords());
                category.setAsset(asset);
                return category;
              })
              .collect(Collectors.toList());

      asset.setCategories(categories);
    }

    return asset;
  }

  private AssetDTO convertToDTO(Asset asset) {
    AssetDTO dto = new AssetDTO();
    dto.setType(asset.getType());

    if (asset.getCategories() != null) {
      List<CategoryDTO> categoryDTOs = asset.getCategories().stream()
              .map(category -> {
                CategoryDTO categoryDTO = new CategoryDTO();
                categoryDTO.setTitle(category.getTitle());
                categoryDTO.setSeverity(category.getSeverity().name());
                categoryDTO.setKeywords(category.getKeywords());
                return categoryDTO;
              })
              .collect(Collectors.toList());

      dto.setCategories(categoryDTOs);
    }
    return dto;
  }

  public void createCategory(String type, CategoryDTO categoryDTO) {
    try {
      Category category = new Category();
      category.setTitle(categoryDTO.getTitle());
      category.setSeverity(Severity.valueOf(categoryDTO.getSeverity()));
      category.setKeywords(categoryDTO.getKeywords());
      category.setAsset(assetRepository.getReferenceById(type));
      categoryRepository.save(category);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //delete category
  @Transactional
  public void deleteCategory(String type, Long categoryId) {
    try {
      Optional<Category> category = categoryRepository.findById(categoryId);
      if (category.isPresent()) {
        categoryRepository.deleteById(categoryId);
      } else {
        System.out.println("Category not found!");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public void createKeyword(String type, Long categoryId, List<String> keywords) {
    Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
    if (categoryOpt.isPresent() && keywords != null) {
      Category category = categoryOpt.get();
      category.getKeywords().addAll(keywords);
      categoryRepository.save(category);
    } else {
      throw new IllegalArgumentException("Category not found");
    }
  }

  @Transactional
  public void updateCategory(String type, Long categoryId, CategoryDTO categoryDTO) {
    Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
    if (categoryOpt.isPresent()) {
      categoryRepository.updateCategory(categoryId, categoryDTO.getSeverity(), categoryDTO.getTitle());
    } else {
      throw new IllegalArgumentException("Category not found");
    }
  }

  @Transactional
  public void updateKeyword(String type, Long categoryId, List<String> keywords) {
    Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
    if (categoryOpt.isPresent()) {
      Category category = categoryOpt.get();
      category.getKeywords().clear();
      category.getKeywords().addAll(keywords);
      categoryRepository.save(category);
    } else {
      throw new IllegalArgumentException("Category not found");
    }
  }

  ////service for the newly created AssetTypeSummaryDTO
//  public Page<AssetTypeSummaryDTO> getAssets(String search, int page, int size, String sort) {
//    Pageable pageable = PageRequest.of(page, size, Sort.by(sort.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "type"));
//    Page<Asset> assets = assetRepository.findByTypeContaining(search == null ? "" : search, pageable);
//
//    return assets.map(asset -> {
//      AssetTypeSummaryDTO summary = new AssetTypeSummaryDTO();
//      summary.setType(asset.getType());
//      summary.setCategories(asset.getCategories().stream()
//              .map(category -> {
//                CategoryDetailDTO categoryDetail = new CategoryDetailDTO();
//                categoryDetail.setId(category.getId());
//                categoryDetail.setName(category.getTitle());
//                categoryDetail.setCount(category.getKeywords().size());
//                return categoryDetail;
//              }).collect(Collectors.toList()));
//      return summary;
//    });
//  }

//  public List<CategoryDetailDTO> getCategories(String type, String search, String severity) {
//    List<Category> categories = categoryRepository.findByAssetTypeAndFilters(type, search, severity);
//    return categories.stream()
//            .map(category -> {
//              CategoryDetailDTO detail = new CategoryDetailDTO();
//              detail.setId(category.getId());
//              detail.setName(category.getTitle());
//              detail.setCount(category.getKeywords().size());
//              return detail;
//            }).collect(Collectors.toList());
//  }




  public Page<Asset> getAssetTypes(int page, int size, String sort,String search) {
    String[] sortParams = sort.split(",");
    String sortProperty = sortParams[0];
    Sort.Direction sortDirection = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;

    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortProperty));
    return assetRepository.findByTypeContaining(search,pageable);
//    return assetRepository.findAll(pageable);
  }




  //get categories endpoint: paginated, sorted alphabetically, returns id,name and count of keywords in it and the totalElements(count of categories)
  public Page<CategoryDetailDTO> getCategories(String type, String search, String severity, int page, int size, String sort) {
//  public List<CategoryDetailDTO> getCategories(String type,String search, String severity){
    String[] sortParams = sort.split(",");
    String newType = type.replace("_"," ");
    String sortProperty = sortParams[0];
    Sort.Direction sortDirection = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;

    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortProperty));
    log.info("type {} ",newType);
    // Convert severity string to enum
    Severity severityEnum = null;
    if (severity != null && !severity.isEmpty()) {
      try {
        severityEnum = Severity.valueOf(severity.toUpperCase()); // Convert to uppercase to match enum naming
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid severity value: " + severity);
      }
    }

    // Fetch categories with filters applied
    Page<Category> categoriesPage = categoryRepository.findByAssetTypeAndFilters(newType, search, severityEnum, pageable);
//    List<Category> categoriesPage = categoryRepository.findByAssetTypeAndFilters(newType, search, severityEnum);
//     Convert Category entities to CategoryDetailDTO
    return categoriesPage.map(category -> {
      CategoryDetailDTO detail = new CategoryDetailDTO();
      detail.setId(category.getId());
      detail.setName(category.getTitle());
      detail.setKeyword_count(category.getKeywords().size());
      detail.setSeverity(category.getSeverity());
      detail.setKeywords(category.getKeywords());
      return detail;
    });

    // Convert Category entities to CategoryDetailDTO
//    List<CategoryDetailDTO> categoryDetailDTOs = categoriesPage.stream()
//            .map(category -> {
//              CategoryDetailDTO detail = new CategoryDetailDTO();
//              detail.setId(category.getId());
//              detail.setName(category.getTitle());
//              detail.setKeyword_count(category.getKeywords().size());
//              detail.setSeverity(category.getSeverity());
//              detail.setKeywords(category.getKeywords());
//              return detail;
//            })
//            .collect(Collectors.toList());

// Return the processed list
//    return categoryDetailDTOs;
  }



  //service for newly created CategoryDetailDTO
//  public List<CategoryDetailDTO> getCategories(String type, String search, String severityString) {
//    // Convert the severityString to Severity enum
//    Severity severity = null;
//    if (severityString != null && !severityString.isEmpty()) {
//      try {
//        severity = Severity.valueOf(severityString.toUpperCase()); // Convert string to enum
//      } catch (IllegalArgumentException e) {
//        throw new IllegalArgumentException("Invalid severity value: " + severityString);
//      }
//    }

  // Fetch categories based on filters
//    List<Category> categories = categoryRepository.findByAssetTypeAndFilters(type, search, severity);
//
//    // Map categories to DTOs
//    return categories.stream()
//            .map(category -> {
//              CategoryDetailDTO detail = new CategoryDetailDTO();
//              detail.setId(category.getId());
//              detail.setName(category.getTitle());
//              detail.setCount(category.getKeywords().size());
//              return detail;
//            }).collect(Collectors.toList());
//  }


}