package com.planit.service;

import com.planit.exception.ResourceNotFoundException;
import com.planit.model.User;
import com.planit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    public User updateProfile(String userId, User.Profile profile) {
        User user = getUserById(userId);
        user.setProfile(profile);
        return userRepository.save(user);
    }

    public User updateAddress(String userId, User.Address address) {
        User user = getUserById(userId);
        user.setAddress(address);
        return userRepository.save(user);
    }

    public User updatePreferences(String userId, User.Preferences preferences) {
        User user = getUserById(userId);
        user.setPreferences(preferences);
        return userRepository.save(user);
    }

    public Page<User> getAllUsers(int page, int pageSize) {
        var pageable = PageRequest.of(Math.max(page - 1, 0), pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return userRepository.findAll(pageable);
    }

    public User suspendUser(String userId) {
        User user = getUserById(userId);
        user.setStatus(User.UserStatus.SUSPENDED);
        log.info("User suspended: {}", userId);
        return userRepository.save(user);
    }

    public User activateUser(String userId) {
        User user = getUserById(userId);
        user.setStatus(User.UserStatus.ACTIVE);
        return userRepository.save(user);
    }
}
