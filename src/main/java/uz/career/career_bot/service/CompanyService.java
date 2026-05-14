package uz.career.career_bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.career.career_bot.dto.CompanyDTO;
import uz.career.career_bot.entity.Company;
import uz.career.career_bot.enums.BotState;
import uz.career.career_bot.enums.CompanyStatus;
import uz.career.career_bot.exception.NotFoundException;
import uz.career.career_bot.repository.CompanyRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    public Company save(Company company) {
        return companyRepository.save(company);
    }

    public Optional<Company> getByTelegramId(Long telegramId) {
        return companyRepository.findByTelegramId(telegramId);
    }

    public boolean existsByTelegramId(Long telegramId) {
        return companyRepository.existsByTelegramId(telegramId);
    }

    public List<Company> getAll() {
        return companyRepository.findAll();
    }

    public Company getById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Company ", id));
    }

    public List<Company> getPendingCompanies() {
        return companyRepository.findByStatus(CompanyStatus.PENDING);
    }

    public void approve(Long id) {
        Company company = getById(id);
        company.setStatus(CompanyStatus.APPROVED);
        companyRepository.save(company);
    }

    public void reject(Long id) {
        Company company = getById(id);
        company.setStatus(CompanyStatus.REJECTED);
        companyRepository.save(company);
    }

    public void updateBotState(Long telegramId, BotState state) {
        Company company = getByTelegramId(telegramId)
                .orElseThrow(() -> new NotFoundException(
                        "Company (telegramId=" + telegramId + ") not found"));
        company.setBotState(state);
        companyRepository.save(company);
    }

    public CompanyDTO toDTO(Company company) {
        return CompanyDTO.builder()
                .id(company.getId())
                .telegramId(company.getTelegramId())
                .companyName(company.getCompanyName())
                .inn(company.getInn())
                .industry(company.getIndustry())
                .contactInfo(company.getContactInfo())
                .status(company.getStatus())
                .totalJobs(company.getJobs() != null ? company.getJobs().size() : 0)
                .build();
    }

    public long count() {
        return companyRepository.count();
    }
}