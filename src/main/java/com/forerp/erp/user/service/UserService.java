package com.forerp.erp.user.service;

import com.forerp.erp.attendance.domain.Attendance;
import com.forerp.erp.attendance.repository.AttendanceRepository;
import com.forerp.erp.auditlog.AuditLogService;
import com.forerp.erp.common.jwt.JwtUtil;
import com.forerp.erp.common.jwt.SecurityUtil;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.user.domain.UserRole;
import com.forerp.erp.user.domain.UserStatus;
import com.forerp.erp.user.dto.LoginRequestDto;
import com.forerp.erp.user.dto.LoginResponseDto;
import com.forerp.erp.user.dto.UserCreateRequestDto;
import com.forerp.erp.user.dto.UserResponseDto;
import com.forerp.erp.user.dto.UserUpdateRequestDto;
import com.forerp.erp.user.repository.UserRepository;
import com.forerp.erp.user.service.support.UserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserReader userReader;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditLogService auditLogService;
    private final SecurityUtil securityUtil;
    private final AttendanceRepository attendanceRepository;

    @Transactional
    public UserResponseDto createUser(UserCreateRequestDto request) {
        userReader.validateNewUser(request.getLoginId());
        Store store = userReader.getStore(request.getStoreId());

        String generatedEmployeeCode = generateNextEmployeeCode();

        User user = User.builder()
                .loginId(request.getLoginId())
                .employeeCode(generatedEmployeeCode)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber().trim())
                .store(store)
                .role(request.getRole())
                .build();

        User saved = userRepository.save(user);
        logAction("CREATE_USER", saved.getId());

        return new UserResponseDto(saved);
    }

    @Transactional
    public void deleteUser(Long id) {
        userReader.getUser(id);

        userRepository.deleteById(id);
        logAction("DELETE_USER", id);
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateRequestDto request) {
        User user = userReader.getUser(id);
        Store store = userReader.getStore(request.getStoreId());

        String encodedPassword = null;
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            encodedPassword = passwordEncoder.encode(request.getPassword());
        }

        user.updateInfo(
                request.getName(),
                trimToNull(request.getPhoneNumber()),
                encodedPassword,
                store,
                request.getRole(),
                request.getStatus()
        );
        logAction("UPDATE_USER", user.getId());

        return new UserResponseDto(user);
    }

    public LoginResponseDto login(LoginRequestDto request) {
        User user = userReader.getUserByLoginId(request.getIdentifier());
        validateActiveUser(user);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return generateTokenResponse(user);
    }

    @Transactional
    public LoginResponseDto loginPos(String storeCode, String employeeCode) {
        User user = userReader.getUserForPos(storeCode, employeeCode);
        validateActiveUser(user);

        if (isAutoAttendanceRole(user.getRole())) {
            autoClockInForStoreAdmin(user);
        }

        return generateTokenResponse(user);
    }

    @Transactional
    public void logoutPos(String storeCode, String employeeCode) {
        User user = userReader.getUserForPos(storeCode, employeeCode);
        validateActiveUser(user);

        if (!isAutoAttendanceRole(user.getRole())) {
            return;
        }

        attendanceRepository.findTopByUser_IdAndClockOutIsNullOrderByClockInDesc(user.getId())
                .ifPresent(Attendance::recordClockOut);
    }

    public UserResponseDto getUser(Long id) {
        return new UserResponseDto(userReader.getUser(id));
    }

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
    }

    public Page<UserResponseDto> searchUsers(
            String storeName,
            String storeCode,
            String name,
            String status,
            String role,
            LocalDate createdFromDate,
            LocalDate createdToDate,
            Pageable pageable
    ) {
        LocalDateTime createdFrom = null;
        LocalDateTime createdTo = null;

        if (createdFromDate != null) {
            createdFrom = createdFromDate.atStartOfDay();
        }
        if (createdToDate != null) {
            createdTo = createdToDate.plusDays(1).atStartOfDay();
        }
        if (createdFrom != null && createdTo != null && createdTo.isBefore(createdFrom)) {
            throw new IllegalArgumentException("createdTo must be on or after createdFrom");
        }

        return userRepository.search(
                        normalize(storeName),
                        normalize(storeCode),
                        normalize(name),
                        parseStatus(status),
                        parseRole(role),
                        createdFrom,
                        createdTo,
                        pageable
                )
                .map(UserResponseDto::new);
    }

    private void validateActiveUser(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("비활성 사용자입니다.");
        }
    }

    private LoginResponseDto generateTokenResponse(User user) {
        String token = jwtUtil.generateToken(user.getLoginId());
        return new LoginResponseDto(token, user.getRole(), user.getId());
    }

    private void autoClockInForStoreAdmin(User user) {
        LocalDate today = LocalDate.now();
        if (attendanceRepository.existsByUser_IdAndWorkDate(user.getId(), today)) {
            return;
        }

        Attendance attendance = Attendance.clockInBuilder()
                .user(user)
                .workDate(today)
                .clockIn(LocalDateTime.now())
                .build();

        attendanceRepository.save(attendance);
    }

    private boolean isAutoAttendanceRole(UserRole role) {
        return role == UserRole.STORE_ADMIN || role == UserRole.HQ_ADMIN;
    }

    private void logAction(String action, Long targetId) {
        try {
            User admin = securityUtil.getCurrentUser();
            auditLogService.logAction(admin, action, "USER", targetId);
        } catch (Exception e) {
            System.out.println("로그 기록 실패: " + e.getMessage());
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String trimToNull(String value) {
        return normalize(value);
    }

    private UserStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return UserStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid user status: " + status);
        }
    }

    private UserRole parseRole(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }

        try {
            return UserRole.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid user role: " + role);
        }
    }

    private String generateNextEmployeeCode() {
        long nextSequence = userReader.getNextEmployeeSequence();
        String employeeCode = String.format("%04d", nextSequence);

        while (userRepository.existsByEmployeeCode(employeeCode)) {
            nextSequence++;
            employeeCode = String.format("%04d", nextSequence);
        }

        return employeeCode;
    }
}
