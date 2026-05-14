package uz.career.career_bot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import uz.career.career_bot.bot.CareerBot;
import uz.career.career_bot.dto.MatchResultDTO;
import uz.career.career_bot.entity.Company;
import uz.career.career_bot.entity.Notification;
import uz.career.career_bot.entity.User;
import uz.career.career_bot.repository.NotificationRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private CareerBot careerBot;

    @InjectMocks
    private NotificationService notificationService;

    private User user;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .id(1L)
                .telegramId(123456789L)
                .chatId(123456789L)
                .name("John Doe")
                .build();
    }

    @Test
    void testSendJobNotification() throws Exception {

        when(applicationContext.getBean(CareerBot.class))
                .thenReturn(careerBot);

        List<MatchResultDTO> matches = List.of(
                MatchResultDTO.builder()
                        .title("Java Developer")
                        .scorePercent(95)
                        .build(),

                MatchResultDTO.builder()
                        .title("Python Developer")
                        .scorePercent(85)
                        .build()
        );

        notificationService.sendJobNotification(user, matches);

        verify(careerBot).execute(any(SendMessage.class));

        ArgumentCaptor<Notification> captor =
                ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();

        assertEquals("JOB_MATCH", saved.getType());

        assertTrue(saved.getMessage().contains("2 new vacancies"));
        assertTrue(saved.getMessage().contains("Java Developer"));
        assertTrue(saved.getMessage().contains("Python Developer"));
    }

    @Test
    void testSendJobNotification_EmptyMatches() throws Exception {

        when(applicationContext.getBean(CareerBot.class))
                .thenReturn(careerBot);

        notificationService.sendJobNotification(user, List.of());

        verify(careerBot).execute(any(SendMessage.class));

        ArgumentCaptor<Notification> captor =
                ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();

        assertEquals("JOB_MATCH", saved.getType());

        assertTrue(saved.getMessage().contains("0 new vacancies"));
    }

    @Test
    void testSendOfferNotification() throws Exception {

        when(applicationContext.getBean(CareerBot.class))
                .thenReturn(careerBot);

        notificationService.sendOfferNotification(
                user,
                "Tech Corp",
                "Java Developer"
        );

        verify(careerBot).execute(any(SendMessage.class));

        ArgumentCaptor<Notification> captor =
                ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();

        assertEquals("JOB_OFFER", saved.getType());

        assertTrue(saved.getMessage().contains("Tech Corp"));
        assertTrue(saved.getMessage().contains("Java Developer"));
    }

    @Test
    void testSendCompanyApprovalNotification_Approved() throws Exception {

        when(applicationContext.getBean(CareerBot.class))
                .thenReturn(careerBot);

        Company company = Company.builder()
                .id(1L)
                .chatId(999999L)
                .build();

        notificationService.sendCompanyApprovalNotification(company, true);

        verify(careerBot).execute(any(SendMessage.class));

        ArgumentCaptor<Notification> captor =
                ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();

        assertEquals("COMPANY_STATUS", saved.getType());

        assertTrue(saved.getMessage().contains("approved"));
    }

    @Test
    void testSendCompanyApprovalNotification_Rejected() throws Exception {

        when(applicationContext.getBean(CareerBot.class))
                .thenReturn(careerBot);

        Company company = Company.builder()
                .id(1L)
                .chatId(999999L)
                .build();

        notificationService.sendCompanyApprovalNotification(company, false);

        verify(careerBot).execute(any(SendMessage.class));

        ArgumentCaptor<Notification> captor =
                ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();

        assertEquals("COMPANY_STATUS", saved.getType());

        assertTrue(saved.getMessage().contains("rejected"));
    }

    @Test
    void testGetUnreadNotifications() {

        Notification notification = Notification.builder()
                .id(1L)
                .user(user)
                .type("JOB_MATCH")
                .message("Test")
                .isRead(false)
                .build();

        when(notificationRepository.findByUserIdAndIsReadFalse(1L))
                .thenReturn(List.of(notification));

        List<Notification> result =
                notificationService.getUnreadNotifications(1L);

        assertEquals(1, result.size());

        assertFalse(result.get(0).getIsRead());
    }

    @Test
    void testGetUserNotifications() {

        Notification notification = Notification.builder()
                .id(1L)
                .user(user)
                .type("JOB_MATCH")
                .message("Test")
                .build();

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));

        List<Notification> result =
                notificationService.getUserNotifications(1L);

        assertEquals(1, result.size());
    }
}