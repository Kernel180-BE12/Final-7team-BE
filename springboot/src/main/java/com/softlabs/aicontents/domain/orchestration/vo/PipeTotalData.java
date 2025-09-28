//package com.softlabs.aicontents.domain.orchestration.vo;
//
//import com.softlabs.aicontents.domain.scheduler.dto.resultDTO.Product;
//import lombok.Data;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//
//@Data
//public class PipeTotalData {
//
//   private int executionId;
//    private int taskId;
//    private String overallStatus;
//    private String startedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//    private String completedAt;
//    private String currentStage;
//    //
//
//// 각 단계별 진행 상황
////ProgressResult progressResult;
//
//    private String keywordExtractionStatus;
//    private int keywordExtractionProgress;
//
//    private String productCrawlingStatus;
//    private int productCrawlingProgress;
//
////    private String contentGenerationStatus;
//    private int contentGenerationProgress;
//
//    private String contentPublishingStatus;
//    private int contentPublishingProgress;
//
//    //내부 클래스를 담을 필드
//    private List<Keyword> keywords=new ArrayList<>();
//    private List<Product> products=new ArrayList<>();
//    private ContentGeneration contentGeneration;
//    private ContentPublishing contentPublishing;
//    private List<Logs> logs=new ArrayList<>();
//
//
//// 단계별 결과 데이터
////StageResults stageResults;
//    //List<Keyword> keywords;
//    @Data
//    public static class Keyword {
//        String keyword;
//        boolean selected;
//        int relevanceScore;
//        //
//        private String keyWordStatusCode;
//    }
//
//
//    //  List<Product> products;
//    @Data
//    public static class ProductCrawling {
//        String productId;
//        String name;
//        int price;
//        String SearchPlatform = "싸다구몰";
//        //
//        private String sourceUrl;
//        private String productStatusCode;
//    }
//
//    //Content
//    @Data
//    public static class ContentGeneration {
//        String title;
//        String content;
//        List<String> tags; //hashtags
//        //
//        private String summary;
//        private String aIContentStatusCode;
//    }
//
//    // PublishingStatus
//    @Data
//    public static class ContentPublishing {
//        String PublishPlatform = "네이버";
//        String status;
//        String blogUrl;
//        //
//        private String publishStatusCode;
//        private String blogPostId;
//    }
//
//    // 로그 정보
//    //List<Logs> logs;
//    @Data
//    public static class Logs {
//        String timestamp;
//        String stage; //stepcode
//        String level;
//        String message;
//    }
//
//
//}
