package uz.career.career_bot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.career.career_bot.entity.Category;
import uz.career.career_bot.entity.Job;
import uz.career.career_bot.entity.Profession;
import uz.career.career_bot.entity.User;
import uz.career.career_bot.enums.ExperienceLevel;
import uz.career.career_bot.enums.JobStatus;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private JobService jobService;

    @Mock
    private UserService userService;

    private StatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        statisticsService = new StatisticsService(jobService, userService);
    }

    @Test
    void testGetJobsByStatus() {
        Job job1 = Job.builder().id(1L).title("Job 1").status(JobStatus.APPROVED).build();
        Job job2 = Job.builder().id(2L).title("Job 2").status(JobStatus.APPROVED).build();
        Job job3 = Job.builder().id(3L).title("Job 3").status(JobStatus.PENDING).build();

        when(jobService.getAll()).thenReturn(List.of(job1, job2, job3));

        Map<String, Long> result = statisticsService.getJobsByStatus();

        assertNotNull(result);
        assertEquals(2L, result.get("APPROVED"));
        assertEquals(1L, result.get("PENDING"));
    }

    @Test
    void testGetUsersByExperience() {
        User user1 = User.builder().id(1L).experienceLevel(ExperienceLevel.JUNIOR).build();
        User user2 = User.builder().id(2L).experienceLevel(ExperienceLevel.MIDDLE).build();
        User user3 = User.builder().id(3L).experienceLevel(ExperienceLevel.SENIOR).build();
        User user4 = User.builder().id(4L).experienceLevel(ExperienceLevel.JUNIOR).build();

        when(userService.getAll()).thenReturn(List.of(user1, user2, user3, user4));

        Map<String, Long> result = statisticsService.getUsersByExperience();

        assertNotNull(result);
        assertEquals(2L, result.get("1-3 yil"));
        assertEquals(1L, result.get("3-5 yil"));
        assertEquals(1L, result.get("5+ yil"));
    }

    @Test
    void testGetTopCategoriesByJobs() {
        Category cat1 = Category.builder().id(1L).name("IT").build();
        Category cat2 = Category.builder().id(2L).name("Marketing").build();

        Profession prof1 = Profession.builder().id(1L).name("Java").category(cat1).build();
        Profession prof2 = Profession.builder().id(2L).name("Python").category(cat1).build();
        Profession prof3 = Profession.builder().id(3L).name("Manager").category(cat2).build();

        Job job1 = Job.builder().id(1L).profession(prof1).build();
        Job job2 = Job.builder().id(2L).profession(prof2).build();
        Job job3 = Job.builder().id(3L).profession(prof2).build();
        Job job4 = Job.builder().id(4L).profession(prof3).build();

        when(jobService.getAll()).thenReturn(List.of(job1, job2, job3, job4));

        Map<String, Long> result = statisticsService.getTopCategoriesByJobs();

        assertNotNull(result);
        assertEquals(3L, result.get("IT"));
        assertEquals(1L, result.get("Marketing"));
        assertTrue(result.size() <= 5);
    }

    @Test
    void testGetJobsByStatus_EmptyJobs() {
        when(jobService.getAll()).thenReturn(List.of());

        Map<String, Long> result = statisticsService.getJobsByStatus();

        assertNotNull(result);
        assertEquals(0L, result.getOrDefault("APPROVED", 0L));
    }

    @Test
    void testGetUsersByExperience_EmptyUsers() {
        when(userService.getAll()).thenReturn(List.of());

        Map<String, Long> result = statisticsService.getUsersByExperience();

        assertNotNull(result);
        assertEquals(0L, result.getOrDefault("1-3 yil", 0L));
    }

    @Test
    void testGetTopCategoriesByJobs_JobsWithoutProfession() {
        Job job1 = Job.builder().id(1L).profession(null).build();
        Job job2 = Job.builder().id(2L).profession(null).build();

        when(jobService.getAll()).thenReturn(List.of(job1, job2));

        Map<String, Long> result = statisticsService.getTopCategoriesByJobs();

        assertNotNull(result);
        assertEquals(0, result.size());
    }
}
