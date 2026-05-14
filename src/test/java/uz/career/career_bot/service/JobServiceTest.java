package uz.career.career_bot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.career.career_bot.entity.Company;
import uz.career.career_bot.entity.Job;
import uz.career.career_bot.entity.Profession;
import uz.career.career_bot.enums.ExperienceLevel;
import uz.career.career_bot.enums.JobSource;
import uz.career.career_bot.enums.JobStatus;
import uz.career.career_bot.enums.JobType;
import uz.career.career_bot.exception.NotFoundException;
import uz.career.career_bot.exception.ValidationException;
import uz.career.career_bot.repository.JobRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ProfessionService professionService;

    private JobService jobService;
    private Job job;
    private Profession profession;
    private Company company;

    @BeforeEach
    void setUp() {
        jobService = new JobService(jobRepository, professionService);

        company = Company.builder()
                .id(1L)
                .companyName("Tech Corp")
                .build();

        profession = Profession.builder().id(1L).name("Java Developer").build();
        job = Job.builder()
                .id(1L)
                .title("Java Developer")
                .description("Looking for experienced Java developer")
                .requirements("5+ years experience")
                .location("Tashkent")
                .salaryMin(5000)
                .salaryMax(10000)
                .jobType(JobType.FULL_TIME)
                .experienceLevel(ExperienceLevel.SENIOR)
                .profession(profession)
                .company(company)
                .source(JobSource.ADMIN)
                .status(JobStatus.APPROVED)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }

    @Test
    @DisplayName("save — job muvaffaqiyatli saqlanadi")
    void testSave() {
        when(jobRepository.save(any())).thenReturn(job);

        Job result = jobService.save(job);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(jobRepository).save(job);
    }

    @Test
    @DisplayName("getById — job topilsa qaytadi")
    void testGetById_Success() {
        when(jobRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(job));

        Job result = jobService.getById(1L);

        assertNotNull(result);
        assertEquals("Java Developer", result.getTitle());
    }

    @Test
    @DisplayName("getById — job topilmasa NotFoundException tashlaydi")
    void testGetById_NotFound() {
        when(jobRepository.findByIdWithDetails(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> jobService.getById(1L));
    }

    @Test
    @DisplayName("createByAdmin — admin tomonidan yangi job yaratadi")
    void testCreateByAdmin_Success() {
        when(professionService.getById(1L)).thenReturn(profession);
        when(jobRepository.save(any())).thenReturn(job);

        Job result = jobService.createByAdmin(
                "Java Developer",
                "Description",
                "Requirements",
                1L,
                "Tashkent",
                5000,
                10000,
                JobType.FULL_TIME,
                ExperienceLevel.SENIOR,
                "https://example.com",
                "contact@company.com"
        );

        assertNotNull(result);
        assertEquals(JobStatus.APPROVED, result.getStatus());
        assertEquals(JobSource.ADMIN, result.getSource());
        verify(jobRepository).save(any());
    }

    @Test
    @DisplayName("createByAdmin — bo'sh nom uchun ValidationException tashlaydi")
    void testCreateByAdmin_EmptyTitle() {
        assertThrows(ValidationException.class, () ->
                jobService.createByAdmin(
                        "",
                        "Description",
                        "Requirements",
                        1L,
                        "Tashkent",
                        5000,
                        10000,
                        JobType.FULL_TIME,
                        ExperienceLevel.SENIOR,
                        "https://example.com",
                        null
                )
        );
    }

    @Test
    @DisplayName("createByAdmin — URL yoki kontakt bo'lmaganda ValidationException tashlaydi")
    void testCreateByAdmin_NoUrlOrContact() {
        assertThrows(ValidationException.class, () ->
                jobService.createByAdmin(
                        "Java Developer",
                        "Description",
                        "Requirements",
                        1L,
                        "Tashkent",
                        5000,
                        10000,
                        JobType.FULL_TIME,
                        ExperienceLevel.SENIOR,
                        null,
                        null
                )
        );
    }

    @Test
    @DisplayName("getAll — barcha joblarni qaytadi")
    void testGetAll() {
        List<Job> jobs = List.of(job);
        when(jobRepository.findAllWithDetails()).thenReturn(jobs);

        List<Job> result = jobService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getApprovedJobs — faqat tasdiqlangan joblarni qaytadi")
    void testGetApprovedJobs() {
        List<Job> jobs = List.of(job);
        when(jobRepository.findByStatusWithDetails(JobStatus.APPROVED)).thenReturn(jobs);

        List<Job> result = jobService.getApprovedJobs();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getPendingJobs — faqat kutilayotgan joblarni qaytadi")
    void testGetPendingJobs() {
        List<Job> jobs = List.of(job);
        when(jobRepository.findByStatusWithDetails(JobStatus.PENDING)).thenReturn(jobs);

        List<Job> result = jobService.getPendingJobs();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getByCompanyId — kompaniya uchun joblarni qaytadi")
    void testGetByCompanyId() {
        List<Job> jobs = List.of(job);
        when(jobRepository.findByCompanyIdWithDetails(1L)).thenReturn(jobs);

        List<Job> result = jobService.getByCompanyId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("approve — job tasdiqladi")
    void testApprove() {
        when(jobRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenReturn(job);

        jobService.approve(1L);

        assertEquals(JobStatus.APPROVED, job.getStatus());
        verify(jobRepository).save(any());
    }

    @Test
    @DisplayName("reject — job rad etiladi")
    void testReject() {
        when(jobRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenReturn(job);

        jobService.reject(1L);

        assertEquals(JobStatus.REJECTED, job.getStatus());
        verify(jobRepository).save(any());
    }
}
