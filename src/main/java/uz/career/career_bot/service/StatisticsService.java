package uz.career.career_bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.career.career_bot.entity.Job;
import uz.career.career_bot.entity.User;
import uz.career.career_bot.enums.ExperienceLevel;
import uz.career.career_bot.enums.JobStatus;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final JobService jobService;
    private final UserService userService;

    /**
     * Calculation of vacancies by status
     */
    public Map<String, Long> getJobsByStatus() {
        Map<String, Long> result = new LinkedHashMap<>();
        List<Job> all = jobService.getAll();
        for (JobStatus status : JobStatus.values()) {
            long count = all.stream().filter(j -> j.getStatus() == status).count();
            result.put(status.name(), count);
        }
        return result;
    }

    /**
     * Counting users by experience level
     */
    public Map<String, Long> getUsersByExperience() {
        Map<String, Long> result = new LinkedHashMap<>();
        List<User> all = userService.getAll();
        result.put("0-1 yil", countByLevel(all, ExperienceLevel.NO_EXPERIENCE));
        result.put("1-3 yil", countByLevel(all, ExperienceLevel.JUNIOR));
        result.put("3-5 yil", countByLevel(all, ExperienceLevel.MIDDLE));
        result.put("5+ yil", countByLevel(all, ExperienceLevel.SENIOR));
        return result;
    }

    /**
     * Top 5 categories — categories with the most vacancies
     */
    public Map<String, Long> getTopCategoriesByJobs() {
        Map<String, Long> raw = new HashMap<>();
        for (Job j : jobService.getAll()) {
            if (j.getProfession() != null && j.getProfession().getCategory() != null) {
                String catName = j.getProfession().getCategory().getName();
                raw.merge(catName, 1L, Long::sum);
            }
        }
        Map<String, Long> result = new LinkedHashMap<>();
        raw.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> result.put(e.getKey(), e.getValue()));
        return result;
    }

    private long countByLevel(List<User> users, ExperienceLevel level) {
        return users.stream().filter(u -> u.getExperienceLevel() == level).count();
    }
}