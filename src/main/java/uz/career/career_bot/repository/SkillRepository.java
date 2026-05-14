package uz.career.career_bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.career.career_bot.entity.Skill;
import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findByCategoryId(Long categoryId);
    Skill findByName(String name);
    @Query("SELECT s FROM Skill s LEFT JOIN FETCH s.category")
    List<Skill> findAllWithCategory();

    @Query("SELECT s FROM Skill s LEFT JOIN FETCH s.category WHERE s.category.id = :categoryId")
    List<Skill> findByCategoryIdWithCategory(@Param("categoryId") Long categoryId);
}