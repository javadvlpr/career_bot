package uz.career.career_bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.career.career_bot.entity.SavedJob;
import java.util.List;
import java.util.Optional;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    List<SavedJob> findByUserId(Long userId);
    Optional<SavedJob> findByUserIdAndJobId(Long userId, Long jobId);
    boolean existsByUserIdAndJobId(Long userId, Long jobId);

    @Query("SELECT s FROM SavedJob s " +
            "LEFT JOIN FETCH s.job j " +
            "LEFT JOIN FETCH j.company " +
            "LEFT JOIN FETCH j.profession p " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH j.skills " +
            "WHERE s.user.id = :userId")
    List<SavedJob> findByUserIdWithDetails(@Param("userId") Long userId);
}