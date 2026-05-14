package uz.career.career_bot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.career.career_bot.entity.Location;
import uz.career.career_bot.exception.AlreadyExistsException;
import uz.career.career_bot.exception.NotFoundException;
import uz.career.career_bot.exception.ValidationException;
import uz.career.career_bot.repository.LocationRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    private LocationService locationService;
    private Location location;

    @BeforeEach
    void setUp() {
        locationService = new LocationService(locationRepository);
        location = Location.builder()
                .id(1L)
                .name("Tashkent")
                .active(true)
                .build();
    }

    @Test
    void testGetAll() {
        List<Location> locations = List.of(location);
        when(locationRepository.findAll()).thenReturn(locations);

        List<Location> result = locationService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetActive() {
        List<Location> locations = List.of(location);
        when(locationRepository.findByActiveTrue()).thenReturn(locations);

        List<Location> result = locationService.getActive();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getActive());
    }

    @Test
    void testGetById_Success() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));

        Location result = locationService.getById(1L);

        assertNotNull(result);
        assertEquals("Tashkent", result.getName());
    }

    @Test
    void testGetById_NotFound() {
        when(locationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> locationService.getById(1L));
    }

    @Test
    void testCreate_Success() {
        when(locationRepository.findByName("Tashkent")).thenReturn(null);
        when(locationRepository.save(any())).thenReturn(location);

        Location result = locationService.create("Tashkent");

        assertNotNull(result);
        assertEquals("Tashkent", result.getName());
        assertTrue(result.getActive());
        verify(locationRepository).save(any());
    }

    @Test
    void testCreate_EmptyName() {
        assertThrows(ValidationException.class, () -> locationService.create(""));
        assertThrows(ValidationException.class, () -> locationService.create(null));
    }

    @Test
    void testCreate_AlreadyExists() {
        when(locationRepository.findByName("Tashkent")).thenReturn(location);

        assertThrows(AlreadyExistsException.class, () -> locationService.create("Tashkent"));
        verify(locationRepository, never()).save(any());
    }

    @Test
    void testUpdate_NameOnly() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(locationRepository.save(any())).thenReturn(location);

        location.setName("Samarkand");
        Location result = locationService.update(1L, "Samarkand", null);

        assertNotNull(result);
        assertEquals("Samarkand", result.getName());
        verify(locationRepository).save(any());
    }

    @Test
    void testUpdate_ActiveOnly() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(locationRepository.save(any())).thenReturn(location);

        location.setActive(false);
        Location result = locationService.update(1L, null, false);

        assertNotNull(result);
        assertFalse(result.getActive());
        verify(locationRepository).save(any());
    }

    @Test
    void testUpdate_BothFields() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(locationRepository.save(any())).thenReturn(location);

        location.setName("Bukhara");
        location.setActive(false);
        Location result = locationService.update(1L, "Bukhara", false);

        assertNotNull(result);
        assertEquals("Bukhara", result.getName());
        assertFalse(result.getActive());
        verify(locationRepository).save(any());
    }

    @Test
    void testDelete() {
        locationService.delete(1L);

        verify(locationRepository).deleteById(1L);
    }

    @Test
    void testCount() {
        when(locationRepository.count()).thenReturn(5L);

        long result = locationService.count();

        assertEquals(5L, result);
    }

    @Test
    void testCreate_TrimsWhitespace() {
        when(locationRepository.findByName("Tashkent")).thenReturn(null);
        when(locationRepository.save(any())).thenReturn(location);

        locationService.create("  Tashkent  ");

        verify(locationRepository).save(argThat(loc -> "Tashkent".equals(loc.getName())));
    }
}
