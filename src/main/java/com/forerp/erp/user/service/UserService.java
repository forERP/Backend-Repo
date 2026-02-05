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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private final AuditLogService auditLogService;
    private final SecurityUtil securityUtil;

    // 유저 생성 (본사 관리자가)
    @Transactional
    public UserResponseDto createUser(UserCreateRequestDto request) {
        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new IllegalArgumentException("이미 사용 중인 로그인 ID 입니다.");
        }

        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));

        long employeeCount = userRepository.countByStore(store);
        String generatedEmployeeCode = String.format("%02d", employeeCount + 1);

        User user = User.builder()
                .loginId(request.getLoginId())
                .employeeCode(generatedEmployeeCode)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .store(store)
                .role(request.getRole())
                .build();

        User saved = userRepository.save(user);

        try{
            User admin = securityUtil.getCurrentUser();
            auditLogService.logAction(admin, "CREATE_USER", "USER", saved.getId());
        }catch (Exception e){
            System.out.println("로그 기록 실패:" + e.getMessage());
        }

        return new UserResponseDto(saved);
    }

    // 유저 삭제 (본사 관리자가)
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalStateException("삭제할 사용자를 찾을 수 없습니다.");
        }
        userRepository.deleteById(id);

        try{
            User admin = securityUtil.getCurrentUser();
            auditLogService.logAction(admin, "DELETE_USER", "USER", id);
        }catch (Exception e){
            System.out.println("로그 기록 실패:" + e.getMessage());
        }
    }

    // 회원 정보 수정
    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateRequestDto request){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Store store = null;
        if(request.getStoreId() != null){
            store = storeRepository.findById(request.getStoreId())
                    .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 업습니다."));
        }

        String encodedPassword = null;
        if(request.getPassword() != null && !request.getPassword().isBlank()){
            encodedPassword = passwordEncoder.encode(request.getPassword());
        }

        user.updateInfo(request.getName(), encodedPassword, store, request.getRole());

        try{
            User admin = securityUtil.getCurrentUser();
            auditLogService.logAction(admin, "UPDATE_USER", "USER", user.getId());
        }catch (Exception e){
            System.out.println("로그 기록 실패:" + e.getMessage());
        }

        return new UserResponseDto(user);
    }

    // 로그인 (관리자 페이지)
    public LoginResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByLoginId(request.getIdentifier())
                .orElseThrow(() -> new IllegalArgumentException("아이디가 일치하지 않습니다."));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("비활성 사용자입니다.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtUtil.generateToken(user.getLoginId());
        return new LoginResponseDto(token, user.getRole(), user.getId());
    }

    // POS 로그인
    public LoginResponseDto loginPos(Long storeId, String employeeCode){
        User user = userRepository.findByStore_IdAndEmployeeCode(storeId, employeeCode)
                .orElseThrow(() -> new IllegalArgumentException("해당 매장에 존재하지 않는 직원입니다."));

        if(user.getStatus() != UserStatus.ACTIVE){
            throw new IllegalArgumentException("퇴사한 직원입니다.");
        }

        String token = jwtUtil.generateToken(user.getLoginId());

        return new LoginResponseDto(token, user.getRole(), user.getId());
    }


    // 회원 정보 조회
    public UserResponseDto getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return new UserResponseDto(user);
    }

    // 회원 정보 모두 조회
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponseDto::new)
                .toList();
    }


}