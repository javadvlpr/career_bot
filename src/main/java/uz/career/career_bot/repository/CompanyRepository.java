package uz.career.career_bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.career.career_bot.entity.Company;
import uz.career.career_bot.enums.CompanyStatus;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByTelegramId(Long telegramId);
    boolean existsByTelegramId(Long telegramId);
    List<Company> findByStatus(CompanyStatus status);
}