package uz.career.career_bot.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uz.career.career_bot.service.JoobleService;
import uz.career.career_bot.service.JobService;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobScheduler {

    private final JoobleService joobleService;
    private final JobService jobService;

    /**
     * Import new vacancies from Jooble every 12 hours
     * Cron: every day at 08:00 and 20:00
     */
    @Scheduled(cron = "0 0 8,20 * * *")
    public void importJobsFromJooble() {
        log.info("Starting scheduled Jooble import...");
        int imported = joobleService.importAllCategories();
        log.info("Scheduled import completed: {} new jobs", imported);
    }

    /**
     * Make expired vacancies EXPIRED every day at midnight
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void expireOldJobs() {
        log.info("Checking for expired jobs...");
        jobService.expireOldJobs();
        log.info("Expired jobs check completed");
    }
}