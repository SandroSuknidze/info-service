package com.sandro.infoservice.controller;


import com.sandro.infoservice.LambdaInvoker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sns.model.UnsubscribeRequest;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final SnsClient snsClient;
  private final LambdaInvoker lambdaInvoker;


  @Value("${aws.sns.topic.arn}")
  private String topicArn;

  @PostMapping("/subscribe")
  public String subscribe(@RequestParam String email) {
    SubscribeRequest request = SubscribeRequest.builder()
        .protocol("email")
        .endpoint(email)
        .returnSubscriptionArn(true)
        .topicArn(topicArn)
        .build();

    snsClient.subscribe(request);
    return "Check your email for subscription confirmation.";
  }

  @PostMapping("/unsubscribe")
  public String unsubscribe(@RequestParam String subscriptionArn) {
    snsClient.unsubscribe(UnsubscribeRequest.builder()
        .subscriptionArn(subscriptionArn)
        .build());
    return "Unsubscribed successfully.";
  }

  @GetMapping("/test")
  public String test() {
    return "Test";
  }

  @GetMapping("/trigger-lambda")
  public String triggerLambda() {
    return lambdaInvoker.invoke();
  }

}
