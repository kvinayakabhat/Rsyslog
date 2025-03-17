package com.sclera.rule_engine.syslog.repository;

import com.sclera.rule_engine.syslog.model.Asset;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssetRepository extends JpaRepository<Asset, String> {

  @Query("SELECT a FROM Asset a WHERE a.type IN :types")
  List<Asset> findByTypeIn(List<String> types);

  Optional<Asset> findByType(String type);


//  @Query("SELECT a FROM Asset a WHERE LOWER(a.type) LIKE LOWER(CONCAT('%', :search, '%'))")
  @Query("SELECT a FROM Asset a WHERE :search IS NULL OR LOWER(a.type) LIKE LOWER(CONCAT('%', :search, '%'))")
  Page<Asset> findByTypeContaining(@Param("search") String search, Pageable pageable);

}
