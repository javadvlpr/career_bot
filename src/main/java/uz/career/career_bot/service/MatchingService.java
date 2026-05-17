package uz.career.career_bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.career.career_bot.dto.MatchResultDTO;
import uz.career.career_bot.entity.Job;
import uz.career.career_bot.entity.Profession;
import uz.career.career_bot.entity.Skill;
import uz.career.career_bot.entity.User;
import uz.career.career_bot.enums.ExperienceLevel;
import uz.career.career_bot.enums.JobStatus;
import uz.career.career_bot.enums.SearchStatus;
import uz.career.career_bot.repository.JobRepository;
import uz.career.career_bot.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    @Value("${matching.weight.skill:0.30}")
    private double skillWeight;

    @Value("${matching.weight.profession:0.25}")
    private double professionWeight;

    @Value("${matching.weight.experience:0.20}")
    private double experienceWeight;

    @Value("${matching.weight.location:0.15}")
    private double locationWeight;

    @Value("${matching.weight.salary:0.10}")
    private double salaryWeight;

    /**
     * Finding the most suitable vacancies for the user
     * score = (w1 * skill) + (w2 * profession) + (w3 * experience)
     *       + (w4 * location) + (w5 * salary)
     */
    @Transactional(readOnly = true)
    public List<MatchResultDTO> findJobsForUser(User user, int limit) {
        List<Job> approvedJobs = jobRepository.findByStatus(JobStatus.APPROVED);
        List<MatchResultDTO> results = new ArrayList<>();

        for (Job job : approvedJobs) {
            double score = calculateScore(user, job);
            if (score > 0.1) {
                results.add(buildJobMatchResult(job, score));
            }
        }

        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return results.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * Finding the most suitable candidates for a vacancy (for HR)
     * Only ACTIVE and PASSIVE users
     */
    @Transactional(readOnly = true)
    public List<MatchResultDTO> findCandidatesForJob(Job job, int limit) {
        List<User> availableUsers = userRepository.findBySearchStatusIn(
                List.of(SearchStatus.ACTIVE, SearchStatus.PASSIVE)
        );
        List<MatchResultDTO> results = new ArrayList<>();

        for (User user : availableUsers) {
            double score = calculateScore(user, job);
            if (score > 0.1) {
                results.add(buildUserMatchResult(user, score));
            }
        }

        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return results.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * Weighted multi-criteria matching formula.
     * All component scores are normalized to [0.0, 1.0].
     * Weights are externalized to application.yaml under matching.weight.*
     */
    public double calculateScore(User user, Job job) {
        double skillScore      = calculateSkillMatch(user.getSkills(), job.getSkills());
        double professionScore = calculateProfessionMatch(user.getProfession(), job.getProfession());
        double expScore        = calculateExperienceMatch(user.getExperienceLevel(), job.getExperienceLevel());
        double locScore        = calculateLocationMatch(user.getLocation(), job.getLocation());
        double salaryScore     = calculateSalaryMatch(user.getExpectedSalary(), job.getSalaryMin(), job.getSalaryMax());

        return skillWeight      * skillScore
                + professionWeight * professionScore
                + experienceWeight * expScore
                + locationWeight   * locScore
                + salaryWeight     * salaryScore;
    }

    public double calculateProfessionMatch(Profession userProf, Profession jobProf) {
        if (userProf == null || jobProf == null) return 0.5;
        if (userProf.getId().equals(jobProf.getId())) return 1.0;
        if (userProf.getCategory() != null && jobProf.getCategory() != null
                && userProf.getCategory().getId().equals(jobProf.getCategory().getId())) {
            return 0.5;
        }
        return 0.0;
    }

    public double calculateSkillMatch(Set<Skill> userSkills, Set<Skill> jobSkills) {
        if (jobSkills == null || jobSkills.isEmpty()) {
            return 0.5;
        }
        if (userSkills == null || userSkills.isEmpty()) {
            return 0.0;
        }

        Set<Long> userSkillIds = userSkills.stream()
                .map(Skill::getId)
                .collect(Collectors.toSet());

        long matchCount = jobSkills.stream()
                .filter(skill -> userSkillIds.contains(skill.getId()))
                .count();

        return (double) matchCount / jobSkills.size();
    }

    public double calculateExperienceMatch(ExperienceLevel userLevel, ExperienceLevel jobLevel) {
        if (userLevel == null || jobLevel == null) {
            return 0.5;
        }

        int diff = Math.abs(userLevel.ordinal() - jobLevel.ordinal());
        return switch (diff) {
            case 0 -> 1.0;
            case 1 -> 0.6;
            case 2 -> 0.3;
            default -> 0.1;
        };
    }

    public double calculateLocationMatch(String userLocation, String jobLocation) {
        if (userLocation == null || jobLocation == null) {
            return 0.5;
        }

        String userLoc = userLocation.trim().toLowerCase();
        String jobLoc = jobLocation.trim().toLowerCase();

        if (userLoc.equals(jobLoc)) {
            return 1.0;
        }
        if (jobLoc.contains("remote") || userLoc.contains("remote")) {
            return 0.8;
        }
        if (jobLoc.contains(userLoc) || userLoc.contains(jobLoc)) {
            return 0.7;
        }
        return 0.0;
    }

    public double calculateSalaryMatch(Integer expectedSalary, Integer salaryMin, Integer salaryMax) {
        if (expectedSalary == null || (salaryMin == null && salaryMax == null)) {
            return 0.5;
        }

        int min = salaryMin != null ? salaryMin : 0;
        int max = salaryMax != null ? salaryMax : min * 2;

        if (max == 0) {
            return 0.5;
        }

        if (expectedSalary >= min && expectedSalary <= max) {
            return 1.0;
        }

        double mid = (min + max) / 2.0;
        double diff = Math.abs(expectedSalary - mid);
        double range = max - min > 0 ? max - min : mid;
        double ratio = diff / range;

        return Math.max(0.0, 1.0 - ratio);
    }

    private MatchResultDTO buildJobMatchResult(Job job, double score) {
        String salaryRange = "";
        if (job.getSalaryMin() != null || job.getSalaryMax() != null) {
            salaryRange = (job.getSalaryMin() != null ? job.getSalaryMin() : "?")
                    + " - "
                    + (job.getSalaryMax() != null ? job.getSalaryMax() : "?");
        }

        String subtitle = "Jooble";
        if (job.getCompany() != null) {
            subtitle = job.getCompany().getCompanyName();
        }

        return MatchResultDTO.builder()
                .targetId(job.getId())
                .title(job.getTitle())
                .subtitle(subtitle)
                .score(score)
                .scorePercent((int) Math.round(score * 100))
                .location(job.getLocation())
                .experience(job.getExperienceLevel() != null ? job.getExperienceLevel().name() : "")
                .salaryRange(salaryRange)
                .build();
    }

    private MatchResultDTO buildUserMatchResult(User user, double score) {
        String profName = user.getProfession() != null ? user.getProfession().getName() : "-";

        return MatchResultDTO.builder()
                .targetId(user.getId())
                .title(user.getName() != null ? user.getName() : "User #" + user.getId())
                .subtitle(profName)
                .score(score)
                .scorePercent((int) Math.round(score * 100))
                .location(user.getLocation())
                .experience(user.getExperienceLevel() != null ? user.getExperienceLevel().name() : "")
                .searchStatus(user.getSearchStatus().name())
                .build();
    }
}