package uz.career.career_bot.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uz.career.career_bot.dto.CategoryCreateDTO;
import uz.career.career_bot.dto.LocationCreateDTO;
import uz.career.career_bot.dto.ProfessionCreateDTO;
import uz.career.career_bot.dto.SkillCreateDTO;
import uz.career.career_bot.service.CategoryService;
import uz.career.career_bot.service.LocationService;
import uz.career.career_bot.service.ProfessionService;
import uz.career.career_bot.service.SkillService;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class TaxonomyController {

    private final CategoryService categoryService;
    private final SkillService skillService;
    private final ProfessionService professionService;
    private final LocationService locationService;

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", categoryService.getAll());
        return "admin/categories";
    }

    @PostMapping("/categories/create")
    public String createCategory(@Valid @ModelAttribute CategoryCreateDTO dto,
                                 BindingResult bindingResult,
                                 RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("error", firstError(bindingResult));
            return "redirect:/admin/categories";
        }
        categoryService.create(dto.getName());
        log.info("Category created: {}", dto.getName());
        ra.addFlashAttribute("success", "Category added: " + dto.getName());
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/update")
    public String updateCategory(@PathVariable Long id,
                                 @Valid @ModelAttribute CategoryCreateDTO dto,
                                 BindingResult bindingResult,
                                 RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("error", firstError(bindingResult));
            return "redirect:/admin/categories";
        }
        categoryService.update(id, dto.getName());
        log.info("Category updated: id={}, name={}", id, dto.getName());
        ra.addFlashAttribute("success", "Category updated");
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes ra) {
        categoryService.delete(id);
        log.info("Category deleted: id={}", id);
        ra.addFlashAttribute("success", "Category added");
        return "redirect:/admin/categories";
    }

    @GetMapping("/skills")
    public String skills(@RequestParam(required = false) Long categoryId, Model model) {
        if (categoryId != null) {
            model.addAttribute("skills", skillService.getByCategoryId(categoryId));
            model.addAttribute("currentCategoryId", categoryId);
        } else {
            model.addAttribute("skills", skillService.getAll());
        }
        model.addAttribute("categories", categoryService.getAll());
        return "admin/skills";
    }

    @PostMapping("/skills/create")
    public String createSkill(@Valid @ModelAttribute SkillCreateDTO dto,
                              BindingResult bindingResult,
                              RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("error", firstError(bindingResult));
            return "redirect:/admin/skills";
        }
        skillService.create(dto.getName(), dto.getCategoryId());
        log.info("Skill created: name={}, categoryId={}", dto.getName(), dto.getCategoryId());
        ra.addFlashAttribute("success", "Skill added: " + dto.getName());
        return "redirect:/admin/skills";
    }

    @PostMapping("/skills/{id}/update")
    public String updateSkill(@PathVariable Long id,
                              @Valid @ModelAttribute SkillCreateDTO dto,
                              BindingResult bindingResult,
                              RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("error", firstError(bindingResult));
            return "redirect:/admin/skills";
        }
        skillService.update(id, dto.getName(), dto.getCategoryId());
        log.info("Skill updated: id={}", id);
        ra.addFlashAttribute("success", "Skill updated");
        return "redirect:/admin/skills";
    }

    @PostMapping("/skills/{id}/delete")
    public String deleteSkill(@PathVariable Long id, RedirectAttributes ra) {
        skillService.delete(id);
        log.info("Skill deleted: id={}", id);
        ra.addFlashAttribute("success", "Skill deleted");
        return "redirect:/admin/skills";
    }

    @GetMapping("/professions")
    public String professions(@RequestParam(required = false) Long categoryId, Model model) {
        if (categoryId != null) {
            model.addAttribute("professions", professionService.getByCategoryId(categoryId));
            model.addAttribute("currentCategoryId", categoryId);
        } else {
            model.addAttribute("professions", professionService.getAll());
        }
        model.addAttribute("categories", categoryService.getAll());
        return "admin/professions";
    }

    @PostMapping("/professions/create")
    public String createProfession(@Valid @ModelAttribute ProfessionCreateDTO dto,
                                   BindingResult bindingResult,
                                   RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("error", firstError(bindingResult));
            return "redirect:/admin/professions";
        }
        professionService.create(dto.getName(), dto.getCategoryId());
        log.info("Profession created: name={}, categoryId={}", dto.getName(), dto.getCategoryId());
        ra.addFlashAttribute("success", "Profession added " + dto.getName());
        return "redirect:/admin/professions";
    }

    @PostMapping("/professions/{id}/update")
    public String updateProfession(@PathVariable Long id,
                                   @Valid @ModelAttribute ProfessionCreateDTO dto,
                                   BindingResult bindingResult,
                                   RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("error", firstError(bindingResult));
            return "redirect:/admin/professions";
        }
        professionService.update(id, dto.getName(), dto.getCategoryId());
        log.info("Profession updated: id={}", id);
        ra.addFlashAttribute("success", "Profession updated");
        return "redirect:/admin/professions";
    }

    @PostMapping("/professions/{id}/delete")
    public String deleteProfession(@PathVariable Long id, RedirectAttributes ra) {
        professionService.delete(id);
        log.info("Profession deleted: id={}", id);
        ra.addFlashAttribute("success", "Profession deleted");
        return "redirect:/admin/professions";
    }

    @GetMapping("/locations")
    public String locations(Model model) {
        model.addAttribute("locations", locationService.getAll());
        return "admin/locations";
    }

    @PostMapping("/locations/create")
    public String createLocation(@Valid @ModelAttribute LocationCreateDTO dto,
                                 BindingResult bindingResult,
                                 RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("error", firstError(bindingResult));
            return "redirect:/admin/locations";
        }
        locationService.create(dto.getName());
        log.info("Location created: {}", dto.getName());
        ra.addFlashAttribute("success", "Location added: " + dto.getName());
        return "redirect:/admin/locations";
    }

    @PostMapping("/locations/{id}/update")
    public String updateLocation(@PathVariable Long id,
                                 @Valid @ModelAttribute LocationCreateDTO dto,
                                 BindingResult bindingResult,
                                 RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("error", firstError(bindingResult));
            return "redirect:/admin/locations";
        }
        locationService.update(id, dto.getName(), dto.getActive() != null && dto.getActive());
        log.info("Location updated: id={}", id);
        ra.addFlashAttribute("success", "Location updated");
        return "redirect:/admin/locations";
    }

    @PostMapping("/locations/{id}/delete")
    public String deleteLocation(@PathVariable Long id, RedirectAttributes ra) {
        locationService.delete(id);
        log.info("Location deleted: id={}", id);
        ra.addFlashAttribute("success", "Location deleted");
        return "redirect:/admin/locations";
    }

    /**
     * Getting the first error message from BindingResult
     */
    private String firstError(BindingResult bindingResult) {
        return bindingResult.getAllErrors().stream()
                .findFirst()
                .map(e -> e.getDefaultMessage())
                .orElse("Validation error");
    }
}