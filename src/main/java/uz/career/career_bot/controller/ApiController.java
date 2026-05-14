package uz.career.career_bot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.career.career_bot.dto.CategoryDTO;
import uz.career.career_bot.dto.JobDTO;
import uz.career.career_bot.dto.UserDTO;
import uz.career.career_bot.entity.Category;
import uz.career.career_bot.entity.Job;
import uz.career.career_bot.entity.User;
import uz.career.career_bot.service.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Career Bot API", description = "API for managing jobs and users")
public class ApiController {

    private final JobService jobService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final JoobleService joobleService;
    private final ProfessionService professionService;

    // === JOBS ===

    @GetMapping("/jobs")
    @Operation(summary = "Get all approved jobs")
    public ResponseEntity<List<JobDTO>> getApprovedJobs() {
        List<JobDTO> jobs = jobService.getApprovedJobs().stream()
                .map(jobService::toDTO)
                .toList();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/jobs/{id}")
    @Operation(summary = "Get job by ID")
    public ResponseEntity<JobDTO> getJobById(@PathVariable Long id) {
        Job job = jobService.getById(id);
        return ResponseEntity.ok(jobService.toDTO(job));
    }


    @GetMapping("/users")
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAll().stream()
                .map(userService::toDTO)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        return ResponseEntity.ok(userService.toDTO(user));
    }


    @GetMapping("/categories")
    @Operation(summary = "Get all categories with skills and professions")
    public ResponseEntity<List<CategoryDTO>> getCategories() {
        return ResponseEntity.ok(categoryService.getAllAsDTO(professionService));
    }

    @PostMapping("/jooble/import")
    @Operation(summary = "Import jobs from Jooble API")
    public ResponseEntity<String> importFromJooble() {
        int imported = joobleService.importAllCategories();
        return ResponseEntity.ok("Imported " + imported + " jobs from Jooble");
    }

    @GetMapping("/stats")
    @Operation(summary = "Get system statistics")
    public ResponseEntity<Object> getStats() {
        return ResponseEntity.ok(new Object() {
            public final long totalUsers = userService.count();
            public final long totalJobs = jobService.count();
        });
    }
}