package com.forerp.erp.user.service;

import com.forerp.erp.attendance.domain.Attendance;
import com.forerp.erp.attendance.repository.AttendanceRepository;
import com.forerp.erp.auditlog.AuditLogAction;
import com.forerp.erp.auditlog.AuditLogService;
import com.forerp.erp.auditlog.AuditLogTargetType;
import com.forerp.erp.common.jwt.JwtUtil;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    private final AttendanceRepository attendanceRepository;

    @Transactional
    public UserResponseDto createUser(User actor, UserCreateRequestDto request) {
        String loginId = normalize(request.getLoginId());
        String employeeCode = normalize(request.getEmployeeCode());
        if (loginId == null) {
            throw new IllegalArgumentException("loginId is required.");
        }
        if (employeeCode == null) {
            throw new IllegalArgumentException("employeeCode is required.");
        }

        enforceStoreScopedCreatePermission(actor, request.getStoreId());
        userReader.validateNewUser(loginId, employeeCode);
        Store store = userReader.getStore(request.getStoreId());

        User user = User.builder()
                .loginId(loginId)
                .employeeCode(employeeCode)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber().trim())
                .store(store)
                .role(request.getRole())
                .build();

        User saved = userRepository.save(user);
        logAction(AuditLogAction.USER_CREATE, saved.getId());

        return new UserResponseDto(saved);
    }

    @Transactional
    public void deleteUser(Long id) {
        userReader.getUser(id);

        userRepository.deleteById(id);
        logAction(AuditLogAction.USER_DELETE, id);
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
        logAction(AuditLogAction.USER_UPDATE, user.getId());

        return new UserResponseDto(user);
    }

    @Transactional(readOnly = false)
    public LoginResponseDto login(LoginRequestDto request) {
        User user = userReader.getUserByLoginId(request.getIdentifier());
        validateActiveUser(user);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password.");
        }

        if (!isBackofficeLoginRole(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자(HQ/STORE) 계정만 관리자페이지에 로그인할 수 있습니다.");
        }

        auditLogService.logActionSafely(user, AuditLogAction.ADMIN_LOGIN, AuditLogTargetType.USER, user.getId());
        return generateTokenResponse(user);
    }

    @Transactional(readOnly = false)
    public void logout(User actor) {
        if (actor == null) {
            return;
        }
        auditLogService.logActionSafely(actor, AuditLogAction.ADMIN_LOGOUT, AuditLogTargetType.USER, actor.getId());
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

    public UserResponseDto getCurrentUser(User actor) {
        if (actor == null) {
            throw new IllegalArgumentException("No authenticated user.");
        }
        return new UserResponseDto(userReader.getUser(actor.getId()));
    }

    public boolean isEmployeeCodeAvailable(String employeeCode) {
        String normalizedEmployeeCode = normalize(employeeCode);
        if (normalizedEmployeeCode == null) {
            return false;
        }
        return userReader.isEmployeeCodeAvailable(normalizedEmployeeCode);
    }

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
    }

    public Page<UserResponseDto> searchUsers(
            String storeKeyword,
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
                        normalize(storeKeyword),
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
            throw new IllegalArgumentException("Inactive user.");
        }
    }

    private LoginResponseDto generateTokenResponse(User user) {
        String token = jwtUtil.generateToken(user.getLoginId());
        return new LoginResponseDto(token, user.getRole(), user.getId(), user.getName());
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

    private boolean isBackofficeLoginRole(UserRole role) {
        return role == UserRole.HQ_ADMIN || role == UserRole.STORE_ADMIN;
    }

    private void enforceStoreScopedCreatePermission(User actor, Long requestStoreId) {
        if (actor == null || actor.getId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        }

        User currentUser = userReader.getUser(actor.getId());
        if (currentUser.getRole() != UserRole.STORE_ADMIN) {
            return;
        }

        Long actorStoreId = currentUser.getStore() != null ? currentUser.getStore().getId() : null;
        if (actorStoreId == null || requestStoreId == null || !actorStoreId.equals(requestStoreId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "매장 관리자는 본인 매장 직원만 등록할 수 있습니다.");
        }
    }

    private void logAction(String action, Long targetId) {
        auditLogService.logCurrentUserAction(action, AuditLogTargetType.USER, targetId);
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

}
