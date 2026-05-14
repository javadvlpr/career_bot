package uz.career.career_bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.career.career_bot.dto.UserDTO;
import uz.career.career_bot.entity.Skill;
import uz.career.career_bot.entity.User;
import uz.career.career_bot.enums.BotState;
import uz.career.career_bot.enums.SearchStatus;
import uz.career.career_bot.exception.NotFoundException;
import uz.career.career_bot.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User save(User user) {
        return userRepository.save(user);
    }

    public Optional<User> getByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }

    public boolean existsByTelegramId(Long telegramId) {
        return userRepository.existsByTelegramId(telegramId);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));
    }

    public List<User> getActiveAndPassiveUsers() {
        return userRepository.findBySearchStatusIn(
                List.of(SearchStatus.ACTIVE, SearchStatus.PASSIVE)
        );
    }

    public void updateBotState(Long telegramId, BotState state) {
        User user = getByTelegramId(telegramId)
                .orElseThrow(() -> new NotFoundException(
                        "User (telegramId=" + telegramId + ") not found"));
        user.setBotState(state);
        userRepository.save(user);
    }

    public void updateSearchStatus(Long telegramId, SearchStatus status) {
        User user = getByTelegramId(telegramId)
                .orElseThrow(() -> new NotFoundException(
                        "User (telegramId=" + telegramId + ") not found"));
        user.setSearchStatus(status);
        userRepository.save(user);
    }

    public UserDTO toDTO(User user) {
        Set<String> skillNames = user.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toSet());

        String profName = user.getProfession() != null ? user.getProfession().getName() : null;

        return UserDTO.builder()
                .id(user.getId())
                .telegramId(user.getTelegramId())
                .name(user.getName())
                .profession(profName)
                .skillNames(skillNames)
                .experienceLevel(user.getExperienceLevel())
                .location(user.getLocation())
                .expectedSalary(user.getExpectedSalary())
                .searchStatus(user.getSearchStatus())
                .language(user.getLanguage())
                .build();
    }

    public void updateCv(Long userId, String fileId, String fileName) {
        User user = getById(userId);
        user.setCvFileId(fileId);
        user.setCvFileName(fileName);
        userRepository.save(user);
    }

    public void removeCv(Long userId) {
        User user = getById(userId);
        user.setCvFileId(null);
        user.setCvFileName(null);
        userRepository.save(user);
    }

    public long count() {
        return userRepository.count();
    }
}