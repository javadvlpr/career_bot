package uz.career.career_bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.career.career_bot.entity.JobOffer;
import uz.career.career_bot.enums.OfferStatus;
import java.util.List;

@Repository
public interface JobOfferRepository extends JpaRepository<JobOffer, Long> {
    List<JobOffer> findByUserId(Long userId);
    List<JobOffer> findByCompanyId(Long companyId);
    List<JobOffer> findByJobId(Long jobId);
    List<JobOffer> findByUserIdAndStatus(Long userId, OfferStatus status);
    boolean existsByJobIdAndUserId(Long jobId, Long userId);
}