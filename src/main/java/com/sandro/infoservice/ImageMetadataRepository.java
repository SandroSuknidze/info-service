package com.sandro.infoservice;

import com.sandro.infoservice.model.ImageMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageMetadataRepository extends JpaRepository<ImageMetadata, String> {
  Optional<ImageMetadata> findByName(String name);

}

