package com.softlabs.aicontents.scheduler.controller;


import com.softlabs.aicontents.scheduler.mapper.SchedulerMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.coyote.http11.filters.SavedRequestInputFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Service
@Slf4j
public class PipelineOrchestrator {

    private final SchedulerMapper schedulerMapper;

    public PipelineOrchestrator(SchedulerMapper schedulerMapper) {
        this.schedulerMapper = schedulerMapper;

    }


    public void executePipeline(Long taskId) {
        ExecutionContext context = new ExecutionContext(schedulerMapper);

      try{
          log.info("파이프라인 실행 시작 - TaskID: {}", taskId);

          //컨택스트 초기화
          context.initialize(taskId);

          //1단계 : 트랜드 크롤링 확인
          String step1Result= executeStep1(context);
          context.saveCurrentStepResult("TREND_CRAWLING", step1Result);

          //2단계 : 컨탠츠 크롤링
          String step2Result = executeStep2(context);
          context.saveCurrentStepResult("SSADAGU_CRAWLING", step2Result);

          //3단계 : AI 컨탠츠 생성
          String step3Result = executeStep3(context);
          context.saveCurrentStepResult("AI_GENERATION", step3Result);

          // 4단계: 블로그 발행
          String step4Result = executeStep4(context);
          context.saveCurrentStepResult("BLOG_PUBLISH", step4Result);

          // 완료 처리
          context.markCompleted();
          log.info("파이프라인 실행 완료 - TaskID: {}", taskId);

      } catch(Exception e) {
          log.error("파이프라인 실행실패 - TaskID: {}, 오류: {}", taskId,e.getMessage(), e);
          context.markFailed(e.getMessage());
      }
    }

    // 메서드

    //1단계
    private String executeStep1(ExecutionContext context){
        log.info("1단계 시작 : 트랜드 크롤링");

        try{
            //설정 조회
            String keywords = context.getTaskSetting("target_keywords");
            log.info("크롤링 키워드 : {}",keywords);

            // TODO: 실제 TrendService 호출
            // TrendService trendService = new TrendService();
            // List<String> results = trendService.crawl(Arrays.asList(keywords.split(",")));

            // 임시 Mock 데이터
            String mockResult = "[\"" + keywords + " 트렌드1\", \"" + keywords + " 트렌드2\", \"최신 이슈\"]";

            log.info("1단계 완료: 트렌드 데이터 수집됨");
            return mockResult;

        } catch(Exception e){
            log.error("1단계 실패:{}",e.getMessage());
            throw new RuntimeException("트랜드 크롤링 실해:" +e.getMessage());
        }


    }
    /**
     * 2단계: 콘텐츠 크롤링
     */
    private String executeStep2(ExecutionContext context) {
        log.info("2단계 시작: 콘텐츠 크롤링");

        try {
            // 이전 단계 결과 조회
            String step1Data = context.getPreviousStepResult("TREND_CRAWLING");
            log.info("1단계 결과 활용: {}", step1Data);

            // TODO: 실제 SsadaguService 호출
            // SsadaguService ssadaguService = new SsadaguService();
            // List<Article> results = ssadaguService.crawl(parseKeywords(step1Data));

            // 임시 Mock 데이터
            String mockResult = "[{\"title\":\"관련 기사1\", \"content\":\"내용1\"}, {\"title\":\"관련 기사2\", \"content\":\"내용2\"}]";

            log.info("2단계 완료: 컨텐츠 크롤링 완료");
            return mockResult;

        } catch (Exception e) {
            log.error("2단계 실패: {}", e.getMessage());
            throw new RuntimeException("콘텐츠 크롤링 실패: " + e.getMessage());
        }
    }

    /**
     * 3단계: AI 콘텐츠 생성
     */
    private String executeStep3(ExecutionContext context) {
        log.info("3단계 시작: AI 콘텐츠 생성");

        try {
            // 이전 단계들 결과 조회
            String step1Data = context.getPreviousStepResult("TREND_CRAWLING");
            String step2Data = context.getPreviousStepResult("SSADAGU_CRAWLING");

            log.info("1,2단계 결과 활용 - 트렌드: {}, 크롤링: {}",
                    step1Data.substring(0, Math.min(50, step1Data.length())),
                    step2Data.substring(0, Math.min(50, step2Data.length())));

            // TODO: 실제 AiService 호출
            // AiService aiService = new AiService();
            // String result = aiService.generateContent(step1Data, step2Data);

            // 임시 Mock 데이터
            String mockResult = "{\"title\":\"AI가 생성한 제목\", \"content\":\"AI가 생성한 블로그 내용...\", \"tags\":[\"태그1\", \"태그2\"]}";

            log.info("3단계 완료: AI 콘텐츠 생성 완료");
            return mockResult;

        }catch (Exception e){
        log.error("3단계 실패: {} ",e.getMessage());
        throw new RuntimeException("AI 콘텐츠 생성 실패: " + e.getMessage());
        }
    }

    /**
     * 4단계: 블로그 발행
     */
    private String executeStep4(ExecutionContext context) {
        log.info("4단계 시작: 블로그 발행");

        try {
            // 이전 단계 결과 조회
            String step3Data = context.getPreviousStepResult("AI_GENERATION");
            log.info("3단계 결과 활용: {}", step3Data.substring(0, Math.min(50, step3Data.length())));

            // TODO: 실제 BlogService 호출
            // BlogService blogService = new BlogService();
            // String result = blogService.publish(step3Data);

            // 임시 Mock 데이터
            String mockResult = "{\"blog_url\":\"https://blog.example.com/post123\", \"post_id\":\"123\", \"status\":\"published\"}";

            log.info("4단계 완료: 블로그 발행 완료");
            return mockResult;

        } catch (Exception e) {
        log.error("4단계 실패: {} ", e.getMessage());
        throw new RuntimeException("블로그 발행 실패: " + e.getMessage());
        }
    }

    /**
     * 테스트용 컨트롤러
     */
    @RestController
    @RequestMapping("/api/test")
    public class PipelineTestController {

        private final PipelineOrchestrator pipelineOrchestrator;

        public PipelineTestController(PipelineOrchestrator pipelineOrchestrator) {
            this.pipelineOrchestrator = pipelineOrchestrator;
        }

        /**
         * 파이프라인 수동 실행 테스트
         */
        @PostMapping("/execute/{taskId}")
        public ResponseEntity<String> testPipelineExecution(@PathVariable Long taskId) {
            try {
                pipelineOrchestrator.executePipeline(taskId);
                return ResponseEntity.ok("파이프라인 실행 완료");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("파이프라인 실행 실패: " + e.getMessage());
            }
        }
    }

        }
