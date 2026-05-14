package uz.career.career_bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.career.career_bot.entity.Profession;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfessionRepository extends JpaRepository<Profession, Long> {
    List<Profession> findByCategoryId(Long categoryId);
    Optional<Profession> findByName(String name);
    @Query("SELECT p FROM Profession p LEFT JOIN FETCH p.category")
    List<Profession> findAllWithCategory();

    @Query("SELECT p FROM Profession p LEFT JOIN FETCH p.category WHERE p.category.id = :categoryId")
    List<Profession> findByCategoryIdWithCategory(@Param("categoryId") Long categoryId);
}