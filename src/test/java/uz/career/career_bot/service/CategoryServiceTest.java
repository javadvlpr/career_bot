package uz.career.career_bot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.career.career_bot.dto.CategoryDTO;
import uz.career.career_bot.entity.Category;
import uz.career.career_bot.exception.AlreadyExistsException;
import uz.career.career_bot.exception.NotFoundException;
import uz.career.career_bot.exception.ValidationException;
import uz.career.career_bot.repository.CategoryRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProfessionService professionService;

    private CategoryService categoryService;
    private Category category;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository);
        category = Category.builder()
                .id(1L)
                .name("Information Technology")
                .skills(Arrays.asList())
                .build();
    }

    @Test
    @DisplayName("getAll — barcha kategoriyalarni qaytadi")
    void testGetAll() {
        List<Category> categories = Arrays.asList(category);
        when(categoryRepository.findAllWithSkills()).thenReturn(categories);

        List<Category> result = categoryService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Information Technology", result.get(0).getName());
        verify(categoryRepository).findAllWithSkills();
    }

    @Test
    @DisplayName("getById — kategoriya topilsa qaytadi")
    void getById_found_returnsCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Category result = categoryService.getById(1L);

        assertEquals("Information Technology", result.getName());
        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("getById — kategoriya topilmasa NotFoundException tashlaydi")
    void getById_notFound_throwsException() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> categoryService.getById(999L));
        assertTrue(ex.getMessage().contains("999"));
    }

    @Test
    @DisplayName("getByName — nomi bo'yicha kategoriyani qaytadi")
    void testGetByName() {
        when(categoryRepository.findByName("Information Technology")).thenReturn(category);

        Category result = categoryService.getByName("Information Technology");

        assertNotNull(result);
        assertEquals("Information Technology", result.getName());
    }

    @Test
    @DisplayName("create — bo'sh nom uchun ValidationException tashlaydi")
    void create_emptyName_throwsValidation() {
        assertThrows(ValidationException.class, () -> categoryService.create(""));
        assertThrows(ValidationException.class, () -> categoryService.create("   "));
        assertThrows(ValidationException.class, () -> categoryService.create(null));
    }

    @Test
    @DisplayName("create — duplikat nom uchun AlreadyExistsException tashlaydi")
    void create_duplicate_throwsAlreadyExists() {
        when(categoryRepository.findByName("Information Technology")).thenReturn(category);

        assertThrows(AlreadyExistsException.class, () -> categoryService.create("Information Technology"));
    }

    @Test
    @DisplayName("create — yangi kategoriya muvaffaqiyatli yaratadi")
    void create_valid_returnsCategory() {
        when(categoryRepository.findByName("New")).thenReturn(null);
        when(categoryRepository.save(any())).thenReturn(category);

        Category result = categoryService.create("New");

        assertNotNull(result);
        verify(categoryRepository).findByName("New");
        verify(categoryRepository).save(any());
    }

    @Test
    @DisplayName("update — kategoriya nomi yangilanadi")
    void testUpdate_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any())).thenReturn(category);

        Category result = categoryService.update(1L, "Updated Category");

        assertNotNull(result);
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any());
    }

    @Test
    @DisplayName("update — bo'sh nom uchun ValidationException tashlaydi")
    void testUpdate_EmptyName() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThrows(ValidationException.class, () -> categoryService.update(1L, ""));
    }

    @Test
    @DisplayName("delete — kategoriya o'chiriladi")
    void testDelete() {
        doNothing().when(categoryRepository).deleteById(1L);

        categoryService.delete(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    @DisplayName("count — kategoriyalar sonini qaytadi")
    void testCount() {
        when(categoryRepository.count()).thenReturn(5L);

        long result = categoryService.count();

        assertEquals(5L, result);
        verify(categoryRepository).count();
    }

    @Test
    @DisplayName("getAllAsDTO — kategoriyalarni DTO ko'rinishida qaytadi")
    void testGetAllAsDTO() {
        List<Category> categories = Arrays.asList(category);
        when(categoryRepository.findAll()).thenReturn(categories);
        when(professionService.getByCategoryId(1L)).thenReturn(Arrays.asList());

        List<CategoryDTO> result = categoryService.getAllAsDTO(professionService);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Information Technology", result.get(0).getName());
        verify(categoryRepository).findAll();
    }
}