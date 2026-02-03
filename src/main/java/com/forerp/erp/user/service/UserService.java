package com.forerp.erp.user.service;

import com.forerp.erp.user.domain.User;
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
