package com.sandro.infoservice.service;

import com.sandro.infoservice.model.ImageAnalytics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class ImageAnalyticsService {

  private final DynamoDbClient dynamoDbClient;
  private final String tableName;

  public ImageAnalyticsService(DynamoDbClient dynamoDbClient,
      @Value("${aws.dynamodb.table.name}") String tableName) {
    this.dynamoDbClient = dynamoDbClient;
    this.tableName = tableName;
    log.info("Using existing DynamoDB table: {}", tableName);
  }

  public void incrementViewCount(Long imageId) {
    try {
      Instant now = Instant.now();

      UpdateItemRequest updateRequest = UpdateItemRequest.builder()
          .tableName(tableName)
          .key(Map.of("id", AttributeValue.builder().n(String.valueOf(imageId)).build()))
          .updateExpression("ADD viewCount :increment SET lastViewTimestamp = :timestamp, updatedAt = :updatedAt")
          .conditionExpression("attribute_exists(id)")
          .expressionAttributeValues(Map.of(
              ":increment", AttributeValue.builder().n("1").build(),
              ":timestamp", AttributeValue.builder().s(now.toString()).build(),
              ":updatedAt", AttributeValue.builder().s(now.toString()).build()
          ))
          .build();

      dynamoDbClient.updateItem(updateRequest);
      log.debug("Incremented view count for image: {}", imageId);

    } catch (ConditionalCheckFailedException e) {
      createImageAnalytics(imageId, 1L, 0L, Instant.now(), null);
    } catch (Exception e) {
      log.error("Error incrementing view count for image {}: {}", imageId, e.getMessage());
      throw new RuntimeException("Failed to increment view count", e);
    }
  }

  public void incrementDownloadCount(Long imageId) {
    try {
      Instant now = Instant.now();

      UpdateItemRequest updateRequest = UpdateItemRequest.builder()
          .tableName(tableName)
          .key(Map.of("id", AttributeValue.builder().n(String.valueOf(imageId)).build()))
          .updateExpression("ADD downloadCount :increment SET lastDownloadTimestamp = :timestamp, updatedAt = :updatedAt")
          .conditionExpression("attribute_exists(id)")
          .expressionAttributeValues(Map.of(
              ":increment", AttributeValue.builder().n("1").build(),
              ":timestamp", AttributeValue.builder().s(now.toString()).build(),
              ":updatedAt", AttributeValue.builder().s(now.toString()).build()
          ))
          .build();

      dynamoDbClient.updateItem(updateRequest);
      log.debug("Incremented download count for image: {}", imageId);

    } catch (ConditionalCheckFailedException e) {
      createImageAnalytics(imageId, 0L, 1L, null, Instant.now());
    } catch (Exception e) {
      log.error("Error incrementing download count for image {}: {}", imageId, e.getMessage());
      throw new RuntimeException("Failed to increment download count", e);
    }
  }

  private void createImageAnalytics(Long imageId, Long viewCount, Long downloadCount,
      Instant lastViewTimestamp, Instant lastDownloadTimestamp) {
    try {
      Instant now = Instant.now();
      Map<String, AttributeValue> item = new HashMap<>();
      item.put("id", AttributeValue.builder().n(String.valueOf(imageId)).build());
      item.put("viewCount", AttributeValue.builder().n(String.valueOf(viewCount)).build());
      item.put("downloadCount", AttributeValue.builder().n(String.valueOf(downloadCount)).build());
      item.put("createdAt", AttributeValue.builder().s(now.toString()).build());
      item.put("updatedAt", AttributeValue.builder().s(now.toString()).build());

      if (lastViewTimestamp != null) {
        item.put("lastViewTimestamp", AttributeValue.builder().s(lastViewTimestamp.toString()).build());
      }
      if (lastDownloadTimestamp != null) {
        item.put("lastDownloadTimestamp", AttributeValue.builder().s(lastDownloadTimestamp.toString()).build());
      }

      PutItemRequest putRequest = PutItemRequest.builder()
          .tableName(tableName)
          .item(item)
          .build();

      dynamoDbClient.putItem(putRequest);
      log.debug("Created analytics record for image: {}", imageId);

    } catch (Exception e) {
      log.error("Error creating analytics record for image {}: {}", imageId, e.getMessage());
      throw new RuntimeException("Failed to create analytics record", e);
    }
  }

//  public Optional<ImageAnalytics> getImageAnalytics(String id) {
//    try {
//      GetItemRequest getRequest = GetItemRequest.builder()
//          .tableName(tableName)
//          .key(Map.of("id", AttributeValue.builder().s(id).build()))
//          .build();
//
//      GetItemResponse response = dynamoDbClient.getItem(getRequest);
//
//      if (!response.hasItem()) {
//        return Optional.empty();
//      }
//
//      Map<String, AttributeValue> item = response.item();
//      return Optional.of(ImageAnalytics.builder()
//          .id(item.get("id").s())
//          .viewCount(Long.valueOf(item.getOrDefault("viewCount", AttributeValue.builder().n("0").build()).n()))
//          .downloadCount(Long.valueOf(item.getOrDefault("downloadCount", AttributeValue.builder().n("0").build()).n()))
//          .lastViewTimestamp(item.containsKey("lastViewTimestamp") ?
//              Instant.parse(item.get("lastViewTimestamp").s()) : null)
//          .lastDownloadTimestamp(item.containsKey("lastDownloadTimestamp") ?
//              Instant.parse(item.get("lastDownloadTimestamp").s()) : null)
//          .createdAt(item.containsKey("createdAt") ?
//              Instant.parse(item.get("createdAt").s()) : null)
//          .updatedAt(item.containsKey("updatedAt") ?
//              Instant.parse(item.get("updatedAt").s()) : null)
//          .build());
//
//    } catch (Exception e) {
//      log.error("Error getting analytics for image {}: {}", id, e.getMessage());
//      return Optional.empty();
//    }
//  }
}

