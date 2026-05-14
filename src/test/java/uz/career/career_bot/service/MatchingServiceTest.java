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
        ReflectionTestUtils.setField(matchingService, "skillWeight", 0.40);
        ReflectionTestUtils.setField(matchingService, "experienceWeight", 0.25);
        ReflectionTestUtils.setField(matchingService, "locationWeight", 0.20);
        ReflectionTestUtils.setField(matchingService, "salaryWeight", 0.15);
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

    // === HELPER ===

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
}