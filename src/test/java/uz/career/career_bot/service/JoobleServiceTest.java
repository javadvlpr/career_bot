package uz.career.career_bot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import uz.career.career_bot.dto.JoobleResponse;
import uz.career.career_bot.entity.Job;
import uz.career.career_bot.enums.JobSource;
import uz.career.career_bot.enums.JobStatus;
import uz.career.career_bot.enums.JobType;
import uz.career.career_bot.repository.JobRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JoobleServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private RestTemplate restTemplate;

    private JoobleService joobleService;

    @BeforeEach
    void setUp() {
        joobleService = new JoobleService(jobRepository);
        ReflectionTestUtils.setField(joobleService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(joobleService, "baseUrl", "https://api.jooble.org/api");
        // RestTemplate ni mock bilan almashtiramiz
        ReflectionTestUtils.setField(joobleService, "restTemplate", restTemplate);
    }

    // ------------------------------------------------------------------ //
    //  searchJobs()
    // ------------------------------------------------------------------ //

    @Test
    void testSearchJobs_Success() {
        JoobleResponse mockResponse = buildResponse("Java Developer", "Remote", 1l);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(JoobleResponse.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));

        JoobleResponse result = joobleService.searchJobs("java", "remote");

        assertNotNull(result);
        assertEquals(1, result.getJobs().size());
        assertEquals("Java Developer", result.getJobs().get(0).getTitle());
    }

    @Test
    void testSearchJobs_ApiError_ReturnsNull() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(JoobleResponse.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        JoobleResponse result = joobleService.searchJobs("java", "remote");

        assertNull(result);
    }

    // ------------------------------------------------------------------ //
    //  importJobs()
    // ------------------------------------------------------------------ //

    @Test
    void testImportJobs_NullResponse_ReturnsZero() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(JoobleResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        int result = joobleService.importJobs("java", "tashkent");

        assertEquals(0, result);
        verify(jobRepository, never()).save(any());
    }

    @Test
    void testImportJobs_EmptyJobsList_ReturnsZero() {
        JoobleResponse emptyResponse = new JoobleResponse();
        emptyResponse.setJobs(List.of());

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(JoobleResponse.class)))
                .thenReturn(ResponseEntity.ok(emptyResponse));

        int result = joobleService.importJobs("java", "tashkent");

        assertEquals(0, result);
        verify(jobRepository, never()).save(any());
    }

    @Test
    void testImportJobs_SavesNewJobs() {
        JoobleResponse mockResponse = buildResponse("Java Developer", "Remote", 1l);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(JoobleResponse.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));
        when(jobRepository.existsByExternalId(1l)).thenReturn(false);

        int result = joobleService.importJobs("java", "remote");

        assertEquals(1, result);
        verify(jobRepository, times(1)).save(any(Job.class));
    }

    @Test
    void testImportJobs_SkipsDuplicates() {
        JoobleResponse mockResponse = buildResponse("Java Developer", "Remote", 1l);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(JoobleResponse.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));
        when(jobRepository.existsByExternalId(1l)).thenReturn(true);

        int result = joobleService.importJobs("java", "remote");

        assertEquals(0, result);
        verify(jobRepository, never()).save(any());
    }

    @Test
    void testImportJobs_SetsCorrectJobProperties() {
        JoobleResponse mockResponse = buildResponse("Python Dev", "Remote", 1l);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(JoobleResponse.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));
        when(jobRepository.existsByExternalId(1l)).thenReturn(false);

        joobleService.importJobs("python", "remote");

        verify(jobRepository).save(argThat(job ->
                JobSource.JOOBLE.equals(job.getSource()) &&
                        JobStatus.APPROVED.equals(job.getStatus()) &&
                        job.getExpiresAt() != null &&
                        job.getExpiresAt().isAfter(LocalDateTime.now())
        ));
    }

    @Test
    void testImportJobs_ParsesSalary() {
        JoobleResponse mockResponse = buildResponseWithSalary("Dev", "Remote", 1l, "$100k - $150k");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(JoobleResponse.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));
        when(jobRepository.existsByExternalId(1l)).thenReturn(false);

        joobleService.importJobs("dev", "remote");

        verify(jobRepository).save(argThat(job ->
                job.getSalaryMin() != null && job.getSalaryMin() > 0 &&
                        job.getSalaryMax() != null && job.getSalaryMax() > 0
        ));
    }

    // ------------------------------------------------------------------ //
    //  JobType parsing
    // ------------------------------------------------------------------ //

    @Test
    void testImportJobs_RemoteLocation_SetsRemoteJobType() {
        JoobleResponse mockResponse = buildResponse("Dev", "Remote", 1l);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(JoobleResponse.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));
        when(jobRepository.existsByExternalId(1l)).thenReturn(false);

        joobleService.importJobs("dev", "remote");

        verify(jobRepository).save(argThat(job ->
                JobType.REMOTE.equals(job.getJobType())
        ));
    }

    // ------------------------------------------------------------------ //
    //  importAllCategories()
    // ------------------------------------------------------------------ //

    @Test
    void testImportAllCategories_CallsImportForEachCategory() {
        // Har bir kategoriya uchun bo'sh javob qaytaramiz (tez ishlashi uchun)
        JoobleResponse emptyResponse = new JoobleResponse();
        emptyResponse.setJobs(List.of());

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(JoobleResponse.class)))
                .thenReturn(ResponseEntity.ok(emptyResponse));

        int result = joobleService.importAllCategories();

        assertEquals(0, result);
        // 10 ta kategoriya bor, har biri uchun 1 ta so'rov
        verify(restTemplate, times(10))
                .exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(JoobleResponse.class));
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    private JoobleResponse buildResponse(String title, String location, Long id) {
        JoobleResponse.JoobleJob job = new JoobleResponse.JoobleJob();
        job.setTitle(title);
        job.setLocation(location);
        job.setId(id);
        job.setLink("https://example.com/job/1");
        job.setSnippet("Test description");

        JoobleResponse response = new JoobleResponse();
        response.setJobs(List.of(job));
        response.setTotalCount(1);
        return response;
    }

    private JoobleResponse buildResponseWithSalary(String title, String location, Long id, String salary) {
        JoobleResponse.JoobleJob job = new JoobleResponse.JoobleJob();
        job.setTitle(title);
        job.setLocation(location);
        job.setId(id);
        job.setLink("https://example.com/job/1");
        job.setSnippet("Test description");
        job.setSalary(salary);

        JoobleResponse response = new JoobleResponse();
        response.setJobs(List.of(job));
        response.setTotalCount(1);
        return response;
    }
}