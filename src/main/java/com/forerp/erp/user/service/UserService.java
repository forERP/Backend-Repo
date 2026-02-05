package com.forerp.erp.user.service;

import com.forerp.erp.common.audit.AuditLogService;
import com.forerp.erp.common.jwt.JwtUtil;
import com.forerp.erp.common.jwt.SecurityUtil;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.store.repository.StoreRepository;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.user.domain.UserStatus;
import com.forerp.erp.user.dto.*;
import com.forerp.erp.user.repository.UserRepository;
import com.forerp.erp.user.service.support.UserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // 유저 생성 (본사 관리자가)
    @Transactional
    public UserResponseDto createUser(UserCreateRequestDto request) {
       userReader.validateNewUser(request.getLoginId());
       Store store = userReader.getStore(request.getStoreId());

       // 직원 코드 생성
       long nextSequence = userReader.getNextEmployeeSequence(store);
       String generatedEmployeeCode = String.format("%04d", nextSequence);

        User user = User.builder()
                .loginId(request.getLoginId())
                .employeeCode(generatedEmployeeCode)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .store(store)
                .role(request.getRole())
                .build();

        User saved = userRepository.save(user);

        logAction("CREATE_USER", saved.getId());

        return new UserResponseDto(saved);
    }

    // 유저 삭제 (본사 관리자가)
    @Transactional
    public void deleteUser(Long id) {
        userReader.getUser(id);

        userRepository.deleteById(id);
        logAction("DELETE_USER", id);
    }

    // 회원 정보 수정
    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateRequestDto request){
        User user = userReader.getUser(id);
        Store store = userReader.getStore(request.getStoreId());

        String encodedPassword = null;
        if(request.getPassword() != null && !request.getPassword().isBlank()){
            encodedPassword = passwordEncoder.encode(request.getPassword());
        }

        user.updateInfo(request.getName(), encodedPassword, store, request.getRole());
        logAction("UPDATE_USER", user.getId());

        return new UserResponseDto(user);
    }

    // 로그인 (관리자 페이지)
    public LoginResponseDto login(LoginRequestDto request) {
        User user = userReader.getUserByLoginId(request.getIdentifier());
        validateActiveUser(user);

        if(!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return generateTokenResponse(user);
    }

    // POS 로그인
    public LoginResponseDto loginPos(Long storeId, String employeeCode){
        User user = userReader.getUserForPos(storeId, employeeCode);
        validateActiveUser(user);

        return generateTokenResponse(user);
    }

    // 회원 정보 조회
    public UserResponseDto getUser(Long id) {
        return new UserResponseDto(userReader.getUser(id));
    }

    // 회원 정보 전체 조회
    public List<UserResponseDto> getAllUsers() {
        return userReader.getAllUsers().stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
    }

    private void validateActiveUser(User user){
        if(user.getStatus() != UserStatus.ACTIVE){
            throw new IllegalArgumentException("비활성 사용자입니다.");
        }
    }

    private LoginResponseDto generateTokenResponse(User user){
        String token = jwtUtil.generateToken(user.getLoginId());
        return new LoginResponseDto(token, user.getRole(), user.getId());
    }

    private void logAction(String action, Long targetId){
        try{
            User admin = securityUtil.getCurrentUser();
            auditLogService.logAction(admin, action, "USER", targetId);
        }catch (Exception e){
            System.out.println("로그 기록 실패:" + e.getMessage());
        }
    }

}