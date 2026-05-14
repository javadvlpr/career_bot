package uz.career.career_bot.entity;

import jakarta.persistence.*;
import lombok.*;
import uz.career.career_bot.enums.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_id", nullable = false, unique = true)
    private Long telegramId;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profession_id")
    private Profession profession;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_skills",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level")
    private ExperienceLevel experienceLevel;

    private String location;

    @Column(name = "expected_salary")
    private Integer expectedSalary;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "cv_file_id")
    private String cvFileId;

    @Column(name = "cv_file_name")
    private String cvFileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "search_status")
    @Builder.Default
    private SearchStatus searchStatus = SearchStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Language language = Language.EN;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_frequency")
    @Builder.Default
    private NotificationFrequency notificationFrequency = NotificationFrequency.DAILY;

    @Enumerated(EnumType.STRING)
    @Column(name = "bot_state")
    @Builder.Default
    private BotState botState = BotState.START;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}