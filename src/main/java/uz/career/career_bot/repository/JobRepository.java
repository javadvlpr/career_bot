package uz.career.career_bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.career.career_bot.entity.Job;
import uz.career.career_bot.enums.JobStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    @Query("SELECT j FROM Job j " +
            "LEFT JOIN FETCH j.company " +
            "LEFT JOIN FETCH j.profession p " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH j.skills " +
            "WHERE j.id = :id")
    Optional<Job> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT j FROM Job j " +
            "LEFT JOIN FETCH j.company " +
            "LEFT JOIN FETCH j.profession p " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH j.skills " +
            "WHERE j.status = :status")
    List<Job> findByStatusWithDetails(@Param("status") JobStatus status);

    @Query("SELECT DISTINCT j FROM Job j " +
            "LEFT JOIN FETCH j.company " +
            "LEFT JOIN FETCH j.profession p " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH j.skills")
    List<Job> findAllWithDetails();

    @Query("SELECT DISTINCT j FROM Job j " +
            "LEFT JOIN FETCH j.company " +
            "LEFT JOIN FETCH j.profession p " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH j.skills " +
            "WHERE j.company.id = :companyId")
    List<Job> findByCompanyIdWithDetails(@Param("companyId") Long companyId);

    List<Job> findByStatus(JobStatus status);
    List<Job> findByCompanyId(Long companyId);
    List<Job> findByStatusAndExpiresAtBefore(JobStatus status, LocalDateTime before);
    boolean existsByExternalId(Long externalId);
}