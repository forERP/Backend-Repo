package com.forerp.erp.user.controller;

import com.forerp.erp.user.dto.*;
import com.forerp.erp.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 유저 생성 (본사 관리자가)
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserCreateRequestDto request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    // 유저 삭제 (본사 관리자가)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // 로그인 (관리자 페이지)
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request,
                                                  HttpServletResponse response) {
        LoginResponseDto loginResponse = userService.login(request);

        response.setHeader("Authorization", "Bearer " + loginResponse.getToken());

        return ResponseEntity.ok(loginResponse);
    }

    // POS 로그인
    @PostMapping("/login/pos")
    public ResponseEntity<LoginResponseDto> loginPos(@RequestBody Map<String, String> request){
        Long storeId = Long.parseLong(request.get("storeId"));
        String employeeCode = request.get("employeeCode");

        return ResponseEntity.ok(userService.loginPos(storeId, employeeCode));
    }

    // 회원 정보 조회
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    // 회원 정보 모두 조회
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // 회원 정보 수정
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequestDto request){
        return ResponseEntity.ok(userService.updateUser(id, request));
    }
}