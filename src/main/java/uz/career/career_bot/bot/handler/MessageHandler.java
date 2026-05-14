package uz.career.career_bot.bot.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.career.career_bot.bot.CareerBot;
import uz.career.career_bot.bot.keyboard.KeyboardFactory;
import uz.career.career_bot.dto.MatchResultDTO;
import uz.career.career_bot.entity.*;
import uz.career.career_bot.enums.*;
import uz.career.career_bot.service.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageHandler {

    private final UserService userService;
    private final CompanyService companyService;
    private final JobService jobService;
    private final CategoryService categoryService;
    private final MatchingService matchingService;
    private final SavedJobService savedJobService;
    private final JobOfferService jobOfferService;
    private final ApplicationService applicationService;
    private final KeyboardFactory keyboard;
    private final ApplicationContext applicationContext;
    private final LocationService locationService;

    // ==================== IN-MEMORY STATE ====================

    /** In-progress vacancy draft held while HR is filling the form */
    private final Map<Long, Job> currentJobDraft = new ConcurrentHashMap<>();

    /** Cached search results per user, used for pagination */
    private final Map<Long, List<MatchResultDTO>> userSearchResults = new ConcurrentHashMap<>();

    /** Cached candidate list per HR session */
    private final Map<Long, List<MatchResultDTO>> hrCandidateResults = new ConcurrentHashMap<>();

    /** Currently selected vacancy for the HR session */
    private final Map<Long, Long> hrSelectedJob = new ConcurrentHashMap<>();

    /** Skill ids currently toggled during user skill selection */
    private final Map<Long, Set<Long>> userSelectedSkills = new ConcurrentHashMap<>();

    /** Skill ids currently toggled during HR vacancy skill selection */
    private final Map<Long, Set<Long>> hrSelectedSkills = new ConcurrentHashMap<>();

    /** Current browse mode for the user (matched or all) */
    private final Map<Long, BrowseMode> userBrowseMode = new ConcurrentHashMap<>();

    /** Current page number being viewed by the user */
    private final Map<Long, Integer> userBrowsePage = new ConcurrentHashMap<>();

    /** Vacancy id pending application while CV upload is awaited */
    private final Map<Long, Long> pendingApplyJobId = new ConcurrentHashMap<>();

    /** Offer id awaiting the user contact before acceptance */
    private final Map<Long, Long> pendingContactOffer = new ConcurrentHashMap<>();

    // Getters — also consumed by CallbackHandler
    public Map<Long, Job> getCurrentJobDraft() { return currentJobDraft; }
    public Map<Long, List<MatchResultDTO>> getUserSearchResults() { return userSearchResults; }
    public Map<Long, List<MatchResultDTO>> getHrCandidateResults() { return hrCandidateResults; }
    public Map<Long, Long> getHrSelectedJob() { return hrSelectedJob; }
    public Map<Long, Set<Long>> getUserSelectedSkills() { return userSelectedSkills; }
    public Map<Long, Set<Long>> getHrSelectedSkills() { return hrSelectedSkills; }
    public Map<Long, Long> getPendingApplyJobId() { return pendingApplyJobId; }
    public Map<Long, Long> getPendingContactOffer() { return pendingContactOffer; }

    public SendMessage handle(Message message) {
        Long chatId = message.getChatId();
        Long telegramId = message.getFrom().getId();
        String text = message.getText();

        if ("/start".equals(text)) return handleStart(chatId, telegramId);

        Optional<User> userOpt = userService.getByTelegramId(telegramId);
        if (userOpt.isPresent()) return handleUserMessage(userOpt.get(), chatId, text);

        Optional<Company> companyOpt = companyService.getByTelegramId(telegramId);
        if (companyOpt.isPresent()) return handleCompanyMessage(companyOpt.get(), chatId, text);

        return sendText(chatId, "Please send /start to begin.");
    }

    private SendMessage handleStart(Long chatId, Long telegramId) {
        Optional<User> userOpt = userService.getByTelegramId(telegramId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setBotState(BotState.USER_MAIN_MENU);
            userService.save(user);
            SendMessage msg = sendText(chatId, "Welcome back! 👋");
            msg.setReplyMarkup(keyboard.userMainMenu());
            return msg;
        }

        Optional<Company> companyOpt = companyService.getByTelegramId(telegramId);
        if (companyOpt.isPresent()) {
            Company company = companyOpt.get();
            if (company.getStatus() == CompanyStatus.APPROVED) {
                company.setBotState(BotState.HR_MAIN_MENU);
                companyService.save(company);
                SendMessage msg = sendText(chatId, "Welcome back! 🏢");
                msg.setReplyMarkup(keyboard.hrMainMenu());
                return msg;
            }
            if (company.getStatus() == CompanyStatus.PENDING) {
                return sendText(chatId, "Your company is not yet approved. Please wait.");
            }
            return sendText(chatId, "Your company has been rejected.");
        }

        SendMessage msg = sendText(chatId, "Welcome! 👋\nHow would you like to register?");
        msg.setReplyMarkup(keyboard.roleKeyboard());
        return msg;
    }

    private SendMessage handleUserMessage(User user, Long chatId, String text) {
        BotState state = user.getBotState();

        switch (state) {
            case ENTER_NAME -> {
                user.setName(text);
                user.setBotState(BotState.SELECT_CATEGORY);
                userService.save(user);
                SendMessage msg = sendText(chatId, "Choose an industry:");
                msg.setReplyMarkup(keyboard.categoryKeyboard(categoryService.getAll(), "ucat_"));
                return msg;
            }
            case SELECT_EXPERIENCE -> {
                Integer years = parsePositiveInt(text);
                if (years == null) return sendText(chatId, "Please enter a positive number (e.g. 3):");
                user.setExperienceLevel(yearsToLevel(years));
                user.setBotState(BotState.SELECT_LOCATION);
                userService.save(user);
                SendMessage msg = sendText(chatId, "Choose your location:");
                msg.setReplyMarkup(keyboard.locationKeyboard(locationService.getActive(), "loc_"));
                return msg;
            }
            case ENTER_SALARY -> {
                Integer salary = parsePositiveInt(text);
                if (salary == null) return sendText(chatId, "Please enter a positive number (e.g. 5000000):");
                user.setExpectedSalary(salary);
                user.setBotState(BotState.USER_MAIN_MENU);
                userService.save(user);
                SendMessage msg = sendText(chatId,
                        "✅ Registration complete!\n\nStart searching for matching vacancies 👇");
                msg.setReplyMarkup(keyboard.userMainMenu());
                return msg;
            }
            case EDIT_NAME -> {
                user.setName(text);
                user.setBotState(BotState.USER_MAIN_MENU);
                userService.save(user);
                return sendText(chatId, "✅ Name updated: " + text);
            }
            case EDIT_EXPERIENCE -> {
                Integer years = parsePositiveInt(text);
                if (years == null) return sendText(chatId, "Please enter a positive number:");
                user.setExperienceLevel(yearsToLevel(years));
                user.setBotState(BotState.USER_MAIN_MENU);
                userService.save(user);
                SendMessage msg = sendText(chatId, "✅ Experience updated: " + years + " yil");
                msg.setReplyMarkup(keyboard.userMainMenu());
                return msg;
            }
            case EDIT_SALARY -> {
                Integer salary = parsePositiveInt(text);
                if (salary == null) return sendText(chatId, "Please enter a positive number:");
                user.setExpectedSalary(salary);
                user.setBotState(BotState.USER_MAIN_MENU);
                userService.save(user);
                return sendText(chatId, "✅ Salary updated: " + salary);
            }
            case EDIT_CONTACT -> {
                if (text == null || text.trim().isEmpty() || text.trim().length() < 3) {
                    return sendText(chatId, "Please enter a valid contact (phone, email, or @username):");
                }
                user.setPhoneNumber(text.trim());
                user.setBotState(BotState.USER_MAIN_MENU);
                userService.save(user);
                log.info("User contact updated: userId={}", user.getId());
                SendMessage msg = sendText(chatId, "✅ Contact updated: " + text);
                msg.setReplyMarkup(keyboard.userMainMenu());
                return msg;
            }
            default -> {
                Long pendingOfferId = pendingContactOffer.get(user.getId());
                if (pendingOfferId != null) return handleTextContact(user, chatId, text, pendingOfferId);
            }
        }

        return switch (text) {
            case "🔍 Search jobs" -> handleSearchJobs(chatId);
            case "📋 My profile" -> handleViewProfile(user, chatId);
            case "💾 Saved jobs" -> handleSavedJobs(user, chatId);
            case "📩 Received offers" -> handleOffers(user, chatId);
            case "⚙️ Settings" -> handleSettings(chatId);
            default -> sendText(chatId, "Choose from the menu below 👇");
        };
    }

    private SendMessage handleCompanyMessage(Company company, Long chatId, String text) {
        BotState state = company.getBotState();

        switch (state) {
            case ENTER_COMPANY_NAME -> {
                company.setCompanyName(text);
                company.setBotState(BotState.ENTER_COMPANY_INN);
                companyService.save(company);
                return sendText(chatId, "Enter the company tax ID (INN/STIR):");
            }
            case ENTER_COMPANY_INN -> {
                company.setInn(text);
                company.setBotState(BotState.SELECT_INDUSTRY);
                companyService.save(company);
                SendMessage msg = sendText(chatId, "Choose an industry:");
                msg.setReplyMarkup(keyboard.industryKeyboard());
                return msg;
            }
            case ENTER_COMPANY_CONTACT -> {
                company.setContactInfo(text);
                company.setStatus(CompanyStatus.PENDING);
                company.setBotState(BotState.COMPANY_PENDING);
                companyService.save(company);
                return sendText(chatId, "✅ Your application has been received!\n\nPlease wait for admin approval.");
            }
            case ADD_JOB_TITLE -> {
                currentJobDraft.put(company.getId(), Job.builder().title(text).build());
                company.setBotState(BotState.ADD_JOB_DESCRIPTION);
                companyService.save(company);
                return sendText(chatId, "Enter the vacancy description:");
            }
            case ADD_JOB_DESCRIPTION -> {
                Job draft = currentJobDraft.get(company.getId());
                if (draft == null) return errorAndReset(company, chatId);
                draft.setDescription(text);
                company.setBotState(BotState.ADD_JOB_CATEGORY);
                companyService.save(company);
                SendMessage msg = sendText(chatId, "Choose the vacancy industry:");
                msg.setReplyMarkup(keyboard.categoryKeyboard(categoryService.getAll(), "jcat_"));
                return msg;
            }
            case ADD_JOB_SALARY -> {
                Job draft = currentJobDraft.get(company.getId());
                if (draft == null) return errorAndReset(company, chatId);
                return parseSalaryAndContinue(company, chatId, text, draft);
            }
            default -> {}
        }

        if (company.getStatus() != CompanyStatus.APPROVED) {
            return sendText(chatId, "Your company is not yet approved.");
        }

        return switch (text) {
            case "➕ Post a vacancy" -> handleAddJob(company, chatId);
            case "📋 My vacancies" -> handleMyJobs(company, chatId);
            case "👥 Matched candidates" -> handleCandidates(company, chatId);
            case "⚙️ Company profile" -> handleCompanyProfile(company, chatId);
            case "📨 Applicants" -> handleApplicantsForHr(company, chatId);
            default -> sendText(chatId, "Choose from the menu below 👇");
        };
    }

    private SendMessage handleApplicantsForHr(Company company, Long chatId) {
        List<Job> approvedJobs = jobService.getByCompanyId(company.getId()).stream()
                .filter(j -> j.getStatus() == JobStatus.APPROVED)
                .toList();
        if (approvedJobs.isEmpty()) {
            return sendText(chatId, "You have no approved vacancies.");
        }
        SendMessage msg = sendText(chatId, "📨 Which vacancy's applicants would you like to see?");
        msg.setReplyMarkup(keyboard.applicantsJobsKeyboard(approvedJobs));
        return msg;
    }

    private SendMessage parseSalaryAndContinue(Company company, Long chatId, String text, Job draft) {
        try {
            if (text.contains("-")) {
                String[] parts = text.split("-");
                int min = Integer.parseInt(parts[0].trim());
                int max = Integer.parseInt(parts[1].trim());
                if (min < 0 || max < 0) return sendText(chatId, "Salary cannot be negative:");
                if (min > max) return sendText(chatId, "Minimum salary cannot exceed maximum:");
                draft.setSalaryMin(min);
                draft.setSalaryMax(max);
            } else {
                int salary = Integer.parseInt(text.trim());
                if (salary < 0) return sendText(chatId, "Salary cannot be negative:");
                draft.setSalaryMin(salary);
                draft.setSalaryMax(salary);
            }
        } catch (NumberFormatException e) {
            return sendText(chatId, "Invalid format. Example: 5000000 or 5000000-8000000");
        }
        company.setBotState(BotState.ADD_JOB_LOCATION);
        companyService.save(company);
        SendMessage msg = sendText(chatId, "Location choose:");
        msg.setReplyMarkup(keyboard.locationKeyboard(locationService.getActive(), "jobloc_"));
        return msg;
    }

    private SendMessage handleSearchJobs(Long chatId) {
        SendMessage msg = sendText(chatId,
                "🔍 How would you like to view jobs?\n\n" +
                        "🎯 Matched jobs — the best fit for you\n" +
                        "🌍 All jobs — every available vacancy");
        msg.setReplyMarkup(keyboard.searchFilterKeyboard());
        return msg;
    }

    public SendMessage showJobsList(User user, Long chatId, BrowseMode mode, int page) {
        List<MatchResultDTO> results;
        if (page == 0) {
            results = loadJobs(user, mode);
            userSearchResults.put(user.getId(), results);
        } else {
            results = userSearchResults.get(user.getId());
            if (results == null) {
                results = loadJobs(user, mode);
                userSearchResults.put(user.getId(), results);
            }
        }

        if (results.isEmpty()) {
            String emptyMsg = switch (mode) {
                case SAVED -> "💾 No saved jobs.";
                default -> "No vacancies available right now 😔";
            };
            return sendText(chatId, emptyMsg);
        }

        userBrowseMode.put(user.getId(), mode);
        userBrowsePage.put(user.getId(), page);

        String title = switch (mode) {
            case MATCHED -> "🎯 Matched vacancies for you (" + results.size() + " ta):";
            case ALL -> "🌍 All vacancies (" + results.size() + " ta):";
            case SAVED -> "💾 Saved jobs (" + results.size() + " ta):";
        };

        SendMessage msg = sendText(chatId, title + "\n\nChoose a vacancy to view details:");
        msg.setReplyMarkup(keyboard.jobListKeyboard(results, page, 5, mode));
        return msg;
    }

    /**
     * Pagination — edits the existing message instead of sending a new one
     */
    public void editJobsList(Long chatId, Integer messageId, User user, BrowseMode mode, int page) {
        List<MatchResultDTO> results = userSearchResults.get(user.getId());
        if (results == null) {
            results = loadJobs(user, mode);
            userSearchResults.put(user.getId(), results);
        }
        if (results.isEmpty()) return;

        userBrowseMode.put(user.getId(), mode);
        userBrowsePage.put(user.getId(), page);

        String title = switch (mode) {
            case MATCHED -> "🎯 Matched vacancies for you (" + results.size() + " ta):";
            case ALL -> "🌍 All vacancies (" + results.size() + " ta):";
            case SAVED -> "💾 Saved jobs (" + results.size() + " ta):";
        };
        String text = title + "\n\nChoose a vacancy to view details:";

        try {
            EditMessageText edit = EditMessageText.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .text(text)
                    .replyMarkup(keyboard.jobListKeyboard(results, page, 5, mode))
                    .build();
            applicationContext.getBean(CareerBot.class).execute(edit);
        } catch (Exception e) {
            log.error("Failed to edit jobs list: {}", e.getMessage());
        }
    }

    private List<MatchResultDTO> loadJobs(User user, BrowseMode mode) {
        return switch (mode) {
            case MATCHED -> matchingService.findJobsForUser(user, 100);
            case ALL -> buildAllJobsWithScore(user);
            case SAVED -> buildSavedJobsWithScore(user);
        };
    }

    private List<MatchResultDTO> buildSavedJobsWithScore(User user) {
        List<SavedJob> savedJobs = savedJobService.getUserSavedJobsWithDetails(user.getId());
        List<MatchResultDTO> results = new ArrayList<>();
        for (SavedJob sj : savedJobs) {
            Job job = sj.getJob();
            if (job.getStatus() != JobStatus.APPROVED) continue;

            double score = matchingService.calculateScore(user, job);
            String subtitle = (job.getCompany() != null) ? job.getCompany().getCompanyName() : "Jooble";
            results.add(MatchResultDTO.builder()
                    .targetId(job.getId())
                    .title(job.getTitle())
                    .subtitle(subtitle)
                    .score(score)
                    .scorePercent((int) Math.round(score * 100))
                    .location(job.getLocation())
                    .build());
        }
        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return results;
    }

    private List<MatchResultDTO> buildAllJobsWithScore(User user) {
        List<Job> allJobs = jobService.getApprovedJobs();
        List<MatchResultDTO> results = new ArrayList<>();
        for (Job job : allJobs) {
            double score = matchingService.calculateScore(user, job);
            String subtitle = (job.getCompany() != null) ? job.getCompany().getCompanyName() : "Jooble";
            results.add(MatchResultDTO.builder()
                    .targetId(job.getId())
                    .title(job.getTitle())
                    .subtitle(subtitle)
                    .score(score)
                    .scorePercent((int) Math.round(score * 100))
                    .location(job.getLocation())
                    .build());
        }
        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return results;
    }

    public SendMessage showJobDetails(User user, Long chatId, Long jobId, int returnPage, BrowseMode mode) {
        Job job = jobService.getById(jobId);
        double score = matchingService.calculateScore(user, job);

        StringBuilder sb = new StringBuilder();
        sb.append("💼 ").append(job.getTitle()).append("\n\n");
        if (job.getCompany() != null) sb.append("🏢 ").append(job.getCompany().getCompanyName()).append("\n");
        if (job.getProfession() != null) sb.append("👔 ").append(job.getProfession().getName()).append("\n");
        sb.append("🎯 Match: ").append((int) Math.round(score * 100)).append("%\n");
        if (job.getDescription() != null) sb.append("\n📝 ").append(job.getDescription()).append("\n");
        if (job.getRequirements() != null) sb.append("\n✅ Requirements: ").append(job.getRequirements()).append("\n");
        if (job.getLocation() != null) sb.append("📍 ").append(job.getLocation()).append("\n");
        if (job.getSalaryMin() != null || job.getSalaryMax() != null) {
            sb.append("💰 ")
                    .append(job.getSalaryMin() != null ? job.getSalaryMin() : "?")
                    .append(" - ")
                    .append(job.getSalaryMax() != null ? job.getSalaryMax() : "?")
                    .append(" so'm\n");
        }
        if (job.getJobType() != null) sb.append("⏱ ").append(job.getJobType().name()).append("\n");

        if (job.getExternalUrl() != null && !job.getExternalUrl().isEmpty()) {
            sb.append("\n🌐 Source: ")
                    .append(job.getSource() != null ? job.getSource().name() : "External site").append("\n");
            sb.append("📄 Short summary. Use the button below to open the source site for the full description and to apply.\n");
        }

        if (job.getContactInfo() != null && !job.getContactInfo().isEmpty()) {
            sb.append("\n📞 Contact: ").append(job.getContactInfo()).append("\n");
        }

        boolean alreadyApplied = applicationService.hasApplied(jobId, user.getId());
        boolean alreadySaved = savedJobService.isJobSaved(user.getId(), jobId);

        SendMessage msg = sendText(chatId, sb.toString());
        msg.setReplyMarkup(keyboard.jobDetailsKeyboard(jobId, returnPage, mode,
                alreadyApplied, alreadySaved, job.getExternalUrl(), job.getContactInfo()));
        return msg;
    }

    private SendMessage handleViewProfile(User user, Long chatId) {
        StringBuilder sb = new StringBuilder("📋 Your profile:\n\n");
        sb.append("👤 Name: ").append(user.getName() != null ? user.getName() : "-").append("\n");

        if (user.getProfession() != null) {
            sb.append("💼 Profession: ").append(user.getProfession().getName()).append("\n");
            if (user.getProfession().getCategory() != null) {
                sb.append("🏭 Industry: ").append(user.getProfession().getCategory().getName()).append("\n");
            }
        }

        sb.append("🛠 Skills: ");
        if (user.getSkills() != null && !user.getSkills().isEmpty()) {
            user.getSkills().forEach(s -> sb.append(s.getName()).append(", "));
        } else {
            sb.append("-");
        }
        sb.append("\n📊 Experience: ")
                .append(user.getExperienceLevel() != null ? user.getExperienceLevel().name() : "-").append("\n");
        sb.append("📍 Location: ").append(user.getLocation() != null ? user.getLocation() : "-").append("\n");
        sb.append("💰 Salary: ").append(user.getExpectedSalary() != null ? user.getExpectedSalary() : "-").append("\n");
        sb.append("📞 Contact: ").append(user.getPhoneNumber() != null ? user.getPhoneNumber() : "-").append("\n");
        sb.append("🔍 Status: ").append(user.getSearchStatus().name()).append("\n");

        boolean hasCv = user.getCvFileId() != null && !user.getCvFileId().isEmpty();
        sb.append("📄 CV: ").append(hasCv ? "not upload (" + user.getCvFileName() + ")" : "no").append("\n");

        SendMessage msg = sendText(chatId, sb.toString());
        msg.setReplyMarkup(keyboard.profileEditKeyboard(hasCv));
        return msg;
    }

    private SendMessage handleSavedJobs(User user, Long chatId) {
        userSearchResults.remove(user.getId());
        return showJobsList(user, chatId, BrowseMode.SAVED, 0);
    }

    private SendMessage handleOffers(User user, Long chatId) {
        List<JobOffer> offers = jobOfferService.getPendingOffersForUser(user.getId());
        if (offers.isEmpty()) return sendText(chatId, "No new offers yet.");

        StringBuilder sb = new StringBuilder("📩 Received offers:\n\n");
        for (JobOffer offer : offers) {
            sb.append("💼 ").append(offer.getJob().getTitle()).append("\n");
            sb.append("🏢 ").append(offer.getCompany().getCompanyName()).append("\n");
            sb.append("📅 ").append(offer.getSentAt().toLocalDate()).append("\n\n");
        }
        SendMessage msg = sendText(chatId, sb.toString());
        msg.setReplyMarkup(keyboard.offerResponseKeyboard(offers.get(0).getId()));
        return msg;
    }

    private SendMessage handleSettings(Long chatId) {
        SendMessage msg = sendText(chatId, "⚙️ Settings:");
        msg.setReplyMarkup(keyboard.userSettingsKeyboard());
        return msg;
    }

    private SendMessage handleAddJob(Company company, Long chatId) {
        company.setBotState(BotState.ADD_JOB_TITLE);
        companyService.save(company);
        return sendText(chatId, "Enter the vacancy title (e.g. Java Backend Developer):");
    }

    private SendMessage handleMyJobs(Company company, Long chatId) {
        List<Job> jobs = jobService.getByCompanyId(company.getId());
        if (jobs.isEmpty()) return sendText(chatId, "You have no vacancies yet.");

        StringBuilder sb = new StringBuilder("📋 Your vacancies:\n\n");
        for (Job job : jobs) {
            String emoji = switch (job.getStatus()) {
                case APPROVED -> "✅";
                case PENDING -> "⏳";
                case REJECTED -> "❌";
                case EXPIRED -> "⌛";
                case CLOSED -> "🔒";
            };
            sb.append(emoji).append(" ").append(job.getTitle())
                    .append(" — ").append(job.getStatus().name()).append("\n");
        }
        sb.append("\nChoose a vacancy to manage:");
        SendMessage msg = sendText(chatId, sb.toString());
        msg.setReplyMarkup(keyboard.manageJobsKeyboard(jobs));
        return msg;
    }

    public SendMessage handleCandidates(Company company, Long chatId) {
        List<Job> approvedJobs = jobService.getByCompanyId(company.getId()).stream()
                .filter(j -> j.getStatus() == JobStatus.APPROVED)
                .toList();
        if (approvedJobs.isEmpty()) return sendText(chatId, "You have no approved vacancies.");

        SendMessage msg = sendText(chatId, "👥 Choose a vacancy:");
        msg.setReplyMarkup(keyboard.myJobsSelectKeyboard(approvedJobs));
        return msg;
    }

    public SendMessage showHrJobActions(Company company, Long chatId, Long jobId) {
        Job job = jobService.getById(jobId);
        hrSelectedJob.put(company.getId(), jobId);

        long applicantCount = applicationService.countApplicationsForJob(jobId);
        String text = "💼 " + job.getTitle() + "\n\n" +
                "📨 Applicants: " + applicantCount + " ta\n\n" +
                "Which list would you like to see?";

        SendMessage msg = sendText(chatId, text);
        msg.setReplyMarkup(keyboard.hrJobActionsKeyboard(jobId));
        return msg;
    }

    public SendMessage showHrCandidatesList(Company company, Long chatId, Long jobId,
                                            CandidateSource source, int page) {
        List<MatchResultDTO> candidates = loadCandidates(company, jobId, source);

        if (candidates.isEmpty()) {
            String emptyMsg = (source == CandidateSource.MATCHED)
                    ? "No matching candidates for this vacancy."
                    : "No one has applied to this vacancy yet.";
            SendMessage msg = sendText(chatId, emptyMsg);
            msg.setReplyMarkup(keyboard.hrJobActionsKeyboard(jobId));
            return msg;
        }

        String title = (source == CandidateSource.MATCHED)
                ? "🎯 Matched candidates (" + candidates.size() + " ta):"
                : "📨 Applicants (" + candidates.size() + " ta):";

        SendMessage msg = sendText(chatId, title + "\n\nSelect a candidate to view details:");
        msg.setReplyMarkup(keyboard.hrCandidatesListKeyboard(candidates, page, 5, source, jobId));
        return msg;
    }

    /**
     * HR candidates for pagination — edits old post
     */
    public void editHrCandidatesList(Long chatId, Integer messageId, Company company,
                                     Long jobId, CandidateSource source, int page) {
        List<MatchResultDTO> candidates = loadCandidates(company, jobId, source);
        if (candidates.isEmpty()) return;

        String title = (source == CandidateSource.MATCHED)
                ? "🎯 Matched candidates (" + candidates.size() + " ta):"
                : "📨 Applicants (" + candidates.size() + " ta):";
        String text = title + "\n\nSelect a candidate to view details:";

        try {
            EditMessageText edit = EditMessageText.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .text(text)
                    .replyMarkup(keyboard.hrCandidatesListKeyboard(candidates, page, 5, source, jobId))
                    .build();
            applicationContext.getBean(CareerBot.class).execute(edit);
        } catch (Exception e) {
            log.error("Failed to edit candidates list: {}", e.getMessage());
        }
    }

    private List<MatchResultDTO> loadCandidates(Company company, Long jobId, CandidateSource source) {
        if (source == CandidateSource.MATCHED) {
            List<MatchResultDTO> cached = hrCandidateResults.get(company.getId());
            if (cached != null) return cached;
            Job job = jobService.getById(jobId);
            List<MatchResultDTO> fresh = matchingService.findCandidatesForJob(job, 100);
            hrCandidateResults.put(company.getId(), fresh);
            return fresh;
        }
        return buildApplicantsList(jobId);
    }

    private List<MatchResultDTO> buildApplicantsList(Long jobId) {
        List<Application> applications = applicationService.getJobApplications(jobId);
        Job job = jobService.getById(jobId);
        List<MatchResultDTO> results = new ArrayList<>();

        for (Application app : applications) {
            User u = app.getUser();
            double score = matchingService.calculateScore(u, job);
            results.add(MatchResultDTO.builder()
                    .targetId(u.getId())
                    .title(u.getName() != null ? u.getName() : "User #" + u.getId())
                    .subtitle(u.getProfession() != null ? u.getProfession().getName() : "-")
                    .score(score)
                    .scorePercent((int) Math.round(score * 100))
                    .location(u.getLocation())
                    .experience(u.getExperienceLevel() != null ? u.getExperienceLevel().name() : "")
                    .searchStatus(u.getSearchStatus().name())
                    .build());
        }
        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return results;
    }

    public SendMessage showHrCandidateDetails(Company company, Long chatId, Long userId,
                                              Long jobId, CandidateSource source, int returnPage) {
        User candidate = userService.getById(userId);
        Job job = jobService.getById(jobId);
        double score = matchingService.calculateScore(candidate, job);

        StringBuilder sb = new StringBuilder();
        sb.append("👤 ").append(candidate.getName() != null ? candidate.getName() : "User").append("\n\n");

        if (candidate.getProfession() != null) {
            sb.append("💼 Profession: ").append(candidate.getProfession().getName()).append("\n");
        }
        sb.append("🎯 Match score: ").append((int) Math.round(score * 100)).append("%\n");

        if (candidate.getSkills() != null && !candidate.getSkills().isEmpty()) {
            sb.append("🛠 Skills: ");
            candidate.getSkills().forEach(s -> sb.append(s.getName()).append(", "));
            sb.append("\n");
        }
        if (candidate.getExperienceLevel() != null) {
            sb.append("📊 Experience: ").append(candidate.getExperienceLevel().name()).append("\n");
        }
        if (candidate.getLocation() != null) {
            sb.append("📍 Location: ").append(candidate.getLocation()).append("\n");
        }
        if (candidate.getExpectedSalary() != null) {
            sb.append("💰 Expected salary: ").append(candidate.getExpectedSalary()).append("\n");
        }

        String statusEmoji = switch (candidate.getSearchStatus()) {
            case ACTIVE -> "🟢";
            case PASSIVE -> "🟡";
            case NOT_LOOKING -> "🔴";
        };
        sb.append(statusEmoji).append(" Status: ").append(candidate.getSearchStatus().name()).append("\n");
        if (source == CandidateSource.APPLIED && candidate.getPhoneNumber() != null) {
            sb.append("\n📞 Contact: ").append(candidate.getPhoneNumber()).append("\n");
        }

        boolean hasCv = candidate.getCvFileId() != null && !candidate.getCvFileId().isEmpty();
        boolean alreadyOffered = jobOfferService.getJobOffers(jobId).stream()
                .anyMatch(o -> o.getUser().getId().equals(userId));

        SendMessage msg = sendText(chatId, sb.toString());
        msg.setReplyMarkup(keyboard.hrCandidateDetailsKeyboard(userId, jobId, source, returnPage, hasCv, alreadyOffered));
        return msg;
    }

    private SendMessage handleCompanyProfile(Company company, Long chatId) {
        long jobsCount = jobService.getByCompanyId(company.getId()).size();
        StringBuilder sb = new StringBuilder("🏢 Company profile:\n\n");
        sb.append("📛 Name: ").append(company.getCompanyName() != null ? company.getCompanyName() : "-").append("\n");
        sb.append("🔢 Tax ID: ").append(company.getInn() != null ? company.getInn() : "-").append("\n");
        sb.append("🏭 Industry: ").append(company.getIndustry() != null ? company.getIndustry() : "-").append("\n");
        sb.append("📞 Contact: ").append(company.getContactInfo() != null ? company.getContactInfo() : "-").append("\n");
        sb.append("✅ Status: ").append(company.getStatus().name()).append("\n");
        sb.append("📊 Vacancies: ").append(jobsCount);
        return sendText(chatId, sb.toString());
    }

    public SendMessage handleContact(Message message) {
        Long telegramId = message.getFrom().getId();
        Long chatId = message.getChatId();
        String phoneNumber = message.getContact().getPhoneNumber();

        Optional<User> userOpt = userService.getByTelegramId(telegramId);
        if (userOpt.isEmpty()) return sendText(chatId, "Error. Send /start command..");

        User user = userOpt.get();
        user.setPhoneNumber(phoneNumber);

        if (user.getBotState() == BotState.EDIT_CONTACT) {
            user.setBotState(BotState.USER_MAIN_MENU);
            userService.save(user);
            log.info("User contact updated via button: userId={}", user.getId());
            SendMessage msg = sendText(chatId, "✅ Contact updated: " + phoneNumber);
            msg.setReplyMarkup(keyboard.userMainMenu());
            return msg;
        }

        userService.save(user);

        Long pendingOfferId = pendingContactOffer.remove(user.getId());
        if (pendingOfferId != null) {
            JobOffer offer = jobOfferService.respondToOffer(pendingOfferId, OfferStatus.INTERESTED);
            notifyCompanyAboutAcceptedOffer(offer);
        }

        SendMessage msg = sendText(chatId, "✅ Contact sent. The HR will reach out to you.");
        msg.setReplyMarkup(keyboard.userMainMenu());
        return msg;
    }

    private SendMessage handleTextContact(User user, Long chatId, String text, Long offerId) {
        user.setPhoneNumber(text.trim());
        userService.save(user);
        pendingContactOffer.remove(user.getId());

        JobOffer offer = jobOfferService.respondToOffer(offerId, OfferStatus.INTERESTED);
        notifyCompanyAboutAcceptedOffer(offer);

        SendMessage msg = sendText(chatId, "✅ Contact sent: " + text + "\n\nThe HR will reach out to you.");
        msg.setReplyMarkup(keyboard.userMainMenu());
        return msg;
    }

    public SendMessage handleFile(Message message) {
        Long telegramId = message.getFrom().getId();
        Long chatId = message.getChatId();

        Optional<User> userOpt = userService.getByTelegramId(telegramId);
        if (userOpt.isEmpty()) return sendText(chatId, "Please send /start to begin.");

        User user = userOpt.get();
        boolean isUploadCv = user.getBotState() == BotState.UPLOAD_CV;
        boolean isEditCv = user.getBotState() == BotState.EDIT_CV;
        boolean hasPendingApply = pendingApplyJobId.get(user.getId()) != null;

        if (!isUploadCv && !isEditCv && !hasPendingApply) {
            return sendText(chatId, "To upload a CV, first tap \"Apply\" or use \"Upload CV\" from your profile.");
        }

        String fileId;
        String fileName;
        if (message.hasDocument()) {
            fileId = message.getDocument().getFileId();
            fileName = message.getDocument().getFileName() != null ? message.getDocument().getFileName() : "CV";
        } else if (message.hasPhoto()) {
            var photos = message.getPhoto();
            fileId = photos.get(photos.size() - 1).getFileId();
            fileName = "CV.jpg";
        } else {
            return sendText(chatId, "Please PDF, Word or picture send.");
        }

        user.setCvFileId(fileId);
        user.setCvFileName(fileName);
        user.setBotState(BotState.USER_MAIN_MENU);
        userService.save(user);
        log.info("CV uploaded: userId={}, fileName={}", user.getId(), fileName);

        if (isEditCv) {
            SendMessage msg = sendText(chatId, "✅ CV updated: " + fileName);
            msg.setReplyMarkup(keyboard.userMainMenu());
            return msg;
        }

        Long pendingJobId = pendingApplyJobId.remove(user.getId());
        if (pendingJobId == null) {
            SendMessage msg = sendText(chatId, "✅ CV saved successfully: " + fileName);
            msg.setReplyMarkup(keyboard.userMainMenu());
            return msg;
        }

        Job job = jobService.getById(pendingJobId);
        Company jobCompany = job.getCompany();
        if (jobCompany == null) {
            return sendText(chatId, "✅ CV saved, but you cannot apply to this vacancy from the bot (external source).");
        }
        applicationService.apply(job, user, jobCompany, null);
        SendMessage msg = sendText(
                chatId,
                "✅ CV saved and applied to \"" + job.getTitle() + "\"!\n\n" +
                        "The HR will review your CV."
        );
        msg.setReplyMarkup(keyboard.userMainMenu());
        return msg;
    }

    private void notifyCompanyAboutAcceptedOffer(JobOffer offer) {
        if (offer == null || offer.getCompany() == null) return;

        StringBuilder message = new StringBuilder("✅ New response!\n\n");

        message.append("👤 ")
                .append(offer.getUser().getName() != null ? offer.getUser().getName() : "User")
                .append(" is interested in your \"")
                .append(offer.getJob().getTitle())
                .append("\" vacancy!\n\n");

        if (offer.getUser().getPhoneNumber() != null) {
            message.append("📞 Contact: ").append(offer.getUser().getPhoneNumber()).append("\n");
        }
//        if (offer.getUser().getTelegramId() != null) {
//            message.append("💬 Telegram: tg://user?id=").append(offer.getUser().getTelegramId());
//        }

        try {
            SendMessage msg = new SendMessage();
            msg.setChatId(offer.getCompany().getChatId().toString());
            msg.setText(message.toString());
            applicationContext.getBean(CareerBot.class).execute(msg);
        } catch (Exception e) {
            log.error("Failed to notify company about accepted offer: {}", e.getMessage());
        }
    }

    private Integer parsePositiveInt(String text) {
        try {
            int n = Integer.parseInt(text.trim());
            return n < 0 ? null : n;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private ExperienceLevel yearsToLevel(int years) {
        if (years <= 1) return ExperienceLevel.NO_EXPERIENCE;
        if (years <= 3) return ExperienceLevel.JUNIOR;
        if (years <= 5) return ExperienceLevel.MIDDLE;
        return ExperienceLevel.SENIOR;
    }

    private SendMessage errorAndReset(Company company, Long chatId) {
        company.setBotState(BotState.HR_MAIN_MENU);
        companyService.save(company);
        return sendText(chatId, "An error occurred. Please start again.");
    }

    private SendMessage sendText(Long chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId.toString());
        msg.setText(text);
        return msg;
    }
}