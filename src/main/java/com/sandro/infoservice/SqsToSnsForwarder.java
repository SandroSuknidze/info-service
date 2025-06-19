package com.sandro.infoservice;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqsToSnsForwarder {

  private final SqsClient sqsClient;
  private final SnsClient snsClient;


  @Value("${aws.sqs.queue.url}")
  private String sqsUrl;

  @Value("${aws.sns.topic.arn}")
  private String snsArn;

  @Value("${app.sqs.processor.thread-pool-size:5}")
  private int threadPoolSize;

  private ExecutorService processingPool;

  @PostConstruct
  public void init() {
    processingPool = Executors.newFixedThreadPool(threadPoolSize);
  }

  @PreDestroy
  public void shutdown() {
    processingPool.shutdown();
    log.info("ExecutorService shut down.");
  }

  @Scheduled(fixedDelay = 5000)
  public void pollAndForward() {
    ReceiveMessageResponse response = sqsClient.receiveMessage(
        ReceiveMessageRequest.builder()
            .queueUrl(sqsUrl)
            .maxNumberOfMessages(10)
            .waitTimeSeconds(10)
            .build());
    for (Message message : response.messages()) {
      processingPool.submit(() -> processMessage(message));
    }
  }

  private void processMessage(Message message) {
    try {
      snsClient.publish(PublishRequest.builder()
          .topicArn(snsArn)
          .subject("New Image Uploaded")
          .message(buildNotificationMessage(message.body())).build());

      sqsClient.deleteMessage(
          DeleteMessageRequest.builder()
              .queueUrl(sqsUrl)
              .receiptHandle(message.receiptHandle())
              .build());

      log.info("Message forwarded & deleted: {}", message.messageId());

    } catch (Exception e) {
      log.error("Error processing message {}: {}", message.messageId(), e.getMessage());
    }
  }

  private String buildNotificationMessage(String json) {
    JSONObject obj = new JSONObject(json);
    return String.format("An image was uploaded!\nName: %s\nSize: %s bytes\nType: %s\nDownload: %s",
        obj.getString("fileName"), obj.getInt("size"), obj.getString("extension"),
        obj.getString("downloadUrl"));
  }
}
