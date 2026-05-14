package uz.career.career_bot.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class JoobleRequest {
    private String keywords;
    private String location;
    private int page;
}