package uz.career.career_bot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.career.career_bot.entity.Category;
import uz.career.career_bot.entity.Skill;
import uz.career.career_bot.exception.NotFoundException;
import uz.career.career_bot.exception.ValidationException;
import uz.career.career_bot.repository.SkillRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private CategoryService categoryService;

    private SkillService skillService;
    private Skill skill;
    private Category category;

    @BeforeEach
    void setUp() {
        skillService = new SkillService(skillRepository, categoryService);

        category = Category.builder().id(1L).name("IT").build();
        skill = Skill.builder()
                .id(1L)
                .name("Java")
                .category(category)
                .build();
    }

    @Test
    void testGetAll() {
        List<Skill> skills = List.of(skill);
        when(skillRepository.findAllWithCategory()).thenReturn(skills);

        List<Skill> result = skillService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetByCategoryId() {
        List<Skill> skills = List.of(skill);
        when(skillRepository.findByCategoryIdWithCategory(1L)).thenReturn(skills);

        List<Skill> result = skillService.getByCategoryId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetByName() {
        when(skillRepository.findByName("Java")).thenReturn(skill);

        Skill result = skillService.getByName("Java");

        assertNotNull(result);
        assertEquals("Java", result.getName());
    }

    @Test
    void testGetById_Success() {
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));

        Skill result = skillService.getById(1L);

        assertNotNull(result);
        assertEquals("Java", result.getName());
    }

    @Test
    void testGetById_NotFound() {
        when(skillRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> skillService.getById(1L));
    }

    @Test
    void testCreate_Success() {
        when(categoryService.getById(1L)).thenReturn(category);
        when(skillRepository.save(any())).thenReturn(skill);

        Skill result = skillService.create("Java", 1L);

        assertNotNull(result);
        assertEquals("Java", result.getName());
        assertEquals(1L, result.getCategory().getId());
        verify(skillRepository).save(any());
    }

    @Test
    void testCreate_EmptyName() {
        assertThrows(ValidationException.class, () ->
                skillService.create("", 1L)
        );
        assertThrows(ValidationException.class, () ->
                skillService.create(null, 1L)
        );
    }

    @Test
    void testUpdate_Success() {
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));
        when(categoryService.getById(1L)).thenReturn(category);
        when(skillRepository.save(any())).thenReturn(skill);

        skill.setName("Python");
        Skill result = skillService.update(1L, "Python", 1L);

        assertNotNull(result);
        assertEquals("Python", result.getName());
        verify(skillRepository).save(any());
    }

    @Test
    void testUpdate_EmptyName() {
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));

        assertThrows(ValidationException.class, () ->
                skillService.update(1L, "", 1L)
        );
    }

    @Test
    void testDelete() {
        skillService.delete(1L);

        verify(skillRepository).deleteById(1L);
    }

    @Test
    void testCount() {
        when(skillRepository.count()).thenReturn(50L);

        long result = skillService.count();

        assertEquals(50L, result);
    }

    @Test
    void testCreate_TrimsWhitespace() {
        when(categoryService.getById(1L)).thenReturn(category);
        when(skillRepository.save(any())).thenReturn(skill);

        skillService.create("  Java  ", 1L);

        verify(skillRepository).save(argThat(s ->
                "Java".equals(s.getName())
        ));
    }
}
