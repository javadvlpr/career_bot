package uz.career.career_bot.dto;

import lombok.*;
import uz.career.career_bot.enums.CompanyStatus;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CompanyDTO {
    private Long id;
    private Long telegramId;
    private String companyName;
    private String inn;
    private String industry;
    private String contactInfo;
    private CompanyStatus status;
    private int totalJobs;
}