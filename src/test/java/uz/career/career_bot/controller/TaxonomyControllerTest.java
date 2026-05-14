package uz.career.career_bot.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import uz.career.career_bot.entity.*;
import uz.career.career_bot.service.*;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaxonomyController.class)
@WithMockUser(roles = "ADMIN")
class TaxonomyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private SkillService skillService;

    @MockitoBean
    private ProfessionService professionService;

    @MockitoBean
    private LocationService locationService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("IT")
                .skills(Collections.emptyList())
                .build();
    }


    @Test
    void testGetCategories_ReturnsView() throws Exception {
        when(categoryService.getAll()).thenReturn(List.of(category));

        mockMvc.perform(get("/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/categories"))
                .andExpect(model().attributeExists("categories"));

        verify(categoryService).getAll();
    }

    @Test
    void testCreateCategory_ValidInput_Redirects() throws Exception {
        when(categoryService.create("IT")).thenReturn(category);

        mockMvc.perform(post("/admin/categories/create")
                        .param("name", "IT")   // CategoryCreateDTO.name field
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"));

        verify(categoryService).create("IT");
    }

    @Test
    void testCreateCategory_BlankName_DoesNotCallService() throws Exception {
        mockMvc.perform(post("/admin/categories/create")
                        .param("name", "")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        verify(categoryService, never()).create(any());
    }

    @Test
    void testUpdateCategory_ValidInput_Redirects() throws Exception {
        when(categoryService.update(1L, "Backend")).thenReturn(category);

        mockMvc.perform(post("/admin/categories/1/update")
                        .param("name", "Backend")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"));

        verify(categoryService).update(1L, "Backend");
    }

    @Test
    void testDeleteCategory_Redirects() throws Exception {
        doNothing().when(categoryService).delete(1L);

        mockMvc.perform(post("/admin/categories/1/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"));

        verify(categoryService).delete(1L);
    }

    @Test
    void testGetSkills_NoCategoryFilter_ReturnsAllSkills() throws Exception {
        when(skillService.getAll()).thenReturn(List.of());
        when(categoryService.getAll()).thenReturn(List.of(category));

        mockMvc.perform(get("/admin/skills"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/skills"))
                .andExpect(model().attributeExists("skills", "categories"));

        verify(skillService).getAll();
        verify(categoryService).getAll();
    }

    @Test
    void testGetSkills_WithCategoryFilter_ReturnsFilteredSkills() throws Exception {
        when(skillService.getByCategoryId(1L)).thenReturn(List.of());
        when(categoryService.getAll()).thenReturn(List.of(category));

        mockMvc.perform(get("/admin/skills").param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/skills"))
                .andExpect(model().attribute("currentCategoryId", 1L));

        verify(skillService).getByCategoryId(1L);
    }

    @Test
    void testCreateSkill_ValidInput_Redirects() throws Exception {
        mockMvc.perform(post("/admin/skills/create")
                        .param("name", "Java")
                        .param("categoryId", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/skills"));

        verify(skillService).create("Java", 1L);
    }

    @Test
    void testDeleteSkill_Redirects() throws Exception {
        doNothing().when(skillService).delete(1L);

        mockMvc.perform(post("/admin/skills/1/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/skills"));

        verify(skillService).delete(1L);
    }

    @Test
    void testGetProfessions_ReturnsView() throws Exception {
        when(professionService.getAll()).thenReturn(List.of());
        when(categoryService.getAll()).thenReturn(List.of(category));

        mockMvc.perform(get("/admin/professions"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/professions"))
                .andExpect(model().attributeExists("professions", "categories"));

        verify(professionService).getAll();
    }

    @Test
    void testCreateProfession_ValidInput_Redirects() throws Exception {
        mockMvc.perform(post("/admin/professions/create")
                        .param("name", "Software Engineer")
                        .param("categoryId", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/professions"));

        verify(professionService).create("Software Engineer", 1L);
    }

    @Test
    void testDeleteProfession_Redirects() throws Exception {
        doNothing().when(professionService).delete(1L);

        mockMvc.perform(post("/admin/professions/1/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/professions"));

        verify(professionService).delete(1L);
    }

    @Test
    void testGetLocations_ReturnsView() throws Exception {
        when(locationService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/locations"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/locations"))
                .andExpect(model().attributeExists("locations"));

        verify(locationService).getAll();
    }

    @Test
    void testCreateLocation_ValidInput_Redirects() throws Exception {
        mockMvc.perform(post("/admin/locations/create")
                        .param("name", "Tashkent")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/locations"));

        verify(locationService).create("Tashkent");
    }

    @Test
    void testDeleteLocation_Redirects() throws Exception {
        doNothing().when(locationService).delete(1L);

        mockMvc.perform(post("/admin/locations/1/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/locations"));

        verify(locationService).delete(1L);
    }
}