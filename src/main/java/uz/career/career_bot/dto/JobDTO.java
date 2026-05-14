package uz.career.career_bot.dto;

import lombok.*;
import uz.career.career_bot.enums.*;
import java.time.LocalDateTime;
import java.util.Set;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class JobDTO {
    private Long id;
    private String title;
    private String description;
    private String requirements;
    private Set<String> skillNames;
    private ExperienceLevel experienceLevel;
    private Integer salaryMin;
    private Integer salaryMax;
    private String location;
    private JobType jobType;
    private JobStatus status;
    private JobSource source;
    private String companyName;
    private String externalUrl;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}