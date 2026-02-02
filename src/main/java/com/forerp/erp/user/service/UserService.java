package com.forerp.erp.user.service;

import com.forerp.erp.user.domain.Role;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.user.dto.UserCreateRequestDto;
import com.forerp.erp.user.dto.UserResponseDto;
import com.forerp.erp.user.repository.RoleRepository;
import com.forerp.erp.user.repository.UserRepository;
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
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDto createUser(UserCreateRequestDto request) {

        User newUser = User.builder()
                .loginId(request.getLoginId())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        User savedUser = userRepository.save(newUser);
        return new UserResponseDto(savedUser);
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
