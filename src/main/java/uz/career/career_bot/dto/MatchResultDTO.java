package uz.career.career_bot.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MatchResultDTO {
    private Long targetId;
    private String title;
    private String subtitle;
    private double score;
    private int scorePercent;
    private String location;
    private String experience;
    private String salaryRange;
    private String searchStatus;
}