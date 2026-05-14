package uz.career.career_bot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SkillCreateDTO {

    @NotBlank(message = "Skill name cannot be empty.")
    @Size(min = 1, max = 80, message = "Skill name must be between 1 and 80 characters long")
    private String name;

    @NotNull(message = "Category selection is required.")
    private Long categoryId;
}