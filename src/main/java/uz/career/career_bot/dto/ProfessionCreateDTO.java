package uz.career.career_bot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ProfessionCreateDTO {

    @NotBlank(message = "Job title cannot be empty.")
    @Size(min = 2, max = 100, message = "The job title must be between 2 and 100 characters long.")
    private String name;

    @NotNull(message = "Category selection is required.")
    private Long categoryId;
}