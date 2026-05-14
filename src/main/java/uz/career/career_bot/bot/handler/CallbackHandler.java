package uz.career.career_bot.bot.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.career.career_bot.bot.CareerBot;
import uz.career.career_bot.bot.keyboard.KeyboardFactory;
import uz.career.career_bot.entity.*;
import uz.career.career_bot.enums.*;
import uz.career.career_bot.exception.BusinessException;
import uz.career.career_bot.service.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallbackHandler {

    private final UserService userService;
    private final CompanyService companyService;
    private final JobService jobService;
    private final SkillService skillService;
    private final CategoryService categoryService;
    private final ProfessionService professionService;
    private final SavedJobService savedJobService;
    private final JobOfferService jobOfferService;
    private final ApplicationService applicationService;
    private final LocationService locationService;
    private final MessageHandler messageHandler;
    private final KeyboardFactory keyboard;
    private final ApplicationContext applicationContext;

    private CareerBot getCareerBot() {
        return applicationContext.getBean(CareerBot.class);
    }

    public SendMessage handle(CallbackQuery callback) {
        Long chatId = callback.getMessage().getChatId();
        Long telegramId = callback.getFrom().getId();
        Integer messageId = callback.getMessage().getMessageId();
        String data = callback.getData();

        if (isSkillToggle(data)) {
            handleSkillToggle(chatId, telegramId, messageId, data);
            return null;
        }

        if (data.startsWith("role_")) return handleRoleSelection(chatId, telegramId, data);

        Optional<User> userOpt = userService.getByTelegramId(telegramId);
        if (userOpt.isPresent()) {
            return handleUserCallback(userOpt.get(), chatId, messageId, data);
        }

        Optional<Company> companyOpt = companyService.getByTelegramId(telegramId);
        if (companyOpt.isPresent()) {
            return handleCompanyCallback(companyOpt.get(), chatId, messageId, data);
        }

        return sendText(chatId, "Unknown command.");
    }

    private boolean isSkillToggle(String data) {
        return (data.startsWith("uskill_") || data.startsWith("jskill_") || data.startsWith("editskill_"))
                && !data.endsWith("_done");
    }

    private SendMessage handleRoleSelection(Long chatId, Long telegramId, String data) {
        if (data.equals("role_user")) {
            User user = User.builder()
                    .telegramId(telegramId)
                    .chatId(chatId)
                    .botState(BotState.ENTER_NAME)
                    .build();
            userService.save(user);
            return sendText(chatId, "Enter your name:");
        }
        Company company = Company.builder()
                .telegramId(telegramId)
                .chatId(chatId)
                .botState(BotState.ENTER_COMPANY_NAME)
                .build();
        companyService.save(company);
        return sendText(chatId, "Enter the company name:");
    }

    private SendMessage handleUserCallback(User user, Long chatId, Integer messageId, String data) {

        if (data.startsWith("ucat_")) return handleUserCategorySelected(user, chatId, data);
        if (data.startsWith("uprof_")) return handleUserProfessionSelected(user, chatId, data);
        if (data.equals("uskill_done")) return handleUserSkillsDone(user, chatId);
        if (data.startsWith("loc_")) return handleUserLocationSelected(user, chatId, data);

        if (data.equals("filter_matched")) {
            messageHandler.getUserSearchResults().remove(user.getId());
            return messageHandler.showJobsList(user, chatId, BrowseMode.MATCHED, 0);
        }
        if (data.equals("filter_all")) {
            messageHandler.getUserSearchResults().remove(user.getId());
            return messageHandler.showJobsList(user, chatId, BrowseMode.ALL, 0);
        }

        if (data.startsWith("joblist_next_") || data.startsWith("joblist_prev_")) {
            String[] parts = data.split("_");
            int page = Integer.parseInt(parts[2]);
            BrowseMode mode = BrowseMode.fromValue(parts[3]);
            int newPage = data.startsWith("joblist_next_") ? page + 1 : page - 1;
            messageHandler.editJobsList(chatId, messageId, user, mode, newPage);
            return null;
        }

        if (data.startsWith("viewjob_")) {
            String[] parts = data.split("_");
            Long jobId = Long.parseLong(parts[1]);
            int returnPage = Integer.parseInt(parts[2]);
            BrowseMode mode = BrowseMode.fromValue(parts[3]);
            return messageHandler.showJobDetails(user, chatId, jobId, returnPage, mode);
        }

        if (data.startsWith("backtolist_")) {
            String[] parts = data.split("_");
            int page = Integer.parseInt(parts[1]);
            BrowseMode mode = BrowseMode.fromValue(parts[2]);
            return messageHandler.showJobsList(user, chatId, mode, page);
        }

        // ----- APPLY -----
        if (data.startsWith("apply_")) return handleApply(user, chatId, data);
        if (data.startsWith("save_job_")) return handleSaveJob(user, chatId, data);

        // ----- SETTINGS -----
        if (data.equals("settings_status")) return showInline(chatId, "🔍 Choose status:", keyboard.searchStatusKeyboard());
        if (data.equals("settings_notif")) return showInline(chatId, "🔔 Notifications:", keyboard.notificationKeyboard());

        if (data.startsWith("status_")) {
            user.setSearchStatus(SearchStatus.valueOf(data.replace("status_", "")));
            userService.save(user);
            return sendText(chatId, "✅ Status updated.");
        }
        if (data.startsWith("notif_")) {
            user.setNotificationFrequency(NotificationFrequency.valueOf(data.replace("notif_", "")));
            userService.save(user);
            return sendText(chatId, "✅ Notification preference updated.");
        }

        // ----- PROFILE EDITING -----
        if (data.equals("edit_name")) return startEdit(user, chatId, BotState.EDIT_NAME, "Enter your new name:");
        if (data.equals("edit_salary")) return startEdit(user, chatId, BotState.EDIT_SALARY, "Enter your new salary:");
        if (data.equals("edit_experience")) return startEdit(user, chatId, BotState.EDIT_EXPERIENCE, "Years of experience (enter a number):");
        if (data.equals("edit_profession")) return handleEditProfession(user, chatId);
        if (data.equals("edit_location")) return handleEditLocation(user, chatId);
        if (data.equals("edit_contact")) {
            user.setBotState(BotState.EDIT_CONTACT);
            userService.save(user);
            SendMessage msg = sendText(chatId,
                    "📞 Send your new contact info.\n\n" +
                            "You can share your phone via the button or send it as text. " +
                            "(phone number, email, @username):");
            msg.setReplyMarkup(keyboard.contactRequestKeyboard());
            return msg;
        }

        if (data.equals("edit_cv")) {
            user.setBotState(BotState.EDIT_CV);
            userService.save(user);
            return sendText(chatId,
                    "📄 Send your new CV (resume) file.\n\n" +
                            "Accepted formats: PDF, Word, or image.");
        }

        if (data.equals("delete_cv")) {
            if (user.getCvFileId() == null) {
                return sendText(chatId, "You have no CV uploaded.");
            }
            user.setCvFileId(null);
            user.setCvFileName(null);
            userService.save(user);
            log.info("CV deleted: userId={}", user.getId());
            return sendText(chatId, "🗑 CV deleted. Open your profile to upload a new one.");
        }

        if (data.startsWith("editcat_")) return handleEditCategorySelected(user, chatId, data);
        if (data.startsWith("editprof_")) return handleEditProfessionSelected(user, chatId, data);
        if (data.equals("editskill_done")) return handleEditSkillsDone(user, chatId);
        if (data.startsWith("editloc_")) return handleEditLocationSelected(user, chatId, data);

        if (data.startsWith("offer_yes_")) return handleOfferYes(user, chatId, data);
        if (data.startsWith("offer_no_")) return handleOfferNo(chatId, data);

        return sendText(chatId, "Noma'lum buyruq.");
    }

    private SendMessage handleUserCategorySelected(User user, Long chatId, String data) {
        Long catId = Long.parseLong(data.replace("ucat_", ""));
        user.setBotState(BotState.SELECT_PROFESSION);
        userService.save(user);

        List<Profession> professions = professionService.getByCategoryId(catId);
        if (professions.isEmpty()) {
            return sendText(chatId, "No professions in this industry. Choose another.");
        }
        SendMessage msg = sendText(chatId, "Choose your profession:");
        msg.setReplyMarkup(keyboard.professionKeyboardDynamic(professions, "uprof_"));
        return msg;
    }

    private SendMessage handleUserProfessionSelected(User user, Long chatId, String data) {
        Long profId = Long.parseLong(data.replace("uprof_", ""));
        Profession prof = professionService.getById(profId);
        user.setProfession(prof);
        user.setBotState(BotState.SELECT_SKILLS);
        userService.save(user);

        messageHandler.getUserSelectedSkills().put(user.getId(), new HashSet<>());
        List<Skill> skills = skillService.getByCategoryId(prof.getCategory().getId());
        SendMessage msg = sendText(chatId, "Choose your skills (multiple allowed):");
        msg.setReplyMarkup(keyboard.skillsKeyboard(skills, new HashSet<>(), "uskill_"));
        return msg;
    }

    private SendMessage handleUserSkillsDone(User user, Long chatId) {
        Set<Long> selected = messageHandler.getUserSelectedSkills().get(user.getId());
        if (selected == null || selected.isEmpty()) {
            return sendText(chatId, "Please choose at least one skill.");
        }
        Set<Skill> userSkills = new HashSet<>();
        for (Long skillId : selected) userSkills.add(skillService.getById(skillId));

        user.setSkills(userSkills);
        user.setBotState(BotState.SELECT_EXPERIENCE);
        userService.save(user);
        messageHandler.getUserSelectedSkills().remove(user.getId());

        return sendText(chatId, "✅ " + userSkills.size() + " skills selected!\n\n" +
                "Years of experience (enter a number, e.g. 3):");
    }

    private SendMessage handleUserLocationSelected(User user, Long chatId, String data) {
        user.setLocation(data.replace("loc_", ""));
        user.setBotState(BotState.ENTER_SALARY);
        userService.save(user);
        return sendText(chatId, "Enter your expected salary (in UZS):");
    }

    private SendMessage handleApply(User user, Long chatId, String data) {
        Long jobId = Long.parseLong(data.replace("apply_", ""));

        if (user.getCvFileId() == null || user.getCvFileId().isEmpty()) {
            messageHandler.getPendingApplyJobId().put(user.getId(), jobId);
            user.setBotState(BotState.UPLOAD_CV);
            userService.save(user);
            return sendText(chatId,
                    "📄 To apply, please upload your CV (resume) first.\n\n" +
                            "Send a CV file in PDF, Word, or image format.");
        }

        Job job = jobService.getById(jobId);
        Company jobCompany = job.getCompany();
        if (jobCompany == null) {
            return sendText(chatId, "This vacancy cannot be applied to from the bot (external source).");
        }

        try {
            applicationService.apply(job, user, jobCompany, null);
            log.info("Apply: user={}, job={}", user.getId(), jobId);
            return sendText(chatId, "✅ Application submitted! The HR will review your CV.");
        } catch (BusinessException e) {
            return sendText(chatId, e.getMessage());
        }
    }

    private SendMessage handleSaveJob(User user, Long chatId, String data) {
        Long jobId = Long.parseLong(data.replace("save_job_", ""));
        savedJobService.saveJob(user, jobService.getById(jobId));
        return sendText(chatId, "💾 Vacancy saved!");
    }

    // ----- USER PROFIL EDIT -----

    private SendMessage startEdit(User user, Long chatId, BotState state, String prompt) {
        user.setBotState(state);
        userService.save(user);
        return sendText(chatId, prompt);
    }

    private SendMessage handleEditProfession(User user, Long chatId) {
        user.setBotState(BotState.EDIT_CATEGORY);
        userService.save(user);
        SendMessage msg = sendText(chatId, "Choose a new industry:");
        msg.setReplyMarkup(keyboard.categoryKeyboard(categoryService.getAll(), "editcat_"));
        return msg;
    }

    private SendMessage handleEditLocation(User user, Long chatId) {
        user.setBotState(BotState.EDIT_LOCATION);
        userService.save(user);
        SendMessage msg = sendText(chatId, "Choose a new location:");
        msg.setReplyMarkup(keyboard.locationKeyboard(locationService.getActive(), "editloc_"));
        return msg;
    }

    private SendMessage handleEditCategorySelected(User user, Long chatId, String data) {
        Long catId = Long.parseLong(data.replace("editcat_", ""));
        user.setBotState(BotState.EDIT_PROFESSION);
        userService.save(user);
        List<Profession> professions = professionService.getByCategoryId(catId);
        SendMessage msg = sendText(chatId, "Choose a profession:");
        msg.setReplyMarkup(keyboard.professionKeyboardDynamic(professions, "editprof_"));
        return msg;
    }

    private SendMessage handleEditProfessionSelected(User user, Long chatId, String data) {
        Long profId = Long.parseLong(data.replace("editprof_", ""));
        Profession prof = professionService.getById(profId);
        user.setProfession(prof);
        user.setBotState(BotState.EDIT_SKILLS);
        userService.save(user);

        Set<Long> currentSkillIds = new HashSet<>();
        if (user.getSkills() != null) {
            user.getSkills().forEach(s -> currentSkillIds.add(s.getId()));
        }
        messageHandler.getUserSelectedSkills().put(user.getId(), currentSkillIds);

        List<Skill> skills = skillService.getByCategoryId(prof.getCategory().getId());
        SendMessage msg = sendText(chatId, "Choose your new skills:");
        msg.setReplyMarkup(keyboard.skillsKeyboard(skills, currentSkillIds, "editskill_"));
        return msg;
    }

    private SendMessage handleEditSkillsDone(User user, Long chatId) {
        Set<Long> selected = messageHandler.getUserSelectedSkills().get(user.getId());
        if (selected == null || selected.isEmpty()) {
            return sendText(chatId, "Please choose at least one skill.");
        }
        Set<Skill> userSkills = new HashSet<>();
        for (Long skillId : selected) userSkills.add(skillService.getById(skillId));

        user.setSkills(userSkills);
        user.setBotState(BotState.USER_MAIN_MENU);
        userService.save(user);
        messageHandler.getUserSelectedSkills().remove(user.getId());

        SendMessage msg = sendText(chatId, "✅ Profile updated!");
        msg.setReplyMarkup(keyboard.userMainMenu());
        return msg;
    }

    private SendMessage handleEditLocationSelected(User user, Long chatId, String data) {
        user.setLocation(data.replace("editloc_", ""));
        user.setBotState(BotState.USER_MAIN_MENU);
        userService.save(user);
        SendMessage msg = sendText(chatId, "✅ Location updated.");
        msg.setReplyMarkup(keyboard.userMainMenu());
        return msg;
    }

    // ----- OFFER RESPONSE -----

    private SendMessage handleOfferYes(User user, Long chatId, String data) {
        Long offerId = Long.parseLong(data.replace("offer_yes_", ""));

        if (user.getPhoneNumber() == null || user.getPhoneNumber().isEmpty()) {
            messageHandler.getPendingContactOffer().put(user.getId(), offerId);
            SendMessage msg = sendText(chatId,
                    "Please send your contact info so the HR can reach you.\n\n" +
                            "📞 Share your phone number via the button\n" +
                            "✏️ Or send it as text (phone, email, or @username)");
            msg.setReplyMarkup(keyboard.contactRequestKeyboard());
            return msg;
        }

        JobOffer offer = jobOfferService.respondToOffer(offerId, OfferStatus.INTERESTED);
        notifyCompanyAboutOfferResponse(offer, true);
        return sendText(chatId, "✅ Your answer was sent! The HR will reach out to you.");
    }

    private SendMessage handleOfferNo(Long chatId, String data) {
        Long offerId = Long.parseLong(data.replace("offer_no_", ""));
        JobOffer offer = jobOfferService.respondToOffer(offerId, OfferStatus.NOT_INTERESTED);
        notifyCompanyAboutOfferResponse(offer, false);
        return sendText(chatId, "❌ Offer declined.");
    }

    // ==================== COMPANY CALLBACK ====================

    private SendMessage handleCompanyCallback(Company company, Long chatId, Integer messageId, String data) {
        if (data.startsWith("ind_")) return handleIndustrySelected(company, chatId, data);
        if (data.startsWith("jcat_")) return handleJobCategorySelected(company, chatId, data);
        if (data.startsWith("jprof_")) return handleJobProfessionSelected(company, chatId, data);
        if (data.equals("jskill_done")) return handleJobSkillsDone(company, chatId);
        if (data.startsWith("jobexp_")) return handleJobExperienceSelected(company, chatId, data);
        if (data.startsWith("jobloc_")) return handleJobLocationSelected(company, chatId, data);
        if (data.startsWith("jtype_")) return handleJobTypeSelected(company, chatId, data);

        if (data.startsWith("applicants_job_")) {
            Long jobId = Long.parseLong(data.replace("applicants_job_", ""));
            return messageHandler.showHrCandidatesList(company, chatId, jobId, CandidateSource.APPLIED, 0);
        }

        // HR — vacancy selection -> matched / applied candidates
        if (data.startsWith("select_job_")) {
            Long jobId = Long.parseLong(data.replace("select_job_", ""));
            return messageHandler.showHrJobActions(company, chatId, jobId);
        }
        if (data.startsWith("hraction_jobback_")) {
            Long jobId = Long.parseLong(data.replace("hraction_jobback_", ""));
            return messageHandler.showHrJobActions(company, chatId, jobId);
        }
        if (data.equals("hraction_back")) {
            return messageHandler.handleCandidates(company, chatId);
        }
        if (data.startsWith("hraction_matched_")) {
            Long jobId = Long.parseLong(data.replace("hraction_matched_", ""));
            messageHandler.getHrCandidateResults().remove(company.getId());
            return messageHandler.showHrCandidatesList(company, chatId, jobId, CandidateSource.MATCHED, 0);
        }
        if (data.startsWith("hraction_applied_")) {
            Long jobId = Long.parseLong(data.replace("hraction_applied_", ""));
            return messageHandler.showHrCandidatesList(company, chatId, jobId, CandidateSource.APPLIED, 0);
        }

        if (data.startsWith("hrcandlist_next_") || data.startsWith("hrcandlist_prev_")) {
            String[] parts = data.split("_");
            int page = Integer.parseInt(parts[2]);
            CandidateSource source = CandidateSource.fromValue(parts[3]);
            Long jobId = Long.parseLong(parts[4]);
            int newPage = data.startsWith("hrcandlist_next_") ? page + 1 : page - 1;
            messageHandler.editHrCandidatesList(chatId, messageId, company, jobId, source, newPage);
            return null;
        }

        if (data.startsWith("hrcand_")) {
            String[] parts = data.split("_");
            Long userId = Long.parseLong(parts[1]);
            Long jobId = Long.parseLong(parts[2]);
            CandidateSource source = CandidateSource.fromValue(parts[3]);
            int returnPage = Integer.parseInt(parts[4]);
            return messageHandler.showHrCandidateDetails(company, chatId, userId, jobId, source, returnPage);
        }

        if (data.startsWith("hrcandback_")) {
            String[] parts = data.split("_");
            Long jobId = Long.parseLong(parts[1]);
            CandidateSource source = CandidateSource.fromValue(parts[2]);
            int page = Integer.parseInt(parts[3]);
            return messageHandler.showHrCandidatesList(company, chatId, jobId, source, page);
        }

        if (data.startsWith("view_cv_")) return handleViewCv(chatId, data);
        if (data.startsWith("close_job_")) return handleCloseJob(company, chatId, data);
        if (data.startsWith("send_offer_")) return handleSendOffer(company, chatId, data);

        return sendText(chatId, "Unknown command.");
    }


    private SendMessage handleIndustrySelected(Company company, Long chatId, String data) {
        company.setIndustry(data.replace("ind_", ""));
        company.setBotState(BotState.ENTER_COMPANY_CONTACT);
        companyService.save(company);
        return sendText(chatId, "Enter contact details:");
    }

    private SendMessage handleJobCategorySelected(Company company, Long chatId, String data) {
        Long catId = Long.parseLong(data.replace("jcat_", ""));
        Job draft = messageHandler.getCurrentJobDraft().get(company.getId());
        if (draft == null) return errorAndReset(company, chatId);

        company.setBotState(BotState.ADD_JOB_PROFESSION);
        companyService.save(company);

        List<Profession> professions = professionService.getByCategoryId(catId);
        SendMessage msg = sendText(chatId, "Choose a profession:");
        msg.setReplyMarkup(keyboard.professionKeyboardDynamic(professions, "jprof_"));
        return msg;
    }

    private SendMessage handleJobProfessionSelected(Company company, Long chatId, String data) {
        Long profId = Long.parseLong(data.replace("jprof_", ""));
        Job draft = messageHandler.getCurrentJobDraft().get(company.getId());
        if (draft == null) return errorAndReset(company, chatId);

        Profession prof = professionService.getById(profId);
        draft.setProfession(prof);
        company.setBotState(BotState.ADD_JOB_SKILLS);
        companyService.save(company);

        List<Skill> skills = skillService.getByCategoryId(prof.getCategory().getId());
        messageHandler.getHrSelectedSkills().put(company.getId(), new HashSet<>());

        SendMessage msg = sendText(chatId, "Choose the required skills:");
        msg.setReplyMarkup(keyboard.skillsKeyboard(skills, new HashSet<>(), "jskill_"));
        return msg;
    }

    private SendMessage handleJobSkillsDone(Company company, Long chatId) {
        Set<Long> selected = messageHandler.getHrSelectedSkills().get(company.getId());
        if (selected == null || selected.isEmpty()) {
            return sendText(chatId, "Please choose at least one skill.");
        }

        Job draft = messageHandler.getCurrentJobDraft().get(company.getId());
        if (draft == null) return errorAndReset(company, chatId);

        Set<Skill> jobSkills = new HashSet<>();
        StringBuilder reqStr = new StringBuilder();
        for (Long skillId : selected) {
            Skill s = skillService.getById(skillId);
            jobSkills.add(s);
            reqStr.append(s.getName()).append(", ");
        }
        draft.setSkills(jobSkills);
        draft.setRequirements(reqStr.toString());

        company.setBotState(BotState.ADD_JOB_EXPERIENCE);
        companyService.save(company);
        messageHandler.getHrSelectedSkills().remove(company.getId());

        SendMessage msg = sendText(chatId, "✅ " + jobSkills.size() + " skills selected!\n\nChoose your experience level:");
        msg.setReplyMarkup(keyboard.jobExperienceKeyboard());
        return msg;
    }

    private SendMessage handleJobExperienceSelected(Company company, Long chatId, String data) {
        Job draft = messageHandler.getCurrentJobDraft().get(company.getId());
        if (draft == null) return errorAndReset(company, chatId);

        draft.setExperienceLevel(ExperienceLevel.valueOf(data.replace("jobexp_", "")));
        company.setBotState(BotState.ADD_JOB_SALARY);
        companyService.save(company);
        return sendText(chatId, "Enter the salary amount (5000000 or 5000000-8000000):");
    }

    private SendMessage handleJobLocationSelected(Company company, Long chatId, String data) {
        Job draft = messageHandler.getCurrentJobDraft().get(company.getId());
        if (draft == null) return errorAndReset(company, chatId);

        draft.setLocation(data.replace("jobloc_", ""));
        company.setBotState(BotState.ADD_JOB_TYPE);
        companyService.save(company);

        SendMessage msg = sendText(chatId, "Select job type:");
        msg.setReplyMarkup(keyboard.jobTypeKeyboard());
        return msg;
    }

    private SendMessage handleJobTypeSelected(Company company, Long chatId, String data) {
        Job draft = messageHandler.getCurrentJobDraft().get(company.getId());
        if (draft == null) return errorAndReset(company, chatId);

        draft.setJobType(JobType.valueOf(data.replace("jtype_", "")));
        draft.setCompany(company);
        draft.setStatus(JobStatus.PENDING);
        draft.setSource(JobSource.HR);
        draft.setExpiresAt(LocalDateTime.now().plusDays(30));

        jobService.save(draft);
        log.info("HR job submitted: company={}, title={}", company.getId(), draft.getTitle());

        messageHandler.getCurrentJobDraft().remove(company.getId());
        company.setBotState(BotState.HR_MAIN_MENU);
        companyService.save(company);

        return sendText(chatId, "✅ Vacancy added! It will become visible to users after admin approval.");
    }

    private SendMessage handleViewCv(Long chatId, String data) {
        Long userId = Long.parseLong(data.replace("view_cv_", ""));
        User candidate = userService.getById(userId);

        if (candidate.getCvFileId() == null || candidate.getCvFileId().isEmpty()) {
            return sendText(chatId, "This candidate does not have a CV..");
        }

        try {
            SendDocument doc = new SendDocument();
            doc.setChatId(chatId.toString());
            doc.setDocument(new InputFile(candidate.getCvFileId()));
            doc.setCaption("📄 " + (candidate.getName() != null ? candidate.getName() : "User") + "'s CV");
            getCareerBot().execute(doc);
            return null;
        } catch (TelegramApiException e) {
            log.error("Error sending CV: {}", e.getMessage());
            return sendText(chatId, "Error sending CV.");
        }
    }

    private SendMessage handleCloseJob(Company company, Long chatId, String data) {
        Long jobId = Long.parseLong(data.replace("close_job_", ""));
        Job job = jobService.getById(jobId);

        if (job.getCompany() == null || !job.getCompany().getId().equals(company.getId())) {
            return sendText(chatId, "This is not your vacancy.");
        }

        jobService.close(jobId);
        log.info("HR closed job: company={}, jobId={}", company.getId(), jobId);
        return sendText(chatId, "🔒 Vacancy closed: " + job.getTitle());
    }

    private SendMessage handleSendOffer(Company company, Long chatId, String data) {
        String[] parts = data.replace("send_offer_", "").split("_");
        Long jobId = Long.parseLong(parts[0]);
        Long userId = Long.parseLong(parts[1]);

        Job job = jobService.getById(jobId);
        User targetUser = userService.getById(userId);

        try {
            jobOfferService.sendOffer(job, targetUser, company);
            log.info("HR sent offer: company={}, job={}, user={}", company.getId(), jobId, userId);
            notifyUserAboutNewOffer(targetUser, job, company);
            return sendText(chatId, "📩 Taklif " + targetUser.getName() + " has been notified!");
        } catch (BusinessException e) {
            return sendText(chatId, e.getMessage());
        }
    }

    private void handleSkillToggle(Long chatId, Long telegramId, Integer messageId, String data) {
        String prefix;
        if (data.startsWith("uskill_")) prefix = "uskill_";
        else if (data.startsWith("jskill_")) prefix = "jskill_";
        else prefix = "editskill_";

        try {
            Long skillId = Long.parseLong(data.replace(prefix, ""));
            Set<Long> selected;
            List<Skill> skills;

            if (prefix.equals("jskill_")) {
                Optional<Company> companyOpt = companyService.getByTelegramId(telegramId);
                if (companyOpt.isEmpty()) return;
                Company company = companyOpt.get();

                selected = messageHandler.getHrSelectedSkills().computeIfAbsent(company.getId(), k -> new HashSet<>());
                toggle(selected, skillId);

                Job draft = messageHandler.getCurrentJobDraft().get(company.getId());
                if (draft == null || draft.getProfession() == null) return;
                skills = skillService.getByCategoryId(draft.getProfession().getCategory().getId());
            } else {
                Optional<User> userOpt = userService.getByTelegramId(telegramId);
                if (userOpt.isEmpty() || userOpt.get().getProfession() == null) return;
                User user = userOpt.get();

                selected = messageHandler.getUserSelectedSkills().computeIfAbsent(user.getId(), k -> new HashSet<>());
                toggle(selected, skillId);

                skills = skillService.getByCategoryId(user.getProfession().getCategory().getId());
            }

            EditMessageReplyMarkup edit = EditMessageReplyMarkup.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .replyMarkup(keyboard.skillsKeyboard(skills, selected, prefix))
                    .build();
            getCareerBot().execute(edit);
        } catch (Exception e) {
            log.error("Skill toggle error: {}", e.getMessage());
        }
    }

    private void toggle(Set<Long> set, Long id) {
        if (set.contains(id)) set.remove(id);
        else set.add(id);
    }
    private void notifyUserAboutNewOffer(User user, Job job, Company company) {
        String text = "📩 You received a new offer!\n\n" +
                "💼 " + job.getTitle() + "\n" +
                "🏢 " + company.getCompanyName() + "\n\n" +
                "Tap \"📩 Received offers\" to view it.";
        try {
            SendMessage msg = new SendMessage();
            msg.setChatId(user.getChatId().toString());
            msg.setText(text);
            getCareerBot().execute(msg);
        } catch (TelegramApiException e) {
            log.error("Failed to notify user about new offer: {}", e.getMessage());
        }
    }

    private void notifyCompanyAboutOfferResponse(JobOffer offer, boolean accepted) {
        if (offer == null || offer.getCompany() == null) return;

        String message;
        if (accepted) {
            StringBuilder sb = new StringBuilder("✅ New response!\n\n");
            sb.append("👤 ")
                    .append(offer.getUser().getName() != null
                            ? offer.getUser().getName()
                            : "User")
                    .append(" is interested in your \"")
                    .append(offer.getJob().getTitle())
                    .append("\" vacancy!\n\n");
            if (offer.getUser().getPhoneNumber() != null) {
                sb.append("📞 Contact: ").append(offer.getUser().getPhoneNumber()).append("\n");
            }
//            if (offer.getUser().getTelegramId() != null) {
//                sb.append("💬 Telegram: tg://user?id=").append(offer.getUser().getTelegramId());
//            }
            message = sb.toString();
        } else {
            message = "❌ \"" + offer.getJob().getTitle() + "\" — the offer was rejected.\n" +
                    "👤 " + (offer.getUser().getName() != null ? offer.getUser().getName() : "User");
        }

        try {
            SendMessage msg = new SendMessage();
            msg.setChatId(offer.getCompany().getChatId().toString());
            msg.setText(message);
            getCareerBot().execute(msg);
        } catch (TelegramApiException e) {
            log.error("Failed to notify company about offer response: {}", e.getMessage());
        }
    }

    private SendMessage showInline(Long chatId, String text,
                                   org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup markup) {
        SendMessage msg = sendText(chatId, text);
        msg.setReplyMarkup(markup);
        return msg;
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