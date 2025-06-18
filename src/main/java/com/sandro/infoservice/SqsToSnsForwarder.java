package com.sandro.infoservice;

import lombok.RequiredArgsConstructor;
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

@Component
@RequiredArgsConstructor
public class SqsToSnsForwarder {

  private final SqsClient sqsClient;
  private final SnsClient snsClient;


  @Value("${aws.sqs.queue.url}")
  private String sqsUrl;

  @Value("${aws.sns.topic.arn}")
  private String snsArn;

  @Scheduled(fixedRate = 30000)
  public void forwardMessages() {
    ReceiveMessageResponse response = sqsClient.receiveMessage(
        ReceiveMessageRequest.builder()
            .queueUrl(sqsUrl)
            .maxNumberOfMessages(10)
            .waitTimeSeconds(10)
            .build());
    for (Message msg : response.messages()) {
      snsClient.publish(PublishRequest.builder()
          .topicArn(snsArn)
          .subject("New Image Uploaded")
//          .message(buildNotificationMessage(msg.body()))
          .message(msg.body())
          .build());

      sqsClient.deleteMessage(DeleteMessageRequest.builder()
          .queueUrl(sqsUrl)
          .receiptHandle(msg.receiptHandle())
          .build());
    }
  }

//  private String buildNotificationMessage(String json) {
//    JSONObject obj = new JSONObject(json);
//    return String.format("An image was uploaded!\nName: %s\nSize: %s bytes\nType: %s\nDownload: %s",
//        obj.getString("fileName"),
//        obj.getInt("size"),
//        obj.getString("extension"),
//        obj.getString("downloadUrl"));
//  }
}
