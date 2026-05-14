package uz.career.career_bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.career.career_bot.entity.Company;
import uz.career.career_bot.entity.Job;
import uz.career.career_bot.entity.JobOffer;
import uz.career.career_bot.entity.User;
import uz.career.career_bot.enums.OfferStatus;
import uz.career.career_bot.exception.AlreadyExistsException;
import uz.career.career_bot.exception.NotFoundException;
import uz.career.career_bot.repository.JobOfferRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobOfferService {

    private final JobOfferRepository jobOfferRepository;

    public JobOffer sendOffer(Job job, User user, Company company) {
        if (jobOfferRepository.existsByJobIdAndUserId(job.getId(), user.getId())) {
            throw new AlreadyExistsException(
                    "This user has already been offered this vacancy.");
        }
        JobOffer offer = JobOffer.builder()
                .job(job)
                .user(user)
                .company(company)
                .build();
        return jobOfferRepository.save(offer);
    }

    public JobOffer respondToOffer(Long offerId, OfferStatus response) {
        JobOffer offer = jobOfferRepository.findById(offerId)
                .orElseThrow(() -> new NotFoundException("JobOffer", offerId));
        offer.setStatus(response);
        offer.setRespondedAt(LocalDateTime.now());
        return jobOfferRepository.save(offer);
    }

    public List<JobOffer> getUserOffers(Long userId) {
        return jobOfferRepository.findByUserId(userId);
    }

    public List<JobOffer> getPendingOffersForUser(Long userId) {
        return jobOfferRepository.findByUserIdAndStatus(userId, OfferStatus.SENT);
    }

    public List<JobOffer> getCompanyOffers(Long companyId) {
        return jobOfferRepository.findByCompanyId(companyId);
    }

    public List<JobOffer> getJobOffers(Long jobId) {
        return jobOfferRepository.findByJobId(jobId);
    }
}