package uz.career.career_bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.career.career_bot.dto.JobDTO;
import uz.career.career_bot.entity.Job;
import uz.career.career_bot.entity.Profession;
import uz.career.career_bot.entity.Skill;
import uz.career.career_bot.enums.ExperienceLevel;
import uz.career.career_bot.enums.JobSource;
import uz.career.career_bot.enums.JobStatus;
import uz.career.career_bot.enums.JobType;
import uz.career.career_bot.exception.NotFoundException;
import uz.career.career_bot.exception.ValidationException;
import uz.career.career_bot.repository.JobRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final ProfessionService professionService;

    public Job save(Job job) {
        return jobRepository.save(job);
    }

    public Job getById(Long id) {
        return jobRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Vacancy ", id));
    }

    public Job createByAdmin(String title, String description, String requirements,
                             Long professionId, String location, Integer salaryMin,
                             Integer salaryMax, JobType jobType,
                             ExperienceLevel experienceLevel,
                             String externalUrl, String contactInfo) {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("The job title cannot be empty.");
        }
        if (externalUrl == null && contactInfo == null) {
            throw new ValidationException("URL or contact information is required.");
        }

        Job job = Job.builder()
                .title(title.trim())
                .description(description)
                .requirements(requirements)
                .location(location)
                .salaryMin(salaryMin)
                .salaryMax(salaryMax)
                .jobType(jobType)
                .experienceLevel(experienceLevel)
                .externalUrl(externalUrl != null && !externalUrl.isBlank() ? externalUrl.trim() : null)
                .contactInfo(contactInfo != null && !contactInfo.isBlank() ? contactInfo.trim() : null)
                .source(JobSource.ADMIN)
                .status(JobStatus.APPROVED)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        if (professionId != null) {
            Profession prof = professionService.getById(professionId);
            job.setProfession(prof);
        }
        return jobRepository.save(job);
    }

    public List<Job> getAll() {
        return jobRepository.findAllWithDetails();
    }

    public List<Job> getApprovedJobs() {
        return jobRepository.findByStatusWithDetails(JobStatus.APPROVED);
    }

    public List<Job> getPendingJobs() {
        return jobRepository.findByStatusWithDetails(JobStatus.PENDING);
    }

    public List<Job> getByCompanyId(Long companyId) {
        return jobRepository.findByCompanyIdWithDetails(companyId);
    }

    public void approve(Long id) {
        Job job = getById(id);
        job.setStatus(JobStatus.APPROVED);
        jobRepository.save(job);
    }

    public void reject(Long id) {
        Job job = getById(id);
        job.setStatus(JobStatus.REJECTED);
        jobRepository.save(job);
    }

    public void close(Long id) {
        Job job = getById(id);
        job.setStatus(JobStatus.CLOSED);
        jobRepository.save(job);
    }

    public void expireOldJobs() {
        List<Job> expiredJobs = jobRepository
                .findByStatusAndExpiresAtBefore(JobStatus.APPROVED, LocalDateTime.now());
        for (Job job : expiredJobs) {
            job.setStatus(JobStatus.EXPIRED);
            jobRepository.save(job);
        }
    }

    public boolean existsByExternalId(Long externalId) {
        return jobRepository.existsByExternalId(externalId);
    }

    public JobDTO toDTO(Job job) {
        Set<String> skillNames = job.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toSet());

        String companyName = job.getCompany() != null
                ? job.getCompany().getCompanyName()
                : "Jooble";

        return JobDTO.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .requirements(job.getRequirements())
                .skillNames(skillNames)
                .experienceLevel(job.getExperienceLevel())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .location(job.getLocation())
                .jobType(job.getJobType())
                .status(job.getStatus())
                .source(job.getSource())
                .companyName(companyName)
                .externalUrl(job.getExternalUrl())
                .createdAt(job.getCreatedAt())
                .expiresAt(job.getExpiresAt())
                .build();
    }

    public long count() {
        return jobRepository.count();
    }
}