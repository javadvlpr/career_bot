package uz.career.career_bot.bot.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.career.career_bot.dto.MatchResultDTO;
import uz.career.career_bot.entity.*;
import uz.career.career_bot.enums.BrowseMode;
import uz.career.career_bot.enums.CandidateSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class KeyboardFactory {

    public InlineKeyboardMarkup roleKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(inlineButton("👤 Job seeker", "role_user")))
                .keyboardRow(List.of(inlineButton("🏢 Company/HR", "role_company")))
                .build();
    }


    public ReplyKeyboardMarkup userMainMenu() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("🔍 Search jobs"));
        row1.add(new KeyboardButton("📋 My profile"));
        rows.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("💾 Saved jobs"));
        row2.add(new KeyboardButton("📩 Received offers"));
        rows.add(row2);

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("⚙️ Settings"));
        rows.add(row3);

        markup.setKeyboard(rows);
        return markup;
    }

    public ReplyKeyboardMarkup hrMainMenu() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("➕ Post a vacancy"));
        row1.add(new KeyboardButton("📋 My vacancies"));
        rows.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("👥 Matched candidates"));
        row2.add(new KeyboardButton("📨 Applicants"));
        rows.add(row2);

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("⚙️ Company profile"));
        rows.add(row3);

        markup.setKeyboard(rows);
        return markup;
    }
    public ReplyKeyboardMarkup contactRequestKeyboard() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);

        KeyboardButton contactButton = new KeyboardButton("📞 Send contact");
        contactButton.setRequestContact(true);

        KeyboardRow row = new KeyboardRow();
        row.add(contactButton);
        markup.setKeyboard(List.of(row));
        return markup;
    }

    public InlineKeyboardMarkup locationKeyboard(List<Location> locations, String prefix) {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        for (int i = 0; i < locations.size(); i += 2) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(inlineButton(locations.get(i).getName(), prefix + locations.get(i).getName()));
            if (i + 1 < locations.size()) {
                row.add(inlineButton(locations.get(i + 1).getName(), prefix + locations.get(i + 1).getName()));
            }
            builder.keyboardRow(row);
        }
        return builder.build();
    }

    public InlineKeyboardMarkup categoryKeyboard(List<Category> categories, String prefix) {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        for (int i = 0; i < categories.size(); i += 2) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(inlineButton(categories.get(i).getName(), prefix + categories.get(i).getId()));
            if (i + 1 < categories.size()) {
                row.add(inlineButton(categories.get(i + 1).getName(), prefix + categories.get(i + 1).getId()));
            }
            builder.keyboardRow(row);
        }
        return builder.build();
    }

    public InlineKeyboardMarkup professionKeyboardDynamic(List<Profession> professions, String prefix) {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        for (Profession p : professions) {
            builder.keyboardRow(List.of(inlineButton(p.getName(), prefix + p.getId())));
        }
        return builder.build();
    }

    public InlineKeyboardMarkup skillsKeyboard(List<Skill> skills, Set<Long> selectedIds, String prefix) {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        for (int i = 0; i < skills.size(); i += 2) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            Skill s1 = skills.get(i);
            row.add(inlineButton((selectedIds.contains(s1.getId()) ? "✅ " : "") + s1.getName(), prefix + s1.getId()));
            if (i + 1 < skills.size()) {
                Skill s2 = skills.get(i + 1);
                row.add(inlineButton((selectedIds.contains(s2.getId()) ? "✅ " : "") + s2.getName(), prefix + s2.getId()));
            }
            builder.keyboardRow(row);
        }
        String doneCallback = prefix.substring(0, prefix.length() - 1) + "_done";
        builder.keyboardRow(List.of(inlineButton("✅ Confirm", doneCallback)));
        return builder.build();
    }

    public InlineKeyboardMarkup industryKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        inlineButton("💻 IT", "ind_IT"),
                        inlineButton("🏦 Finance", "ind_Moliya")
                ))
                .keyboardRow(List.of(
                        inlineButton("🏗 Construction", "ind_Qurilish"),
                        inlineButton("🎓 Education", "ind_Talim")
                ))
                .keyboardRow(List.of(
                        inlineButton("🏥 Medicine", "ind_Tibbiyot"),
                        inlineButton("📝 Other", "ind_Boshqa")
                )).build();
    }

    public InlineKeyboardMarkup jobExperienceKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        inlineButton("0-1 years", "jobexp_NO_EXPERIENCE"),
                        inlineButton("1-3 years", "jobexp_JUNIOR")
                ))
                .keyboardRow(List.of(
                        inlineButton("3-5 years", "jobexp_MIDDLE"),
                        inlineButton("5+ years", "jobexp_SENIOR")
                )).build();
    }

    public InlineKeyboardMarkup jobTypeKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        inlineButton("Full-time", "jtype_FULL_TIME"),
                        inlineButton("Part-time", "jtype_PART_TIME")
                ))
                .keyboardRow(List.of(
                        inlineButton("Remote", "jtype_REMOTE"),
                        inlineButton("Freelance", "jtype_FREELANCE")
                ))
                .keyboardRow(List.of(inlineButton("Internship", "jtype_INTERNSHIP")))
                .build();
    }

    public InlineKeyboardMarkup profileEditKeyboard(boolean hasCv) {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        inlineButton("✏️ Name", "edit_name"),
                        inlineButton("💼 Profession", "edit_profession")
                ))
                .keyboardRow(List.of(
                        inlineButton("📊 Experience", "edit_experience"),
                        inlineButton("📍 Location", "edit_location")
                ))
                .keyboardRow(List.of(
                        inlineButton("💰 Salary", "edit_salary"),
                        inlineButton("📞 Contact", "edit_contact")
                ));

        if (hasCv) {
            builder.keyboardRow(List.of(
                    inlineButton("📄 CV update", "edit_cv"),
                    inlineButton("🗑 Delete CV", "delete_cv")
            ));
        } else {
            builder.keyboardRow(List.of(inlineButton("📄 CV update", "edit_cv")));
        }

        return builder.build();
    }

    public InlineKeyboardMarkup userSettingsKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(inlineButton("🔍 Search status", "settings_status")))
                .keyboardRow(List.of(inlineButton("🔔 Notifications", "settings_notif")))
                .build();
    }

    public InlineKeyboardMarkup searchStatusKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(inlineButton("🟢 Actively searching", "status_ACTIVE")))
                .keyboardRow(List.of(inlineButton("🟡 Passively open", "status_PASSIVE")))
                .keyboardRow(List.of(inlineButton("🔴 Not looking", "status_NOT_LOOKING")))
                .build();
    }

    public InlineKeyboardMarkup notificationKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        inlineButton("📅 Every day", "notif_DAILY"),
                        inlineButton("📆 Every week", "notif_WEEKLY")
                ))
                .keyboardRow(List.of(inlineButton("🔕 Off", "notif_OFF")))
                .build();
    }

    public InlineKeyboardMarkup searchFilterKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(inlineButton("🎯 Matched jobs only", "filter_matched")))
                .keyboardRow(List.of(inlineButton("🌍 All jobs", "filter_all")))
                .build();
    }

    public InlineKeyboardMarkup jobListKeyboard(List<MatchResultDTO> jobs, int currentPage,
                                                int pageSize, BrowseMode mode) {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, jobs.size());
        int totalPages = (int) Math.ceil((double) jobs.size() / pageSize);

        for (int i = start; i < end; i++) {
            MatchResultDTO j = jobs.get(i);
            String label = "💼 " + j.getTitle() + " — " + j.getScorePercent() + "%";
            if (label.length() > 60) label = label.substring(0, 57) + "...";
            builder.keyboardRow(List.of(
                    inlineButton(label, "viewjob_" + j.getTargetId() + "_" + currentPage + "_" + mode.getValue())
            ));
        }

        if (totalPages > 1) {
            List<InlineKeyboardButton> navRow = new ArrayList<>();
            navRow.add(currentPage > 0
                    ? inlineButton("⬅️", "joblist_prev_" + currentPage + "_" + mode.getValue())
                    : inlineButton(" ", "noop"));
            navRow.add(inlineButton((currentPage + 1) + "/" + totalPages, "noop"));
            navRow.add(currentPage < totalPages - 1
                    ? inlineButton("➡️", "joblist_next_" + currentPage + "_" + mode.getValue())
                    : inlineButton(" ", "noop"));
            builder.keyboardRow(navRow);
        }
        return builder.build();
    }

    public InlineKeyboardMarkup jobDetailsKeyboard(Long jobId, int returnPage, BrowseMode mode,
                                                   boolean alreadyApplied, boolean alreadySaved,
                                                   String externalUrl, String contactInfo) {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();

        if (externalUrl != null && !externalUrl.isEmpty()) {
            InlineKeyboardButton urlButton = new InlineKeyboardButton();
            urlButton.setText("🌐 Open external link");
            urlButton.setUrl(externalUrl);
            builder.keyboardRow(List.of(urlButton));
        } else if (contactInfo == null || contactInfo.isEmpty()) {
            builder.keyboardRow(List.of(inlineButton(
                    alreadyApplied ? "✅ Applied" : "📨 Apply",
                    alreadyApplied ? "noop" : "apply_" + jobId)));
        }

        builder.keyboardRow(List.of(inlineButton(
                alreadySaved ? "💾 Saved" : "💾 Save",
                alreadySaved ? "noop" : "save_job_" + jobId)));
        builder.keyboardRow(List.of(
                inlineButton("🔙 Back to list", "backtolist_" + returnPage + "_" + mode.getValue())
        ));

        return builder.build();
    }

    public InlineKeyboardMarkup offerResponseKeyboard(Long offerId) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        inlineButton("✅ Interesting", "offer_yes_" + offerId),
                        inlineButton("❌ Not interesting", "offer_no_" + offerId)
                )).build();
    }

    public InlineKeyboardMarkup myJobsSelectKeyboard(List<Job> jobs) {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        for (Job job : jobs) {
            builder.keyboardRow(List.of(
                    inlineButton("💼 " + job.getTitle(), "select_job_" + job.getId())
            ));
        }
        return builder.build();
    }

    public InlineKeyboardMarkup manageJobsKeyboard(List<Job> jobs) {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        for (Job job : jobs) {
            String s = job.getStatus().name();
            if (s.equals("APPROVED") || s.equals("PENDING")) {
                builder.keyboardRow(List.of(
                        inlineButton("🔒 " + job.getTitle() + " — Close", "close_job_" + job.getId())
                ));
            }
        }
        return builder.build();
    }

    public InlineKeyboardMarkup hrJobActionsKeyboard(Long jobId) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(inlineButton("🎯 Matched candidates", "hraction_matched_" + jobId)))
                .keyboardRow(List.of(inlineButton("📨 Applicants", "hraction_applied_" + jobId)))
                .keyboardRow(List.of(inlineButton("🔙 Back to vacancies", "hraction_back")))
                .build();
    }

    public InlineKeyboardMarkup hrCandidatesListKeyboard(List<MatchResultDTO> candidates, int currentPage,
                                                         int pageSize, CandidateSource source, Long jobId) {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, candidates.size());
        int totalPages = (int) Math.ceil((double) candidates.size() / pageSize);
        String src = source.getValue();

        for (int i = start; i < end; i++) {
            MatchResultDTO c = candidates.get(i);
            String label = "👤 " + c.getTitle() + " — " + c.getScorePercent() + "%";
            if (label.length() > 60) label = label.substring(0, 57) + "...";
            builder.keyboardRow(List.of(
                    inlineButton(label, "hrcand_" + c.getTargetId() + "_" + jobId + "_" + src + "_" + currentPage)
            ));
        }

        if (totalPages > 1) {
            List<InlineKeyboardButton> navRow = new ArrayList<>();
            navRow.add(currentPage > 0
                    ? inlineButton("⬅️", "hrcandlist_prev_" + currentPage + "_" + src + "_" + jobId)
                    : inlineButton(" ", "noop"));
            navRow.add(inlineButton((currentPage + 1) + "/" + totalPages, "noop"));
            navRow.add(currentPage < totalPages - 1
                    ? inlineButton("➡️", "hrcandlist_next_" + currentPage + "_" + src + "_" + jobId)
                    : inlineButton(" ", "noop"));
            builder.keyboardRow(navRow);
        }
        builder.keyboardRow(List.of(inlineButton("🔙 Back to vacancy", "hraction_jobback_" + jobId)));
        return builder.build();
    }

    public InlineKeyboardMarkup hrCandidateDetailsKeyboard(Long userId, Long jobId, CandidateSource source,
                                                           int returnPage, boolean hasCv, boolean alreadyOffered) {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();

        builder.keyboardRow(List.of(inlineButton(
                alreadyOffered ? "✅ Offer sent" : "📩 Send offer",
                alreadyOffered ? "noop" : "send_offer_" + jobId + "_" + userId)));

        if (hasCv) {
            builder.keyboardRow(List.of(inlineButton("📄 CV ko'rish", "view_cv_" + userId)));
        }

        builder.keyboardRow(List.of(
                inlineButton("🔙 Back to list",
                        "hrcandback_" + jobId + "_" + source.getValue() + "_" + returnPage)
        ));
        return builder.build();
    }

    private InlineKeyboardButton inlineButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }
    public InlineKeyboardMarkup applicantsJobsKeyboard(List<Job> jobs) {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        for (Job job : jobs) {
            builder.keyboardRow(List.of(
                    inlineButton("💼 " + job.getTitle(), "applicants_job_" + job.getId())
            ));
        }
        return builder.build();
    }
}