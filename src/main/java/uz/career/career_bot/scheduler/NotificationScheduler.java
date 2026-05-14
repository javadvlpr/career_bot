package uz.career.career_bot.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uz.career.career_bot.dto.MatchResultDTO;
import uz.career.career_bot.entity.User;
import uz.career.career_bot.enums.NotificationFrequency;
import uz.career.career_bot.enums.SearchStatus;
import uz.career.career_bot.service.MatchingService;
import uz.career.career_bot.service.NotificationService;
import uz.career.career_bot.service.UserService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final UserService userService;
    private final MatchingService matchingService;
    private final NotificationService notificationService;

    /**
     * Send suitable vacancies to DAILY users every morning at 9:00 AM
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendDailyNotifications() {
        log.info("Sending daily job notifications...");
        sendNotifications(NotificationFrequency.DAILY);
    }

    /**
     * Every Monday at 9:00 for WEEKLY users
     */
    @Scheduled(cron = "0 0 9 * * MON")
    public void sendWeeklyNotifications() {
        log.info("Sending weekly job notifications...");
        sendNotifications(NotificationFrequency.WEEKLY);
    }

    private void sendNotifications(NotificationFrequency frequency) {
        List<User> users = userService.getActiveAndPassiveUsers();

        int sent = 0;
        for (User user : users) {
            if (user.getNotificationFrequency() != frequency) continue;
            if (user.getSearchStatus() == SearchStatus.NOT_LOOKING) continue;

            List<MatchResultDTO> matches = matchingService.findJobsForUser(user, 5);
            if (!matches.isEmpty()) {
                notificationService.sendJobNotification(user, matches);
                sent++;
            }
        }
        log.info("Sent {} {} notifications", sent, frequency);
    }
}