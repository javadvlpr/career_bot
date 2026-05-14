package uz.career.career_bot.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import uz.career.career_bot.dto.*;
import uz.career.career_bot.entity.*;
import uz.career.career_bot.enums.*;
import uz.career.career_bot.service.*;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApiController.class)
@WithMockUser
class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobService jobService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JoobleService joobleService;

    @MockitoBean
    private ProfessionService professionService;

    private Job testJob;
    private User testUser;

    @BeforeEach
    void setUp() {
        testJob = Job.builder()
                .id(1L)
                .title("Senior Developer")
                .description("We need a senior developer")
                .build();

        testUser = User.builder()
                .id(1L)
                .name("John Doe")
                .experienceLevel(ExperienceLevel.SENIOR)
                .location("Tashkent")
                .build();
    }

    @Test
    void testGetApprovedJobs_ReturnsJobList() throws Exception {
        JobDTO jobDTO = JobDTO.builder()
                .id(1L)
                .title("Senior Developer")
                .build();

        when(jobService.getApprovedJobs()).thenReturn(List.of(testJob));
        when(jobService.toDTO(any(Job.class))).thenReturn(jobDTO);

        mockMvc.perform(get("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Senior Developer"));

        verify(jobService).getApprovedJobs();
    }

    @Test
    void testGetApprovedJobs_EmptyList_ReturnsEmptyArray() throws Exception {
        when(jobService.getApprovedJobs()).thenReturn(List.of());

        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testGetJobById_ReturnsJob() throws Exception {
        JobDTO jobDTO = JobDTO.builder().id(1L).title("Senior Developer").build();

        when(jobService.getById(1L)).thenReturn(testJob);
        when(jobService.toDTO(testJob)).thenReturn(jobDTO);

        mockMvc.perform(get("/api/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Senior Developer"));

        verify(jobService).getById(1L);
    }

    @Test
    void testGetAllUsers_ReturnsUserList() throws Exception {
        UserDTO userDTO = UserDTO.builder()
                .id(1L)
                .name("John Doe")
                .build();

        when(userService.getAll()).thenReturn(List.of(testUser));
        when(userService.toDTO(any(User.class))).thenReturn(userDTO);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"));

        verify(userService).getAll();
    }

    @Test
    void testGetStats_ReturnsTotalCounts() throws Exception {
        when(userService.count()).thenReturn(100L);
        when(jobService.count()).thenReturn(50L);

        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(100))
                .andExpect(jsonPath("$.totalJobs").value(50));

        verify(userService).count();
        verify(jobService).count();
    }

    @Test
    void testImportFromJooble_ReturnsImportedCount() throws Exception {
        when(joobleService.importAllCategories()).thenReturn(25);

        mockMvc.perform(post("/api/jooble/import")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Imported 25 jobs from Jooble"));

        verify(joobleService).importAllCategories();
    }


    @Test
    void testGetCategories_ReturnsList() throws Exception {
        when(categoryService.getAllAsDTO(professionService)).thenReturn(List.of());

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(categoryService).getAllAsDTO(professionService);
    }
}