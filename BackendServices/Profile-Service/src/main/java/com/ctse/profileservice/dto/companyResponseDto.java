package com.ctse.profileservice.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class companyResponseDto {
    private Long id;
    private String employeeId;
    private String companyName;
    private String location;
    private String industry;
    private String background;
    private String website;
    private String logoUrl;
    private Double reputationScore;
    private List<String> cultureTags;
    private Long followersCount;
    private boolean isFollowedByMe;
}
