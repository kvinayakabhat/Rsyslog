package com.sclera.rule_engine.syslog.repository;

import com.sclera.rule_engine.syslog.Severity;
import com.sclera.rule_engine.syslog.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  @Transactional
  @Modifying
  @Query(value = "UPDATE categories SET title=?3, severity=?2 WHERE id=?1", nativeQuery = true)
  void updateCategory(Long categoryId, String severity, String title);

//  @Query("SELECT c FROM Category c WHERE c.asset.type = :type " +
//          "AND (:search IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%'))) " +
//          "AND (:severity IS NULL OR c.severity = :severity)")
//  List<Category> findByAssetTypeAndFilters(@Param("type") String type,
//                                           @Param("search") String search,
//                                           @Param("severity") Severity severity);


  //
  @Query("SELECT c FROM Category c WHERE c.asset.type = :type " +
          "AND (:search IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%'))) " +
          "AND (:severity IS NULL OR c.severity = :severity)")
  Page<Category> findByAssetTypeAndFilters(
          @Param("type") String type,
          @Param("search") String search,
          @Param("severity") Severity severity,
          Pageable pageable);

//  List<Category> findByAssetTypeAndFilters(
//          @Param("type") String type,
//          @Param("search") String search,
//          @Param("severity") Severity severity);


}
