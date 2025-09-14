package com.softlabs.aicontents.domain.publish.service;

import com.softlabs.aicontents.domain.publish.dto.request.AiPostDto;
import com.softlabs.aicontents.domain.publish.dto.request.PublishReqDto;
import com.softlabs.aicontents.domain.publish.dto.response.PublishResDto;
import com.softlabs.aicontents.domain.publish.mapper.AiPostMapper;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class PublishService {
  private final AiPostMapper aiPostMapper;

  WebClient client = WebClient.builder().baseUrl("http://localhost:8000").build();

  public PublishResDto publishByPostId(Long postId) {

    // 1) DB에서 로드
    AiPostDto post = aiPostMapper.selectByPostId(postId);
    if (post == null) throw new IllegalArgumentException("AI_POST not found: " + postId);

    // 2) 매핑 (서비스 내부 프라이빗 메서드로)
    PublishReqDto req = toPublishReq(post);

    return client
        .post()
        .uri("/publish")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(req)
        .retrieve()
        .bodyToMono(PublishResDto.class)
        .block(Duration.ofSeconds(120));
  }

  private PublishReqDto toPublishReq(AiPostDto src) {
    return PublishReqDto.builder()
        .aiContentId(src.getPostId()) // 필요시 src.getGenId() 로 교체 가능
        .title(src.getTitle())
        .metaDescription(src.getMetaDescription())
        .markdown(src.getBodyMarkdown())
        .hashtag(src.getHashtagsCsv())
        .build();
  }
}
