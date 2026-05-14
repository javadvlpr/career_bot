package uz.career.career_bot.dto;

import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CategoryDTO {
    private Long id;
    private String name;
    private List<SkillDTO> skills;
    private List<ProfessionDTO> professions;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class SkillDTO {
        private Long id;
        private String name;
    }

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class ProfessionDTO {
        private Long id;
        private String name;
    }
}