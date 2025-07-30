package com.sandro.infoservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageAnalytics {

  private Long id;
  private Long viewCount;
  private Long downloadCount;
  private Instant lastViewTimestamp;
  private Instant lastDownloadTimestamp;
  private Instant createdAt;
  private Instant updatedAt;
}

