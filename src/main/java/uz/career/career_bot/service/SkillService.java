package uz.career.career_bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.career.career_bot.entity.Category;
import uz.career.career_bot.entity.Skill;
import uz.career.career_bot.exception.NotFoundException;
import uz.career.career_bot.exception.ValidationException;
import uz.career.career_bot.repository.SkillRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;
    private final CategoryService categoryService;

    public List<Skill> getAll() {
        return skillRepository.findAllWithCategory();
    }

    public List<Skill> getByCategoryId(Long categoryId) {
        return skillRepository.findByCategoryIdWithCategory(categoryId);
    }

    public Skill getByName(String name) {
        return skillRepository.findByName(name);
    }

    public Skill getById(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Skill", id));
    }

    public Skill create(String name, Long categoryId) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Skill name cannot be empty.");
        }
        Category category = categoryService.getById(categoryId);
        return skillRepository.save(Skill.builder()
                .name(name.trim())
                .category(category)
                .build());
    }

    public Skill update(Long id, String name, Long categoryId) {
        Skill skill = getById(id);
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Skill name cannot be empty.");
        }
        skill.setName(name.trim());
        if (categoryId != null) {
            skill.setCategory(categoryService.getById(categoryId));
        }
        return skillRepository.save(skill);
    }

    public void delete(Long id) {
        skillRepository.deleteById(id);
    }

    public long count() {
        return skillRepository.count();
    }
}