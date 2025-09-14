package com.softlabs.aicontents.domain.publish.controller;

import com.softlabs.aicontents.domain.publish.dto.response.PublishResDto;
import com.softlabs.aicontents.domain.publish.service.PublishService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/publish")
public class PublishController {
  private final PublishService publishService;

  @PostMapping("/{postId}")
  public PublishResDto publish(@PathVariable Long postId) {
    return publishService.publishByPostId(postId);
  }
}
