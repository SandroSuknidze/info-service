package com.sandro.infoservice.controller;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class DependencyFactory {

  private DependencyFactory() {}

  /**
   * @return an instance of S3Client
   */
  public static S3Client s3Client() {
    return S3Client.builder()
//        .credentialsProvider(ProfileCredentialsProvider.create("user2"))
        .region(Region.US_WEST_2)
        .httpClientBuilder(ApacheHttpClient.builder())
        .build();
  }
}
