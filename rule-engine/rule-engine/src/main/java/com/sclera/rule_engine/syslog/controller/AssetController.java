package com.sclera.rule_engine.syslog.controller;

import com.sclera.rule_engine.syslog.dto.AssetDTO;
import com.sclera.rule_engine.syslog.dto.AssetTypeSummaryDTO;
import com.sclera.rule_engine.syslog.dto.CategoryDTO;
import com.sclera.rule_engine.syslog.dto.CategoryDetailDTO;
import com.sclera.rule_engine.syslog.model.Asset;
import com.sclera.rule_engine.syslog.service.AssetService;

//import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;   // dont import any other .Page
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
@CrossOrigin(value = "*", origins = "*")

public class AssetController {

  private final  AssetService assetService;

  @PostMapping
  public ResponseEntity<String> createAsset(@RequestBody AssetDTO assetDTO) {
    if(assetService.createAsset(assetDTO)){
      return ResponseEntity.ok().build();
    }else{
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body("Cannot add asset that already exists.");
    }
  }

  @DeleteMapping("/type/{type}")
  public ResponseEntity<Void> deleteAsset(@PathVariable String type) {
    assetService.deleteAsset(type);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/types")
  public ResponseEntity<List<AssetDTO>> getAssetByType(@RequestBody List<String> types) {
    return ResponseEntity.ok(assetService.getAssetsByTypes(types));
  }

  @GetMapping("/all")
  public ResponseEntity<List<AssetDTO>> getAssets() {
    return ResponseEntity.ok(assetService.getAssets());
  }

  //to create a category
  @PostMapping("/type/{type}/category")
  public ResponseEntity<Void> createCategory(@PathVariable String type, @RequestBody CategoryDTO categoryDTO) {
    assetService.createCategory(type, categoryDTO);
    return ResponseEntity.ok().build();
  }

  //delete category api
  @DeleteMapping("/type/{type}/category/{categoryId}")
  public ResponseEntity<Void> deleteCategory(@PathVariable String type, @PathVariable Long categoryId) {
    assetService.deleteCategory(type, categoryId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/type/{type}/category/{categoryId}/keyword")
  public ResponseEntity<Void> createKeyword(@PathVariable String type, @PathVariable Long categoryId, @RequestBody List<String> keywords) {
    assetService.createKeyword(type, categoryId, keywords);
    return ResponseEntity.ok().build();
  }

  @PutMapping("/type/{type}/category/{categoryId}")
  public ResponseEntity<Void> updateCategory(@PathVariable String type, @PathVariable Long categoryId, @RequestBody CategoryDTO categoryDTO) {
    assetService.updateCategory(type, categoryId, categoryDTO);
    return ResponseEntity.ok().build();
  }

  @PutMapping("/type/{type}/category/{categoryId}/keyword")
  public ResponseEntity<Void> updateKeyword(@PathVariable String type, @PathVariable Long categoryId, @RequestBody List<String> keywords) {
    assetService.updateKeyword(type, categoryId, keywords);
    return ResponseEntity.ok().build();
  }

//to just get the asset types
//  @GetMapping("/all")
//  public ResponseEntity<Page<AssetTypeSummaryDTO>> getAssets(
//          @RequestParam(required = false) String search, //search
//          @RequestParam(defaultValue = "0") int page, //pagination
//          @RequestParam(defaultValue = "10") int size, //size
//          @RequestParam(defaultValue = "asc") String sort) { //for sorting
//    return ResponseEntity.ok(assetService.getAssets(search, page, size, sort));
//  }

//  @GetMapping("/type/{type}/categories")
//  public ResponseEntity<List<CategoryDetailDTO>> getCategories(
//          @PathVariable String type,
//          @RequestParam(required = false) String search,// search
//          @RequestParam(required = false) String severity) {
//    return ResponseEntity.ok(assetService.getCategories(type, search, severity));
//  }

  @GetMapping("/type/{type}/categories")
  public ResponseEntity<Map<String, Object>> getCategories(
          @PathVariable String type,
          @RequestParam(required = false) String search,
          @RequestParam(required = false) String severity,
          @RequestParam(value = "pageNumber",defaultValue = "0") int pageNumber,
          @RequestParam(value = "pageSize",defaultValue = "3000") int pageSize,
          @RequestParam(value = "sort", defaultValue = "title,asc") String sort) {
         System.out.println("search "+search);

    Page<CategoryDetailDTO> categoriesPage = assetService.getCategories(type, search, severity, pageNumber, pageSize, sort);
//  List<CategoryDetailDTO> categoriesPage = assetService.getCategories(type,search, severity);

    //  the response
//    for(CategoryDetailDTO d:categoriesPage) {
//      Map<String, Object> response = new LinkedHashMap<>();
//      response.put("categories", d.getContent());
//      response.put("count", categoriesPage.getTotalElements());
//      response.put("totalPages", categoriesPage.getTotalPages());
//      response.put("pageNumber", categoriesPage.getNumber());
//      response.put("pageSize", categoriesPage.getSize());
//      response.put("sorted", categoriesPage.getSort().isSorted());
//      response.put("first", categoriesPage.isFirst());
//      response.put("last", categoriesPage.isLast());
//    }

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("categories", categoriesPage.getContent());
    response.put("count", categoriesPage.getTotalElements());
    response.put("totalPages", categoriesPage.getTotalPages());
//    response.put("pageNumber", categoriesPage.getNumber());
//    response.put("pageSize", categoriesPage.getSize());
    response.put("sorted", categoriesPage.getSort().isSorted());
    response.put("first", categoriesPage.isFirst());
    response.put("last", categoriesPage.isLast());

    return ResponseEntity.ok(response);
  }

  // GEt-Assets endpoint : sorted alphabetically, paginated and count of all the assets
  @GetMapping("/assets")
  public ResponseEntity<Map<String, Object>> getAssetTypes(
          @RequestParam(value = "search",required=false) String search,
          @RequestParam(value = "pageNumber") int pageNumber,
          @RequestParam(value = "pageSize")int pageSize,
          @RequestParam(value = "sort", defaultValue = "type,asc") String sort) {
    System.out.println(search + "search");
    pageNumber=pageNumber-1;

    Page<Asset> assetsPage = assetService.getAssetTypes(pageNumber, pageSize, sort,search);

    // Map the data to a simplified response
    List<String> assetTypes = assetsPage.getContent().stream()
            .map(Asset::getType)
            .collect(Collectors.toList());

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("assetTypes", assetTypes);
    response.put("count", assetsPage.getTotalElements());
    response.put("totalPages", assetsPage.getTotalPages());
    response.put("pageNumber", assetsPage.getNumber());
    response.put("pageSize", assetsPage.getSize());
    response.put("sorted", assetsPage.getSort().isSorted());
    response.put("first", assetsPage.isFirst());
    response.put("last", assetsPage.isLast());

    return ResponseEntity.ok(response);
  }

}