package uz.career.career_bot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uz.career.career_bot.entity.Skill;
import uz.career.career_bot.enums.ExperienceLevel;
import uz.career.career_bot.repository.JobRepository;
import uz.career.career_bot.repository.UserRepository;
import uz.career.career_bot.entity.Category;
import uz.career.career_bot.entity.Job;
import uz.career.career_bot.entity.Profession;
import uz.career.career_bot.entity.User;
import uz.career.career_bot.enums.SearchStatus;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MatchingService matchingService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(matchingService, "skillWeight", 0.30);
        ReflectionTestUtils.setField(matchingService, "professionWeight", 0.25);
        ReflectionTestUtils.setField(matchingService, "experienceWeight", 0.20);
        ReflectionTestUtils.setField(matchingService, "locationWeight", 0.15);
        ReflectionTestUtils.setField(matchingService, "salaryWeight", 0.10);
    }

    // === SKILL MATCHING ===

    @Test
    @DisplayName("Barcha skilllar mos kelsa 1.0 qaytadi")
    void skillMatch_allMatch_returns1() {
        Set<Skill> userSkills = createSkills(1L, 2L, 3L);
        Set<Skill> jobSkills = createSkills(1L, 2L, 3L);
        assertEquals(1.0, matchingService.calculateSkillMatch(userSkills, jobSkills));
    }

    @Test
    @DisplayName("Yarmi mos kelsa 0.5 qaytadi")
    void skillMatch_partialMatch_returnsRatio() {
        Set<Skill> userSkills = createSkills(1L, 2L);
        Set<Skill> jobSkills = createSkills(1L, 2L, 3L, 4L);
        assertEquals(0.5, matchingService.calculateSkillMatch(userSkills, jobSkills));
    }

    @Test
    @DisplayName("Hech biri mos kelmasa 0.0 qaytadi")
    void skillMatch_noMatch_returns0() {
        Set<Skill> userSkills = createSkills(1L, 2L);
        Set<Skill> jobSkills = createSkills(5L, 6L);
        assertEquals(0.0, matchingService.calculateSkillMatch(userSkills, jobSkills));
    }

    @Test
    @DisplayName("Vakansiyada skill talab qilinmasa 0.5 qaytadi")
    void skillMatch_noJobSkillsRequired_returnsHalf() {
        Set<Skill> userSkills = createSkills(1L, 2L);
        assertEquals(0.5, matchingService.calculateSkillMatch(userSkills, new HashSet<>()));
    }

    @Test
    @DisplayName("User skill yo'q, lekin vakansiya talab qilsa 0.0")
    void skillMatch_noUserSkills_returns0() {
        Set<Skill> jobSkills = createSkills(1L, 2L);
        assertEquals(0.0, matchingService.calculateSkillMatch(new HashSet<>(), jobSkills));
    }

    // === EXPERIENCE MATCHING ===

    @Test
    @DisplayName("Bir xil tajriba 1.0 qaytadi")
    void experienceMatch_sameLevel_returns1() {
        assertEquals(1.0,
                matchingService.calculateExperienceMatch(ExperienceLevel.MIDDLE, ExperienceLevel.MIDDLE));
    }

    @Test
    @DisplayName("1 daraja farq 0.6 qaytadi")
    void experienceMatch_oneDiff_returns06() {
        assertEquals(0.6,
                matchingService.calculateExperienceMatch(ExperienceLevel.JUNIOR, ExperienceLevel.MIDDLE));
    }

    @Test
    @DisplayName("Tajriba null bo'lsa 0.5 qaytadi")
    void experienceMatch_null_returnsHalf() {
        assertEquals(0.5,
                matchingService.calculateExperienceMatch(null, ExperienceLevel.MIDDLE));
    }

    // === LOCATION MATCHING ===

    @Test
    @DisplayName("Bir xil lokatsiya 1.0 qaytadi")
    void locationMatch_same_returns1() {
        assertEquals(1.0, matchingService.calculateLocationMatch("Toshkent", "Toshkent"));
    }

    @Test
    @DisplayName("Remote 0.8 qaytadi")
    void locationMatch_remote_returns08() {
        assertEquals(0.8, matchingService.calculateLocationMatch("Toshkent", "Remote"));
    }

    @Test
    @DisplayName("Boshqa lokatsiya 0.0 qaytadi")
    void locationMatch_different_returns0() {
        assertEquals(0.0, matchingService.calculateLocationMatch("Toshkent", "Samarqand"));
    }

    // === SALARY MATCHING ===

    @Test
    @DisplayName("Diapazonda bo'lsa 1.0 qaytadi")
    void salaryMatch_inRange_returns1() {
        assertEquals(1.0, matchingService.calculateSalaryMatch(7000000, 5000000, 8000000));
    }

    @Test
    @DisplayName("Salary null bo'lsa 0.5 qaytadi")
    void salaryMatch_null_returnsHalf() {
        assertEquals(0.5, matchingService.calculateSalaryMatch(null, 5000000, 8000000));
    }

    // === PROFESSION MATCHING ===

    @Test
    @DisplayName("Bir xil kasb 1.0 qaytadi")
    void professionMatch_sameProfession_returns1() {
        Profession prof = createProfession(1L, 1L);
        assertEquals(1.0, matchingService.calculateProfessionMatch(prof, prof));
    }

    @Test
    @DisplayName("Bir xil kategoriyadagi turli kasb 0.5 qaytadi")
    void professionMatch_sameCategory_returnsHalf() {
        Profession userProf = createProfession(1L, 1L); // category 1
        Profession jobProf = createProfession(2L, 1L);  // same category
        assertEquals(0.5, matchingService.calculateProfessionMatch(userProf, jobProf));
    }

    @Test
    @DisplayName("Boshqa kategoriyadagi kasb 0.0 qaytadi")
    void professionMatch_differentCategory_returns0() {
        Profession userProf = createProfession(1L, 1L);
        Profession jobProf = createProfession(2L, 2L);
        assertEquals(0.0, matchingService.calculateProfessionMatch(userProf, jobProf));
    }

    @Test
    @DisplayName("Kasb null bo'lsa 0.5 qaytadi")
    void professionMatch_null_returnsHalf() {
        assertEquals(0.5, matchingService.calculateProfessionMatch(null, null));
    }

    // === COMPOSITE SCORE ===

    @Test
    @DisplayName("Hamma mezon mos kelsa score 1.0 ga yaqin bo'ladi")
    void calculateScore_perfectMatch_returnsHighScore() {
        Profession prof = createProfession(1L, 1L);

        User user = User.builder()
                .profession(prof)
                .skills(createSkills(1L, 2L))
                .experienceLevel(ExperienceLevel.MIDDLE)
                .location("Tashkent")
                .expectedSalary(5_000_000)
                .searchStatus(SearchStatus.ACTIVE)
                .build();

        Job job = Job.builder()
                .profession(prof)
                .skills(createSkills(1L, 2L))
                .experienceLevel(ExperienceLevel.MIDDLE)
                .location("Tashkent")
                .salaryMin(4_000_000)
                .salaryMax(6_000_000)
                .build();

        double score = matchingService.calculateScore(user, job);

        assertTrue(score > 0.9,
                "Hamma mezon bo'yicha to'liq mos kelganda score 0.9 dan yuqori bo'lishi kerak");
    }

    @Test
    @DisplayName("Hech qanday mezon mos kelmasa score past bo'ladi")
    void calculateScore_noMatch_returnsLowScore() {
        Profession userProf = createProfession(1L, 1L);
        Profession jobProf = createProfession(2L, 2L);

        User user = User.builder()
                .profession(userProf)
                .skills(createSkills(1L, 2L))
                .experienceLevel(ExperienceLevel.JUNIOR)
                .location("Tashkent")
                .expectedSalary(3_000_000)
                .build();

        Job job = Job.builder()
                .profession(jobProf)
                .skills(createSkills(5L, 6L))
                .experienceLevel(ExperienceLevel.SENIOR)
                .location("Samarkand")
                .salaryMin(10_000_000)
                .salaryMax(15_000_000)
                .build();

        double score = matchingService.calculateScore(user, job);

        assertTrue(score < 0.2,
                "Hech qanday mezon mos kelmaganda score 0.2 dan past bo'lishi kerak");
    }


    private Set<Skill> createSkills(Long... ids) {
        Set<Skill> skills = new HashSet<>();
        for (Long id : ids) {
            Skill s = new Skill();
            s.setId(id);
            s.setName("Skill" + id);
            skills.add(s);
        }
        return skills;
    }

    private Profession createProfession(Long id, Long categoryId) {
        Category category = new Category();
        category.setId(categoryId);
        category.setName("Category" + categoryId);

        Profession profession = new Profession();
        profession.setId(id);
        profession.setName("Profession" + id);
        profession.setCategory(category);
        return profession;
    }
}