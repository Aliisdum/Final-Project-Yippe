package az.developia.flight_booking_name.service;

import az.developia.flight_booking_name.entity.User;
import az.developia.flight_booking_name.entity.UserRole;
import az.developia.flight_booking_name.exception.ConflictException;
import az.developia.flight_booking_name.exception.ResourceNotFoundException;
import az.developia.flight_booking_name.repository.UserRepository;
import az.developia.flight_booking_name.request.RegisterRequest;
import az.developia.flight_booking_name.response.UserResponse;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class UserService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    private static final String PROFILE_PICTURE_DIR = "uploads/profile-pictures";
    private final Path pictureStorageLocation = Paths.get(PROFILE_PICTURE_DIR).toAbsolutePath().normalize();

    @PostConstruct
    private void initStorage() {
        try {
            Files.createDirectories(pictureStorageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already exists: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(UserRole.CUSTOMER)
                .enabled(true)
                .build();

        return userRepository.save(user);
    }

    public User createAirlineManager(RegisterRequest request, Long airlineId) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already exists: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(UserRole.AIRLINE_MANAGER)
                .airlineId(airlineId)
                .enabled(true)
                .build();

        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }



    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        List<UserResponse> responses = users.getContent().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, users.getTotalElements());
    }

    public Page<UserResponse> getUsersByRole(UserRole role, Pageable pageable) {
        List<User> users = userRepository.findByRole(role);
        List<UserResponse> responses = users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());
        return new PageImpl<>(responses.subList(start, end), pageable, responses.size());
    }

    public void updateUserStatus(Long id, Boolean enabled) {
        User user = getUserById(id);
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    public void disableUser(Long id) {
        updateUserStatus(id, false);
    }

    public String saveProfilePicture(Long id, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Profile picture file is required");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("Invalid file name");
        }

        String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')) : "";
        String finalFileName = "profile-" + id + "-" + UUID.randomUUID() + extension;
        Path targetLocation = pictureStorageLocation.resolve(finalFileName);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Could not store profile picture: " + finalFileName, e);
        }

        User user = getUserById(id);
        if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
            deleteStoredProfilePicture(user.getProfilePictureUrl());
        }
        user.setProfilePictureUrl("/uploads/" + finalFileName);
        userRepository.save(user);
        return user.getProfilePictureUrl();
    }

    public void deleteProfilePicture(Long id) {
        User user = getUserById(id);
        if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
            deleteStoredProfilePicture(user.getProfilePictureUrl());
            user.setProfilePictureUrl(null);
            userRepository.save(user);
        }
    }

    private void deleteStoredProfilePicture(String profilePictureUrl) {
        String fileName = profilePictureUrl.substring(profilePictureUrl.lastIndexOf('/') + 1);
        Path filePath = pictureStorageLocation.resolve(fileName);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignore) {
        }
    }

    public UserResponse mapToUserResponse(User user) {
        String fullName = user.getFullName() != null ? user.getFullName().trim() : "";
        String[] nameParts = fullName.split(" ");
        String firstName = nameParts.length > 0 ? nameParts[0] : "";
        String lastName = nameParts.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(nameParts, 1, nameParts.length)) : "";

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(fullName)
                .name(firstName)
                .surname(lastName)
                .profilePictureUrl(user.getProfilePictureUrl())
                .role(user.getRole().toString())
                .build();
    }
}
