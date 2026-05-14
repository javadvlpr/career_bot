package uz.career.career_bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.career.career_bot.entity.Job;
import uz.career.career_bot.entity.SavedJob;
import uz.career.career_bot.entity.User;
import uz.career.career_bot.repository.SavedJobRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedJobService {

    private final SavedJobRepository savedJobRepository;

    public void saveJob(User user, Job job) {
        if (!savedJobRepository.existsByUserIdAndJobId(user.getId(), job.getId())) {
            SavedJob savedJob = SavedJob.builder()
                    .user(user)
                    .job(job)
                    .build();
            savedJobRepository.save(savedJob);
        }
    }

    public void unsaveJob(Long userId, Long jobId) {
        savedJobRepository.findByUserIdAndJobId(userId, jobId)
                .ifPresent(savedJobRepository::delete);
    }

    public List<SavedJob> getUserSavedJobs(Long userId) {
        return savedJobRepository.findByUserId(userId);
    }

    public boolean isJobSaved(Long userId, Long jobId) {
        return savedJobRepository.existsByUserIdAndJobId(userId, jobId);
    }

    public List<SavedJob> getUserSavedJobsWithDetails(Long userId) {
        return savedJobRepository.findByUserIdWithDetails(userId);
    }
}