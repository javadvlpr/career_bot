package uz.career.career_bot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.career.career_bot.entity.Company;
import uz.career.career_bot.entity.Job;
import uz.career.career_bot.entity.JobOffer;
import uz.career.career_bot.entity.User;
import uz.career.career_bot.enums.OfferStatus;
import uz.career.career_bot.exception.AlreadyExistsException;
import uz.career.career_bot.exception.NotFoundException;
import uz.career.career_bot.repository.JobOfferRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobOfferServiceTest {

    @Mock
    private JobOfferRepository jobOfferRepository;

    private JobOfferService jobOfferService;
    private JobOffer jobOffer;
    private Job job;
    private User user;
    private Company company;

    @BeforeEach
    void setUp() {
        jobOfferService = new JobOfferService(jobOfferRepository);

        user = User.builder().id(1L).name("John Doe").build();
        job = Job.builder().id(1L).title("Java Developer").build();
        company = Company.builder().id(1L).companyName("Tech Corp").build();
        jobOffer = JobOffer.builder()
                .id(1L)
                .job(job)
                .user(user)
                .company(company)
                .status(OfferStatus.SENT)
                .build();
    }

    @Test
    void testSendOffer_Success() {
        when(jobOfferRepository.existsByJobIdAndUserId(1L, 1L)).thenReturn(false);
        when(jobOfferRepository.save(any())).thenReturn(jobOffer);

        JobOffer result = jobOfferService.sendOffer(job, user, company);

        assertNotNull(result);
        assertEquals(OfferStatus.SENT, result.getStatus());
        verify(jobOfferRepository).save(any());
    }

    @Test
    void testSendOffer_AlreadyExists() {
        when(jobOfferRepository.existsByJobIdAndUserId(1L, 1L)).thenReturn(true);

        assertThrows(AlreadyExistsException.class, () ->
                jobOfferService.sendOffer(job, user, company)
        );
        verify(jobOfferRepository, never()).save(any());
    }

    @Test
    void testRespondToOffer_Interested() {
        when(jobOfferRepository.findById(1L)).thenReturn(Optional.of(jobOffer));
        when(jobOfferRepository.save(any())).thenReturn(jobOffer);

        JobOffer result = jobOfferService.respondToOffer(1L, OfferStatus.INTERESTED);

        assertEquals(OfferStatus.INTERESTED, result.getStatus());
        assertNotNull(result.getRespondedAt());
        verify(jobOfferRepository).save(any());
    }

    @Test
    void testRespondToOffer_NotInterested() {
        when(jobOfferRepository.findById(1L)).thenReturn(Optional.of(jobOffer));
        when(jobOfferRepository.save(any())).thenReturn(jobOffer);

        JobOffer result = jobOfferService.respondToOffer(1L, OfferStatus.NOT_INTERESTED);

        assertEquals(OfferStatus.NOT_INTERESTED, result.getStatus());
        assertNotNull(result.getRespondedAt());
        verify(jobOfferRepository).save(any());
    }

    @Test
    void testRespondToOffer_NotFound() {
        when(jobOfferRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                jobOfferService.respondToOffer(1L, OfferStatus.INTERESTED)
        );
    }

    @Test
    void testGetUserOffers() {
        List<JobOffer> offers = List.of(jobOffer);
        when(jobOfferRepository.findByUserId(1L)).thenReturn(offers);

        List<JobOffer> result = jobOfferService.getUserOffers(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(user.getId(), result.get(0).getUser().getId());
    }

    @Test
    void testGetPendingOffersForUser() {
        List<JobOffer> pending = List.of(jobOffer);
        when(jobOfferRepository.findByUserIdAndStatus(1L, OfferStatus.SENT)).thenReturn(pending);

        List<JobOffer> result = jobOfferService.getPendingOffersForUser(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(OfferStatus.SENT, result.get(0).getStatus());
    }

    @Test
    void testGetCompanyOffers() {
        List<JobOffer> offers = List.of(jobOffer);
        when(jobOfferRepository.findByCompanyId(1L)).thenReturn(offers);

        List<JobOffer> result = jobOfferService.getCompanyOffers(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetJobOffers() {
        List<JobOffer> offers = List.of(jobOffer);
        when(jobOfferRepository.findByJobId(1L)).thenReturn(offers);

        List<JobOffer> result = jobOfferService.getJobOffers(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
