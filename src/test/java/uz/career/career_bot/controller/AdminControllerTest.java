package uz.career.career_bot.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import uz.career.career_bot.bot.CareerBot;
import uz.career.career_bot.entity.Company;
import uz.career.career_bot.entity.Job;
import uz.career.career_bot.enums.*;
import uz.career.career_bot.service.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@WithMockUser(roles = "ADMIN")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CompanyService companyService;

    @MockitoBean
    private JobService jobService;

    @MockitoBean
    private JoobleService joobleService;

    @MockitoBean
    private ProfessionService professionService;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private LocationService locationService;

    @MockitoBean
    private ApplicationService applicationService;

    @MockitoBean
    private SkillService skillService;

    @MockitoBean
    private StatisticsService statisticsService;

    @MockitoBean
    private CareerBot careerBot;

    private Company testCompany;
    private Job testJob;

    @BeforeEach
    void setUp() {
        testCompany = Company.builder()
                .id(1L)
                .companyName("Test Company")
                .telegramId(123456789L)
                .status(CompanyStatus.PENDING)
                .chatId(123456789L)
                .build();

        testJob = Job.builder()
                .id(1L)
                .title("Test Job")
                .company(testCompany)
                .status(JobStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }


    @Test
    void testLoginPage_ReturnsView() throws Exception {
        mockMvc.perform(get("/admin/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/login"));
    }


    @Test
    void testDashboard_ReturnsViewWithModelAttributes() throws Exception {
        when(userService.count()).thenReturn(10L);
        when(companyService.count()).thenReturn(5L);
        when(jobService.count()).thenReturn(20L);
        when(applicationService.count()).thenReturn(15L);
        when(categoryService.count()).thenReturn(3L);
        when(skillService.count()).thenReturn(30L);
        when(professionService.count()).thenReturn(8L);

        when(companyService.getPendingCompanies()).thenReturn(List.of(testCompany));
        when(jobService.getPendingJobs()).thenReturn(List.of(testJob));

        when(statisticsService.getJobsByStatus()).thenReturn(Map.of("PENDING", 2L));
        when(statisticsService.getUsersByExperience()).thenReturn(Map.of("JUNIOR", 5L));
        when(statisticsService.getTopCategoriesByJobs()).thenReturn(Map.of("IT", 10L)); // ← Map, List emas

        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists(
                        "totalUsers", "totalCompanies", "totalJobs",
                        "totalApplications", "pendingCompanies", "pendingJobs"
                ));
    }


    @Test
    void testGetCompanies_NoFilter_ReturnsAllCompanies() throws Exception {
        when(companyService.getAll()).thenReturn(List.of(testCompany));

        mockMvc.perform(get("/admin/companies"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/companies"))
                .andExpect(model().attributeExists("companies"));
    }

    @Test
    void testApproveCompany_RedirectsAndCallsService() throws Exception {
        when(companyService.getById(1L)).thenReturn(testCompany);
        when(careerBot.execute(any(SendMessage.class))).thenReturn(new Message());

        mockMvc.perform(post("/admin/companies/1/approve").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/companies?status=PENDING"));

        verify(companyService).approve(1L);
    }

    @Test
    void testRejectCompany_RedirectsAndCallsService() throws Exception {
        when(companyService.getById(1L)).thenReturn(testCompany);
        when(careerBot.execute(any(SendMessage.class))).thenReturn(new Message());

        mockMvc.perform(post("/admin/companies/1/reject").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/companies?status=PENDING"));

        verify(companyService).reject(1L);
    }


    @Test
    void testGetJobs_NoFilter_ReturnsAllJobs() throws Exception {
        when(jobService.getAll()).thenReturn(List.of(testJob));

        mockMvc.perform(get("/admin/jobs"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/jobs"))
                .andExpect(model().attributeExists("jobs"));
    }


    @Test
    void testApproveJob_RedirectsAndCallsService() throws Exception {
        when(jobService.getById(1L)).thenReturn(testJob);
        when(careerBot.execute(any(SendMessage.class))).thenReturn(new Message());

        mockMvc.perform(post("/admin/jobs/1/approve").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/jobs?status=PENDING"));

        verify(jobService).approve(1L);
    }

    @Test
    void testRejectJob_RedirectsAndCallsService() throws Exception {
        when(jobService.getById(1L)).thenReturn(testJob);
        when(careerBot.execute(any(SendMessage.class))).thenReturn(new Message());

        mockMvc.perform(post("/admin/jobs/1/reject").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/jobs?status=PENDING"));

        verify(jobService).reject(1L);
    }

    @Test
    void testCloseJob_RedirectsAndCallsService() throws Exception {
        mockMvc.perform(post("/admin/jobs/1/close").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/jobs"));

        verify(jobService).close(1L);
    }


    @Test
    void testGetUsers_ReturnsView() throws Exception {
        when(userService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"));
    }
    @Test
    void testImportFromJooble_RedirectsToDashboard() throws Exception {
        when(joobleService.importAllCategories()).thenReturn(10);

        mockMvc.perform(post("/admin/jooble/import").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));

        verify(joobleService).importAllCategories();
    }
}