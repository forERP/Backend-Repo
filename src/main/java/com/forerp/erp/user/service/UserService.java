package com.forerp.erp.user.service;

import com.forerp.erp.common.audit.AuditLogService;
import com.forerp.erp.common.jwt.JwtUtil;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.user.dto.LoginRequestDto;
import com.forerp.erp.user.dto.UserSetupRequestDto;
import com.forerp.erp.user.dto.UserResponseDto;
import com.forerp.erp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditLogService auditLogService;


    @Transactional
    public UserResponseDto setupUser(Long id, UserSetupRequestDto request) {

        Set<String> userPermissions;

        if(id == 1){
            userPermissions= Set.of(
                    "USER_CREATE",
                    "USER_DELETE"
            );
        }else{
            userPermissions = new HashSet<>();
        }

        User user = User.builder()
                .id(id)
                .loginId(request.getLoginId())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .permission(userPermissions)
                .build();

        User savedUser = userRepository.save(user);

        auditLogService.logAction(savedUser, "CREATE_USER", "USER", savedUser.getId());

        return new UserResponseDto(savedUser);
    }

    // 로그인 처리
    public String login(LoginRequestDto request){
        String identifier = request.getIdentifier();
        String password = request.getPassword();

        User user = userRepository.findByLoginIdOrEmployeeCode(identifier, identifier)
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 직원코드가 일치하지 않습니다."));
        if(!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return jwtUtil.generateToken(user.getLoginId(), user.getRole());
    }

    public UserResponseDto getUser(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return new UserResponseDto(user);
    }

    public List<UserResponseDto> getAllUser(){
        return  userRepository.findAll().stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(Long id){
        if(!userRepository.existsById(id)){
            throw new IllegalStateException("삭제할 사용자를 찾을 수 없습니다.");
        }

        userRepository.deleteById(id);
    }
}
