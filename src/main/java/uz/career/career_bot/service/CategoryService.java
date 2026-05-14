package uz.career.career_bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.career.career_bot.dto.CategoryDTO;
import uz.career.career_bot.entity.Category;
import uz.career.career_bot.exception.AlreadyExistsException;
import uz.career.career_bot.exception.NotFoundException;
import uz.career.career_bot.exception.ValidationException;
import uz.career.career_bot.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getAll() {
        return categoryRepository.findAllWithSkills();
    }

    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category", id));
    }

    public Category getByName(String name) {
        return categoryRepository.findByName(name);
    }

    public Category create(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Category name cannot be empty.");
        }
        if (categoryRepository.findByName(name.trim()) != null) {
            throw new AlreadyExistsException("This category already exists.");
        }
        return categoryRepository.save(Category.builder().name(name.trim()).build());
    }

    public Category update(Long id, String name) {
        Category category = getById(id);
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Category name cannot be empty. ");
        }
        category.setName(name.trim());
        return categoryRepository.save(category);
    }

    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }

    public long count() {
        return categoryRepository.count();
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllAsDTO(ProfessionService professionService) {
        return categoryRepository.findAll().stream().map(cat -> {
            List<CategoryDTO.SkillDTO> skillDtos = cat.getSkills() == null
                    ? List.of()
                    : cat.getSkills().stream()
                    .map(s -> CategoryDTO.SkillDTO.builder()
                            .id(s.getId())
                            .name(s.getName())
                            .build())
                    .toList();

            List<CategoryDTO.ProfessionDTO> profDtos = professionService.getByCategoryId(cat.getId()).stream()
                    .map(p -> CategoryDTO.ProfessionDTO.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .build())
                    .toList();

            return CategoryDTO.builder()
                    .id(cat.getId())
                    .name(cat.getName())
                    .skills(skillDtos)
                    .professions(profDtos)
                    .build();
        }).toList();
    }
}