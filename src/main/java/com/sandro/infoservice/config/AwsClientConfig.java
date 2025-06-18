package com.sandro.infoservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class AwsClientConfig {

  @Bean
  public SnsClient snsClient() {
    return SnsClient.builder()
        .region(Region.of("us-west-2"))
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();
  }

  @Bean
  public SqsClient sqsClient() {
    return SqsClient.builder()
        .region(Region.of("us-west-2"))
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();
  }
}
