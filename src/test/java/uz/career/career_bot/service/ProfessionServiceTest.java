package uz.career.career_bot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.career.career_bot.entity.Category;
import uz.career.career_bot.entity.Profession;
import uz.career.career_bot.exception.NotFoundException;
import uz.career.career_bot.exception.ValidationException;
import uz.career.career_bot.repository.ProfessionRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfessionServiceTest {

    @Mock
    private ProfessionRepository professionRepository;

    @Mock
    private CategoryService categoryService;

    private ProfessionService professionService;
    private Profession profession;
    private Category category;

    @BeforeEach
    void setUp() {
        professionService = new ProfessionService(professionRepository, categoryService);
        
        category = Category.builder().id(1L).name("IT").build();
        profession = Profession.builder()
                .id(1L)
                .name("Java Developer")
                .category(category)
                .build();
    }

    @Test
    void testGetAll() {
        List<Profession> professions = List.of(profession);
        when(professionRepository.findAllWithCategory()).thenReturn(professions);

        List<Profession> result = professionService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetByCategoryId() {
        List<Profession> professions = List.of(profession);
        when(professionRepository.findByCategoryIdWithCategory(1L)).thenReturn(professions);

        List<Profession> result = professionService.getByCategoryId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getCategory().getId());
    }

    @Test
    void testGetById_Success() {
        when(professionRepository.findById(1L)).thenReturn(Optional.of(profession));

        Profession result = professionService.getById(1L);

        assertNotNull(result);
        assertEquals("Java Developer", result.getName());
    }

    @Test
    void testGetById_NotFound() {
        when(professionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> professionService.getById(1L));
    }

    @Test
    void testGetByName() {
        when(professionRepository.findByName("Java Developer")).thenReturn(Optional.of(profession));

        Optional<Profession> result = professionService.getByName("Java Developer");

        assertTrue(result.isPresent());
        assertEquals("Java Developer", result.get().getName());
    }

    @Test
    void testCreate_Success() {
        when(categoryService.getById(1L)).thenReturn(category);
        when(professionRepository.save(any())).thenReturn(profession);

        Profession result = professionService.create("Java Developer", 1L);

        assertNotNull(result);
        assertEquals("Java Developer", result.getName());
        assertEquals(1L, result.getCategory().getId());
        verify(professionRepository).save(any());
    }

    @Test
    void testCreate_EmptyName() {
        assertThrows(ValidationException.class, () ->
                professionService.create("", 1L)
        );
        assertThrows(ValidationException.class, () ->
                professionService.create(null, 1L)
        );
    }

    @Test
    void testUpdate_Success() {
        when(professionRepository.findById(1L)).thenReturn(Optional.of(profession));
        when(categoryService.getById(1L)).thenReturn(category);
        when(professionRepository.save(any())).thenReturn(profession);

        profession.setName("Python Developer");
        Profession result = professionService.update(1L, "Python Developer", 1L);

        assertNotNull(result);
        assertEquals("Python Developer", result.getName());
        verify(professionRepository).save(any());
    }

    @Test
    void testUpdate_EmptyName() {
        when(professionRepository.findById(1L)).thenReturn(Optional.of(profession));

        assertThrows(ValidationException.class, () ->
                professionService.update(1L, "", 1L)
        );
    }

    @Test
    void testDelete() {
        professionService.delete(1L);

        verify(professionRepository).deleteById(1L);
    }

    @Test
    void testCount() {
        when(professionRepository.count()).thenReturn(20L);

        long result = professionService.count();

        assertEquals(20L, result);
    }

    @Test
    void testSave() {
        when(professionRepository.save(any())).thenReturn(profession);

        Profession result = professionService.save(profession);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(professionRepository).save(profession);
    }

    @Test
    void testCreate_TrimsWhitespace() {
        when(categoryService.getById(1L)).thenReturn(category);
        when(professionRepository.save(any())).thenReturn(profession);

        professionService.create("  Java Developer  ", 1L);

        verify(professionRepository).save(argThat(prof ->
                "Java Developer".equals(prof.getName())
        ));
    }
}
