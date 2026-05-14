package uz.career.career_bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.career.career_bot.bot.CareerBot;
import uz.career.career_bot.dto.MatchResultDTO;
import uz.career.career_bot.entity.Company;
import uz.career.career_bot.entity.Notification;
import uz.career.career_bot.entity.User;
import uz.career.career_bot.repository.NotificationRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ApplicationContext applicationContext;

    /**
     * Lazy lookup to avoid the circular dependency
     * CareerBot -> handlers -> services -> CareerBot.
     */
    private CareerBot getCareerBot() {
        return applicationContext.getBean(CareerBot.class);
    }

    /**
     * Send matching vacancies to the user (Telegram + DB record).
     * Used by NotificationScheduler for daily and weekly broadcasts.
     */
    public void sendJobNotification(User user, List<MatchResultDTO> matches) {
        StringBuilder text = new StringBuilder();
        text.append("🔔 ").append(matches.size())
                .append(" new vacancies matched your profile:\n\n");

        int index = 1;
        for (MatchResultDTO match : matches) {
            text.append(index++).append(". 💼 ").append(match.getTitle())
                    .append(" (").append(match.getScorePercent()).append("% match)\n");
        }
        text.append("\nOpen the bot and tap \"🔍 Search jobs\" to see details.");

        String message = text.toString();
        sendTelegramMessage(user.getChatId(), message);

        Notification notification = Notification.builder()
                .user(user)
                .type("JOB_MATCH")
                .message(message)
                .build();
        notificationRepository.save(notification);

        log.info("Job notification sent to user {} (chatId={})",
                user.getTelegramId(), user.getChatId());
    }

    /**
     * Notify the user about a new job offer (Telegram + DB record).
     */
    public void sendOfferNotification(User user, String companyName, String jobTitle) {
        String message = "📩 You received a new job offer!\n\n"
                + "🏢 Company: " + companyName + "\n"
                + "💼 Position: " + jobTitle + "\n\n"
                + "Open the bot and tap \"📨 My offers\" to respond.";

        sendTelegramMessage(user.getChatId(), message);

        Notification notification = Notification.builder()
                .user(user)
                .type("JOB_OFFER")
                .message(message)
                .build();
        notificationRepository.save(notification);

        log.info("Offer notification sent to user {} (chatId={})",
                user.getTelegramId(), user.getChatId());
    }

    /**
     * Notify the company about its approval/rejection (Telegram + DB record).
     */
    public void sendCompanyApprovalNotification(Company company, boolean approved) {
        String message = approved
                ? "✅ Your company has been approved! You can now post vacancies."
                : "❌ Your company application was rejected. Please contact the administrator.";

        sendTelegramMessage(company.getChatId(), message);

        Notification notification = Notification.builder()
                .company(company)
                .type("COMPANY_STATUS")
                .message(message)
                .build();
        notificationRepository.save(notification);

        log.info("Company approval notification sent to company {} (chatId={}, approved={})",
                company.getId(), company.getChatId(), approved);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Low-level Telegram delivery. Failures are logged but not propagated
     * so that a single delivery error does not abort the whole batch.
     */
    private void sendTelegramMessage(Long chatId, String text) {
        if (chatId == null) {
            log.warn("Cannot send Telegram message: chatId is null");
            return;
        }
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId.toString());
            sendMessage.setText(text);
            getCareerBot().execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to send Telegram message to chatId={}: {}",
                    chatId, e.getMessage());
        }
    }
}
