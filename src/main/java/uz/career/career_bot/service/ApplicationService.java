package uz.career.career_bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.career.career_bot.entity.Application;
import uz.career.career_bot.entity.Company;
import uz.career.career_bot.entity.Job;
import uz.career.career_bot.entity.User;
import uz.career.career_bot.enums.ApplicationStatus;
import uz.career.career_bot.exception.AlreadyExistsException;
import uz.career.career_bot.exception.NotFoundException;
import uz.career.career_bot.repository.ApplicationRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    public Application apply(Job job, User user, Company company, String coverLetter) {
        if (applicationRepository.existsByJobIdAndUserId(job.getId(), user.getId())) {
            throw new AlreadyExistsException("You have already applied for this vacancy.");
        }
        Application application = Application.builder()
                .job(job)
                .user(user)
                .company(company)
                .coverLetter(coverLetter)
                .status(ApplicationStatus.PENDING)
                .build();
        return applicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public List<Application> getUserApplications(Long userId) {
        return applicationRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Application> getJobApplications(Long jobId) {
        return applicationRepository.findByJobId(jobId);
    }

    @Transactional(readOnly = true)
    public List<Application> getCompanyApplications(Long companyId) {
        return applicationRepository.findByCompanyId(companyId);
    }

    public boolean hasApplied(Long jobId, Long userId) {
        return applicationRepository.existsByJobIdAndUserId(jobId, userId);
    }

    @Transactional(readOnly = true)
    public Application getById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application", id));
    }

    public Application updateStatus(Long applicationId, ApplicationStatus status) {
        Application app = getById(applicationId);
        app.setStatus(status);
        app.setReviewedAt(LocalDateTime.now());
        return applicationRepository.save(app);
    }

    public long countApplicationsForJob(Long jobId) {
        return applicationRepository.countByJobId(jobId);
    }

    public List<Application> getAll() {
        return applicationRepository.findAll();
    }

    public List<Application> getByStatus(ApplicationStatus status) {
        return applicationRepository.findAll().stream()
                .filter(a -> a.getStatus() == status)
                .toList();
    }

    public long count() {
        return applicationRepository.count();
    }
}