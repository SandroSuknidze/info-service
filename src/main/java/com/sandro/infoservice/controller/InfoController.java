package com.sandro.infoservice.controller;

import com.amazonaws.util.EC2MetadataUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class InfoController {

  @GetMapping("/info")
  public Map<String, String> getInfo() {
    String az = EC2MetadataUtils.getAvailabilityZone();
    String region = EC2MetadataUtils.getEC2InstanceRegion();

    Map<String, String> info = new HashMap<>();
    info.put("region", region != null ? region : "unknown");
    info.put("availabilityZone", az != null ? az : "unknown");

    return info;
  }

}
