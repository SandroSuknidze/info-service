package com.sandro.infoservice;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

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

  public static S3Presigner s3Presigner() {
    return S3Presigner.builder()
        .region(Region.US_WEST_2)
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();
  }

}
