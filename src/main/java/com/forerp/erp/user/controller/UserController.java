package com.forerp.erp.user.controller;

import com.forerp.erp.user.dto.LoginRequestDto;
import com.forerp.erp.user.dto.LoginResponseDto;
import com.forerp.erp.user.dto.UserCreateRequestDto;
import com.forerp.erp.user.dto.UserResponseDto;
import com.forerp.erp.user.dto.UserUpdateRequestDto;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserCreateRequestDto request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletResponse response
    ) {
        LoginResponseDto loginResponse = userService.login(request);
        response.setHeader("Authorization", "Bearer " + loginResponse.getToken());

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal User actor) {
        userService.logout(actor);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login/pos")
    public ResponseEntity<LoginResponseDto> loginPos(@RequestBody Map<String, String> request) {
        String storeCode = request.get("storeCode");
        String employeeCode = request.get("employeeCode");

        return ResponseEntity.ok(userService.loginPos(storeCode, employeeCode));
    }

    @PostMapping("/logout/pos")
    public ResponseEntity<Void> logoutPos(@RequestBody Map<String, String> request) {
        String storeCode = request.get("storeCode");
        String employeeCode = request.get("employeeCode");

        userService.logoutPos(storeCode, employeeCode);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserResponseDto>> searchUsers(
            @RequestParam(required = false) String storeKeyword,
            @RequestParam(required = false) String storeName,
            @RequestParam(required = false) String storeCode,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(userService.searchUsers(
                storeKeyword,
                storeName,
                storeCode,
                name,
                status,
                role,
                createdFrom,
                createdTo,
                pageable
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequestDto request
    ) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }
}
