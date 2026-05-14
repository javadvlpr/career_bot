package uz.career.career_bot.dto;

import lombok.*;
import uz.career.career_bot.enums.*;
import java.util.Set;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private Long telegramId;
    private String name;
    private String profession;
    private Set<String> skillNames;
    private ExperienceLevel experienceLevel;
    private String location;
    private Integer expectedSalary;
    private SearchStatus searchStatus;
    private Language language;
}