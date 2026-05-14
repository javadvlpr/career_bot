package uz.career.career_bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uz.career.career_bot.dto.JoobleRequest;
import uz.career.career_bot.dto.JoobleResponse;
import uz.career.career_bot.entity.Job;
import uz.career.career_bot.enums.JobSource;
import uz.career.career_bot.enums.JobStatus;
import uz.career.career_bot.enums.JobType;
import uz.career.career_bot.repository.JobRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JoobleService {

    private final JobRepository jobRepository;

    @Value("${jooble.api-key}")
    private String apiKey;

    @Value("${jooble.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public JoobleResponse searchJobs(String keywords, String location) {
        String url = baseUrl + "/" + apiKey;

        JoobleRequest request = JoobleRequest.builder()
                .keywords(keywords)
                .location(location)
                .page(1)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<JoobleRequest> entity = new HttpEntity<>(request, headers);

        log.info("Jooble API request: keywords='{}', location='{}'", keywords, location);

        try {
            ResponseEntity<JoobleResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, JoobleResponse.class
            );
            JoobleResponse body = response.getBody();
            if (body != null) {
                log.info("Jooble response: totalCount={}, received={}",
                        body.getTotalCount(),
                        body.getJobs() != null ? body.getJobs().size() : 0);
            }
            return body;
        } catch (Exception e) {
            log.error("Jooble API error: {}", e.getMessage(), e);
            return null;
        }
    }

    public int importJobs(String keywords, String location) {
        JoobleResponse response = searchJobs(keywords, location);

        if (response == null || response.getJobs() == null || response.getJobs().isEmpty()) {
            log.warn("Jooble returned no jobs for: '{}' '{}'", keywords, location);
            return 0;
        }

        int imported = 0;
        int skipped = 0;

        for (JoobleResponse.JoobleJob joobleJob : response.getJobs()) {
            if (joobleJob.getId() != null && jobRepository.existsByExternalId(joobleJob.getId())) {
                skipped++;
                continue;
            }

            Job job = Job.builder()
                    .title(cleanText(joobleJob.getTitle()))
                    .description(cleanText(joobleJob.getSnippet()))
                    .location(joobleJob.getLocation())
                    .externalUrl(joobleJob.getLink())
                    .externalId(joobleJob.getId())
                    .source(JobSource.JOOBLE)
                    .status(JobStatus.APPROVED)
                    .jobType(parseJobType(joobleJob.getType(), joobleJob.getLocation()))
                    .expiresAt(LocalDateTime.now().plusDays(30))
                    .build();

            parseSalary(joobleJob.getSalary(), job);
            jobRepository.save(job);
            imported++;
        }

        log.info("Jooble import '{}' '{}': imported={}, skipped={}", keywords, location, imported, skipped);
        return imported;
    }

    /**
     * Remote jobs — import by profession
     */
    public int importAllCategories() {
        List<String[]> searches = List.of(
                new String[]{"Java developer", "Remote"},
                new String[]{"Python developer", "Remote"},
                new String[]{"Frontend developer", "Remote"},
                new String[]{"Backend developer", "Remote"},
                new String[]{"DevOps", "Remote"},
                new String[]{"Data analyst", "Remote"},
                new String[]{"UI/UX designer", "Remote"},
                new String[]{"Project manager", "Remote"},
                new String[]{"Marketing manager", "Remote"},
                new String[]{"Content writer", "Remote"}
        );

        int totalImported = 0;
        for (String[] search : searches) {
            try {
                totalImported += importJobs(search[0], search[1]);
                Thread.sleep(500);
            } catch (Exception e) {
                log.error("Error importing '{}' '{}': {}", search[0], search[1], e.getMessage());
            }
        }
        log.info("Total Jooble import: {} jobs", totalImported);
        return totalImported;
    }

    private JobType parseJobType(String type, String location) {
        if (location != null && location.toLowerCase().contains("remote")) {
            return JobType.REMOTE;
        }
        if (type == null || type.isEmpty()) return JobType.FULL_TIME;
        String lower = type.toLowerCase();
        if (lower.contains("part")) return JobType.PART_TIME;
        if (lower.contains("remote")) return JobType.REMOTE;
        if (lower.contains("freelance")) return JobType.FREELANCE;
        if (lower.contains("intern")) return JobType.INTERNSHIP;
        return JobType.FULL_TIME;
    }

    /**
     * Cleans up HTML tags and codes like &nbsp;
     */
    private String cleanText(String text) {
        if (text == null) return null;
        return text
                .replaceAll("<[^>]+>", "")
                .replaceAll("&nbsp;", " ")
                .replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&quot;", "\"")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Jooble parses salaries:
     * "$100k", "$100k - $150k", "$30 per hour", "$8,625 per month"
     * Will convert everything into approximate annual UZS sums.
     */
    private void parseSalary(String salary, Job job) {
        if (salary == null || salary.isEmpty()) return;

        try {
            String s = salary.toLowerCase()
                    .replace("$", "")
                    .replace(",", "")
                    .trim();

            boolean perHour = s.contains("per hour") || s.contains("/hr");
            boolean perMonth = s.contains("per month") || s.contains("/mo");
            boolean isK = s.contains("k");

            String cleaned = s.replaceAll("[^0-9.\\-]", "");

            int min, max;
            if (cleaned.contains("-")) {
                String[] parts = cleaned.split("-");
                if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) return;
                min = (int) Double.parseDouble(parts[0]);
                max = (int) Double.parseDouble(parts[1]);
            } else if (!cleaned.isEmpty()) {
                min = max = (int) Double.parseDouble(cleaned);
            } else {
                return;
            }

            if (isK) {
                min *= 1000;
                max *= 1000;
            }

            if (perHour) {
                min *= 2080;
                max *= 2080;
            } else if (perMonth) {
                min *= 12;
                max *= 12;
            }

            int usdToUzs = 12500;
            job.setSalaryMin(min * usdToUzs);
            job.setSalaryMax(max * usdToUzs);

        } catch (Exception e) {
            log.debug("Could not parse salary '{}': {}", salary, e.getMessage());
        }
    }
}