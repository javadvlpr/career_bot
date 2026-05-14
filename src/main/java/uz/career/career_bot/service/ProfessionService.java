package uz.career.career_bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.career.career_bot.entity.Category;
import uz.career.career_bot.entity.Profession;
import uz.career.career_bot.exception.NotFoundException;
import uz.career.career_bot.exception.ValidationException;
import uz.career.career_bot.repository.ProfessionRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfessionService {

    private final ProfessionRepository professionRepository;
    private final CategoryService categoryService;

    public List<Profession> getAll() {
        return professionRepository.findAllWithCategory();
    }

    public List<Profession> getByCategoryId(Long categoryId) {
        return professionRepository.findByCategoryIdWithCategory(categoryId);
    }

    public Profession getById(Long id) {
        return professionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Profession ", id));
    }

    public Optional<Profession> getByName(String name) {
        return professionRepository.findByName(name);
    }

    public Profession create(String name, Long categoryId) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Profession title cannot be empty.");
        }
        Category category = categoryService.getById(categoryId);
        return professionRepository.save(Profession.builder()
                .name(name.trim())
                .category(category)
                .build());
    }

    public Profession update(Long id, String name, Long categoryId) {
        Profession profession = getById(id);
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Profession title cannot be empty.");
        }
        profession.setName(name.trim());
        if (categoryId != null) {
            profession.setCategory(categoryService.getById(categoryId));
        }
        return professionRepository.save(profession);
    }

    public void delete(Long id) {
        professionRepository.deleteById(id);
    }

    public long count() {
        return professionRepository.count();
    }

    public Profession save(Profession profession) {
        return professionRepository.save(profession);
    }
}