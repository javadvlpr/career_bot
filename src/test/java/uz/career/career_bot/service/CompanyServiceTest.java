package uz.career.career_bot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.career.career_bot.entity.Company;
import uz.career.career_bot.enums.BotState;
import uz.career.career_bot.enums.CompanyStatus;
import uz.career.career_bot.exception.NotFoundException;
import uz.career.career_bot.repository.CompanyRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    private CompanyService companyService;
    private Company company;

    @BeforeEach
    void setUp() {
        companyService = new CompanyService(companyRepository);
        company = Company.builder()
                .id(1L)
                .telegramId(123456789L)
                .companyName("Tech Corp")
                .inn("123456789")
                .industry("IT")
                .contactInfo("contact@tech.com")
                .status(CompanyStatus.PENDING)
                .build();
    }

    @Test
    void testSave() {
        when(companyRepository.save(any())).thenReturn(company);

        Company result = companyService.save(company);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(companyRepository).save(company);
    }

    @Test
    void testGetByTelegramId_Success() {
        when(companyRepository.findByTelegramId(123456789L)).thenReturn(Optional.of(company));

        Optional<Company> result = companyService.getByTelegramId(123456789L);

        assertTrue(result.isPresent());
        assertEquals(company.getId(), result.get().getId());
    }

    @Test
    void testGetByTelegramId_Empty() {
        when(companyRepository.findByTelegramId(999999999L)).thenReturn(Optional.empty());

        Optional<Company> result = companyService.getByTelegramId(999999999L);

        assertFalse(result.isPresent());
    }

    @Test
    void testExistsByTelegramId_True() {
        when(companyRepository.existsByTelegramId(123456789L)).thenReturn(true);

        boolean result = companyService.existsByTelegramId(123456789L);

        assertTrue(result);
    }

    @Test
    void testExistsByTelegramId_False() {
        when(companyRepository.existsByTelegramId(999999999L)).thenReturn(false);

        boolean result = companyService.existsByTelegramId(999999999L);

        assertFalse(result);
    }

    @Test
    void testGetAll() {
        List<Company> companies = List.of(company);
        when(companyRepository.findAll()).thenReturn(companies);

        List<Company> result = companyService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetById_Success() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        Company result = companyService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetById_NotFound() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> companyService.getById(1L));
    }

    @Test
    void testGetPendingCompanies() {
        List<Company> pending = List.of(company);
        when(companyRepository.findByStatus(CompanyStatus.PENDING)).thenReturn(pending);

        List<Company> result = companyService.getPendingCompanies();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(CompanyStatus.PENDING, result.get(0).getStatus());
    }

    @Test
    void testApprove() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(companyRepository.save(any())).thenReturn(company);

        companyService.approve(1L);

        assertEquals(CompanyStatus.APPROVED, company.getStatus());
        verify(companyRepository).save(any());
    }

    @Test
    void testReject() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(companyRepository.save(any())).thenReturn(company);

        companyService.reject(1L);

        assertEquals(CompanyStatus.REJECTED, company.getStatus());
        verify(companyRepository).save(any());
    }

    @Test
    void testUpdateBotState() {
        when(companyRepository.findByTelegramId(123456789L)).thenReturn(Optional.of(company));
        when(companyRepository.save(any())).thenReturn(company);

        companyService.updateBotState(123456789L, BotState.ENTER_NAME);

        verify(companyRepository).save(argThat(c -> c.getBotState() == BotState.ENTER_NAME));
    }

    @Test
    void testUpdateBotState_NotFound() {
        when(companyRepository.findByTelegramId(999999999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                companyService.updateBotState(999999999L, BotState.ENTER_NAME)
        );
    }

    @Test
    void testToDTO() {
        var dto = companyService.toDTO(company);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Tech Corp", dto.getCompanyName());
        assertEquals(CompanyStatus.PENDING, dto.getStatus());
    }

    @Test
    void testCount() {
        when(companyRepository.count()).thenReturn(10L);

        long result = companyService.count();

        assertEquals(10L, result);
    }

    @Test
    void testApprove_ChangesStatusToApproved() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(companyRepository.save(any())).thenAnswer(inv -> {
            Company c = inv.getArgument(0);
            c.setStatus(CompanyStatus.APPROVED);
            return c;
        });

        companyService.approve(1L);

        verify(companyRepository).save(argThat(c -> c.getStatus() == CompanyStatus.APPROVED));
    }

    @Test
    void testReject_ChangesStatusToRejected() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(companyRepository.save(any())).thenAnswer(inv -> {
            Company c = inv.getArgument(0);
            c.setStatus(CompanyStatus.REJECTED);
            return c;
        });

        companyService.reject(1L);

        verify(companyRepository).save(argThat(c -> c.getStatus() == CompanyStatus.REJECTED));
    }
}
