package com.sandro.infoservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;


@Service
public class LambdaInvoker {

  private final LambdaClient lambdaClient;

  @Value("${aws.lambda.dataConsistencyFunction}")
  private String functionName;

  public LambdaInvoker() {
    this.lambdaClient = LambdaClient.create();
  }

  public String invoke() {
    InvokeRequest request = InvokeRequest.builder()
        .functionName(functionName)
        .payload(SdkBytes.fromUtf8String("{\"detail-type\": \"triggered-from-web\"}"))
        .build();

    InvokeResponse response = lambdaClient.invoke(request);
    return response.payload().asUtf8String();
  }
}

