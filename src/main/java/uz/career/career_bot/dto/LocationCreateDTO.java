package uz.career.career_bot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LocationCreateDTO {

    @NotBlank(message = "Location name cannot be empty.")
    @Size(min = 2, max = 100, message = "Location name must be between 2 and 100 characters long")
    private String name;

    private Boolean active;
}