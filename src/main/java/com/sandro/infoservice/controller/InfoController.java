package com.sandro.infoservice.controller;

import com.amazonaws.util.EC2MetadataUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class InfoController {

  private final S3Client s3Client;

  public InfoController() {
    s3Client = DependencyFactory.s3Client();
  }

  @GetMapping("/info")
  public Map<String, String> getInfo() {
    String az = EC2MetadataUtils.getAvailabilityZone();
    String region = EC2MetadataUtils.getEC2InstanceRegion();

    Map<String, String> info = new HashMap<>();
    info.put("region", region != null ? region : "unknown");
    info.put("availabilityZone", az != null ? az : "unknown");

    return info;
  }

  @GetMapping("/hi")
  public String hi() {
    return "Hi there!";
  }

  @GetMapping("/welcome")
  public String welcome() {
    return "Welcome to the Info Service!";
  }

  @GetMapping("/file-download")
  public String fileDownload(@RequestParam String bucketName, @RequestParam String key) {
    try {
      GetObjectRequest getObjectRequest = GetObjectRequest.builder()
          .bucket(bucketName)
          .key(key)
          .build();

      ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);

      Path localPath = Paths.get("downloads/" + key);
      Files.createDirectories(localPath.getParent());
      Files.copy(s3Object, localPath, StandardCopyOption.REPLACE_EXISTING);

      return "From " + bucketName + " object " + key + " downloaded successfully to " + localPath;
    } catch (Exception e) {
      return "Error downloading file: " + e.getMessage();
    }
  }

  @GetMapping("/show-metadata")
  public Object showMetadataByImage(@RequestParam String bucketName, @RequestParam String key) {
    HeadObjectRequest getObjectRequest = HeadObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .build();

    HeadObjectResponse response = s3Client.headObject(getObjectRequest);

    Map<String, Object> metadata = new HashMap<>();
    metadata.put("key", key);
    metadata.put("contentType", response.contentType());
    metadata.put("contentLength", response.contentLength());
    metadata.put("lastModified", response.lastModified());
    metadata.put("eTag", response.eTag());
    metadata.put("userMetadata", response.metadata());
    metadata.put("storageClass", response.storageClassAsString());

    return metadata;
  }

  @GetMapping("/show-metadata-random")
  public Object showMetadataByImageRandom(@RequestParam String bucketName) {
    List<String> keys = s3Client.listObjects(ListObjectsRequest.builder()
        .bucket(bucketName)
        .build())
        .contents()
        .stream()
        .map(S3Object::key)
        .toList();

    return showMetadataByImage(bucketName, keys.get((int) (Math.random() * keys.size())));
  }

  @PostMapping("/upload-image")
  public Object uploadImage(
      @RequestParam String bucketName,
      @RequestParam("file") MultipartFile file) {

    try {
      if (file.isEmpty()) {
        return ResponseEntity.badRequest();
      }

      String fileName = file.getOriginalFilename();
      String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
      long fileSizeInBytes = file.getSize();
      Instant currentTime = Instant.now();

      String contentType = file.getContentType();
      if (contentType == null || !contentType.startsWith("image/")) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", "File must be an image"));
      }

      Map<String, String> userMetadata = new HashMap<>();
      userMetadata.put("last-update-date", currentTime.toString());
      userMetadata.put("original-name", fileName);
      userMetadata.put("size-bytes", String.valueOf(fileSizeInBytes));
      userMetadata.put("file-extension", fileExtension);


      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(bucketName)
          .key(fileName)
          .contentType(contentType)
          .contentLength(fileSizeInBytes)
          .metadata(userMetadata)
          .build();

      PutObjectResponse response = s3Client.putObject(putObjectRequest,
          RequestBody.fromInputStream(file.getInputStream(), fileSizeInBytes));

      Map<String, Object> result = new HashMap<>();
      result.put("message", "Image uploaded successfully");
      result.put("key", fileName);
      result.put("bucket", bucketName);
      result.put("eTag", response.eTag());
      result.put("metadata", Map.of(
          "lastUpdateDate", currentTime.toString(),
          "name", fileName,
          "sizeInBytes", fileSizeInBytes,
          "fileExtension", fileExtension
      ));

      return ResponseEntity.ok(result);
    } catch (Exception e) {
      return "Error uploading file: " + e.getMessage();
    }
  }

  @DeleteMapping("/delete-image")
  public Object deleteImage(
      @RequestParam String bucketName,
      @RequestParam String key
  ) {
    try {
      DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
          .bucket(bucketName)
          .key(key)
          .build();

      s3Client.deleteObject(deleteObjectRequest);

      return "Image deleted successfully";
    } catch (Exception e) {
      return "Error deleting file: " + e.getMessage();
    }
  }

}
