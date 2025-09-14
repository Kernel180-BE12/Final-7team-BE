package com.softlabs.aicontents.domain.scheduler.dto.resultDTO;
import lombok.Data;



@Data
public class Keyword {

    String keyword;
    boolean selected;
    int relevanceScore;

}
