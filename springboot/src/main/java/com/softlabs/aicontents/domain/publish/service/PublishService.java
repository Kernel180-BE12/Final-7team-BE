package com.softlabs.aicontents.domain.publish.service;

import com.softlabs.aicontents.domain.publish.dto.request.AiPostDto;
import com.softlabs.aicontents.domain.publish.dto.request.PublishReqDto;
import com.softlabs.aicontents.domain.publish.dto.response.PublishResDto;
import com.softlabs.aicontents.domain.publish.mapper.AiPostMapper;
import com.softlabs.aicontents.domain.publish.mapper.PublishResultMapper;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PublishService {
  private final AiPostMapper aiPostMapper;
  private final PublishResultMapper publishResultMapper;

  WebClient client = WebClient.builder().baseUrl("http://localhost:8000").build();

  public PublishResDto publishByPostId(Long postId) {
    // 1) DB 로드
    AiPostDto post = aiPostMapper.selectByPostId(postId);
    if (post == null) throw new IllegalArgumentException("AI_POST not found: " + postId);

    // 2) 매핑
    PublishReqDto req = toPublishReq(post);

    // 3) FastAPI 호출 (에러바디 보존)
    PublishResDto res =
        client
            .post()
            .uri("/publish")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .exchangeToMono(
                resp -> {
                  var st = resp.statusCode();
                  if (st.is2xxSuccessful()) {
                    return resp.bodyToMono(PublishResDto.class);
                  }
                  return resp.bodyToMono(String.class)
                      .defaultIfEmpty("")
                      .flatMap(
                          body ->
                              Mono.error(
                                  new RuntimeException(
                                      "FastAPI error "
                                          + st.value()
                                          + (body.isEmpty() ? "" : ": " + body))));
                })
            .timeout(Duration.ofSeconds(120))
            .block();

    if (res == null) throw new IllegalStateException("FastAPI returned null");

    // 4) 기본값 보정
    if (res.getBlogPlatform() == null) res.setBlogPlatform("NAVER");
    if (res.getAttemptCount() == null) res.setAttemptCount(1);

    // 5) DB INSERT
    publishResultMapper.insertPublishResult(res);

    return res;
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

  public Long saveFromResponse(PublishResDto res) {
    // enum → 문자열로 변환이 필요하면 여기서 처리
    // (아래 XML에서 #{publishStatusName}를 쓸 계획이라면 이 변환도 불필요)
    publishResultMapper.insertPublishResult(res);
    return res.getPublishId(); // selectKey로 PK가 세팅됨
  }
}
