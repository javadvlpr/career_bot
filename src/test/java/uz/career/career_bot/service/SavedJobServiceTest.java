package uz.career.career_bot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.career.career_bot.entity.Job;
import uz.career.career_bot.entity.SavedJob;
import uz.career.career_bot.entity.User;
import uz.career.career_bot.repository.SavedJobRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavedJobServiceTest {

    @Mock
    private SavedJobRepository savedJobRepository;

    private SavedJobService savedJobService;
    private SavedJob savedJob;
    private User user;
    private Job job;

    @BeforeEach
    void setUp() {
        savedJobService = new SavedJobService(savedJobRepository);

        user = User.builder().id(1L).name("John Doe").build();
        job = Job.builder().id(1L).title("Java Developer").build();
        savedJob = SavedJob.builder()
                .id(1L)
                .user(user)
                .job(job)
                .build();
    }

    @Test
    void testSaveJob_NewJob() {
        when(savedJobRepository.existsByUserIdAndJobId(1L, 1L)).thenReturn(false);
        when(savedJobRepository.save(any())).thenReturn(savedJob);

        savedJobService.saveJob(user, job);

        verify(savedJobRepository).save(any());
    }

    @Test
    void testSaveJob_AlreadySaved() {
        when(savedJobRepository.existsByUserIdAndJobId(1L, 1L)).thenReturn(true);

        savedJobService.saveJob(user, job);

        verify(savedJobRepository, never()).save(any());
    }

    @Test
    void testUnsaveJob() {
        when(savedJobRepository.findByUserIdAndJobId(1L, 1L)).thenReturn(Optional.of(savedJob));

        savedJobService.unsaveJob(1L, 1L);

        verify(savedJobRepository).delete(savedJob);
    }

    @Test
    void testUnsaveJob_NotFound() {
        when(savedJobRepository.findByUserIdAndJobId(1L, 1L)).thenReturn(Optional.empty());

        savedJobService.unsaveJob(1L, 1L);

        verify(savedJobRepository, never()).delete(any());
    }

    @Test
    void testGetUserSavedJobs() {
        List<SavedJob> savedJobs = List.of(savedJob);
        when(savedJobRepository.findByUserId(1L)).thenReturn(savedJobs);

        List<SavedJob> result = savedJobService.getUserSavedJobs(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(job.getId(), result.get(0).getJob().getId());
    }

    @Test
    void testIsJobSaved_True() {
        when(savedJobRepository.existsByUserIdAndJobId(1L, 1L)).thenReturn(true);

        boolean result = savedJobService.isJobSaved(1L, 1L);

        assertTrue(result);
    }

    @Test
    void testIsJobSaved_False() {
        when(savedJobRepository.existsByUserIdAndJobId(1L, 1L)).thenReturn(false);

        boolean result = savedJobService.isJobSaved(1L, 1L);

        assertFalse(result);
    }

    @Test
    void testGetUserSavedJobsWithDetails() {
        List<SavedJob> savedJobs = List.of(savedJob);
        when(savedJobRepository.findByUserIdWithDetails(1L)).thenReturn(savedJobs);

        List<SavedJob> result = savedJobService.getUserSavedJobsWithDetails(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
