package uz.career.career_bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.career.career_bot.entity.Location;
import uz.career.career_bot.exception.AlreadyExistsException;
import uz.career.career_bot.exception.NotFoundException;
import uz.career.career_bot.exception.ValidationException;
import uz.career.career_bot.repository.LocationRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    public List<Location> getAll() {
        return locationRepository.findAll();
    }

    public List<Location> getActive() {
        return locationRepository.findByActiveTrue();
    }

    public Location getById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location ", id));
    }

    public Location create(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Location name cannot be empty.");
        }
        if (locationRepository.findByName(name.trim()) != null) {
            throw new AlreadyExistsException("This location already exists.");
        }
        return locationRepository.save(Location.builder()
                .name(name.trim())
                .active(true)
                .build());
    }

    public Location update(Long id, String name, Boolean active) {
        Location location = getById(id);
        if (name != null && !name.trim().isEmpty()) {
            location.setName(name.trim());
        }
        if (active != null) {
            location.setActive(active);
        }
        return locationRepository.save(location);
    }

    public void delete(Long id) {
        locationRepository.deleteById(id);
    }

    public long count() {
        return locationRepository.count();
    }
}