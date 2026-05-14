package uz.career.career_bot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uz.career.career_bot.entity.Application;
import uz.career.career_bot.entity.Company;
import uz.career.career_bot.entity.Job;
import uz.career.career_bot.entity.User;

import uz.career.career_bot.enums.ApplicationStatus;

import uz.career.career_bot.exception.AlreadyExistsException;
import uz.career.career_bot.exception.NotFoundException;

import uz.career.career_bot.repository.ApplicationRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    private ApplicationService applicationService;

    private Application application;
    private Job job;
    private User user;
    private Company company;

    @BeforeEach
    void setUp() {

        applicationService =
                new ApplicationService(applicationRepository);

        user = User.builder()
                .id(1L)
                .name("John Doe")
                .build();

        company = Company.builder()
                .id(1L)
                .companyName("Tech Corp")
                .build();

        job = Job.builder()
                .id(1L)
                .title("Senior Developer")
                .company(company)
                .build();

        application = Application.builder()
                .id(1L)
                .job(job)
                .user(user)
                .company(company)
                .status(ApplicationStatus.PENDING)
                .coverLetter("I am interested in this position")
                .build();
    }

    @Test
    @DisplayName("apply — duplicate application throws exception")
    void apply_alreadyApplied_throwsException() {

        when(applicationRepository.existsByJobIdAndUserId(1L, 1L))
                .thenReturn(true);

        assertThrows(
                AlreadyExistsException.class,
                () -> applicationService.apply(
                        job,
                        user,
                        company,
                        null
                )
        );

        verify(applicationRepository)
                .existsByJobIdAndUserId(1L, 1L);

        verify(applicationRepository, never())
                .save(any(Application.class));
    }

    @Test
    @DisplayName("apply — successfully creates new application")
    void apply_new_returnsApplication() {

        when(applicationRepository.existsByJobIdAndUserId(1L, 1L))
                .thenReturn(false);

        when(applicationRepository.save(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Application result =
                applicationService.apply(
                        job,
                        user,
                        company,
                        "Cover letter"
                );

        assertNotNull(result);

        assertEquals(
                "Cover letter",
                result.getCoverLetter()
        );

        assertEquals(
                ApplicationStatus.PENDING,
                result.getStatus()
        );

        assertEquals(
                user,
                result.getUser()
        );

        assertEquals(
                job,
                result.getJob()
        );

        verify(applicationRepository)
                .save(any(Application.class));
    }

    @Test
    @DisplayName("getUserApplications returns user applications")
    void testGetUserApplications() {

        List<Application> applications =
                List.of(application);

        when(applicationRepository.findByUserId(1L))
                .thenReturn(applications);

        List<Application> result =
                applicationService.getUserApplications(1L);

        assertNotNull(result);

        assertEquals(1, result.size());

        verify(applicationRepository)
                .findByUserId(1L);
    }

    @Test
    @DisplayName("getJobApplications returns job applications")
    void testGetJobApplications() {

        List<Application> applications =
                List.of(application);

        when(applicationRepository.findByJobId(1L))
                .thenReturn(applications);

        List<Application> result =
                applicationService.getJobApplications(1L);

        assertNotNull(result);

        assertEquals(1, result.size());

        verify(applicationRepository)
                .findByJobId(1L);
    }

    @Test
    @DisplayName("getCompanyApplications returns company applications")
    void testGetCompanyApplications() {

        List<Application> applications =
                List.of(application);

        when(applicationRepository.findByCompanyId(1L))
                .thenReturn(applications);

        List<Application> result =
                applicationService.getCompanyApplications(1L);

        assertNotNull(result);

        assertEquals(1, result.size());

        verify(applicationRepository)
                .findByCompanyId(1L);
    }

    @Test
    @DisplayName("hasApplied returns true")
    void testHasApplied_True() {

        when(applicationRepository.existsByJobIdAndUserId(1L, 1L))
                .thenReturn(true);

        boolean result =
                applicationService.hasApplied(1L, 1L);

        assertTrue(result);
    }

    @Test
    @DisplayName("hasApplied returns false")
    void testHasApplied_False() {

        when(applicationRepository.existsByJobIdAndUserId(1L, 1L))
                .thenReturn(false);

        boolean result =
                applicationService.hasApplied(1L, 1L);

        assertFalse(result);
    }

    @Test
    @DisplayName("getById returns application")
    void testGetById_Success() {

        when(applicationRepository.findById(1L))
                .thenReturn(Optional.of(application));

        Application result =
                applicationService.getById(1L);

        assertNotNull(result);

        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("getById throws NotFoundException")
    void getById_notFound_throws() {

        when(applicationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> applicationService.getById(999L)
        );
    }

    @Test
    @DisplayName("updateStatus updates application status")
    void testUpdateStatus() {

        when(applicationRepository.findById(1L))
                .thenReturn(Optional.of(application));

        when(applicationRepository.save(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Application result =
                applicationService.updateStatus(
                        1L,
                        ApplicationStatus.ACCEPTED
                );

        assertNotNull(result);

        assertEquals(
                ApplicationStatus.ACCEPTED,
                result.getStatus()
        );

        assertNotNull(result.getReviewedAt());

        verify(applicationRepository)
                .save(any(Application.class));
    }

    @Test
    @DisplayName("countApplicationsForJob returns count")
    void testCountApplicationsForJob() {

        when(applicationRepository.countByJobId(1L))
                .thenReturn(5L);

        long result =
                applicationService.countApplicationsForJob(1L);

        assertEquals(5L, result);
    }

    @Test
    @DisplayName("getAll returns all applications")
    void testGetAll() {

        List<Application> applications =
                List.of(application);

        when(applicationRepository.findAll())
                .thenReturn(applications);

        List<Application> result =
                applicationService.getAll();

        assertNotNull(result);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getByStatus returns filtered applications")
    void testGetByStatus() {

        List<Application> applications =
                List.of(application);

        when(applicationRepository.findAll())
                .thenReturn(applications);

        List<Application> result =
                applicationService.getByStatus(
                        ApplicationStatus.PENDING
                );

        assertNotNull(result);

        assertEquals(1, result.size());

        assertEquals(
                ApplicationStatus.PENDING,
                result.get(0).getStatus()
        );
    }

    @Test
    @DisplayName("count returns total applications count")
    void testCount() {

        when(applicationRepository.count())
                .thenReturn(15L);

        long result =
                applicationService.count();

        assertEquals(15L, result);
    }
}