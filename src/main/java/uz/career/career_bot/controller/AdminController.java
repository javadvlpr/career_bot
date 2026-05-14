package uz.career.career_bot.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.career.career_bot.bot.CareerBot;
import uz.career.career_bot.dto.JobCreateDTO;
import uz.career.career_bot.entity.Application;
import uz.career.career_bot.entity.Company;
import uz.career.career_bot.entity.Job;
import uz.career.career_bot.enums.ApplicationStatus;
import uz.career.career_bot.enums.ExperienceLevel;
import uz.career.career_bot.enums.JobType;
import uz.career.career_bot.service.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final CompanyService companyService;
    private final JobService jobService;
    private final JoobleService joobleService;
    private final ProfessionService professionService;
    private final CategoryService categoryService;
    private final LocationService locationService;
    private final ApplicationService applicationService;
    private final SkillService skillService;
    private final StatisticsService statisticsService;

    @Lazy
    private final CareerBot careerBot;

    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", userService.count());
        model.addAttribute("totalCompanies", companyService.count());
        model.addAttribute("totalJobs", jobService.count());
        model.addAttribute("pendingCompanies", companyService.getPendingCompanies().size());
        model.addAttribute("pendingJobs", jobService.getPendingJobs().size());

        model.addAttribute("totalApplications", applicationService.count());
        model.addAttribute("totalCategories", categoryService.count());
        model.addAttribute("totalSkills", skillService.count());
        model.addAttribute("totalProfessions", professionService.count());

        model.addAttribute("jobsByStatus", statisticsService.getJobsByStatus());
        model.addAttribute("usersByExperience", statisticsService.getUsersByExperience());
        model.addAttribute("topCategories", statisticsService.getTopCategoriesByJobs());

        return "admin/dashboard";
    }

    @GetMapping("/companies")
    public String companies(@RequestParam(required = false) String status, Model model) {
        List<Company> companies;
        if (status != null && !status.isEmpty()) {
            companies = companyService.getAll().stream()
                    .filter(c -> c.getStatus().name().equals(status))
                    .toList();
        } else {
            companies = companyService.getAll();
        }
        model.addAttribute("companies", companies);
        model.addAttribute("currentStatus", status);
        return "admin/companies";
    }

    @PostMapping("/companies/{id}/approve")
    public String approveCompany(@PathVariable Long id) {
        Company company = companyService.getById(id);
        companyService.approve(id);
        log.info("Company approved: id={}, name={}", id, company.getCompanyName());
        sendMessageToCompany(company.getChatId(),
                "✅Congratulations! Your company has been approved.\n\n" +
                        "Now you can add a vacancy. Send /start to the bot.");
        return "redirect:/admin/companies?status=PENDING";
    }

    @PostMapping("/companies/{id}/reject")
    public String rejectCompany(@PathVariable Long id) {
        Company company = companyService.getById(id);
        companyService.reject(id);
        log.info("Company rejected: id={}, name={}", id, company.getCompanyName());
        sendMessageToCompany(company.getChatId(),
                "❌ Unfortunately, your company was rejected.\n\n" +
                        "Contact the admin for more information.");
        return "redirect:/admin/companies?status=PENDING";
    }

    @GetMapping("/jobs")
    public String jobs(@RequestParam(required = false) String status, Model model) {
        List<Job> jobs;
        if (status != null && !status.isEmpty()) {
            jobs = jobService.getAll().stream()
                    .filter(j -> j.getStatus().name().equals(status))
                    .toList();
        } else {
            jobs = jobService.getAll();
        }
        model.addAttribute("jobs", jobs);
        model.addAttribute("currentStatus", status);
        return "admin/jobs";
    }

    @PostMapping("/jobs/{id}/approve")
    public String approveJob(@PathVariable Long id) {
        Job job = jobService.getById(id);
        jobService.approve(id);
        log.info("Job approved: id={}, title={}", id, job.getTitle());
        if (job.getCompany() != null) {
            sendMessageToCompany(job.getCompany().getChatId(),
                    "✅ Your vacancy has been confirmed!\n\n" +
                            "💼 " + job.getTitle() + "\n\n" +
                            "Now users can see it.");
        }
        return "redirect:/admin/jobs?status=PENDING";
    }

    @PostMapping("/jobs/{id}/reject")
    public String rejectJob(@PathVariable Long id) {
        Job job = jobService.getById(id);
        jobService.reject(id);
        log.info("Job rejected: id={}, title={}", id, job.getTitle());
        if (job.getCompany() != null) {
            sendMessageToCompany(job.getCompany().getChatId(),
                    "❌ Vacancy rejected \n\n" +
                            "💼 " + job.getTitle());
        }
        return "redirect:/admin/jobs?status=PENDING";
    }

    @PostMapping("/jobs/{id}/close")
    public String closeJob(@PathVariable Long id) {
        jobService.close(id);
        log.info("Job closed: id={}", id);
        return "redirect:/admin/jobs";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.getAll());
        return "admin/users";
    }

    @PostMapping("/jooble/import")
    public String importFromJooble() {
        int imported = joobleService.importAllCategories();
        log.info("Jooble import done: {} jobs", imported);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/jobs/new")
    public String newJobForm(Model model) {
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("professions", professionService.getAll());
        model.addAttribute("locations", locationService.getActive());
        return "admin/job-new";
    }

    @PostMapping("/jobs/create")
    public String createJob(@Valid @ModelAttribute JobCreateDTO dto,
                            BindingResult bindingResult,
                            RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("error", firstError(bindingResult));
            return "redirect:/admin/jobs/new";
        }
        jobService.createByAdmin(
                dto.getTitle(), dto.getDescription(), dto.getRequirements(),
                dto.getProfessionId(), dto.getLocation(),
                dto.getSalaryMin(), dto.getSalaryMax(),
                dto.getJobType(), dto.getExperienceLevel(),
                dto.getExternalUrl(), dto.getContactInfo()
        );
        log.info("Job created by admin: {}", dto.getTitle());
        ra.addFlashAttribute("success", "Vacancy added: " + dto.getTitle());
        return "redirect:/admin/jobs";
    }

    private String firstError(BindingResult bindingResult) {
        return bindingResult.getAllErrors().stream()
                .findFirst()
                .map(e -> e.getDefaultMessage())
                .orElse("Validation error");
    }

    @GetMapping("/applications")
    public String applications(@RequestParam(required = false) String status, Model model) {
        List<Application> applications;
        if (status != null && !status.isEmpty()) {
            applications = applicationService.getByStatus(ApplicationStatus.valueOf(status));
        } else {
            applications = applicationService.getAll();
        }
        model.addAttribute("applications", applications);
        model.addAttribute("currentStatus", status);
        return "admin/applications";
    }

    // === HELPER ===
    private void sendMessageToCompany(Long chatId, String text) {
        try {
            SendMessage msg = new SendMessage();
            msg.setChatId(chatId.toString());
            msg.setText(text);
            careerBot.execute(msg);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to company {}: {}", chatId, e.getMessage());
        }
    }
}