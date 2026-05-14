package uz.career.career_bot.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import uz.career.career_bot.enums.ExperienceLevel;
import uz.career.career_bot.enums.JobType;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class JobCreateDTO {

    @NotBlank(message = "The job title cannot be empty.")
    @Size(min = 3, max = 200, message = "The job title must be between 3 and 200 characters.")
    private String title;

    @Size(max = 5000, message = "Description should not exceed 5000 characters.")
    private String description;

    @Size(max = 2000, message = "Requests should not exceed 2000 characters.")
    private String requirements;

    private Long professionId;

    @Size(max = 100, message = "Location should not exceed 100 characters.")
    private String location;

    @Min(value = 0, message = "Min monthly cannot be negative.")
    private Integer salaryMin;

    @Min(value = 0, message = "Max monthly cannot be negative.")
    private Integer salaryMax;

    private JobType jobType;

    private ExperienceLevel experienceLevel;

    @Pattern(
            regexp = "^$|^https?://.+",
            message = "The URL must start with https:// or http://"
    )
    private String externalUrl;

    @Size(max = 500, message = "Contact should not exceed 500 characters.")
    private String contactInfo;

    /**
     * Custom validation: At least one of the URL or contact fields must be filled in.
     */
    @AssertTrue(message = "URL or contact information is required.")
    public boolean isUrlOrContactPresent() {
        boolean hasUrl = externalUrl != null && !externalUrl.isBlank();
        boolean hasContact = contactInfo != null && !contactInfo.isBlank();
        return hasUrl || hasContact;
    }

    /**
     * Custom validation: salaryMax >= salaryMin
     */
    @AssertTrue(message = "Max month cannot be less than min month")
    public boolean isSalaryRangeValid() {
        if (salaryMin == null || salaryMax == null) return true;
        return salaryMax >= salaryMin;
    }
}