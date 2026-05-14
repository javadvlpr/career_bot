package uz.career.career_bot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import uz.career.career_bot.entity.AdminUser;
import uz.career.career_bot.repository.AdminUserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminUserService adminUserService;

    @BeforeEach
    void setUp() {

        adminUserService =
                new AdminUserService(adminUserRepository, passwordEncoder);
    }

    @Test
    void testCreateDefaultAdmin_WhenAdminDoesNotExist() {

        when(adminUserRepository.findByUsername("admin"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("admin123"))
                .thenReturn("encoded_password");

        when(adminUserRepository.save(any(AdminUser.class)))
                .thenAnswer(invocation -> {

                    AdminUser admin = invocation.getArgument(0);
                    admin.setId(1L);

                    return admin;
                });

        adminUserService.createDefaultAdmin();

        verify(adminUserRepository).save(
                argThat(admin ->
                        admin.getUsername().equals("admin")
                                && admin.getPassword().equals("encoded_password")
                                && admin.getRole().equals("ADMIN")
                )
        );
    }

    @Test
    void testCreateDefaultAdmin_WhenAdminAlreadyExists() {

        AdminUser existingAdmin = AdminUser.builder()
                .id(1L)
                .username("admin")
                .password("encoded_password")
                .role("ADMIN")
                .build();

        when(adminUserRepository.findByUsername("admin"))
                .thenReturn(Optional.of(existingAdmin));

        adminUserService.createDefaultAdmin();

        verify(adminUserRepository, never())
                .save(any(AdminUser.class));
    }

    @Test
    void testLoadUserByUsername_Success() {

        AdminUser admin = AdminUser.builder()
                .id(1L)
                .username("admin")
                .password("encoded_password")
                .role("ADMIN")
                .build();

        when(adminUserRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));

        UserDetails userDetails =
                adminUserService.loadUserByUsername("admin");

        assertNotNull(userDetails);

        assertEquals("admin", userDetails.getUsername());

        assertEquals(
                "encoded_password",
                userDetails.getPassword()
        );

        assertTrue(
                userDetails.getAuthorities()
                        .stream()
                        .anyMatch(a ->
                                a.getAuthority().equals("ROLE_ADMIN")
                        )
        );
    }

    @Test
    void testLoadUserByUsername_NotFound() {

        when(adminUserRepository.findByUsername("nonexistent"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> adminUserService.loadUserByUsername("nonexistent")
        );

        verify(adminUserRepository)
                .findByUsername("nonexistent");
    }

    @Test
    void testLoadUserByUsername_VerifyCorrectUsername() {

        AdminUser admin = AdminUser.builder()
                .id(1L)
                .username("testadmin")
                .password("pass")
                .role("ADMIN")
                .build();

        when(adminUserRepository.findByUsername("testadmin"))
                .thenReturn(Optional.of(admin));

        UserDetails userDetails =
                adminUserService.loadUserByUsername("testadmin");

        assertEquals(
                "testadmin",
                userDetails.getUsername()
        );

        verify(adminUserRepository)
                .findByUsername("testadmin");
    }
}