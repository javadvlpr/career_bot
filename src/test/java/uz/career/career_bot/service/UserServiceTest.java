package uz.career.career_bot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.career.career_bot.entity.User;
import uz.career.career_bot.enums.BotState;
import uz.career.career_bot.enums.ExperienceLevel;
import uz.career.career_bot.enums.SearchStatus;
import uz.career.career_bot.exception.NotFoundException;
import uz.career.career_bot.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;
    private User user;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);

        user = User.builder()
                .id(1L)
                .telegramId(123456789L)
                .name("John Doe")
                .searchStatus(SearchStatus.ACTIVE)
                .botState(BotState.USER_MAIN_MENU)
                .experienceLevel(ExperienceLevel.SENIOR)
                .location("Tashkent")
                .expectedSalary(5000)
                .skills(new java.util.HashSet<>())
                .build();
    }

    @Test
    @DisplayName("save — user muvaffaqiyatli saqlanadi")
    void testSave() {
        when(userRepository.save(any())).thenReturn(user);

        User result = userService.save(user);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("getByTelegramId — user topilsa Optional qaytadi")
    void testGetByTelegramId_Success() {
        when(userRepository.findByTelegramId(123456789L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getByTelegramId(123456789L);

        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
    }

    @Test
    @DisplayName("getByTelegramId — user topilmasa bo'sh Optional qaytadi")
    void testGetByTelegramId_Empty() {
        when(userRepository.findByTelegramId(999999999L)).thenReturn(Optional.empty());

        Optional<User> result = userService.getByTelegramId(999999999L);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("existsByTelegramId — user mavjudligini tekshiradi (true)")
    void testExistsByTelegramId_True() {
        when(userRepository.existsByTelegramId(123456789L)).thenReturn(true);

        boolean result = userService.existsByTelegramId(123456789L);

        assertTrue(result);
    }

    @Test
    @DisplayName("existsByTelegramId — user mavjudligini tekshiradi (false)")
    void testExistsByTelegramId_False() {
        when(userRepository.existsByTelegramId(999999999L)).thenReturn(false);

        boolean result = userService.existsByTelegramId(999999999L);

        assertFalse(result);
    }

    @Test
    @DisplayName("getAll — barcha userlarni qaytadi")
    void testGetAll() {
        List<User> users = List.of(user);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("getById — user topilsa qaytadi")
    void testGetById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
    }

    @Test
    @DisplayName("getById — user topilmasa NotFoundException tashlaydi")
    void testGetById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getById(999L));
    }

    @Test
    @DisplayName("getActiveAndPassiveUsers — aktiv va passiv userlarni qaytadi")
    void testGetActiveAndPassiveUsers() {
        List<User> users = List.of(user);
        when(userRepository.findBySearchStatusIn(
                List.of(SearchStatus.ACTIVE, SearchStatus.PASSIVE)
        )).thenReturn(users);

        List<User> result = userService.getActiveAndPassiveUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("updateBotState — user bot statusi yangilanadi")
    void testUpdateBotState() {
        when(userRepository.findByTelegramId(123456789L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        userService.updateBotState(123456789L, BotState.ENTER_NAME);

        verify(userRepository).findByTelegramId(123456789L);
        verify(userRepository).save(any());
    }

    @Test
    @DisplayName("updateBotState — user topilmasa NotFoundException tashlaydi")
    void testUpdateBotState_NotFound() {
        when(userRepository.findByTelegramId(999999999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, 
                () -> userService.updateBotState(999999999L, BotState.ENTER_NAME));
    }

    @Test
    @DisplayName("updateSearchStatus — search statusi yangilanadi")
    void testUpdateSearchStatus() {
        when(userRepository.findByTelegramId(123456789L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        userService.updateSearchStatus(123456789L, SearchStatus.PASSIVE);

        verify(userRepository).findByTelegramId(123456789L);
        verify(userRepository).save(any());
    }

    @Test
    @DisplayName("updateSearchStatus — user topilmasa NotFoundException tashlaydi")
    void testUpdateSearchStatus_NotFound() {
        when(userRepository.findByTelegramId(999999999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, 
                () -> userService.updateSearchStatus(999999999L, SearchStatus.PASSIVE));
    }

    @Test
    @DisplayName("toDTO — user DTO ga konvertiladi")
    void testToDTO() {
        var result = userService.toDTO(user);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals(ExperienceLevel.SENIOR, result.getExperienceLevel());
        assertEquals("Tashkent", result.getLocation());
    }

    @Test
    @DisplayName("updateCv — user CV fayli yangilanadi")
    void testUpdateCv() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        userService.updateCv(1L, "file123", "resume.pdf");

        verify(userRepository).findById(1L);
        verify(userRepository).save(any());
    }

    @Test
    @DisplayName("removeCv — user CV fayli o'chiriladi")
    void testRemoveCv() {
        user.setCvFileId("file123");
        user.setCvFileName("resume.pdf");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        userService.removeCv(1L);

        verify(userRepository).findById(1L);
        verify(userRepository).save(any());
    }
}
